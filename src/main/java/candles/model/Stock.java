package candles.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static candles.model.CandleBuilder.candle;
import static java.lang.Math.min;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Stock {

    private static final Logger LOG = LoggerFactory.getLogger(Stock.class);

    public final String stockName;
    // long-term ready candle storage
    private final Map<CandleSize, List<Candle>> candles;
    // storage for not-ready candles
    private final Map<CandleSize, CandleBuilder> currentCandles;
    // storage of current trades, queue is periodically emptied to calculate finalized candles
    private final Map<CandleSize, Deque<Trade>> curTrades;
    private final Map<CandleSize, Lock> consistencyLocks;

    public Stock(String stockName, List<CandleSize> candleUnits, ScheduledExecutorService scheduler) {
        this.stockName = stockName;

        candles = new ConcurrentHashMap<>();
        currentCandles = new ConcurrentHashMap<>();
        curTrades = new ConcurrentHashMap<>();
        consistencyLocks = new ConcurrentHashMap<>();

        candleUnits.forEach(cu -> {
            candles.put(cu, new LinkedList<>());
            final var durationInMillis = min(cu.getDurationInMillis(), 60_000);//euristic for big candles
            curTrades.put(cu, new ConcurrentLinkedDeque<>());
            consistencyLocks.put(cu, new ReentrantLock());
            scheduler.scheduleAtFixedRate(() -> {
                calculateCandles(cu);
            }, 1000, durationInMillis, MILLISECONDS);
        });
    }

    public void addTrade(Trade trade) {
        curTrades.values().forEach(q -> q.add(trade));
    }

    public List<Candle> getCandles(CandleSize candleSize) {
        final var lock = consistencyLocks.get(candleSize);
        lock.lock();

        try {
            calculateCandles(candleSize);

            final var lastCandle = currentCandles.get(candleSize);
            if (lastCandle == null) {
                return unmodifiableList(candles.get(candleSize));
            } else {
                final var res = new LinkedList<>(candles.get(candleSize));
                res.add(lastCandle.build());
                return unmodifiableList(res);
            }
        } finally {
            lock.unlock();
        }
    }

    private void calculateCandles(CandleSize candleSize) {
        final var lock = consistencyLocks.get(candleSize);
        lock.lock();
        try {
            final var tradesIterator = curTrades.get(candleSize).iterator();//starting from the oldest trade

            if (tradesIterator.hasNext()) {
                final var firstTrade = tradesIterator.next();

                var existingCandle = currentCandles.get(candleSize);
                if (existingCandle == null) {
                    existingCandle = candle(candleSize, firstTrade);
                    currentCandles.put(candleSize, existingCandle);
                }

                var existingCandleSliceTimeLeft = candleSize.calculateAbsoluteStartDate(existingCandle.openTime);
                var existingCandleSliceTimeRight = candleSize.calculateAbsoluteEndDate(existingCandleSliceTimeLeft);

                // case when first trade we scan closes existing current trade
                if (firstTrade.time.isAfter(existingCandleSliceTimeRight)) {
                    final var newCandle = existingCandle.build();
                    candles.get(candleSize).add(newCandle);
                    existingCandle = currentCandles.put(candleSize, candle(candleSize, firstTrade));
                    tradesIterator.remove();
                }

                while (tradesIterator.hasNext()) {
                    final var trade = tradesIterator.next();

                    existingCandleSliceTimeLeft = candleSize.calculateAbsoluteStartDate(existingCandle.openTime);
                    existingCandleSliceTimeRight = candleSize.calculateAbsoluteEndDate(existingCandleSliceTimeLeft);

                    // closing current candle
                    if (trade.time.isAfter(existingCandleSliceTimeRight)) {
                        final var newCandle = existingCandle.build();
                        candles.get(candleSize).add(newCandle);
                        existingCandle = candle(candleSize, trade);
                        currentCandles.put(candleSize, existingCandle);
                        tradesIterator.remove();
                        continue;
                    }
                    existingCandle = existingCandle.addTrade(trade);
                    currentCandles.put(candleSize, existingCandle);
                    tradesIterator.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
