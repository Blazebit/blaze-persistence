/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            return Mono.subscriberContext().map(ctx -> {
                try {
                    if (ctx.hasKey(EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM)) {
                        idAttributeAccessor.entityViewIdLookupMap.put(jsonParser, ctx.get(EntityViewIdAwareWebFilter.ENTITY_VIEW_ID_CONTEXT_PARAM));
                    }
                    Object value = reader.readValue(jsonParser);
                    logValue(value, hints);
                    return value;
                } catch (InvalidDefinitionException ex) {
                    return Mono.error(new CodecException("Type definition error: " + ex.getType(), ex));
                } catch (JsonProcessingException ex) {
                    return Mono.error(new DecodingException("JSON decoding error: " + ex.getOriginalMessage(), ex));
                } catch (IOException ex) {
                    return Mono.error(new DecodingException("I/O error while parsing input stream", ex));
                } finally {
                    idAttributeAccessor.entityViewIdLookupMap.remove(jsonParser);
                }
            });
        });
    }

    private void logValue(Object value, Map<String, Object> hints) {
        try {
            Field loggerField = Jackson2CodecSupport.class.getDeclaredField("logger");
            Log logger = (Log) loggerField.get(this);
            Class<?> hintsClass = Class.forName("org.springframework.core.codec.Hints");
            Method isLoggingSuppressed = hintsClass.getMethod("isLoggingSuppressed", Map.class);
            if (!(boolean) isLoggingSuppressed.invoke(null, hints)) {
                Method getLogPrefixMethod = hintsClass.getMethod("getLogPrefix", Map.class);
                Class<?> logFormatUtilsClass = Class.forName("org.springframework.core.log.LogFormatUtils");
                Method traceDebugMethod = logFormatUtilsClass.getMethod("traceDebug", Log.class, Function.class);
                Method formatValueMethod = logFormatUtilsClass.getMethod("formatValue", Object.class, boolean.class);
                traceDebugMethod.invoke(null, logger, (Function<Boolean, String>) traceOn -> {
                    try {
                        String formatted = (String) formatValueMethod.invoke(null, value, !traceOn);
                        String logPrefix = (String) getLogPrefixMethod.invoke(null, hints);
                        return logPrefix + "Decoded [" + formatted + "]";
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
