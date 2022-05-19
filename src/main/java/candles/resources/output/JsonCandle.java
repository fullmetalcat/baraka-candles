package candles.resources.output;

import candles.model.Candle;
import candles.resources.output.serializers.CustomDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JsonCandle {
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    public final LocalDateTime openTime;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    public final LocalDateTime closeTime;

    public final BigDecimal minPrice;
    public final BigDecimal maxPrice;
    public final BigDecimal openPrice;
    public final BigDecimal closePrice;

    public JsonCandle(Candle candle) {
        this.openTime = candle.openTime;
        this.closeTime = candle.closeTime;
        this.minPrice = candle.minPrice;
        this.maxPrice = candle.maxPrice;
        this.openPrice = candle.openPrice;
        this.closePrice = candle.closePrice;
    }
}
