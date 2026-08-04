package hn.infatlan.msvc_transactions.jackson;

import hn.infatlan.msvc_transactions.util.InputSanitizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class SanitizeIdentityDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        String value = parser.getValueAsString();
        return InputSanitizer.sanitizeIdentity(value);
    }
}
