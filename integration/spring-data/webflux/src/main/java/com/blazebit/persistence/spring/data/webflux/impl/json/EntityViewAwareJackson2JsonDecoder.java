/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webflux.impl.json;

import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewAwareJackson2JsonDecoder extends Jackson2JsonDecoder {
    private static final Method DEFER_CONTEXTUAL;
    private static final Method SUBSCRIBER_CONTEXT;

    private static final Method HAS_KEY;
    private static final Method GET;

    static {
        try {
            Method deferContextual = null;
            Method subscriberContext = null;
            try {
                deferContextual = Mono.class.getMethod("deferContextual", Function.class);
            } catch (NoSuchMethodException e) {
                subscriberContext = Mono.class.getMethod("subscriberContext");
            }
            DEFER_CONTEXTUAL = deferContextual;
            SUBSCRIBER_CONTEXT = subscriberContext;
            Method hasKey = null;
            Method get = null;
            try {
                Class<?> contextView = Class.forName("reactor.util.context.ContextView");
                hasKey = contextView.getMethod("hasKey", Object.class);
                get = contextView.getMethod("get", Object.class);
            } catch (ClassNotFoundException ex) {
                hasKey = Context.class.getMethod("hasKey", Object.class);
                get = Context.class.getMethod("get", Object.class);
            }
            HAS_KEY = hasKey;
            GET = get;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't setup the Blaze-Persistence Webflux integration for Jackson. Please report this problem!", e);
        }
    }

    private final JsonFactory jsonFactory;
    private final EntityViewIdValueAccessorImpl idAttributeAccessor;

    public EntityViewAwareJackson2JsonDecoder(final EntityViewManager entityViewManager, EntityViewIdValueAccessorImpl idAttributeAccessor) {
        // This will register the deserializers into the object mapper
        new EntityViewAwareObjectMapper(entityViewManager, getObjectMapper(), idAttributeAccessor);
        this.jsonFactory = getObjectMapper().getFactory().copy()
                .disable(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING);
        this.idAttributeAccessor = idAttributeAccessor;
    }

    @Override
    public Mono<Object> decodeToMono(Publisher<DataBuffer> input, ResolvableType elementType,
                                     @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        Flux<TokenBuffer> tokens = Jackson2Tokenizer.tokenize(
                Flux.from(input), this.jsonFactory, getObjectMapper(), false);
        return decodeInternal(tokens, elementType, mimeType, hints).singleOrEmpty();
    }

    private Flux<Object> decodeInternal(Flux<TokenBuffer> tokens, ResolvableType elementType,
                                        @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        Assert.notNull(tokens, "'tokens' must not be null");
        Assert.notNull(elementType, "'elementType' must not be null");

        MethodParameter param = getParameter(elementType);
        Class<?> contextClass = (param != null ? param.getContainingClass() : null);
        JavaType javaType = getJavaType(elementType.getType(), contextClass);
        Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);

        ObjectReader reader = (jsonView != null ?
                getObjectMapper().readerWithView(jsonView).forType(javaType) :
                getObjectMapper().readerFor(javaType));

        return tokens.flatMap(tokenBuffer -> {
            JsonParser jsonParser = tokenBuffer.asParser(getObjectMapper());
            Function<Object, Mono<?>> function = ctx -> {
                try {
                    registerParser(jsonParser, ctx);
                    Object value = reader.readValue(jsonParser);
                    logValue(value, hints);
                    return Mono.just(value);
                } catch (InvalidDefinitionException ex) {
                    return Mono.error(new CodecException("Type definition error: " + ex.getType(), ex));
                } catch (JsonProcessingException ex) {
                    return Mono.error(new DecodingException("JSON decoding error: " + ex.getOriginalMessage(), ex));
                } catch (IOException ex) {
                    return Mono.error(new DecodingException("I/O error while parsing input stream", ex));
                } finally {
                    idAttributeAccessor.entityViewIdLookupMap.remove(jsonParser);
                }
            };
            try {
                if (DEFER_CONTEXTUAL == null) {
                    return ((Mono<Object>) SUBSCRIBER_CONTEXT.invoke(null)).flatMap(function);
                } else {
                    return (Mono<Object>) DEFER_CONTEXTUAL.invoke(null, function);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while creating contextual mono. Please report this problem!", e);
            }
        });
    }

    private void logValue(@Nullable Object value, @Nullable Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
            });
        }
    }

    private void registerParser(JsonParser jsonParser, Object ctx) {
        try {
            if ((boolean) HAS_KEY.invoke(ctx, EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM)) {
                idAttributeAccessor.entityViewIdLookupMap.put(jsonParser, GET.invoke(ctx, EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while registering json parser. Please report this problem!", e);
        }
    }

}
