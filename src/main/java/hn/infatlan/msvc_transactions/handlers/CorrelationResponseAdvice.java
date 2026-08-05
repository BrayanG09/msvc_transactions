package hn.infatlan.msvc_transactions.handlers;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.util.CorrelationContext;

@ControllerAdvice
public class CorrelationResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ResponseDTO<?> responseDTO) {
            if (responseDTO.getCorrelationId() == null || responseDTO.getCorrelationId().isBlank()) {
                responseDTO.setCorrelationId(CorrelationContext.getCorrelationId());
            }
        }
        return body;
    }
}
