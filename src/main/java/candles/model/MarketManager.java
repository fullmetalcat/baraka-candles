package candles.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
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

    public List<Candle> getCandles(String stockName, CandleSize candleSize) {
        final var stock = market.get(stockName);
        return stock.getCandles(candleSize);
    }

    public List<CandleSize> getCandleUnits() {
        return unmodifiableList(candleUnits);
    }

    public Set<String> getStockNames() {
        return unmodifiableSet(market.keySet());
    }
}
