package io.serenditree.root.util.conifer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to retrieve config files from resources.
 */
public class Conifer {

    private Conifer() {
    }

    /**
     * Returns an {@link InputStream} for the given config file.
     * @param config Name of the config file.
     * @return {@link InputStream}
     */
    public static InputStream get(final String config) {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(config);
    }

    /**
     * Returns the given config file as {@link String}.
     * @param config Name of the config file.
     * @return {@link String}
     */
    public static String getString(final String config) throws IOException {
        return new String(Conifer.get(config).readAllBytes(), StandardCharsets.UTF_8);
    }
}
