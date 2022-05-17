package model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketManager {

    private final Map<String, Stock> market;

    private final List<ChronoUnit> candleUnits;

    // TODO from config
    public MarketManager(List<ChronoUnit> candleUnits) {
        this.market = new ConcurrentHashMap<>();
        this.candleUnits = candleUnits;
    }

    public void processMarketEvent(Trade event) {
        final var stock = market.computeIfAbsent(event.stockName, k -> new Stock(k, candleUnits));
        stock.addTrade(event);
    }

}
