package candles.resources.output.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static candles.Application.DEFAULT_TIME_ZONE_OFFSET;

public class CustomOptionalDateTimeSerializer extends StdSerializer<Optional<LocalDateTime>> {
    public CustomOptionalDateTimeSerializer(Class<Optional<LocalDateTime>> t) {
        super(t);
    }

    protected CustomOptionalDateTimeSerializer() {
        this(null);
    }

    @Override
    public void serialize(Optional<LocalDateTime> value, JsonGenerator gen, SerializerProvider sp)
        throws IOException {
        if (value.isPresent()) {
            final var epoch = value.get().atZone(DEFAULT_TIME_ZONE_OFFSET).toInstant().getEpochSecond();
            gen.writeString(Long.toString(epoch));
        } else {
            gen.writeString("");
        }
    }
}
