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

package com.blazebit.persistence.deltaspike.data.rest.impl;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.deltaspike.data.KeysetPageRequest;
import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.deltaspike.data.rest.KeysetPageableConfiguration;
import com.blazebit.persistence.deltaspike.data.rest.PageableConfiguration;
import com.blazebit.reflection.ReflectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Provider
public class KeysetPageableParamConverter implements ParamConverter<Pageable> {

    private static final ConcurrentMap<PropertyCacheKey, Class<? extends Serializable>> PROPERTY_TYPE_CACHE = new ConcurrentHashMap<>();

    private final Class<?> keysetClass;
    private final ObjectMapper mapper;
    private final UriInfo requestUriInfo;
    private final PageableConfiguration pageableConfiguration;
    private final KeysetPageableConfiguration keysetPageableConfiguration;

    public KeysetPageableParamConverter(Class<?> keysetClass, ObjectMapper mapper, UriInfo requestUriInfo, PageableConfiguration pageableConfiguration, KeysetPageableConfiguration keysetPageableConfiguration) {
        this.keysetClass = keysetClass;
        this.mapper = mapper;
        this.requestUriInfo = requestUriInfo;
        this.pageableConfiguration = pageableConfiguration;
        this.keysetPageableConfiguration = keysetPageableConfiguration;
    }

