package candles.resources.output;

import candles.model.Candle;
import candles.model.CandleSize;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonCandles {

    public final List<JsonCandle> candles;
    public final Integer candleSize;
    public final String candleChronoUnit;
    public final String stockName;

    public JsonCandles(List<Candle> candles, CandleSize candleSize, String stockName) {
        this.candles = candles.stream().map(c -> new JsonCandle(c)).collect(toList());
        this.candleChronoUnit = candleSize.unit.name();
        this.candleSize = candleSize.size;
        this.stockName = stockName;

    }
}
