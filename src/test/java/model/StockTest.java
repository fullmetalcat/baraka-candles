package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static java.time.temporal.ChronoUnit.SECONDS;

class StockTest {

    @Test
    void should_return_zero_candles_with_zero_trades() {
        // given
        var stock = new Stock("APPL", List.of(SECONDS));

        // when
        var result = stock.getCandles(SECONDS);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void should_return_return_single_unfinished_candle() {
        // given
        var time = LocalDateTime.now();
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(SECONDS));
            stock.addTrade(new Trade("APPL", time, new BigDecimal(1)));

        // when
        var result = stock.getCandles(SECONDS);

        // then
        assertThat(result).isEqualTo(List.of(new Candle(SECONDS, time, price, price, price, price)));
    }

    @Test
    void should_return_return_single_finished_candle_if_time_passed() throws InterruptedException {
        // given
        var time = LocalDateTime.now().minus(5, SECONDS);
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(SECONDS));
        stock.addTrade(new Trade("APPL", time, new BigDecimal(1)));
        Thread.sleep(200);
        // when
        var result = stock.getCandles(SECONDS);

        // then
        assertThat(result).isEqualTo(List.of(new Candle(SECONDS, time, time, price, price, price, price)));
    }

    @Test
    void should_return_return_finished_and_unfinished_candle() {
        // given
        var timeFinished = LocalDateTime.now().minus(5, SECONDS);
        var timeNotFinished = LocalDateTime.now();
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(SECONDS));
        stock.addTrade(new Trade("APPL", timeFinished, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeNotFinished, new BigDecimal(1)));

        // when
        var result = stock.getCandles(SECONDS);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(SECONDS, timeFinished, timeFinished, price, price, price, price),
            new Candle(SECONDS, timeNotFinished, price, price, price, price)
            ));
    }

    @Test
    void should_return_return_finished_candles_and_unfinished_candle() {
        // given
        var timeFinished1 = LocalDateTime.now().minus(5, SECONDS);
        var timeFinished2 = LocalDateTime.now().minus(3, SECONDS);
        var timeNotFinished = LocalDateTime.now();
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(SECONDS));
        stock.addTrade(new Trade("APPL", timeFinished1, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeFinished2, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeNotFinished, new BigDecimal(1)));

        // when
        var result = stock.getCandles(SECONDS);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(SECONDS, timeFinished1, timeFinished1, price, price, price, price),
            new Candle(SECONDS, timeFinished2, timeFinished2, price, price, price, price),
            new Candle(SECONDS, timeNotFinished, price, price, price, price)
        ));
    }
}