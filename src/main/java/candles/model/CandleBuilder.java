package candles.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CandleBuilder {
    public final CandleSize candleUnit;
    public final LocalDateTime openTime;
    public final LocalDateTime closeTime;
    public final BigDecimal minPrice;
    public final BigDecimal maxPrice;
    public final BigDecimal openPrice;
    public final BigDecimal closePrice;

    public static CandleBuilder candle(CandleSize candleUnit, Trade firstTrade) {
        return new CandleBuilder(candleUnit, firstTrade);
    }

    private CandleBuilder(CandleSize candleUnit,
                         LocalDateTime openTime,
                         LocalDateTime closeTime,
                         BigDecimal minPrice,
                         BigDecimal maxPrice,
                         BigDecimal openPrice,
                         BigDecimal closePrice) {
        this.candleUnit = candleUnit;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
    }

    private CandleBuilder(CandleSize candleUnit, Trade firstTrade) {
        this.candleUnit = candleUnit;
        this.openTime = firstTrade.time;
        this.closeTime = firstTrade.time;
        this.minPrice = firstTrade.price;
        this.maxPrice = firstTrade.price;
        this.openPrice = firstTrade.price;
        this.closePrice = firstTrade.price;
    }

    public Candle build() {
        return new Candle(candleUnit, openTime, closeTime, minPrice, maxPrice, openPrice, closePrice);
    }

    //this method assumes adding trades sorted by time as they were received
    public CandleBuilder addTrade(Trade trade) {
        final var closeTime = trade.time;
        final var minPrice = this.minPrice.min(trade.price);
        final var maxPrice = this.maxPrice.max(trade.price);
        final var closePrice = trade.price;

        return new CandleBuilder(this.candleUnit, this.openTime, closeTime, minPrice, maxPrice, this.openPrice, closePrice);
    }

}
