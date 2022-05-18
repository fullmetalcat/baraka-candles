package candles.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
    public final int port;
    public final String url;

    public Config(@JsonProperty("port")  int port,
                  @JsonProperty("url") String url) {
        this.port = port;
        this.url = url;
    }
}
