package candles.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CandleSizeTest {

    @Test
    void should_return_correct_bigger_time_unit() {
        assertThat(CandleSize.getBiggerTimeUnit(SECONDS)).isEqualTo(MINUTES);
        assertThat(CandleSize.getBiggerTimeUnit(MINUTES)).isEqualTo(HOURS);
        assertThat(CandleSize.getBiggerTimeUnit(HOURS)).isEqualTo(DAYS);
    }

    @Test
    void should_not_be_able_to_create_bad_candle() {
        assertThatThrownBy(() -> new CandleSize(7, SECONDS)).hasMessage("tried to create inappropriate candle with unit Second sand size 7");
    }

    @Test
    void should_throw_if_unit_is_not_supported() {
        assertThatThrownBy(() -> CandleSize.getBiggerTimeUnit(DAYS)).hasMessage("unsupported ChronoUnit");
    }

    @ParameterizedTest
    @MethodSource("timesForFiveSeconds")
    void should_return_correct_time_truncations(LocalDateTime tradeTime, LocalDateTime expectedTime) {
        // given
        var candleSize = new CandleSize(5, SECONDS);

        //when
        var borderTime = candleSize.calculateAbsoluteStartDate(tradeTime);

        //then
        assertThat(borderTime).isEqualTo(expectedTime);
    }

    static Stream<Arguments> timesForFiveSeconds() {
        return Stream.of(
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 9), LocalDateTime.of(2022, 12, 12, 12 , 12, 5)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 10), LocalDateTime.of(2022, 12, 12, 12 , 12, 10)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 10, 100), LocalDateTime.of(2022, 12, 12, 12 , 12, 10)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 12), LocalDateTime.of(2022, 12, 12, 12 , 12, 10)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 13), LocalDateTime.of(2022, 12, 12, 12 , 12, 10)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 14), LocalDateTime.of(2022, 12, 12, 12 , 12, 10)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 15), LocalDateTime.of(2022, 12, 12, 12 , 12, 15)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 15, 100), LocalDateTime.of(2022, 12, 12, 12 , 12, 15)),
            arguments(LocalDateTime.of(2022, 12, 12, 12 , 12, 16), LocalDateTime.of(2022, 12, 12, 12 , 12, 15))
        );
    }
}