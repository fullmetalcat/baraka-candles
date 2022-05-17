package model;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class CandleSize {

    //available units
    public final List<ChronoUnit> unitRelations = List.of(SECONDS, MINUTES, HOURS, DAYS);

    public final int size;
    public final ChronoUnit unit;

    public CandleSize(int size, ChronoUnit unit) {
        this.size = size;
        this.unit = unit;
    }

    public long getDurationInMillis() {
        return unit.getDuration().toMillis() * size;
    }

    public ChronoUnit getBiggerTimeUnit(ChronoUnit unit) {
        for (var i = 0; i < unitRelations.size() - 2; ++i) {
            if (unit == unitRelations.get(i)) {
                return unitRelations.get(i + 1);
            }
        }
        throw new RuntimeException("unsupported ChronoUnit");
    }
}
