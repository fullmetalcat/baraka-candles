package candles.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static candles.model.CandleBuilder.candle;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CandleBuilderTest {

    @Test
    void should_correclty_add_new_trade() {
        // given
        var time = LocalDateTime.of(2022, 12, 12, 12, 12, 12);
        var candleSize = new CandleSize(5, SECONDS);
        var trade = new Trade("APPL", time, new BigDecimal(1));
        var candleBuilder = candle(candleSize, trade);
        var nextTrade = new Trade("AAPL", time.plus(1, SECONDS), new BigDecimal(2));

        // when
        var newCandleBuilder = candleBuilder.addTrade(nextTrade);

        // then
        assertThat(newCandleBuilder.openPrice).isEqualTo(new BigDecimal(1));
        assertThat(newCandleBuilder.closePrice).isEqualTo(new BigDecimal(2));
        assertThat(newCandleBuilder.maxPrice).isEqualTo(new BigDecimal(2));
        assertThat(newCandleBuilder.minPrice).isEqualTo(new BigDecimal(1));
        assertThat(newCandleBuilder.openTime).isEqualTo(time);
        assertThat(newCandleBuilder.closeTime).isEqualTo(time.plus(1, SECONDS));

    }
}