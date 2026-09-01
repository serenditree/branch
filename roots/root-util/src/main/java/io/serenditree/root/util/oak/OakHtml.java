package io.serenditree.root.util.oak;

import jakarta.ws.rs.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamEventReceiverWrapper;
import org.owasp.html.PolicyFactory;

import java.util.List;

/**
 * Class for deciding globally if user input is oak or nut(s).
 */
public class OakHtml {

    private static final class HtmlSecurityEventReceiver extends HtmlStreamEventReceiverWrapper {

        public HtmlSecurityEventReceiver(HtmlStreamEventReceiver underlying) {
            super(underlying);
        }

        @Override
        public void openTag(@NonNull String tag, @NonNull List<String> attributes) {
            throw new BadRequestException("HTML tag detected: " + tag);
        }

        @Override
        public void closeTag(@NonNull String tag) {
            throw new BadRequestException("HTML tag detected: " + tag);
        }
    }

    private static final PolicyFactory NO_HTML_SECURITY_POLICY = new HtmlPolicyBuilder()
        .withPreprocessor(HtmlSecurityEventReceiver::new)
        .toFactory();

    private OakHtml() {
    }

    /**
     * Checks input for tags and protects against XSS;
     *
     * @param input Text
     */
    public static void sanitize(String input) {
        NO_HTML_SECURITY_POLICY.sanitize(input);
    }
}
