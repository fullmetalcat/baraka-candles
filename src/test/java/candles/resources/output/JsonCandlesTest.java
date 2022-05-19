package candles.resources.output;

import candles.model.Candle;
import candles.model.CandleSize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static candles.Application.OBJECT_MAPPER;
import static java.time.LocalDateTime.of;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonCandlesTest {

    @Test
    void should_serialize_candles_correctly() throws Exception {
        // given
        var candleSize = new CandleSize(5, SECONDS);
        var time = of(2022, 12, 12, 12, 12, 12);
        var price = new BigDecimal("10.123");
        var candle = new Candle(candleSize, time, time, price, price, price, price);
        var jsonCandles = new JsonCandles(List.of(candle), candleSize);

        // when
        var result = OBJECT_MAPPER.writeValueAsString(jsonCandles);

        //then
        assertThat(result).isEqualTo("{\"candles\":[{\"openTime\":\"1670847132\",\"closeTime\":\"1670847132\",\"minPrice\":10.123,\"maxPrice\":10.123,\"openPrice\":10.123,\"closePrice\":10.123}],\"candleSize\":5,\"candleChronoUnit\":\"SECONDS\"}");
    }

}