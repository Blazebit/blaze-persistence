/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.spring.data.rest.impl;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.spring.data.rest.KeysetConfig;
import com.blazebit.reflection.ReflectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

    private static final String DEFAULT_PREVIOUS_PAGE_PARAMETER = "prevPage";
    private static final String DEFAULT_PREVIOUS_SIZE_PARAMETER = "prevSize";
    private static final String DEFAULT_LOWEST_PARAMETER = "lowest";
    private static final String DEFAULT_HIGHEST_PARAMETER = "highest";
    private static final String INVALID_DEFAULT_PAGE_SIZE = "Invalid default page size configured for method %s! Must not be less than one!";
    private static final String INVALID_KEYSET_DOMAIN_CLASS = "Invalid keyset domain class configured for method %s! Should be an entity type!";
    private static final KeysetPageable DEFAULT_PAGE_REQUEST = new KeysetPageRequest(null, null, 0, 20);
    private final ConcurrentMap<PropertyCacheKey, Class<? extends Serializable>> propertyTypeCache = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final SortHandlerMethodArgumentResolver sortResolver;
    private final ConversionService conversionService;
    private KeysetPageable fallbackPageable = DEFAULT_PAGE_REQUEST;
    private String previousPageParameterName = DEFAULT_PREVIOUS_PAGE_PARAMETER;
    private String previousSizeParameterName = DEFAULT_PREVIOUS_SIZE_PARAMETER;
    private String lowestParameterName = DEFAULT_LOWEST_PARAMETER;
    private String highestParameterName = DEFAULT_HIGHEST_PARAMETER;

    public KeysetPageableHandlerMethodArgumentResolver() {
        this(null);
    }

    public KeysetPageableHandlerMethodArgumentResolver(ConversionService conversionService) {
        this(null, conversionService);
    }

    public KeysetPageableHandlerMethodArgumentResolver(SortHandlerMethodArgumentResolver sortResolver, ConversionService conversionService) {
        super(sortResolver = sortResolver == null ? new SortHandlerMethodArgumentResolver() : sortResolver);
        this.sortResolver = sortResolver;
        this.conversionService = conversionService;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class PropertyCacheKey {
        private final Class<?> clazz;
        private final String property;

        public PropertyCacheKey(Class<?> clazz, String property) {
            this.clazz = clazz;
            this.property = property;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PropertyCacheKey)) {
                return false;
            }

            PropertyCacheKey that = (PropertyCacheKey) o;

            if (!clazz.equals(that.clazz)) {
                return false;
            }
            return property.equals(that.property);
        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + property.hashCode();
            return result;
        }
    }

    @Override
    public void setFallbackPageable(Pageable fallbackPageable) {
        setFallbackPageable((KeysetPageable) fallbackPageable);
    }

    public void setFallbackPageable(KeysetPageable fallbackPageable) {
        this.fallbackPageable = fallbackPageable;
    }

    @Override
    public boolean isFallbackPageable(Pageable pageable) {
        return fallbackPageable == null ? false : fallbackPageable.equals(pageable);
    }

    public String getPreviousPageParameterName() {
        return previousPageParameterName;
    }

    public void setPreviousPageParameterName(String previousPageParameterName) {
        this.previousPageParameterName = previousPageParameterName;
    }

    public String getPreviousSizeParameterName() {
        return previousSizeParameterName;
    }

    public void setPreviousSizeParameterName(String previousSizeParameterName) {
        this.previousSizeParameterName = previousSizeParameterName;
    }

    public String getLowestParameterName() {
        return lowestParameterName;
    }

    public void setLowestParameterName(String lowestParameterName) {
        this.lowestParameterName = lowestParameterName;
    }

    public String getHighestParameterName() {
        return highestParameterName;
    }

    public void setHighestParameterName(String highestParameterName) {
        this.highestParameterName = highestParameterName;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return KeysetPageable.class.equals(parameter.getParameterType());
    }

    @Override
    public KeysetPageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        assertPageableUniqueness(methodParameter);

        Pageable defaultOrFallback = getDefaultFromAnnotationOrFallback(methodParameter);

        String pageString = webRequest.getParameter(getParameterNameToUse(getPageParameterName(), methodParameter));
        String pageSizeString = webRequest.getParameter(getParameterNameToUse(getSizeParameterName(), methodParameter));

        boolean pageAndSizeGiven = StringUtils.hasText(pageString) && StringUtils.hasText(pageSizeString);

        if (!pageAndSizeGiven && defaultOrFallback == null) {
            return null;
        }

        int page = StringUtils.hasText(pageString) ? parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, true)
                : defaultOrFallback.getPageNumber();
        int pageSize = StringUtils.hasText(pageSizeString) ? parseAndApplyBoundaries(pageSizeString, getMaxPageSize(), false)
                : defaultOrFallback.getPageSize();

        // Limit lower bound
        pageSize = pageSize < 1 ? defaultOrFallback.getPageSize() : pageSize;
        // Limit upper bound
        pageSize = pageSize > getMaxPageSize() ? getMaxPageSize() : pageSize;

        Sort sort = sortResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        // Default if necessary and default configured
        sort = sort == null && defaultOrFallback != null ? defaultOrFallback.getSort() : sort;

        KeysetPage keysetPage = null;
        Iterator<Sort.Order> iterator;
        if (sort != null && (iterator = sort.iterator()).hasNext()) {
            String previousPageString = webRequest.getParameter(getParameterNameToUse(getPreviousPageParameterName(), methodParameter));
            if (StringUtils.hasText(previousPageString)) {
                int previousPage = parseAndApplyBoundaries(previousPageString, Integer.MAX_VALUE, true);
                String previousPageSizeString = webRequest.getParameter(getParameterNameToUse(getPreviousSizeParameterName(), methodParameter));
                int previousPageSize = StringUtils.hasText(previousPageSizeString) ? parseAndApplyBoundaries(previousPageSizeString, getMaxPageSize(), false)
                        : pageSize;

                String lowestString = webRequest.getParameter(getParameterNameToUse(getLowestParameterName(), methodParameter));
                String highestString = webRequest.getParameter(getParameterNameToUse(getHighestParameterName(), methodParameter));
                if (StringUtils.hasText(lowestString) && StringUtils.hasText(highestString)) {
                    List<Serializable> lowest = new ArrayList<>();
                    List<Serializable> highest = new ArrayList<>();
                    JsonNode lowestObject;
                    JsonNode highestObject;
                    try {
                        lowestObject = mapper.readTree(lowestString);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException("Invalid lowest object!", ex);
                    }
                    try {
                        highestObject = mapper.readTree(highestString);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException("Invalid highest object!", ex);
                    }

                    while (iterator.hasNext()) {
                        Sort.Order o = iterator.next();
                        JsonNode low = lowestObject;
                        JsonNode high = highestObject;
                        String[] propertyParts = o.getProperty().split("\\.");
                        Class<? extends Serializable> propertyType = getPropertyType(methodParameter, o.getProperty());

                        for (int i = 0; i < propertyParts.length; i++) {
                            low = low == null ? null : low.get(propertyParts[i]);
                            high = high == null ? null : high.get(propertyParts[i]);
                        }

                        lowest.add(low == null ? null : convert(low, propertyType));
                        highest.add(high == null ? null : convert(high, propertyType));
                    }
                    keysetPage = new KeysetPageImpl(
                            previousPage * previousPageSize,
                            previousPageSize,
                            lowest.toArray(new Serializable[lowest.size()]),
                            highest.toArray(new Serializable[highest.size()])
                    );
                }
            }
        }
        return new KeysetPageRequest(keysetPage, sort, page, pageSize);
    }

    private Serializable convert(JsonNode valueNode, Class<? extends Serializable> propertyType) {
        switch (valueNode.getNodeType()) {
            case NULL:
                return null;
            case BOOLEAN:
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    return valueNode.asBoolean();
                } else {
                    return conversionService.convert(valueNode.asBoolean(), propertyType);
                }
            case NUMBER:
                Number number = valueNode.numberValue();
                if (propertyType == Integer.class || propertyType == int.class) {
                    return number.intValue();
                } else if (propertyType == Long.class || propertyType == long.class) {
                    return number.longValue();
                } else if (propertyType == Double.class || propertyType == double.class) {
                    return number.doubleValue();
                } else if (propertyType == Float.class || propertyType == float.class) {
                    return number.floatValue();
                } else if (propertyType == Byte.class || propertyType == byte.class) {
                    return number.byteValue();
                } else if (propertyType == Short.class || propertyType == short.class) {
                    return number.shortValue();
                } else if (propertyType == BigInteger.class) {
                    return valueNode.bigIntegerValue();
                } else if (propertyType == BigDecimal.class) {
                    return valueNode.decimalValue();
                } else if (conversionService.canConvert(number.getClass(), propertyType)) {
                    return conversionService.convert(number, propertyType);
                } else {
                    return conversionService.convert(valueNode.asText(), propertyType);
                }
            case STRING:
                if (propertyType == String.class) {
                    return valueNode.asText();
                } else {
                    return conversionService.convert(valueNode.asText(), propertyType);
                }
            default:
                throw new IllegalArgumentException("Can't convert value of type '" + valueNode.getNodeType() + "' to '" + propertyType.getName() + "'");
        }
    }

    private Class<? extends Serializable> getPropertyType(MethodParameter methodParameter, String property) {
        KeysetConfig keysetConfig = methodParameter.getParameterAnnotation(KeysetConfig.class);
        Class<?> clazz = keysetConfig.keysetClass();
        if (clazz == void.class) {
            clazz = keysetConfig.value();
        }

        if (clazz == void.class) {
            Method annotatedMethod = methodParameter.getMethod();
            throw new IllegalStateException(String.format(INVALID_KEYSET_DOMAIN_CLASS, annotatedMethod));
        }

        PropertyCacheKey cacheKey = new PropertyCacheKey(clazz, property);
        Class<? extends Serializable> propertyType = propertyTypeCache.get(cacheKey);
        if (propertyType == null) {
            propertyType = getPropertyType(clazz, property);
            propertyTypeCache.putIfAbsent(cacheKey, propertyType);
        }
        return propertyType;
    }

    private Class<? extends Serializable> getPropertyType(Class<?> baseClazz, String property) {
        Class<?> clazz = baseClazz;
        String[] propertyParts = property.split("\\.");
        for (int i = 0; i < propertyParts.length; i++) {
            Method getter = ReflectionUtils.getGetter(clazz, propertyParts[i]);
            if (getter == null) {
                Field field = ReflectionUtils.getField(clazz, propertyParts[i]);
                if (field == null) {
                    throw new IllegalArgumentException("Couldn't find property '" + propertyParts[i] + "' on type '" + clazz + "' while resolving path '" + property + "' on type '" + baseClazz + "'");
                }
                Class<?>[] typeArguments = ReflectionUtils.getResolvedFieldTypeArguments(clazz, field);
                if (typeArguments.length == 0) {
                    clazz = ReflectionUtils.getResolvedFieldType(clazz, field);
                } else {
                    clazz = typeArguments[typeArguments.length - 1];
                }
            } else {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(clazz, getter);
                if (typeArguments.length == 0) {
                    clazz = ReflectionUtils.getResolvedMethodReturnType(clazz, getter);
                } else {
                    clazz = typeArguments[typeArguments.length - 1];
                }
            }
        }

        return (Class<? extends Serializable>) clazz;
    }

    private KeysetPageable getDefaultFromAnnotationOrFallback(MethodParameter methodParameter) {
        if (methodParameter.hasParameterAnnotation(PageableDefault.class)) {
            return getDefaultPageRequestFrom(methodParameter);
        }

        return fallbackPageable;
    }

    private static KeysetPageable getDefaultPageRequestFrom(MethodParameter parameter) {
        PageableDefault defaults = parameter.getParameterAnnotation(PageableDefault.class);

        Integer defaultPageNumber = defaults.page();
        Integer defaultPageSize = getSpecificPropertyOrDefaultFromValue(defaults, "size");

        if (defaultPageSize < 1) {
            Method annotatedMethod = parameter.getMethod();
            throw new IllegalStateException(String.format(INVALID_DEFAULT_PAGE_SIZE, annotatedMethod));
        }

        if (defaults.sort().length == 0) {
            return new KeysetPageRequest(null, null, defaultPageNumber, defaultPageSize);
        }
        return new KeysetPageRequest(null, new Sort(defaults.direction(), defaults.sort()), defaultPageNumber, defaultPageSize);
    }

    private int parseAndApplyBoundaries(String parameter, int upper, boolean shiftIndex) {
        try {
            int parsed = Integer.parseInt(parameter) - (isOneIndexedParameters() && shiftIndex ? 1 : 0);
            return parsed < 0 ? 0 : parsed > upper ? upper : parsed;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* Copied from org.springframework.data.web.SpringDataAnnotationUtils because the class isn't public... */

    private static <T> T getSpecificPropertyOrDefaultFromValue(Annotation annotation, String property) {
        Object propertyDefaultValue = AnnotationUtils.getDefaultValue(annotation, property);
        Object propertyValue = AnnotationUtils.getValue(annotation, property);

        return (T) (ObjectUtils.nullSafeEquals(propertyDefaultValue, propertyValue) ? AnnotationUtils.getValue(annotation)
                : propertyValue);
    }

    private static void assertPageableUniqueness(MethodParameter parameter) {
        Method method = parameter.getMethod();

        if (containsMoreThanOnePageableParameter(method)) {
            Annotation[][] annotations = method.getParameterAnnotations();
            assertQualifiersFor(method.getParameterTypes(), annotations);
        }
    }

    private static boolean containsMoreThanOnePageableParameter(Method method) {
        boolean pageableFound = false;
        for (Class<?> type : method.getParameterTypes()) {

            if (pageableFound && type.equals(Pageable.class)) {
                return true;
            }

            if (type.equals(Pageable.class)) {
                pageableFound = true;
            }
        }

        return false;
    }

    private static void assertQualifiersFor(Class<?>[] parameterTypes, Annotation[][] annotations) {
        Set<String> values = new HashSet<String>();
        for (int i = 0; i < annotations.length; i++) {
            if (Pageable.class.equals(parameterTypes[i])) {
                Qualifier qualifier = findAnnotation(annotations[i]);

                if (null == qualifier) {
                    throw new IllegalStateException(
                            "Ambiguous Pageable arguments in handler method. If you use multiple parameters of type Pageable you need to qualify them with @Qualifier");
                }

                if (values.contains(qualifier.value())) {
                    throw new IllegalStateException("Values of the user Qualifiers must be unique!");
                }

                values.add(qualifier.value());
            }
        }
    }

    private static Qualifier findAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Qualifier) {
                return (Qualifier) annotation;
            }
        }

        return null;
    }
}
