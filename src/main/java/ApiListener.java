import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketState;
import input.InputJson;
import model.MarketManager;
import model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

public class ApiListener extends WebSocketAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiListener.class);
    private static final ObjectMapper mapper = new ObjectMapper()
        .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    private final MarketManager marketManager;

    public ApiListener(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        super.onStateChanged(websocket, newState);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        LOG.info("connected");
        super.onConnected(websocket, headers);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        final var request = mapper.readValue(text, InputJson.class);
        for (var event : request.data) {
            var trade = new Trade(event.stock, event.time, event.price);
            marketManager.processMarketEvent(trade);
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onUnexpectedError(websocket, cause);
    }
}
