package hn.infatlan.msvc_transactions.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import hn.infatlan.msvc_transactions.util.jackson.SanitizeIdentityDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = SanitizeIdentityDeserializer.class)
public @interface SanitizeIdentity {
}
