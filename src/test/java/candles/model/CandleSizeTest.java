package candles.model;

import org.junit.jupiter.api.Test;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class CandleSizeTest {

    @Test
    void get_bigger_time_unit_should_work_for_supported_time_units() {
        assertThat(CandleSize.getBiggerTimeUnit(SECONDS)).isEqualTo(MINUTES);
        assertThat(CandleSize.getBiggerTimeUnit(MINUTES)).isEqualTo(HOURS);
        assertThat(CandleSize.getBiggerTimeUnit(HOURS)).isEqualTo(DAYS);
    }
}