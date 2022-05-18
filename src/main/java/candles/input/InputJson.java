package candles.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InputJson {

    public final List<InputJsonTick> data;
    public final String type;

    public InputJson(@JsonProperty("data") List<InputJsonTick> data,
                     @JsonProperty("type") String type) {
        this.data = data;
        this.type = type.intern();
    }

    @Override
    public String toString() {
        return "InputJson{" +
            "data=" + data +
            ", type='" + type + '\'' +
            '}';
    }
}
