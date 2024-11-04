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
import org.apache.commons.logging.Log;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewAwareJackson2JsonDecoder extends Jackson2JsonDecoder {
    private static final Field LOGGER_FIELD;
    private static final Method IS_LOGGING_SUPPRESSED;
    private static final Method GET_LOG_PREFIX_METHOD;
    private static final Method TRACE_DEBUG_METHOD;
    private static final Method FORMAT_VALUE_METHOD;

    private static final Method DEFER_CONTEXTUAL;
    private static final Method SUBSCRIBER_CONTEXT;

    private static final Method HAS_KEY;
    private static final Method GET;

    static {
        try {
            Field loggerField = null;
            Method isLoggingSuppressed = null;
            Method traceDebugMethod = null;
            Method formatValueMethod = null;
            Method getLogPrefixMethod = null;
            try {
                loggerField = Jackson2CodecSupport.class.getDeclaredField("logger");
                Class<?> hintsClass = Class.forName("org.springframework.core.codec.Hints");
                Class<?> logFormatUtilsClass = Class.forName("org.springframework.core.log.LogFormatUtils");
                isLoggingSuppressed = hintsClass.getMethod("isLoggingSuppressed", Map.class);
                traceDebugMethod = logFormatUtilsClass.getMethod("traceDebug", Log.class, Function.class);
                formatValueMethod = logFormatUtilsClass.getMethod("formatValue", Object.class, boolean.class);
                getLogPrefixMethod = hintsClass.getMethod("getLogPrefix", Map.class);
            } catch (NoSuchFieldException ex) {
                // Ignore
            }
            LOGGER_FIELD = loggerField;
            IS_LOGGING_SUPPRESSED = isLoggingSuppressed;
            GET_LOG_PREFIX_METHOD = getLogPrefixMethod;
            TRACE_DEBUG_METHOD = traceDebugMethod;
            FORMAT_VALUE_METHOD = formatValueMethod;
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

    private void registerParser(JsonParser jsonParser, Object ctx) {
        try {
            if ((boolean) HAS_KEY.invoke(ctx, EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM)) {
                idAttributeAccessor.entityViewIdLookupMap.put(jsonParser, GET.invoke(ctx, EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while registering json parser. Please report this problem!", e);
        }
    }

    private void logValue(Object value, Map<String, Object> hints) {
        try {
            if (LOGGER_FIELD != null && !(boolean) IS_LOGGING_SUPPRESSED.invoke(null, hints)) {
                Log logger = (Log) LOGGER_FIELD.get(this);
                TRACE_DEBUG_METHOD.invoke(null, logger, (Function<Boolean, String>) traceOn -> {
                    try {
                        String formatted = (String) FORMAT_VALUE_METHOD.invoke(null, value, !traceOn);
                        String logPrefix = (String) GET_LOG_PREFIX_METHOD.invoke(null, hints);
                        return logPrefix + "Decoded [" + formatted + "]";
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
