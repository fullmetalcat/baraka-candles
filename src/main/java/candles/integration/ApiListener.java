package candles.integration;

import candles.input.InputJson;
import candles.model.MarketManager;
import candles.model.Trade;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static candles.Application.OBJECT_MAPPER;

public class ApiListener extends WebSocketAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiListener.class);

    private final MarketManager marketManager;

    public ApiListener(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) {
        LOG.info("state changed to {}", newState);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        LOG.info("connected");
        super.onConnected(websocket, headers);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        final var request = OBJECT_MAPPER.readValue(text, InputJson.class);
        for (var event : request.data) {
            var trade = new Trade(event.stock, event.time, event.price);
            marketManager.processMarketEvent(trade);
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        LOG.error("error", cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        LOG.error("unecpected error", cause);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        LOG.info("disconected by" + (closedByServer ? "server" : "error"));
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        LOG.error("connection error", exception);
    }
}
