import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        System.out.println("Hello world!");

        var factory = new WebSocketFactory();
        var uri = URI.create("ws://b-mocks.dev.app.getbaraka.com:9989");
        try {
            final var socket = factory.createSocket(uri);
            LOG.info("open: {}", socket.isOpen());

            final var listener = new ApiListener();
            socket.addListener(listener);
            LOG.info(socket.getAgreedProtocol());
            socket.connect();
        } catch (IOException | WebSocketException e) {
            throw new RuntimeException(e);
        }
    }
}