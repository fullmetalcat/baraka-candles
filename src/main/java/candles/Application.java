package candles;

import candles.config.Config;
import candles.config.ConfigParser;
import candles.model.CandleSize;
import candles.model.MarketManager;
import candles.resources.CandleResource;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;

import static candles.config.ConfigParser.loadLocalConfigFrom;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static java.lang.String.format;
import static java.nio.ByteBuffer.wrap;
import static java.nio.file.Files.readString;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class Application {

    public final static ZoneOffset DEFAULT_TIME_ZONE_OFFSET = UTC;

    public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(WRITE_BIGDECIMAL_AS_PLAIN, true).configure(FAIL_ON_UNKNOWN_PROPERTIES, false).configure(USE_BIG_DECIMAL_FOR_FLOATS, true).configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false).configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        final var configPath = args[args.length - 1];

        try {
            final var config = loadLocalConfigFrom(configPath);

            start(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void start(Config config) {
        try {
            var factory = new WebSocketFactory();
            var uri = URI.create(config.url);
            final var socket = factory.createSocket(uri);
            LOG.info("open: {}", socket.isOpen());

            final var market = new MarketManager(List.of(new CandleSize(1, SECONDS), new CandleSize(5, SECONDS), new CandleSize(10, SECONDS), new CandleSize(15, SECONDS), new CandleSize(30, SECONDS), new CandleSize(1, MINUTES), new CandleSize(5, MINUTES), new CandleSize(10, MINUTES), new CandleSize(15, MINUTES), new CandleSize(30, MINUTES), new CandleSize(1, HOURS)));
            final var listener = new ApiListener(market, OBJECT_MAPPER);

            Spark.port(config.port);
            final var candleResource = new CandleResource(market);
            candleResource.registerRoutes();
            registerShutdownHook();
            Spark.awaitInitialization();

            socket.addListener(listener);
            socket.connect();
        } catch (IOException | WebSocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping Spark");
            Spark.stop();

            LOG.info("Awaiting Spark termination...");
            Spark.awaitStop();
        }));
    }

}