    @Override
    public Pageable fromString(String value) {
        MultivaluedMap<String, String> queryParameters = requestUriInfo.getQueryParameters();

        Pageable defaultOrFallback = pageableConfiguration.getFallbackPageable();

        String offsetString = queryParameters.getFirst(pageableConfiguration.getPrefix() + pageableConfiguration.getOffsetParameterName());
        String pageString = queryParameters.getFirst(pageableConfiguration.getPrefix() + pageableConfiguration.getPageParameterName());
        String pageSizeString = queryParameters.getFirst(pageableConfiguration.getPrefix() + pageableConfiguration.getSizeParameterName());

        boolean pageAndSizeGiven = (!StringUtils.isEmpty(pageString) || !StringUtils.isEmpty(offsetString))  && !StringUtils.isEmpty(pageSizeString);

        if (!pageAndSizeGiven && defaultOrFallback == null) {
            return null;
        }

        int maxPageSize = pageableConfiguration.getMaxPageSize();
        int pageSize = !StringUtils.isEmpty(pageSizeString) ? parseAndApplyBoundaries(pageSizeString, maxPageSize, false)
                : defaultOrFallback.getPageSize();

        // Limit lower bound
        pageSize = pageSize < 1 ? defaultOrFallback.getPageSize() : pageSize;
        // Limit upper bound
        pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;

        int offset;
        if (!StringUtils.isEmpty(offsetString)) {
            offset = parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, false);
        } else if (!StringUtils.isEmpty(pageString)) {
            offset = pageSize * parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, true);
        } else {
            offset = pageSize * defaultOrFallback.getPageNumber();
        }

        Sort sort = resolveSort(queryParameters.get(pageableConfiguration.getSortParameterName()));

        // Default if necessary and default configured
        sort = sort == null && defaultOrFallback != null ? defaultOrFallback.getSort() : sort;

        KeysetPage keysetPage = null;
        Iterator<Sort.Order> iterator;
        if (keysetClass != null) {
            if (sort != null && (iterator = sort.iterator()).hasNext()) {
                String previousOffsetString = queryParameters.getFirst(keysetPageableConfiguration.getPrefix() + keysetPageableConfiguration.getPreviousOffsetParameterName());
                String previousPageString = queryParameters.getFirst(keysetPageableConfiguration.getPrefix() + keysetPageableConfiguration.getPreviousPageParameterName());
                if (!StringUtils.isEmpty(previousOffsetString) || !StringUtils.isEmpty(previousPageString)) {
                    String previousPageSizeString = queryParameters.getFirst(keysetPageableConfiguration.getPrefix() + keysetPageableConfiguration.getPreviousSizeParameterName());
                    int previousPageSize = StringUtils.isEmpty(previousPageSizeString) ? pageSize : parseAndApplyBoundaries(previousPageSizeString, maxPageSize, false);

                    int previousOffset;
                    if (!StringUtils.isEmpty(previousOffsetString)) {
                        previousOffset = parseAndApplyBoundaries(previousOffsetString, Integer.MAX_VALUE, false);
                    } else {
                        int previousPage = parseAndApplyBoundaries(previousPageString, Integer.MAX_VALUE, true);
                        previousOffset = previousPage * previousPageSize;
                    }

                    String lowestString = queryParameters.getFirst(keysetPageableConfiguration.getPrefix() + keysetPageableConfiguration.getLowestParameterName());
                    String highestString = queryParameters.getFirst(keysetPageableConfiguration.getPrefix() + keysetPageableConfiguration.getHighestParameterName());
                    if (!StringUtils.isEmpty(lowestString) && !StringUtils.isEmpty(highestString)) {
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
                            String[] propertyParts = o.getPath().split("\\.");
                            Class<? extends Serializable> propertyType = getPropertyType(keysetClass, o.getPath());

                            for (int i = 0; i < propertyParts.length; i++) {
                                low = low == null ? null : low.get(propertyParts[i]);
                                high = high == null ? null : high.get(propertyParts[i]);
                            }

                            lowest.add(low == null ? null : convert(low, propertyType));
                            highest.add(high == null ? null : convert(high, propertyType));
                        }
                        keysetPage = new KeysetPageImpl(
                                previousOffset,
                                previousPageSize,
                                lowest.toArray(new Serializable[lowest.size()]),
                                highest.toArray(new Serializable[highest.size()])
                        );
                    }
                }
            }
            
            return new KeysetPageRequest(keysetPage, sort, offset, pageSize);
        }

        return new PageRequest(sort, offset, pageSize);
    }

    private Serializable convert(JsonNode valueNode, Class<? extends Serializable> propertyType) {
        switch (valueNode.getNodeType()) {
            case NULL:
                return null;
            case BOOLEAN:
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    return valueNode.asBoolean();
                } else {
                    throw new IllegalArgumentException("Can't convert boolean to type: " + propertyType.getName());
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
                } else {
                    throw new IllegalArgumentException("Can't convert number to type: " + propertyType.getName());
                }
            case STRING:
                if (propertyType == String.class) {
                    return valueNode.asText();
                } else {
                    throw new IllegalArgumentException("Can't convert string to type: " + propertyType.getName());
                }
            default:
                throw new IllegalArgumentException("Can't convert value of type '" + valueNode.getNodeType() + "' to '" + propertyType.getName() + "'");
        }
    }

    private Sort resolveSort(List<String> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return null;
        }

        List<Sort.Order> orders = new ArrayList<>(sorts.size());
        for (int i = 0; i < sorts.size(); i++) {
            String[] elements = sorts.get(i).split(",");
            if (!StringUtils.isEmpty(elements[0])) {
                Sort.Direction direction = null;
                Sort.NullHandling nullHandling = Sort.NullHandling.NATIVE;
                if (elements.length > 1) {
                    direction = Sort.Direction.fromStringOrNull(elements[1]);
                }
                if (elements.length > 2) {
                    switch (elements[2].toLowerCase()) {
                        case "native":
                            nullHandling = Sort.NullHandling.NATIVE;
                            break;
                        case "first":
                            nullHandling = Sort.NullHandling.NULLS_FIRST;
                            break;
                        case "last":
                            nullHandling = Sort.NullHandling.NULLS_LAST;
                            break;
                        default:
                            break;
                    }
                }

                orders.add(new Sort.Order(direction, elements[0], nullHandling));
            }
        }
        return new Sort(orders);
    }

    @Override
    public String toString(Pageable value) {
        throw new UnsupportedOperationException();
    }

    private int parseAndApplyBoundaries(String parameter, int upper, boolean shiftIndex) {
        try {
            int parsed = Integer.parseInt(parameter) - (pageableConfiguration.isOneIndexedParameters() && shiftIndex ? 1 : 0);
            return parsed < 0 ? 0 : parsed > upper ? upper : parsed;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Class<? extends Serializable> getPropertyType(Class<?> clazz, String property) {
        PropertyCacheKey cacheKey = new PropertyCacheKey(clazz, property);
        Class<? extends Serializable> propertyType = PROPERTY_TYPE_CACHE.get(cacheKey);
        if (propertyType == null) {
            propertyType = resolvePropertyType(clazz, property);
            PROPERTY_TYPE_CACHE.putIfAbsent(cacheKey, propertyType);
        }
        return propertyType;
    }

    private Class<? extends Serializable> resolvePropertyType(Class<?> baseClazz, String property) {
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

}
