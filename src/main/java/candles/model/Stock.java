package candles.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Stock {

    private static final Logger LOG = LoggerFactory.getLogger(Stock.class);

    public final String stockName;
    // long-term candle storage
    private final Map<CandleSize, List<Candle>> candles;
    // storage of current trades, queue is periodically emptied to calculate finalized candles
    private final Map<CandleSize, Deque<Trade>> curTrades;
    private final Map<CandleSize, Lock> consistencyLocks;

    public Stock(String stockName, List<CandleSize> candleUnits, ScheduledExecutorService scheduler) {
        this.stockName = stockName;

        candles = new ConcurrentHashMap<>();
        curTrades = new ConcurrentHashMap<>();
        consistencyLocks = new ConcurrentHashMap<>();

        candleUnits.forEach(cu -> {
            candles.put(cu, new LinkedList<>());
            final var durationInMillis = max(cu.getDurationInMillis(), 60_000);//euristic for big candles
            curTrades.put(cu, new ConcurrentLinkedDeque<>());
            consistencyLocks.put(cu, new ReentrantLock());
            scheduler.scheduleAtFixedRate(() -> {
                calculateFinalCandles(cu);
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
            calculateFinalCandles(candleSize);// this guarantees curTrades contains only non-final candle data

            final var lastCandle = calculateNonFinalCandle(candleSize);
            final var finalCandles = candles.get(candleSize);

            if (lastCandle.isPresent()) {
                final var res = new LinkedList<>(finalCandles);
                res.add(lastCandle.get());
                return unmodifiableList(res);
            } else {
                return unmodifiableList(finalCandles);
            }
        } finally {
            lock.unlock();
        }
    }

    public Optional<Candle> calculateNonFinalCandle(CandleSize candleSize) {

        final var tradesIterator = curTrades.get(candleSize).iterator();

        var maxPrice = new BigDecimal(0);
        var minPrice = new BigDecimal(0);
        Trade firstTrade = null;
        Trade lastTrade = null;

        if (tradesIterator.hasNext()) {
            final var trade = tradesIterator.next();
            firstTrade = trade;
            lastTrade = trade;
            maxPrice = trade.price;
            minPrice = trade.price;
        } else {
            return Optional.empty();
        }

        while (tradesIterator.hasNext()) {
            final var trade = tradesIterator.next();
            if (trade.price.compareTo(maxPrice) > 0) {
                maxPrice = trade.price;
            }
            if (trade.price.compareTo(minPrice) < 0) {
                minPrice = trade.price;
            }
            lastTrade = trade;
        }

        final var candle = new Candle(candleSize, firstTrade.time, minPrice, maxPrice, firstTrade.price, lastTrade.price);
        return Optional.of(candle);
    }

    public void calculateFinalCandles(CandleSize candleSize) {
        final var lock = consistencyLocks.get(candleSize);
        lock.lock();

        try {
            final var tradesIterator = curTrades.get(candleSize).iterator();//starting from the oldest trade
            final var candleTrades = new ArrayList<Trade>(); // will search all trades within candle time interval to add to the list
            LocalDateTime candleSliceTimeLeft;
            LocalDateTime candleSliceTimeRight;
            BigDecimal maxPrice;
            BigDecimal minPrice;

            if (tradesIterator.hasNext()) { // scanning first trade as a candle start
                var trade = tradesIterator.next();

                candleSliceTimeLeft = candleSize.calculateAbsoluteStartDate(trade.time);
                candleSliceTimeRight = candleSize.calculateAbsoluteEndDate(candleSliceTimeLeft);

                maxPrice = trade.price;
                minPrice = trade.price;
                candleTrades.add(trade);
                tradesIterator.remove();

                while (tradesIterator.hasNext()) {
                    trade = tradesIterator.next();

                    if (trade.time.isAfter(candleSliceTimeLeft) && trade.time.isBefore(candleSliceTimeRight)) {
                        candleTrades.add(trade);
                        tradesIterator.remove();// removing trade
                        if (trade.price.compareTo(maxPrice) > 0) {
                            maxPrice = trade.price;
                        }
                        if (trade.price.compareTo(minPrice) < 0) {
                            minPrice = trade.price;
                        }

                    } else if (trade.time.isAfter(candleSliceTimeRight)) {//reached next candle
                        // adding candle to the list of final candles
                        if (candleTrades.size() != 0) {

                            final var candle = new Candle(candleSize,
                                candleTrades.get(0).time, candleTrades.get(candleTrades.size() - 1).time,
                                minPrice, maxPrice,
                                candleTrades.get(0).price, candleTrades.get(candleTrades.size() - 1).price
                            );
                            candles.get(candleSize).add(candle);
                        }
                        maxPrice = trade.price;
                        minPrice = trade.price;
                        candleSliceTimeLeft = candleSliceTimeLeft.plus(candleSize.size, candleSize.unit);
                        candleSliceTimeRight = candleSliceTimeRight.plus(candleSize.size, candleSize.unit);
                        candleTrades.clear();
                        candleTrades.add(trade);
                        tradesIterator.remove();
                    }
                }
            }
            curTrades.put(candleSize, new ConcurrentLinkedDeque<>(candleTrades));//re-adding trades from non-final candle
        } finally {
            lock.unlock();
        }
    }

}
