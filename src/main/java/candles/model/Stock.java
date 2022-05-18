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

import static candles.Application.DEFAULT_TIME_ZONE_OFFSET;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Stock {

    private static final Logger LOG = LoggerFactory.getLogger(Stock.class);

    public final String stockName;
    private final Map<CandleSize, List<Candle>> candles; // long-term candle storage
    private final Map<CandleSize, Deque<Trade>> curTrades; // storage of current trades, queue is periodically emptied to calculate another candle
    private final Map<CandleSize, Lock> consistencyLocks;

    public Stock(String stockName, List<CandleSize> candleUnits, ScheduledExecutorService scheduler) {
        this.stockName = stockName;

        candles = new ConcurrentHashMap<>();
        curTrades = new ConcurrentHashMap<>();
        consistencyLocks = new ConcurrentHashMap<>();

        candleUnits.forEach(cu -> {
            candles.put(cu, new LinkedList<>());
            final var durationInMillis = cu.getDurationInMillis();
            curTrades.put(cu, new ConcurrentLinkedDeque<>());
            consistencyLocks.put(cu, new ReentrantLock());
            scheduler.scheduleAtFixedRate(() -> {
                calculateFinalCandles(cu);
            }, 1000, durationInMillis, MILLISECONDS);
        });

    }

    // this introduces weak consisteny into the API - last candle might be inconsistent
    public void addTrade(Trade trade) {
        curTrades.values().forEach(q -> q.add(trade));
    }

    public List<Candle> getCandles(CandleSize candleSize) {
        final var lock = consistencyLocks.get(candleSize);
        lock.lock();
        //LOG.info("returning candles");

        try {
            calculateFinalCandles(candleSize);// this guarantees curTrades contains only non-final candle data

            final var last = calculateNonFinalCandle(candleSize);
            final var finalCandles = candles.get(candleSize);

            if (last.isPresent()) {
                final var res = new LinkedList<>(finalCandles);
                res.add(last.get());
                return unmodifiableList(res);
            } else {
                return unmodifiableList(finalCandles);
            }

        } finally {
            System.out.println("returned candles");
            lock.unlock();
        }
    }

    public Optional<Candle> calculateNonFinalCandle(CandleSize candleSize) {
        System.out.println("calculating last candle");
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

        var candle = new Candle(candleSize, firstTrade.time, minPrice, maxPrice, firstTrade.price, lastTrade.price);
        System.out.println("last candle calculated:" + candle);
        return Optional.of(candle);
    }

    // task that calculates all complete candles from the queue
    public void calculateFinalCandles(CandleSize candleSize) {
        //LOG.info("attempting to calculate candles for {}", stockName);
        final var lock = consistencyLocks.get(candleSize);
        lock.lock();

        try {
            final var tradesIterator = curTrades.get(candleSize).iterator();//starting from the oldest trade
            final List<Trade> candleTrades = new ArrayList<>(); // will search all trades within candle time interval to add to the list
            LocalDateTime candleSliceTimeLeft;
            LocalDateTime candleSliceTimeRight;
            BigDecimal maxPrice;
            BigDecimal minPrice;

            if (tradesIterator.hasNext()) { // scanning first trade as a candle start
                var trade = tradesIterator.next();

                candleSliceTimeLeft = calculateAbsoluteStartDate(trade.time, candleSize);
                candleSliceTimeRight = calculateAbsoluteEndDate(candleSliceTimeLeft, candleSize);

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

                            var candle = new Candle(candleSize,
                                candleTrades.get(0).time, candleTrades.get(candleTrades.size() - 1).time,
                                minPrice, maxPrice,
                                candleTrades.get(0).price, candleTrades.get(candleTrades.size() - 1).price
                            );
                            //LOG.info("candle calculated:" + candle);
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
            curTrades.put(candleSize, new ConcurrentLinkedDeque<>(candleTrades));//re-ading trades if no closed candle was calculated
        } catch (Exception e) {
            LOG.error("exception", e);
        } finally {
            lock.unlock();
            //LOG.info("finished attempt to calculate candles for {}", stockName);
        }

    }

    public LocalDateTime calculateAbsoluteStartDate(LocalDateTime curTime, CandleSize candle) {
        // TODO inject timezone
        final var rougthTrunc = curTime.truncatedTo(candle.getBiggerTimeUnit(candle.unit)).toInstant(DEFAULT_TIME_ZONE_OFFSET).toEpochMilli();
        final var minorTrunc = curTime.truncatedTo(candle.unit).toInstant(DEFAULT_TIME_ZONE_OFFSET).toEpochMilli();
        final var delta = minorTrunc - rougthTrunc;

        // 5 mins is millis
        final var intervalSize = candle.unit.getDuration().toMillis() * candle.size;

        // 8
        final var intervalsAmount = delta / intervalSize;

        return Instant.ofEpochMilli(rougthTrunc + intervalSize * intervalsAmount).atZone(DEFAULT_TIME_ZONE_OFFSET).toLocalDateTime();
    }

    public LocalDateTime calculateAbsoluteEndDate(LocalDateTime beginigTime, CandleSize candle) {
        return beginigTime.plus(candle.unit.getDuration().multipliedBy(candle.size));
    }

}
