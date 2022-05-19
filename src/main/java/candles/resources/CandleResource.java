package candles.resources;

import candles.model.CandleSize;
import candles.model.MarketManager;
import candles.resources.output.JsonCandles;
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
    }

    private Route getCandles() {
        return (request, response) -> {
            final var stockName = request.params("stock");
            final var chronoUnit = ChronoUnit.valueOf(request.queryParams("cu").toUpperCase());
            final var timeUnitLength = Integer.parseInt(request.queryParams("l"));

            final var candleSize = new CandleSize(timeUnitLength, chronoUnit);
            final var candles =  marketManager.getCandles(stockName, candleSize);

            if (candles.isPresent()) {
                final var jsonCandles = new JsonCandles(candles.get(), candleSize, stockName);
                return OBJECT_MAPPER.writeValueAsString(jsonCandles);
            } else {
                response.status(404);
                return "";
            }
        };
    }

}
