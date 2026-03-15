package com.serenditree.root.util.oak;

import jakarta.ws.rs.BadRequestException;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamEventReceiverWrapper;
import org.owasp.html.PolicyFactory;

import java.util.List;

/**
 * Class for deciding globally if user input is oak or nut(s).
 */
public class OakHtml {

    private OakHtml() {
    }

    private static final class HtmlSecurityEventReceiver extends HtmlStreamEventReceiverWrapper {

        public HtmlSecurityEventReceiver(HtmlStreamEventReceiver underlying) {
            super(underlying);
        }

        @Override
        public void openTag(String tag, List<String> attributes) {
            throw new BadRequestException("HTML tag detected: " + tag);
        }
    }

    private static final PolicyFactory HTML_SECURITY_POLICY = new HtmlPolicyBuilder()
        .withPreprocessor(HtmlSecurityEventReceiver::new)
        .toFactory();

    /**
     * Checks input for tags and protects against XSS;
     *
     * @param input Text
     * @return Sanitized text.
     */
    public static String sanitize(String input) {
        return HTML_SECURITY_POLICY.sanitize(input);
    }
}
