package candles.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static candles.Application.DEFAULT_TIME_ZONE_OFFSET;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class CandleSize {
    //available units, ordered
    private final static List<ChronoUnit> unitRelations = List.of(SECONDS, MINUTES, HOURS, DAYS);

    public final int size;
    public final ChronoUnit unit;

    public CandleSize(int size, ChronoUnit unit) {
        this.size = size;
        this.unit = unit;

        final var length = unit.getDuration().toMillis() * size;
        final var higherUnitLength = getBiggerTimeUnit(unit).getDuration().toMillis();
        if (higherUnitLength % length != 0) {
            throw new RuntimeException(String.format("tried to create inappropriate candle with unit %s and size %s", unit, size));
        }
    }

    public long getDurationInMillis() {
        return unit.getDuration().toMillis() * size;
    }

    // this method truncated time passed to the candle interval begining. for example if time is 12:12, candle is 5 minutes, result will be 12:10
    public LocalDateTime calculateAbsoluteStartDate(LocalDateTime curTime) {
        final var rougthTrunc = curTime.truncatedTo(getBiggerTimeUnit(this.unit)).toInstant(DEFAULT_TIME_ZONE_OFFSET).toEpochMilli();
        final var minorTrunc = curTime.truncatedTo(this.unit).toInstant(DEFAULT_TIME_ZONE_OFFSET).toEpochMilli();
        final var delta = minorTrunc - rougthTrunc;

        final var intervalSize = this.unit.getDuration().toMillis() * this.size;

        final var intervalsAmount = delta / intervalSize;

        return Instant.ofEpochMilli(rougthTrunc + intervalSize * intervalsAmount).atZone(DEFAULT_TIME_ZONE_OFFSET).toLocalDateTime();
    }

    public LocalDateTime calculateAbsoluteEndDate(LocalDateTime beginigTime) {
        return beginigTime.plus(unit.getDuration().multipliedBy(size));
    }

    public static ChronoUnit getBiggerTimeUnit(ChronoUnit unit) {
        for (var i = 0; i < unitRelations.size() - 1; ++i) {
            if (unit == unitRelations.get(i)) {
                return unitRelations.get(i + 1);
            }
        }
        throw new RuntimeException("unsupported ChronoUnit");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CandleSize that = (CandleSize) o;
        return size == that.size && Objects.equals(unitRelations, that.unitRelations) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitRelations, size, unit);
    }

    @Override
    public String toString() {
        return "CandleSize{" +
            ", size=" + size +
            ", unit=" + unit +
            '}';
    }
}
