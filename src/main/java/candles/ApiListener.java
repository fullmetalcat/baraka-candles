package candles;

import candles.input.InputJson;
import candles.model.MarketManager;
import candles.model.Trade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ApiListener extends WebSocketAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiListener.class);
    private final ObjectMapper mapper;

    private final MarketManager marketManager;

    public ApiListener(MarketManager marketManager, ObjectMapper mapper) {
        this.marketManager = marketManager;
        this.mapper = mapper;
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
