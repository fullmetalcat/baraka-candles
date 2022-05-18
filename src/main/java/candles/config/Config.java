package candles.config;

import candles.model.CandleSize;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

public class Config {
    public final int port;
    public final int threadPoolSize;
    public final String url;
    public final List<CandleSize> candleSizes;

    public Config(@JsonProperty("port")  int port,
                  @JsonProperty("threadPoolSize") int threadPoolSize,
                  @JsonProperty("url") String url,
                  @JsonProperty("candles") List<String> candleSizes) {
        this.port = port;
        this.url = url;
        this.threadPoolSize = threadPoolSize;
        this.candleSizes = candleSizes.stream().map(s -> {
            final var split = s.split(":");
            return new CandleSize(Integer.parseInt(split[0]), ChronoUnit.valueOf(split[1]));
        }).collect(toUnmodifiableList());
    }
}
