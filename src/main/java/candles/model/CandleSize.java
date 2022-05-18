package candles.model;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class CandleSize {

    //available units TODO move to config
    public final static List<ChronoUnit> unitRelations = List.of(SECONDS, MINUTES, HOURS, DAYS);

    public final int size;
    public final ChronoUnit unit;

    public CandleSize(int size, ChronoUnit unit) {
        this.size = size;
        this.unit = unit;

        //TODO check that bigger time unit can be divided by the offered one without remainder
    }

    public long getDurationInMillis() {
        return unit.getDuration().toMillis() * size;
    }

    public final static ChronoUnit getBiggerTimeUnit(ChronoUnit unit) {
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
