package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Stock {

    public final String stockName;
    private final Map<ChronoUnit, List<Candle>> candles; // long-term candle storage
    private final Map<ChronoUnit, Deque<Trade>> curTrades; // storage of current trades, queue is periodically emptied to calculate another candle
    private final Map<ChronoUnit, Lock> consistencyLocks;

    private final ScheduledExecutorService scheduler = newScheduledThreadPool(10);

    public Stock(String stockName, List<ChronoUnit> candleUnits) {
        this.stockName = stockName;

        candles = new ConcurrentHashMap<>();
        curTrades = new ConcurrentHashMap<>();
        consistencyLocks = new ConcurrentHashMap<>();

        candleUnits.forEach(cu -> {
            candles.put(cu, new LinkedList<>());
            final var durationInMillis = cu.getDuration().toMillis();
            curTrades.put(cu, new ConcurrentLinkedDeque<>());
            consistencyLocks.put(cu, new ReentrantLock());
            scheduler.scheduleAtFixedRate(() -> {
                calculateFinalCandles(cu);
            }, 10, durationInMillis, MILLISECONDS);//initial delay to increase probability of hitting final candles
        });

    }

    // this introduces weak consisteny into the API - last candle might be inconsistent
    public void addTrade(Trade trade) {
        curTrades.values().forEach(q -> q.add(trade));
    }

    public List<Candle> getCandles(ChronoUnit candleChronoUnit) {
        final var lock = consistencyLocks.get(candleChronoUnit);
        lock.lock();
        System.out.println("returning candles");

        try {
            calculateFinalCandles(candleChronoUnit);// this guarantees curTrades contains only non-final candle data

            final var last = calculateNonFinalCandle(candleChronoUnit);
            final var finalCandles = candles.get(candleChronoUnit);

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

    public Optional<Candle> calculateNonFinalCandle(ChronoUnit candleChronoUnit) {
        System.out.println("calculating last candle");
        final var tradesIterator = curTrades.get(candleChronoUnit).iterator();

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
            firstTrade = trade;
        }

        return Optional.of(new Candle(candleChronoUnit, firstTrade.time, minPrice, maxPrice, firstTrade.price, lastTrade.price));
    }

    // task that calculates all complete candles from the queue
    public void calculateFinalCandles(ChronoUnit candleChronoUnit) {

        final var lock = consistencyLocks.get(candleChronoUnit);
        lock.lock();
        System.out.println("calculating final candles");

        try {
            final var tradesIterator = curTrades.get(candleChronoUnit).iterator();//starting from the oldest trade
            final List<Trade> candleTrades = new ArrayList<>(); // will search all trades within candle time interval to add to the list
            LocalDateTime candleSliceTimeLeft;
            LocalDateTime candleSliceTimeRight;
            BigDecimal maxPrice;
            BigDecimal minPrice;

            if (tradesIterator.hasNext()) { // scanning first trade as a candle start
                var trade = tradesIterator.next();
                candleSliceTimeLeft = trade.time
                    .truncatedTo(candleChronoUnit)
                    .minus(1, candleChronoUnit);
                candleSliceTimeRight = trade.time
                    .truncatedTo(candleChronoUnit);
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
                            candles.get(candleChronoUnit).add(
                                new Candle(candleChronoUnit,
                                    candleTrades.get(candleTrades.size() - 1).time, candleTrades.get(0).time,
                                    minPrice, maxPrice,
                                    candleTrades.get(candleTrades.size() - 1).price, candleTrades.get(0).price
                                ));
                        }
                        maxPrice = trade.price;
                        minPrice = trade.price;
                        candleSliceTimeLeft = candleSliceTimeLeft.plus(1, candleChronoUnit);
                        candleSliceTimeRight = candleSliceTimeRight.plus(1, candleChronoUnit);
                        candleTrades.clear();
                        candleTrades.add(trade);
                        System.out.println("created final candle");
                    }
                }

                final var currentTime = LocalDateTime.now();
                final var diff = candleChronoUnit.between(candleTrades.get(candleTrades.size() - 1).time.truncatedTo(candleChronoUnit),
                    currentTime.truncatedTo(candleChronoUnit));
                if (diff >=1) {

                    candles.get(candleChronoUnit).add(
                        new Candle(candleChronoUnit,
                            candleTrades.get(candleTrades.size() - 1).time, candleTrades.get(0).time,
                            minPrice, maxPrice,
                            candleTrades.get(candleTrades.size() - 1).price, candleTrades.get(0).price
                        ));
                    candleTrades.clear();
                }
            }
            curTrades.put(candleChronoUnit, new ConcurrentLinkedDeque<>(candleTrades));//re-ading trades if no closed candle was calculated
        } finally {
            lock.unlock();
        }

    }

//    public void calculateCandles(ChronoUnit candleChronoUnit) {
//
//        final var curTime = LocalDateTime.now();
//        final var candleSliceTimeLeft = curTime
//            .truncatedTo(candleChronoUnit)
//            .minus(1, candleChronoUnit);
//        final var candleSliceTimeRight = curTime
//            .truncatedTo(candleChronoUnit);
//
//        Trade prevTrade = null;
//        Trade lastCandleTrade = null;
//        Trade firstCandleTrade = null;
//        var maxPrice = new BigDecimal(0);
//        var minPrice = new BigDecimal(0);
//
//        final var iterator = trades.descendingIterator();
//
//        while (iterator.hasNext()) {  //skipping in case new trades received
//            final var trade = iterator.next();
//            maxPrice = trade.price;
//            minPrice = trade.price;
//            if (trade.time.isBefore(candleSliceTimeRight)) {
//                lastCandleTrade = trade;
//                prevTrade = trade;
//                break;
//            }
//        }
//
//        while (iterator.hasNext()) {
//            final var trade = iterator.next();
//
//            if (trade.time.isAfter(candleSliceTimeLeft)) {
//                prevTrade = trade;
//
//                if (trade.price.compareTo(maxPrice) > 0) {
//                    maxPrice = trade.price;
//                }
//
//                if (trade.price.compareTo(minPrice) < 0) {
//                    minPrice = trade.price;
//                }
//
//            } else if (trade.time.isBefore(candleSliceTimeLeft)) {
//                firstCandleTrade = prevTrade;
//                break;
//            }
//
//        }
//
//        if (firstCandleTrade != null) {
//            final var candle = new Candle(candleChronoUnit,
//                firstCandleTrade.time, lastCandleTrade.time,
//                minPrice, maxPrice,
//                firstCandleTrade.price, lastCandleTrade.price);
//
//            candles.get(candleChronoUnit).add(candle);
//        }
//
//    }

}
