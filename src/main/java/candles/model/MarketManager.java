package candles.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class MarketManager {

    private final Map<String, Stock> market;

    private final List<CandleSize> candleUnits;

    private final ScheduledExecutorService scheduler;

    public MarketManager(List<CandleSize> candleUnits, int threadPoolSize) {
        this.market = new ConcurrentHashMap<>();
        this.candleUnits = candleUnits;
        this.scheduler = newScheduledThreadPool(threadPoolSize);
    }

    public void processMarketEvent(Trade event) {
        final var stock = market.computeIfAbsent(event.stockName, k -> new Stock(k, candleUnits, scheduler));
        stock.addTrade(event);
    }

    public Optional<List<Candle>> getCandles(String stockName, CandleSize candleSize) {
        if (!candleUnits.contains(candleSize)) {
            return Optional.empty();
        }
        final var stock = market.get(stockName);
        if (stock != null) {
            return Optional.of(stock.getCandles(candleSize));
        }
        return Optional.empty();
    }

}
