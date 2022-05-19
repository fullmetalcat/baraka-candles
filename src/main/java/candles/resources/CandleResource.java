package candles.resources;

import candles.model.CandleSize;
import candles.model.MarketManager;
import spark.Route;

import java.time.temporal.ChronoUnit;

import static candles.Application.OBJECT_MAPPER;
import static spark.Spark.get;
import static spark.Spark.path;

public class CandleResource implements Resource {

    MarketManager marketManager;

    public CandleResource(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @Override
    public void registerRoutes() {
        path("/:stock", () -> {
            get("/candles", getCandles());
        });

        path("/candles", () -> {
            get("", getCandlesInfo());
        });

        path("/stocks", () -> {
            get("", getAvailableStocks());
        });
    }

    private Route getCandles() {
        return (request, response) -> {
            final var stockName = request.params("stock");
            final var chronoUnit = ChronoUnit.valueOf(request.queryParams("cu"));
            final var timeUnitLength = Integer.parseInt(request.queryParams("l"));

            final var candleSize = new CandleSize(timeUnitLength, chronoUnit);
            final var candles =  marketManager.getCandles(stockName, candleSize);
            return OBJECT_MAPPER.writeValueAsString(candles);
        };
    }

    private Route getCandlesInfo() {
        return (request, response) -> {
            return marketManager.getCandleUnits();
        };
    }

    private Route getAvailableStocks() {
        return (request, response) -> {
            return marketManager.getStockNames();
        };
    }

}
