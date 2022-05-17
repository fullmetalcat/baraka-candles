package candles.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

import static java.util.Comparator.comparing;

public class Trade implements Comparable<Trade> {

    public String stockName;
    public LocalDateTime time;
    public BigDecimal price;

    private static final Comparator<Trade> COMPARATOR = comparing((Trade entry) -> entry.time);

    public Trade(String stockName, LocalDateTime time, BigDecimal price) {
        this.stockName = stockName;
        this.time = time;
        this.price = price;
    }

    @Override
    public int compareTo(Trade that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public String toString() {
        return "Trade{" +
            "stockName='" + stockName + '\'' +
            ", time=" + time +
            ", price=" + price +
            '}';
    }
}
