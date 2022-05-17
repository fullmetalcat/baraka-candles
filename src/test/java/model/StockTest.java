package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

class StockTest {
    private final ScheduledExecutorService scheduler = newScheduledThreadPool(1);

    @Test
    void should_return_zero_candles_with_zero_trades() {
        // given
        var candleSize = new CandleSize(1, SECONDS);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void should_return_return_single_unfinished_candle() {
        // given
        var time = LocalDateTime.now();
        var candleSize = new CandleSize(1, SECONDS);
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", time, new BigDecimal(1)));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(new Candle(candleSize, time, price, price, price, price)));
    }

    @Test
    void should_return_return_finished_and_unfinished_candle() {
        // given
        var candleSize = new CandleSize(1, SECONDS);
        var timeFinished = LocalDateTime.now().minus(5, SECONDS);
        var timeNotFinished = LocalDateTime.now();
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeNotFinished, new BigDecimal(1)));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished, timeFinished, price, price, price, price),
            new Candle(candleSize, timeNotFinished, price, price, price, price)
        ));
    }

    @Test
    void should_return_finished_candles_and_unfinished_candle() {
        // given
        var candleSize = new CandleSize(1, SECONDS);
        var timeFinished1 = LocalDateTime.now().minus(5, SECONDS);
        var timeFinished2 = LocalDateTime.now().minus(3, SECONDS);
        var timeNotFinished = LocalDateTime.now();
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished1, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeFinished2, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeNotFinished, new BigDecimal(1)));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished1, timeFinished1, price, price, price, price),
            new Candle(candleSize, timeFinished2, timeFinished2, price, price, price, price),
            new Candle(candleSize, timeNotFinished, price, price, price, price)
        ));
    }

    @Test
    void should_support_complex_intervals_and_return_candles() {
        // given
        var candleSize = new CandleSize(5, MINUTES);
        var time = LocalDateTime.of(2022, 12, 12, 12, 12, 12);
        var timeFinished1 = time.minus(8, MINUTES);
        var timeFinished2 = time.minus(3, MINUTES);
        var timeNotFinished = time;
        var price = new BigDecimal(1);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished1, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeFinished2, new BigDecimal(1)));
        stock.addTrade(new Trade("APPL", timeNotFinished, new BigDecimal(1)));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished1, timeFinished1, price, price, price, price),
            new Candle(candleSize, timeFinished2, timeFinished2, price, price, price, price),
            new Candle(candleSize, timeNotFinished, price, price, price, price)
        ));
    }
}