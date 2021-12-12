/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.jaxrs.jsonb;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.integration.jsonb.EntityViewIdValueAccessor;
import com.blazebit.persistence.integration.jsonb.EntityViewJsonbDeserializer;
import com.blazebit.persistence.view.ConvertOperationBuilder;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.EntityViewBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushOperationBuilder;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.persistence.EntityManager;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Priority(Priorities.USER - 1)
@Provider
// "*/*" needs to be included since Jersey does not support the "application/*+json" notation
@Consumes({"application/json", "application/*+json", "text/json", "*/*"})
public class EntityViewMessageBodyReader implements MessageBodyReader<Object> {

    private static final ParamConverterProvider FROM_STRING_PARAM_CONVERTER_PROVIDER = new FromStringParamConverterProvider();

    @Inject
    private Instance<EntityViewManager> entityViewManager;
    @Inject
    private Instance<JsonbConfig> jsonbConfig;
    @Inject
    @Any
    private Instance<ParamConverterProvider> paramConverterProviders;
    @Context
    private UriInfo uriInfo;
    @Context
    private Providers providers;

    private Jsonb jsonb;
    private final ThreadLocal<String> idValueHolder = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        if (entityViewManager.isUnsatisfied()) {
            this.jsonb = null;
        } else {
            JsonbConfig config = null;
            ContextResolver<JsonbConfig> resolver;
            if (providers != null && (resolver = providers.getContextResolver(JsonbConfig.class, MediaType.APPLICATION_JSON_TYPE)) != null) {
                config = resolver.getContext(EntityViewMessageBodyReader.class);
            }
            if (config == null) {
                if (jsonbConfig.isUnsatisfied()) {
                    config = new JsonbConfig();
                } else {
                    config = jsonbConfig.get();
                }
            }
            EntityViewJsonbDeserializer.integrate(config, entityViewManager.get(), new EntityViewIdValueAccessor() {
                @Override
                public <T> T getValue(JsonParser jsonParser, DeserializationContext deserializationContext, Class<T> idType) {
                    String value = idValueHolder.get();
                    if (value == null || String.class.equals(idType)) {
                        return (T) value;
                    } else {
                        ParamConverter<T> paramConverter = null;
                        for (ParamConverterProvider paramConverterProvider : paramConverterProviders) {
                            if ((paramConverter = paramConverterProvider.getConverter(idType, idType, null)) != null) {
                                break;
                            }
                        }
                        if (paramConverter == null) {
                            paramConverter = FROM_STRING_PARAM_CONVERTER_PROVIDER.getConverter(idType, idType, null);
                        }
                        if (paramConverter == null) {
                            throw new RuntimeException("No " + ParamConverter.class.getName() + " could be found to convert to type " + idType.getName());
                        } else {
                            return paramConverter.fromString(value);
                        }
                    }
                }
            });
            this.jsonb = JsonbBuilder.create(config);
//            this.jsonb = new EntityViewAwareObjectMapper(new LazyEntityViewManager(), config, );
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !entityViewManager.isUnsatisfied()
                && entityViewManager.get().getMetamodel().view(type) != null
                && hasMatchingMediaType(mediaType)
                && !InputStream.class.isAssignableFrom(type)
                && !Reader.class.isAssignableFrom(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        EntityViewId entityViewAnnotation = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(EntityViewId.class)) {
                entityViewAnnotation = (EntityViewId) annotation;
                break;
            }
        }
        if (entityViewAnnotation != null) {
            String pathVariableName = entityViewAnnotation.value().isEmpty() ? entityViewAnnotation.name() : entityViewAnnotation.value();
            if (pathVariableName.isEmpty()) {
                throw new IllegalArgumentException(
                        "Entity view id path param name for argument type [" + type.getName() +
                                "] not available.");
            }
            String pathVariableStringValue = uriInfo.getPathParameters().getFirst(pathVariableName);
            idValueHolder.set(pathVariableStringValue);
        }

        try {
            if (jsonb != null) {
                return jsonb.fromJson(entityStream, genericType);
            }
        } finally {
            idValueHolder.remove();
        }

