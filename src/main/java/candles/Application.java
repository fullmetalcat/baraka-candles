package candles;

import candles.config.Config;
import candles.integration.ApiListener;
import candles.model.MarketManager;
import candles.resources.CandleResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneOffset;

import static candles.config.ConfigLoader.loadLocalConfigFrom;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static java.time.ZoneOffset.UTC;

public class Application {

    public final static ZoneOffset DEFAULT_TIME_ZONE_OFFSET = UTC;

    public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .disable(WRITE_DATES_AS_TIMESTAMPS);

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

    public static void start(Config config, ApiListener apiListener, MarketManager market) {
        try {
            initializeHttpResources(config, market);

            final var socket = initializeWebSocet(config, apiListener);
            socket.connect();//blocks thread
        } catch (IOException | WebSocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void start(Config config) {
        final var market = new MarketManager(config.candleSizes, config.threadPoolSize);
        final var listener = new ApiListener(market);
        start(config, listener, market);
    }

    private static WebSocket initializeWebSocet(Config config, ApiListener apiListener) throws IOException {
        var factory = new WebSocketFactory();
        factory.setConnectionTimeout(1000);
        var uri = URI.create(config.url);
        final var socket = factory.createSocket(uri);
        LOG.info("open: {}", socket.isOpen());
        socket.addListener(apiListener);
        return socket;
    }

    private static void initializeHttpResources(Config config, MarketManager market) {
        Spark.port(config.port);
        final var candleResource = new CandleResource(market);
        candleResource.registerRoutes();
        registerShutdownHook();
        Spark.awaitInitialization();
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