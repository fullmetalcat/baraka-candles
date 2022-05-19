package candles.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static candles.Application.OBJECT_MAPPER;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.readString;

public class ConfigLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

    public static Config loadLocalConfigFrom(String configLocation) {
        LOG.info("Loading config from file {}", configLocation);

        try {
            final var fileToString = readString(new File(configLocation).toPath(), defaultCharset());
            return loadFrom(fileToString);
        } catch (IOException e) {
            throw new UncheckedIOException(format("Unable to load configuration from path %s", configLocation), e);
        }
    }


    public static Config loadFrom(String content) {
        try {
            return parse(new YAMLFactory().createParser(content), Config.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Can not parse config", e);
        }
    }

    public static <T> T parse(JsonParser parser, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(parser, type);
        } catch (IOException e) {
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            else
                throw new RuntimeException(e);
        }
    }
}
