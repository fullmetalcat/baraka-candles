package model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

public class Candle {
    public final CandleSize candleUnit;
    public final LocalDateTime openTime;
    public final Optional<LocalDateTime> closeTime;
    public final BigDecimal minPrice;
    public final BigDecimal maxPrice;
    public final BigDecimal openPrice;
    public final BigDecimal closePrice;


    public Candle(CandleSize candleUnit,
                  LocalDateTime openTime, LocalDateTime closeTime,
                  BigDecimal minPrice, BigDecimal maxPrice,
                  BigDecimal openPrice, BigDecimal closePrice) {
        this.candleUnit = candleUnit;
        this.openTime = openTime;
        this.closeTime = Optional.of(closeTime);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
    }

    public Candle(CandleSize candleUnit,
                  LocalDateTime openTime,
                  BigDecimal minPrice, BigDecimal maxPrice,
                  BigDecimal openPrice, BigDecimal closePrice) {
        this.candleUnit = candleUnit;
        this.openTime = openTime;
        this.closeTime = Optional.empty();
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Candle candle = (Candle) o;
        return candleUnit == candle.candleUnit && Objects.equals(openTime, candle.openTime) && Objects.equals(closeTime, candle.closeTime) && Objects.equals(minPrice, candle.minPrice) && Objects.equals(maxPrice, candle.maxPrice) && Objects.equals(openPrice, candle.openPrice) && Objects.equals(closePrice, candle.closePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candleUnit, openTime, closeTime, minPrice, maxPrice, openPrice, closePrice);
    }

    @Override
    public String toString() {
        return "Candle{" +
            "candleUnit=" + candleUnit +
            ", openTime=" + openTime +
            ", closeTime=" + closeTime +
            ", minPrice=" + minPrice +
            ", maxPrice=" + maxPrice +
            ", openPrice=" + openPrice +
            ", closePrice=" + closePrice +
            '}';
    }
}
