package com.taptap.tds.registration.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taptap.tds.registration.server.configuration.PublicityProperties;
import com.taptap.tds.registration.server.util.SignUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class SignWebFilter implements WebFilter {

    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PublicityProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if(path.startsWith("/ws")){
            return chain.filter(exchange);
        }

        String signHeader = exchange.getRequest().getHeaders().getFirst("sign");
        if(StringUtils.isBlank(signHeader)){
            return writeErrorMessage(exchange.getResponse());
        }

        Map<String, String> stringStringMap = exchange.getRequest().getQueryParams().toSingleValueMap();

        if(exchange.getRequest().getHeaders().getContentLength() > 0) {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        DataBufferUtils.retain(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux.defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));

                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };

                        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                        return ServerRequest.create(exchange.mutate().request(mutatedRequest).build(), messageReaders)
                                .bodyToMono(String.class)
                                .map(body->{
                                    String sign = SignUtil.generateSignature(stringStringMap, body, this.properties.getSignKey());
                                    return signHeader.equals(sign);
                                }).flatMap(success->{
                                    if(success) {
                                        return chain.filter(mutatedExchange);
                                    } else {
                                        return writeErrorMessage(exchange.getResponse());
                                    }
                                });
                    });
        } else {
            String sign = SignUtil.generateSignature(stringStringMap, "", this.properties.getSignKey());
            if(!signHeader.equals(sign)){
                return writeErrorMessage(exchange.getResponse());
            }

        }
        return chain.filter(exchange);
    }

    protected Mono<Void> writeErrorMessage(ServerHttpResponse response)  {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(new ApiResponseDto(401, "认证失败"));
        } catch (JsonProcessingException e) {
            Mono.error(e);
        }
        DataBuffer dataBuffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }
}
