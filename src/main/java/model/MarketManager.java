package model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class MarketManager {

    private final Map<String, Stock> market;

    private final List<CandleSize> candleUnits;

    private final ScheduledExecutorService scheduler = newScheduledThreadPool(100);

    // TODO from config
    public MarketManager(List<CandleSize> candleUnits) {
        this.market = new ConcurrentHashMap<>();
        this.candleUnits = candleUnits;
    }

    public void processMarketEvent(Trade event) {
        final var stock = market.computeIfAbsent(event.stockName, k -> new Stock(k, candleUnits, scheduler));
        stock.addTrade(event);
    }

}
