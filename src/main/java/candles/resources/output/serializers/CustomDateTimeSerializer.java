package candles.resources.output.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

import static candles.Application.DEFAULT_TIME_ZONE_OFFSET;

public class CustomDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public CustomDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    protected CustomDateTimeSerializer() {
        this(null);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp)
        throws IOException {
        Long epoch = value.atZone(DEFAULT_TIME_ZONE_OFFSET).toInstant().getEpochSecond();
        gen.writeString(epoch.toString());

    }
}
