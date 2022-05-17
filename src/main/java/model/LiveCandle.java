package model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class LiveCandle {
    private String stockName;
    private Duration duration;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;

    public LiveCandle(String stockName, Duration duration, LocalDateTime openTime, BigDecimal openPrice) {
        this.stockName = stockName;
        this.duration = duration;
        this.openTime = openTime;
        this.openPrice = openPrice;
    }

}
