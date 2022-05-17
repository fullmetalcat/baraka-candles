package candles;

import candles.model.CandleSize;
import candles.model.MarketManager;
import candles.resources.CandleResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.SECONDS;

public class Main {
    public final static ZoneOffset DEFAULT_TIME_ZONE_OFFSET = UTC;
    public final static ObjectMapper mapper = new ObjectMapper()
        .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        var factory = new WebSocketFactory();
        var uri = URI.create("ws://b-mocks.dev.app.getbaraka.com:9989");
        try {
            final var socket = factory.createSocket(uri);
            LOG.info("open: {}", socket.isOpen());

            final var market = new MarketManager(List.of(new CandleSize(5, SECONDS)));
            final var listener = new ApiListener(market, mapper);

            Spark.port(8080);
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