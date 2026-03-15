package io.serenditree.root.util.oak;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OakHtmlTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "<p>Hello World!</p>",
        "Hello World!<br/>",
        "Hello World!<txt>",
        "<script>alert('XSS')</script>"
    })
    void sanitizeThrows(final String input) {
        assertThrows(BadRequestException.class, () -> OakHtml.sanitize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Hello World!\n",
        "a -> b",
        "< text >"
    })
    void sanitizeDoesNotThrow(final String input) {
        assertDoesNotThrow(() -> OakHtml.sanitize(input));
    }
}
