package candles.model;

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

    private final LocalDateTime time = LocalDateTime.of(2022, 12, 12, 12, 12, 12);
    BigDecimal price1 = new BigDecimal(1);
    BigDecimal price2 = new BigDecimal(2);
    BigDecimal price3= new BigDecimal(2);
    BigDecimal price4 = new BigDecimal(2);
    BigDecimal price5 = new BigDecimal(2);

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
        var candleSize = new CandleSize(10, SECONDS);
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", time, price1));
        stock.addTrade(new Trade("APPL", time.plus(1, SECONDS), price2));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(new Candle(candleSize, time, time.plus(1, SECONDS), price1, price2, price1, price2)));
    }

    @Test
    void should_return_return_finished_and_unfinished_candle() {
        // given
        var candleSize = new CandleSize(1, SECONDS);
        var timeFinished = LocalDateTime.now().minus(5, SECONDS);
        var timeNotFinished = LocalDateTime.now();
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished, price1));
        stock.addTrade(new Trade("APPL", timeNotFinished, price2));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished, timeFinished, price1, price1, price1, price1),
            new Candle(candleSize, timeNotFinished, timeNotFinished, price2, price2, price2, price2)
        ));
    }

    @Test
    void should_return_finished_candles_and_unfinished_candle() {
        // given
        var candleSize = new CandleSize(1, SECONDS);
        var timeFinished1 = time.minus(5, SECONDS);
        var timeFinished2 = time.minus(3, SECONDS);
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
            new Candle(candleSize, timeNotFinished, timeNotFinished, price, price, price, price)
        ));
    }

    @Test
    void should_support_complex_intervals_and_return_candles() {
        // given
        var candleSize = new CandleSize(5, MINUTES);
        var timeFinished1 = time.minus(8, MINUTES);
        var timeFinished2 = time.minus(3, MINUTES);
        var timeNotFinished = time;
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished1, price1));
        stock.addTrade(new Trade("APPL", timeFinished2, price1));
        stock.addTrade(new Trade("APPL", timeNotFinished, price1));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished1, timeFinished1, price1, price1, price1, price1),
            new Candle(candleSize, timeFinished2, timeFinished2, price1, price1, price1, price1),
            new Candle(candleSize, timeNotFinished, timeNotFinished, price1, price1, price1, price1)
        ));
    }

    @Test
    void should_calculate_max_values() {
        // given
        var candleSize = new CandleSize(12, MINUTES);
        var timeFinished1 = time.minus(4, MINUTES);
        var timeFinished2 = time.minus(3, MINUTES);
        var timeFinished3 = time.minus(2, MINUTES);
        var timeFinished4 = time.minus(1, MINUTES);
        var timeNotFinished = time;
        var stock = new Stock("APPL", List.of(candleSize), scheduler);
        stock.addTrade(new Trade("APPL", timeFinished1, price1));
        stock.addTrade(new Trade("APPL", timeFinished2, price2));
        stock.addTrade(new Trade("APPL", timeFinished3, price3));
        stock.addTrade(new Trade("APPL", timeFinished4, price1));
        stock.addTrade(new Trade("APPL", timeNotFinished, price2));

        // when
        var result = stock.getCandles(candleSize);

        // then
        assertThat(result).isEqualTo(List.of(
            new Candle(candleSize, timeFinished1, timeFinished4, price1, price3, price1, price1),
            new Candle(candleSize, timeNotFinished, timeNotFinished, price2, price2, price2, price2)
        ));
    }
}