        return null;
    }

    /**
     * Copy of {@link com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider#hasMatchingMediaType(javax.ws.rs.core.MediaType)}
     *
     * @param mediaType the media type to be matched
     * @return true, if this reader accepts the given mediaType or false otherwise
     */
    private boolean hasMatchingMediaType(MediaType mediaType) {
        /* As suggested by Stephen D, there are 2 ways to check: either
         * being as inclusive as possible (if subtype is "json"), or
         * exclusive (major type "application", minor type "json").
         * Let's start with inclusive one, hard to know which major
         * types we should cover aside from "application".
         */
        if (mediaType != null) {
            // Ok: there are also "xxx+json" subtypes, which count as well
            String subtype = mediaType.getSubtype();

            // [Issue#14]: also allow 'application/javascript'
            return "json".equalsIgnoreCase(subtype)
                    || subtype.endsWith("+json")
                    || "javascript".equals(subtype)
                    // apparently Microsoft once again has interesting alternative types?
                    || "x-javascript".equals(subtype)
                    || "x-json".equals(subtype) // [Issue#40]
                    ;
        }
        /* Not sure if this can happen; but it seems reasonable
         * that we can at least produce JSON without media type?
         */
        return true;
    }

    /**
     * ParamConverterProvider for default parameter conversion using fromString method if present
     */
    private static class FromStringParamConverterProvider implements ParamConverterProvider {

        private static final Map<Class<?>, ParamConverter<?>> CACHED_PARAM_CONVERTERS = new ConcurrentHashMap<>();

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
            ParamConverter<T> converter = (ParamConverter<T>) CACHED_PARAM_CONVERTERS.get(clazz);
            if (converter == null) {
                // Follow the logic described in https://docs.jboss.org/resteasy/docs/3.5.0.Final/userguide/html/StringConverter.html#d4e1480
                Class<?> effectiveClass;
                if (short.class.equals(clazz)) {
                    effectiveClass = Short.class;
                } else if (int.class.equals(clazz)) {
                    effectiveClass = Integer.class;
                } else if (long.class.equals(clazz)) {
                    effectiveClass = Long.class;
                } else if (float.class.equals(clazz)) {
                    effectiveClass = Float.class;
                } else if (double.class.equals(clazz)) {
                    effectiveClass = Double.class;
                } else if (boolean.class.equals(clazz)) {
                    effectiveClass = Boolean.class;
                } else if (byte.class.equals(clazz)) {
                    effectiveClass = Byte.class;
                } else if (char.class.equals(clazz)) {
                    effectiveClass = Character.class;
                } else {
                    effectiveClass = clazz;
                }
                Method fromStringMethod = null;
                try {
                    fromStringMethod = effectiveClass.getMethod("fromString", String.class);
                } catch (NoSuchMethodException e) {
                    // ignore
                }
                Method valueOfMethod = null;
                try {
                    valueOfMethod = effectiveClass.getMethod("valueOf", String.class);
                } catch (NoSuchMethodException e) {
                    // ignore
                }

                Method effectiveMethod;
                if (fromStringMethod != null && valueOfMethod != null) {
                    effectiveMethod = effectiveClass.isEnum() ? fromStringMethod : valueOfMethod;
                } else if (fromStringMethod != null) {
                    effectiveMethod = fromStringMethod;
                } else {
                    effectiveMethod = valueOfMethod;
                }

                if (effectiveClass == Character.class) {
                    converter = (ParamConverter<T>) new CharacterParamConverter();
                } else if (effectiveMethod == null) {
                    Constructor<T> constructor = null;
                    try {
                        constructor = ((Class<T>) effectiveClass).getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        // ignore
                    }
                    converter = constructor == null ? null : new ConstructorBasedParamConverter<>(constructor);
                } else {
                    converter = new MethodBasedParamConverter<>(effectiveMethod);
                }
                if (converter != null) {
                    CACHED_PARAM_CONVERTERS.put(clazz, converter);
                }
            }
            return converter;
        }

        /**
         * ParamConverter for default parameter conversion using the given {@link java.lang.String} consuming method
         *
         * @param <T> The parameter type
         */
        private static class MethodBasedParamConverter<T> implements javax.ws.rs.ext.ParamConverter<T> {
            private final Method stringConsumingMethod;

            public MethodBasedParamConverter(Method stringConsumingMethod) {
                this.stringConsumingMethod = stringConsumingMethod;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T fromString(String s) {
                try {
                    return (T) stringConsumingMethod.invoke(null, s);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new BadRequestException("Malformed input: " + s);
                }
            }

            @Override
            public String toString(T o) {
                return o.toString();
            }
        }

        /**
         * ParamConverter for default parameter conversion using the given {@link java.lang.String} consuming constructor
         *
         * @param <T> The parameter type
         */
        private static class ConstructorBasedParamConverter<T> implements javax.ws.rs.ext.ParamConverter<T> {
            private final Constructor<T> stringConstructor;

            public ConstructorBasedParamConverter(Constructor<T> stringConstructor) {
                this.stringConstructor = stringConstructor;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T fromString(String s) {
                try {
                    return (T) stringConstructor.newInstance(s);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new BadRequestException("Malformed input: " + s);
                }
            }

            @Override
            public String toString(T o) {
                return o.toString();
            }
        }

        /**
         * ParamConverter for default parameter conversion for {@link java.lang.Character}
         */
        private static class CharacterParamConverter implements javax.ws.rs.ext.ParamConverter<Character> {

            @Override
            @SuppressWarnings("unchecked")
            public Character fromString(String s) {
                Character c;
                if (s == null || s.length() > 1) {
                    c = null;
                } else {
                    c = s.charAt(0);
                }
                return c;
            }

            @Override
            public String toString(Character o) {
                return o.toString();
            }
        }
    }

    /**
     * @author Moritz Becker
     * @since 1.6.0
     */
    private final class LazyEntityViewManager implements EntityViewManager {
        public ViewMetamodel getMetamodel() {
            return entityViewManager.get().getMetamodel();
        }

        public Map<String, Object> getOptionalParameters() {
            return entityViewManager.get().getOptionalParameters();
        }

        public <T> T find(EntityManager entityManager, Class<T> entityViewClass, Object entityId) {
            return entityViewManager.get().find(entityManager, entityViewClass, entityId);
        }

        public <T> T find(EntityManager entityManager, EntityViewSetting<T, CriteriaBuilder<T>> entityViewSetting, Object entityId) {
            return entityViewManager.get().find(entityManager, entityViewSetting, entityId);
        }

        public <T> T getReference(Class<T> entityViewClass, Object id) {
            return entityViewManager.get().getReference(entityViewClass, id);
        }

        public <T> T getEntityReference(EntityManager entityManager, Object entityView) {
            return entityViewManager.get().getEntityReference(entityManager, entityView);
        }

        public <T> SingularChangeModel<T> getChangeModel(T entityView) {
            return entityViewManager.get().getChangeModel(entityView);
        }

        public <T> T create(Class<T> entityViewClass) {
            return entityViewManager.get().create(entityViewClass);
        }

        public <T> T create(Class<T> entityViewClass, Map<String, Object> optionalParameters) {
            return entityViewManager.get().create(entityViewClass, optionalParameters);
        }

        public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz) {
            return entityViewManager.get().createBuilder(clazz);
        }

        public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, String constructorName) {
            return entityViewManager.get().createBuilder(clazz, constructorName);
        }

        public <X> EntityViewBuilder<X> createBuilder(X view) {
            return entityViewManager.get().createBuilder(view);
        }

        public <X> EntityViewBuilder<X> createBuilder(X view, String constructorName) {
            return entityViewManager.get().createBuilder(view, constructorName);
        }

        public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters) {
            return entityViewManager.get().createBuilder(clazz, optionalParameters);
        }

        public <X> EntityViewBuilder<X> createBuilder(Class<X> clazz, Map<String, Object> optionalParameters, String constructorName) {
            return entityViewManager.get().createBuilder(clazz, optionalParameters, constructorName);
        }

        public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters) {
            return entityViewManager.get().createBuilder(view, optionalParameters);
        }

        public <X> EntityViewBuilder<X> createBuilder(X view, Map<String, Object> optionalParameters, String constructorName) {
            return entityViewManager.get().createBuilder(view, optionalParameters, constructorName);
        }

        public <T> T convert(Object source, Class<T> entityViewClass, ConvertOption... convertOptions) {
            return entityViewManager.get().convert(source, entityViewClass, convertOptions);
        }

        public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions) {
            return entityViewManager.get().convert(source, entityViewClass, constructorName, convertOptions);
        }

        public <T> T convert(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
            return entityViewManager.get().convert(source, entityViewClass, optionalParameters, convertOptions);
        }

        public <T> T convert(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
            return entityViewManager.get().convert(source, entityViewClass, constructorName, optionalParameters, convertOptions);
        }

        public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, ConvertOption... convertOptions) {
            return entityViewManager.get().convertWith(source, entityViewClass, convertOptions);
        }

        public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, ConvertOption... convertOptions) {
            return entityViewManager.get().convertWith(source, entityViewClass, constructorName, convertOptions);
        }

        public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
            return entityViewManager.get().convertWith(source, entityViewClass, optionalParameters, convertOptions);
        }

        public <T> ConvertOperationBuilder<T> convertWith(Object source, Class<T> entityViewClass, String constructorName, Map<String, Object> optionalParameters, ConvertOption... convertOptions) {
            return entityViewManager.get().convertWith(source, entityViewClass, constructorName, optionalParameters, convertOptions);
        }

        public void save(EntityManager entityManager, Object view) {
            entityViewManager.get().save(entityManager, view);
        }

        public void saveFull(EntityManager entityManager, Object view) {
            entityViewManager.get().saveFull(entityManager, view);
        }

        public void saveTo(EntityManager entityManager, Object view, Object entity) {
            entityViewManager.get().saveTo(entityManager, view, entity);
        }

        public void saveFullTo(EntityManager entityManager, Object view, Object entity) {
            entityViewManager.get().saveFullTo(entityManager, view, entity);
        }

        @Deprecated
        public void update(EntityManager entityManager, Object view) {
            entityViewManager.get().update(entityManager, view);
        }

        @Deprecated
        public void updateFull(EntityManager entityManager, Object view) {
            entityViewManager.get().updateFull(entityManager, view);
        }

        public FlushOperationBuilder saveWith(EntityManager entityManager, Object view) {
            return entityViewManager.get().saveWith(entityManager, view);
        }

        public FlushOperationBuilder saveFullWith(EntityManager entityManager, Object view) {
            return entityViewManager.get().saveFullWith(entityManager, view);
        }

        public FlushOperationBuilder saveWithTo(EntityManager entityManager, Object view, Object entity) {
            return entityViewManager.get().saveWithTo(entityManager, view, entity);
        }

        public FlushOperationBuilder saveFullWithTo(EntityManager entityManager, Object view, Object entity) {
            return entityViewManager.get().saveFullWithTo(entityManager, view, entity);
        }

        public void remove(EntityManager entityManager, Object view) {
            entityViewManager.get().remove(entityManager, view);
        }

        public FlushOperationBuilder removeWith(EntityManager entityManager, Object view) {
            return entityViewManager.get().removeWith(entityManager, view);
        }

        public void remove(EntityManager entityManager, Class<?> entityViewClass, Object viewId) {
            entityViewManager.get().remove(entityManager, entityViewClass, viewId);
        }

        public FlushOperationBuilder removeWith(EntityManager entityManager, Class<?> entityViewClass, Object viewId) {
            return entityViewManager.get().removeWith(entityManager, entityViewClass, viewId);
        }

        public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder) {
            return entityViewManager.get().applySetting(setting, criteriaBuilder);
        }

        public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot) {
            return entityViewManager.get().applySetting(setting, criteriaBuilder, entityViewRoot);
        }

        public <T> T getService(Class<T> serviceClass) {
            return entityViewManager.get().getService(serviceClass);
        }
    }
}
