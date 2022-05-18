package candles.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static candles.Application.DEFAULT_TIME_ZONE_OFFSET;

public class InputJsonTick {

    public final BigDecimal price;
    public final String stock;
    public final LocalDateTime time;

    public InputJsonTick(@JsonProperty("p") BigDecimal price,
                         @JsonProperty("s") String stock,
                         @JsonProperty("t") long timestamp) {
        this.price = price;
        this.stock = stock.intern();
        this.time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_TIME_ZONE_OFFSET);
    }

    @Override
    public String toString() {
        return "InputJsonTick{" +
            "price=" + price +
            ", stock='" + stock + '\'' +
            ", time=" + time +
            '}';
    }
}
