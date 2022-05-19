package candles.resources.output;

import candles.model.Candle;
import candles.resources.output.serializers.CustomDateTimeSerializer;
import candles.resources.output.serializers.CustomOptionalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class JsonCandle {
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    public final LocalDateTime openTime;

    @JsonSerialize(using = CustomOptionalDateTimeSerializer.class)
    public final Optional<LocalDateTime> closeTime;

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
