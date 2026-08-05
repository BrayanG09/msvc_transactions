package hn.infatlan.msvc_transactions.util.jackson;

import hn.infatlan.msvc_transactions.annotations.SanitizeText;
import hn.infatlan.msvc_transactions.util.InputSanitizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class SanitizeTextDeserializer extends ValueDeserializer<String> {

    private final int maxLength;
    private final boolean blankToNull;

    public SanitizeTextDeserializer() {
        this(255, false);
    }

    private SanitizeTextDeserializer(int maxLength, boolean blankToNull) {
        this.maxLength = maxLength;
        this.blankToNull = blankToNull;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        if (property == null) {
            return this;
        }

        SanitizeText annotation = property.getAnnotation(SanitizeText.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(SanitizeText.class);
        }

        if (annotation == null) {
            return this;
        }

        return new SanitizeTextDeserializer(annotation.maxLength(), annotation.blankToNull());
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        String value = parser.getValueAsString();
        if (value == null) {
            return null;
        }

        String sanitized = InputSanitizer.truncate(InputSanitizer.sanitize(value), maxLength);
        if (blankToNull && (sanitized == null || sanitized.isBlank())) {
            return null;
        }
        return sanitized;
    }
}
