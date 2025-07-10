/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DefaultKeyset;
import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewType;
import graphql.relay.Connection;
import graphql.relay.DefaultConnection;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLImplementingType;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static graphql.schema.GraphQLTypeUtil.isLeaf;
import static graphql.schema.GraphQLTypeUtil.unwrapAll;

/**
 * A support class to interact with entity views in a GraphQL environment.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLEntityViewSupport {

    /**
     * Default name for the page size field.
     */
    public static final String PAGE_SIZE_NAME = "first";
    /**
     * Default name for the page size field.
     */
    public static final String RELAY_LAST_NAME = "last";
    /**
     * Default name for the offset field.
     */
    public static final String OFFSET_NAME = "offset";
    /**
     * Default name for the before cursor field.
     */
    public static final String BEFORE_CURSOR_NAME = "before";
    /**
     * Default name for the after cursor field.
     */
    public static final String AFTER_CURSOR_NAME = "after";
    /**
     * Default name for the edges field.
     */
    public static final String EDGES_NAME = "edges";
    /**
     * Default name for the node field.
     */
    public static final String EDGE_NODE_NAME = "node";
    /**
     * Default name for the cursor field.
     */
    public static final String EDGE_CURSOR_NAME = "cursor";
    /**
     * Default name for the total count field.
     */
    public static final String TOTAL_COUNT_NAME = "totalCount";

    // GraphQL defines meta fields that can be used on any type: https://graphql.org/learn/queries/#meta-fields
    private static final Set<String> META_FIELDS = new HashSet<>(Arrays.asList("__typename"));

    private final Map<String, ManagedViewType<?>> typeNameToViewType;
    private final Map<String, Map<String, String>> typeNameToFieldMapping;
    private final Map<String, Set<DefaultFetchMapping>> typeNameToDefaultFetchMappings;
    private final Set<String> serializableBasicTypes;
    private final ConcurrentMap<TypeRootCacheKey, GraphQLUnmodifiedType> typeReferenceCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String[]> selectedFieldCache = new ConcurrentHashMap<>();

    private final String pageSizeName;
    private final String offsetName;
    private final String beforeCursorName;
    private final String afterCursorName;
    private final String totalCountName;
    private final String pageElementsName;
    private final String pageElementObjectName;
    private final String elementCursorName;

    /**
     * A default constructor to make this class proxyable.
     */
    GraphQLEntityViewSupport() {
        this(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     * It uses the GraphQL Relay specification names for accessing page info fields for paginated settings.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     * @deprecated Use {@link GraphQLEntityViewSupport#GraphQLEntityViewSupport(Map, Map, Map, Set)} instead
     */
    @Deprecated
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Set<String> serializableBasicTypes) {
        this(typeNameToViewType, Collections.emptyMap(), Collections.emptyMap(), serializableBasicTypes, PAGE_SIZE_NAME, OFFSET_NAME, BEFORE_CURSOR_NAME, AFTER_CURSOR_NAME, TOTAL_COUNT_NAME, EDGES_NAME, EDGE_NODE_NAME, EDGE_CURSOR_NAME);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     * It uses the GraphQL Relay specification names for accessing page info fields for paginated settings.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param typeNameToFieldMapping The mapping from GraphQL type names to a map from GraphQL field name to entity view attribute name
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     * @deprecated Use {@link GraphQLEntityViewSupport#GraphQLEntityViewSupport(Map, Map, Map, Set)} instead
     */
    @Deprecated
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Map<String, Map<String, String>> typeNameToFieldMapping, Set<String> serializableBasicTypes) {
        this(typeNameToViewType, typeNameToFieldMapping, Collections.emptyMap(), serializableBasicTypes, PAGE_SIZE_NAME, OFFSET_NAME, BEFORE_CURSOR_NAME, AFTER_CURSOR_NAME, TOTAL_COUNT_NAME, EDGES_NAME, EDGE_NODE_NAME, EDGE_CURSOR_NAME);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     * It uses the GraphQL Relay specification names for accessing page info fields for paginated settings.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param typeNameToFieldMapping The mapping from GraphQL type names to a map from GraphQL field name to entity view attribute name
     * @param typeNameToDefaultFetchMappings The mapping from GraphQL type names to a list of default fetch mappings
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     */
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Map<String, Map<String, String>> typeNameToFieldMapping, Map<String, Set<DefaultFetchMapping>> typeNameToDefaultFetchMappings, Set<String> serializableBasicTypes) {
        this(typeNameToViewType, typeNameToFieldMapping, typeNameToDefaultFetchMappings, serializableBasicTypes, PAGE_SIZE_NAME, OFFSET_NAME, BEFORE_CURSOR_NAME, AFTER_CURSOR_NAME, TOTAL_COUNT_NAME, EDGES_NAME, EDGE_NODE_NAME, EDGE_CURSOR_NAME);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     * @param pageSizeName The name of the page size field
     * @param offsetName The name of the offset field
     * @param beforeCursorName The name of the beforeCursor field
     * @param afterCursorName The name of the afterCursor field
     * @param totalCountName The name of the totalCount field
     * @param pageElementsName The name of the elements field
     * @param pageElementObjectName The name of the element object field within elements
     * @param elementCursorName The name of the cursor field within elements
     * @deprecated Use {@link GraphQLEntityViewSupport#GraphQLEntityViewSupport(Map, Map, Map, Set, String, String, String, String, String, String, String, String)} instead
     */
    @Deprecated
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Set<String> serializableBasicTypes, String pageSizeName, String offsetName, String beforeCursorName, String afterCursorName, String totalCountName, String pageElementsName, String pageElementObjectName, String elementCursorName) {
        this(typeNameToViewType, Collections.emptyMap(), Collections.emptyMap(), serializableBasicTypes, pageSizeName, offsetName, beforeCursorName, afterCursorName, totalCountName, pageElementsName, pageElementObjectName, elementCursorName);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param typeNameToFieldMapping The mapping from GraphQL type names to a map from GraphQL field name to entity view attribute name
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     * @param pageSizeName The name of the page size field
     * @param offsetName The name of the offset field
     * @param beforeCursorName The name of the beforeCursor field
     * @param afterCursorName The name of the afterCursor field
     * @param totalCountName The name of the totalCount field
     * @param pageElementsName The name of the elements field
     * @param pageElementObjectName The name of the element object field within elements
     * @param elementCursorName The name of the cursor field within elements
     * @deprecated Use {@link GraphQLEntityViewSupport#GraphQLEntityViewSupport(Map, Map, Map, Set, String, String, String, String, String, String, String, String)} instead
     */
    @Deprecated
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Map<String, Map<String, String>> typeNameToFieldMapping, Set<String> serializableBasicTypes, String pageSizeName, String offsetName, String beforeCursorName, String afterCursorName, String totalCountName, String pageElementsName, String pageElementObjectName, String elementCursorName) {
        this(typeNameToViewType, typeNameToFieldMapping, Collections.emptyMap(), serializableBasicTypes, pageSizeName, offsetName, beforeCursorName, afterCursorName, totalCountName, pageElementsName, pageElementObjectName, elementCursorName);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     *
     * @param typeNameToViewType The mapping from GraphQL type names to entity view metamodels
     * @param typeNameToFieldMapping The mapping from GraphQL type names to a map from GraphQL field name to entity view attribute name
     * @param typeNameToDefaultFetchMappings The mapping from GraphQL type names to a list of default fetch mappings
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     * @param pageSizeName The name of the page size field
     * @param offsetName The name of the offset field
     * @param beforeCursorName The name of the beforeCursor field
     * @param afterCursorName The name of the afterCursor field
     * @param totalCountName The name of the totalCount field
     * @param pageElementsName The name of the elements field
     * @param pageElementObjectName The name of the element object field within elements
     * @param elementCursorName The name of the cursor field within elements
     */
    public GraphQLEntityViewSupport(Map<String, ManagedViewType<?>> typeNameToViewType, Map<String, Map<String, String>> typeNameToFieldMapping, Map<String, Set<DefaultFetchMapping>> typeNameToDefaultFetchMappings, Set<String> serializableBasicTypes, String pageSizeName, String offsetName, String beforeCursorName, String afterCursorName, String totalCountName, String pageElementsName, String pageElementObjectName, String elementCursorName) {
        this.pageSizeName = pageSizeName;
        this.offsetName = offsetName;
        this.beforeCursorName = beforeCursorName;
        this.afterCursorName = afterCursorName;
        this.totalCountName = totalCountName;
        this.pageElementsName = pageElementsName;
        this.typeNameToViewType = typeNameToViewType;
        this.typeNameToFieldMapping = typeNameToFieldMapping;
        this.typeNameToDefaultFetchMappings = typeNameToDefaultFetchMappings;
        this.serializableBasicTypes = serializableBasicTypes;
        this.pageElementObjectName = pageElementObjectName;
        this.elementCursorName = elementCursorName;
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(DataFetchingEnvironment dataFetchingEnvironment) {
        return createPaginatedSetting(dataFetchingEnvironment, pageElementsName);
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param first The GraphQL relay first parameter
     * @param last The GraphQL relay last parameter
     * @param offset The offset from which to fetch
     * @param beforeCursor The GraphQL relay before cursor
     * @param afterCursor The GraphQL relay after cursor
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(DataFetchingEnvironment dataFetchingEnvironment, Integer first, Integer last, Integer offset, String beforeCursor, String afterCursor) {
        return createPaginatedSetting(dataFetchingEnvironment, pageElementsName, first, last, offset, beforeCursor, afterCursor);
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        String objectRoot;
        if (pageElementObjectName == null || pageElementObjectName.isEmpty()) {
            objectRoot = elementRoot;
        } else if (elementRoot == null || elementRoot.isEmpty()) {
            objectRoot = pageElementObjectName;
        } else {
            objectRoot = elementRoot + "/" + pageElementObjectName;
        }
        String typeName = getElementTypeName(dataFetchingEnvironment, objectRoot);
        ManagedViewType<?> entityViewClass = typeNameToViewType.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return createPaginatedSetting((Class<T>) entityViewClass.getJavaType(), dataFetchingEnvironment, elementRoot);
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param first The GraphQL relay first parameter
     * @param last The GraphQL relay last parameter
     * @param offset The offset from which to fetch
     * @param beforeCursor The GraphQL relay before cursor
     * @param afterCursor The GraphQL relay after cursor
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot, Integer first, Integer last, Integer offset, String beforeCursor, String afterCursor) {
        String objectRoot;
        if (pageElementObjectName == null || pageElementObjectName.isEmpty()) {
            objectRoot = elementRoot;
        } else if (elementRoot == null || elementRoot.isEmpty()) {
            objectRoot = pageElementObjectName;
        } else {
            objectRoot = elementRoot + "/" + pageElementObjectName;
        }
        String typeName = getElementTypeName(dataFetchingEnvironment, objectRoot);
        ManagedViewType<?> entityViewClass = typeNameToViewType.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return createPaginatedSetting((Class<T>) entityViewClass.getJavaType(), dataFetchingEnvironment, elementRoot, extractKeysetPage(first, last, beforeCursor, afterCursor), first, last, offset);
    }

    /**
     * Like calling {@link #createSetting(Class, DataFetchingEnvironment, String)} with the configured page elements name.
     *
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment) {
        return createPaginatedSetting(entityViewClass, dataFetchingEnvironment, pageElementsName);
    }

    /**
     * Like calling {@link #createSetting(Class, DataFetchingEnvironment, String)} with the configured page elements name.
     *
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        KeysetPage keysetPage = extractKeysetPage(dataFetchingEnvironment);
        Integer pageSize = null;
        Integer offset = null;
        Integer last = null;
        if (keysetPage != null) {
            pageSize = dataFetchingEnvironment.getArgument(pageSizeName);
            offset = dataFetchingEnvironment.getArgument(offsetName);
            // Apply computed offset if no explicit offset is given and the keyset page is valid
            if (offset == null && keysetPage.getLowest() != null && keysetPage.getHighest() != null) {
                offset = keysetPage.getFirstResult() + keysetPage.getMaxResults();
            }
            last = dataFetchingEnvironment.getArgument(RELAY_LAST_NAME);
        }
        return createPaginatedSetting(entityViewClass, dataFetchingEnvironment, elementRoot, keysetPage, pageSize, last, offset);
    }

    /**
     * Like calling {@link #createSetting(Class, DataFetchingEnvironment, String, KeysetPage, Integer, Integer, Integer)} with the configured page elements name.
     *
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param keysetPage The keyset page to use for pagination
     * @param first The GraphQL relay first parameter
     * @param last The GraphQL relay last parameter
     * @param offset The offset from which to fetch
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment, String elementRoot, KeysetPage keysetPage, Integer first, Integer last, Integer offset) {
        String objectRoot;
        if (pageElementObjectName == null || pageElementObjectName.isEmpty()) {
            objectRoot = elementRoot;
        } else if (elementRoot == null || elementRoot.isEmpty()) {
            objectRoot = pageElementObjectName;
        } else {
            objectRoot = elementRoot + "/" + pageElementObjectName;
        }
        EntityViewSetting<T, PaginatedCriteriaBuilder<T>> setting = (EntityViewSetting<T, PaginatedCriteriaBuilder<T>>) (EntityViewSetting<?, ?>) createSetting(entityViewClass, dataFetchingEnvironment, objectRoot, keysetPage, first, last, offset);

        if (!dataFetchingEnvironment.getSelectionSet().contains(totalCountName)) {
            setting.setProperty(ConfigurationProperties.PAGINATION_DISABLE_COUNT_QUERY, Boolean.TRUE);
        }

        if (elementCursorName != null && !elementCursorName.isEmpty()) {
            String elementCursorPath;
            if (elementRoot == null || elementRoot.isEmpty()) {
                elementCursorPath = elementCursorName;
            } else {
                elementCursorPath = elementRoot + "/" + elementCursorName;
            }

            if (dataFetchingEnvironment.getSelectionSet().contains(elementCursorPath)) {
                setting.setProperty(ConfigurationProperties.PAGINATION_EXTRACT_ALL_KEYSETS, Boolean.TRUE);
            }
        }
        return setting;
    }

    /**
     * Like calling {@link #createSetting(DataFetchingEnvironment, String)} with an empty element root.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(DataFetchingEnvironment dataFetchingEnvironment) {
        return createSetting(dataFetchingEnvironment, "");
    }

    /**
     * Returns a new entity view setting for the given data fetching environment and element root.
     * Determines the entity view class by using the type of the element root as resolved with the {@link DataFetchingEnvironment}.
     * Like calling {@link #createSetting(Class, DataFetchingEnvironment, String)} with the explicit entity view class.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        String typeName = getElementTypeName(dataFetchingEnvironment, elementRoot);
        ManagedViewType<?> entityViewClass = typeNameToViewType.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return createSetting((Class<T>) entityViewClass.getJavaType(), dataFetchingEnvironment, elementRoot);
    }

    public String getElementTypeName(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        return getElementType(dataFetchingEnvironment, elementRoot).getName();
    }

    public GraphQLUnmodifiedType getElementType(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        GraphQLUnmodifiedType type = unwrapAll(dataFetchingEnvironment.getFieldDefinition().getType());
        TypeRootCacheKey cacheKey = new TypeRootCacheKey(type, elementRoot);
        GraphQLUnmodifiedType cachedType = typeReferenceCache.get(cacheKey);
        if (cachedType != null) {
            return cachedType;
        }

        String[] parts = elementRoot.isEmpty() ? new String[0] : elementRoot.split("/");
        for (String part : parts) {
            if (type instanceof GraphQLFieldsContainer) {
                if (part.length() > 0) {
                    type = unwrapAll(((GraphQLFieldsContainer) type).getFieldDefinition(part).getType());
                }
            } else {
                throw new IllegalArgumentException("The element root part '" + part + "' wasn't found on type: " + type);
            }
        }
        typeReferenceCache.putIfAbsent(cacheKey, type);
        return type;
    }

    /**
     * Like calling {@link #createSetting(Class, DataFetchingEnvironment, String)} with an empty element root.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment) {
        return createSetting(entityViewClass, dataFetchingEnvironment, "");
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        KeysetPage keysetPage = extractKeysetPage(dataFetchingEnvironment);
        Integer pageSize = null;
        Integer offset = null;
        Integer last = null;
        if (keysetPage != null) {
            pageSize = dataFetchingEnvironment.getArgument(pageSizeName);
            offset = dataFetchingEnvironment.getArgument(offsetName);
            // Apply computed offset if no explicit offset is given and the keyset page is valid
            if (offset == null && keysetPage.getLowest() != null && keysetPage.getHighest() != null) {
                offset = keysetPage.getFirstResult() + keysetPage.getMaxResults();
            }
            last = dataFetchingEnvironment.getArgument(RELAY_LAST_NAME);
        }
        return createSetting(entityViewClass, dataFetchingEnvironment, elementRoot, keysetPage, pageSize, last, offset);
    }

    /**
     * Returns a new entity view setting for the given data fetching environment.
     *
     * @param <T> The entity view type
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param keysetPage The keyset page to use for pagination
     * @param first The GraphQL relay first parameter
     * @param last The GraphQL relay last parameter
     * @param offset The offset from which to fetch
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment, String elementRoot, KeysetPage keysetPage, Integer first, Integer last, Integer offset) {
        EntityViewSetting<T, CriteriaBuilder<T>> setting;
        boolean forceUseKeyset = false;
        if (keysetPage == null) {
            int pageSize;
            if (first == null) {
                pageSize = Integer.MAX_VALUE;
            } else if (first < 0) {
                throw new RuntimeException("Illegal negative " + pageSizeName + " parameter: " + first);
            } else {
                pageSize = first;
            }
            if (pageSize == Integer.MAX_VALUE && offset == null) {
                setting = EntityViewSetting.create(entityViewClass);
            } else {
                setting = (EntityViewSetting<T, CriteriaBuilder<T>>) (EntityViewSetting<?, ?>) EntityViewSetting.create(entityViewClass, offset == null ? 0 : (int) offset, pageSize);
            }
        } else {
            int pageSize;
            if (first == null) {
                pageSize = Integer.MAX_VALUE;
            } else if (first < 0) {
                throw new RuntimeException("Illegal negative " + pageSizeName + " parameter: " + first);
            } else {
                pageSize = first;
            }
            if (last != null) {
                if (last < 0) {
                    throw new RuntimeException("Illegal negative " + RELAY_LAST_NAME + " parameter: " + last);
                }
                if (Integer.MAX_VALUE == pageSize) {
                    pageSize = last;
                    if (offset == null) {
                        forceUseKeyset = true;
                    }
                } else {
                    if (offset == null) {
                        offset = first - last;
                        pageSize = last;
                    } else {
                        offset += first - last;
                        pageSize = last;
                    }
                    if (offset < 0) {
                        offset = 0;
                    }
                    if (keysetPage.getLowest() != null || keysetPage.getHighest() != null) {
                        forceUseKeyset = true;
                    }
                }
            } else if (offset == null) {
                forceUseKeyset = true;
            } else if (offset < 0) {
                throw new RuntimeException("Illegal negative " + offsetName + " parameter: " + offset);
            }
            setting = (EntityViewSetting<T, CriteriaBuilder<T>>) (EntityViewSetting<?, ?>) EntityViewSetting.create(entityViewClass, offset == null ? 0 : (int) offset, pageSize);
            setting.withKeysetPage(keysetPage);
        }

        if (forceUseKeyset) {
            setting.setProperty(ConfigurationProperties.PAGINATION_FORCE_USE_KEYSET, true);
        }
        applyFetches(dataFetchingEnvironment, setting, elementRoot);
        return setting;
    }

    /**
     * Extracts the {@link KeysetPage} from the {@link DataFetchingEnvironment} by extracting page size and offset,
     * as well as deserializing before or after cursors.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @return the {@link KeysetPage} or <code>null</code>
     */
    public KeysetPage extractKeysetPage(DataFetchingEnvironment dataFetchingEnvironment) {
        Integer pageSize = dataFetchingEnvironment.getArgument(pageSizeName);
        Integer last = dataFetchingEnvironment.getArgument(RELAY_LAST_NAME);
        String beforeCursor = dataFetchingEnvironment.getArgument(beforeCursorName);
        String afterCursor = dataFetchingEnvironment.getArgument(afterCursorName);
        return extractKeysetPage(pageSize, last, beforeCursor, afterCursor);
    }

    /**
     * Extracts the {@link KeysetPage} from the page size and offset,
     * as well as deserializing before or after cursors.
     *
     * @param first The GraphQL relay first parameter
     * @param last The GraphQL relay last parameter
     * @param beforeCursor The GraphQL relay before cursor
     * @param afterCursor The GraphQL relay after cursor
     * @return the {@link KeysetPage} or <code>null</code>
     */
    public KeysetPage extractKeysetPage(Integer first, Integer last, String beforeCursor, String afterCursor) {
        if (first == null && last == null && beforeCursor == null && afterCursor == null) {
            return null;
        } else {
            KeysetPage keysetPage;

            if (beforeCursor != null) {
                if (afterCursor != null) {
                    throw new RuntimeException("Can't provide both beforeCursor and afterCursor!");
                }
                GraphQLCursor cursor = deserialize(beforeCursor);
                keysetPage = new DefaultKeysetPage(cursor.getOffset(), cursor.getPageSize(), new DefaultKeyset(cursor.getTuple()), null);
            } else if (afterCursor != null) {
                if (last != null) {
                    // Using an after cursor with last does not make sense, so skip using the cursor
                    // The only problem with that is, that the cursor could refer to the last element
                    // If that is the case, we would still get a result, which is IMO an edge case and can be ignored
                    keysetPage = new DefaultKeysetPage(0, last, new DefaultKeyset(null), null);
                } else {
                    GraphQLCursor cursor = deserialize(afterCursor);
                    keysetPage = new DefaultKeysetPage(cursor.getOffset(), cursor.getPageSize(), null, new DefaultKeyset(cursor.getTuple()));
                }
            } else if (first != null) {
                keysetPage = new DefaultKeysetPage(0, first, null, null);
            } else {
                // Keyset with empty tuple is a special case for traversing the result list in reverse order
                keysetPage = new DefaultKeysetPage(0, last, new DefaultKeyset(null), null);
            }

            return keysetPage;
        }
    }

    /**
     * Like {@link #applyFetches(DataFetchingEnvironment, EntityViewSetting, String)} but with an empty element root.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param setting The entity view setting
     */
    public void applyFetches(DataFetchingEnvironment dataFetchingEnvironment, EntityViewSetting<?, ?> setting) {
        applyFetches(dataFetchingEnvironment, setting, "");
    }

    /**
     * Applies the fetches to the {@link EntityViewSetting} as requested by the selection set of {@link DataFetchingEnvironment}
     * and interpreting the only paths below the given element root.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param setting The entity view setting
     * @param elementRoot The field at which to find the elements for fetch extraction
     */
    public void applyFetches(DataFetchingEnvironment dataFetchingEnvironment, EntityViewSetting<?, ?> setting, String elementRoot) {
        DataFetchingFieldSelectionSet selectionSet = dataFetchingEnvironment.getSelectionSet();
        GraphQLSchema graphQLSchema = dataFetchingEnvironment.getGraphQLSchema();
        OUTER:
        for (SelectedField field : selectionSet.getFields()) {
            String fqFieldName = field.getFullyQualifiedName();
            String[] resolvedFetches = selectedFieldCache.get(fqFieldName);
            if (resolvedFetches != null) {
                for (String resolvedFetch : resolvedFetches) {
                    setting.fetch(resolvedFetch);
                }
                continue;
            }
            if (!isLeaf(field.getType())) {
                // Skip non-leaf types because these intermediate objects types would
                // lead to fetching the whole subtree in EntityViewConfiguration#getFetches
                continue;
            }
            if (!field.getQualifiedName().startsWith(elementRoot)) {
                // ignore fields not under the elementRoot
                continue;
            }
            String[] fieldParts = fqFieldName.split("/");
            String[] elementRootParts = elementRoot.isEmpty() ? new String[0] : elementRoot.split("/");
            String fetch = "";
            String currentTypeName = getElementTypeName(dataFetchingEnvironment, elementRoot);
            GraphQLNamedType currentType = (GraphQLNamedType) graphQLSchema.getType(currentTypeName);
            Set<DefaultFetchMapping> defaultFetchMappings = typeNameToDefaultFetchMappings.get(currentTypeName);
            HashSet<String> fetches = null;
            for (int i = 0; i < fieldParts.length; i++) {
                String[] fieldPartComponents = fieldParts[i].split("\\.");
                String typeName = fieldPartComponents[0];
                String fieldName = fieldPartComponents[1];
                if (elementRootParts.length > i && elementRootParts[i].equals(fieldName)) {
                    // ignore the element root prefix
                    continue;
                }
                if (defaultFetchMappings != null) {
                    List<SelectedField> fields = selectionSet.getImmediateFields();
                    for (int j = 0; j < i; j++) {
                        fields = getSelectedField(fields, fieldParts[j]).getSelectionSet().getImmediateFields();
                    }
                    for (DefaultFetchMapping defaultFetchMapping : defaultFetchMappings) {
                        if (matches(defaultFetchMapping, fields)) {
                            if (fetches == null) {
                                fetches = new HashSet<>();
                            }
                            fetches.add(appendPath(fetch, defaultFetchMapping.getAttributeName()));
                        }
                    }
                }
                if (META_FIELDS.contains(fieldName)) {
                    // If this is a GraphQL meta field (e.g. __typename) then there's no EV mapping for it but still
                    // we must fetch the data from DB to populate it. If the enclosing EV is a standard view then use
                    // its id field. Otherwise, skip this fieldPart and effectively put the intermediate field path
                    // into fetches - in reality this wouldn't lead to any unnecessary joins since the EV in that case
                    // corresponds to an embeddable entity.
                    for (String metaFieldTypeName : field.getObjectTypeNames()) {
                        // Meta field might have more parent types (in case of a union type). Try to find a first
                        // ViewType among them and use its id field to ensure the type info can be obtained from DB.
                        ManagedViewType<?> managedViewType = typeNameToViewType.get(metaFieldTypeName);
                        if (managedViewType instanceof ViewType) {
                            fetch = appendPath(fetch, ((ViewType<?>) managedViewType).getIdAttribute().getName());
                            break;
                        }
                    }
                    continue;
                }
                GraphQLOutputType fieldType = getFieldType(currentType, fieldName);
                Map<String, String> fieldMappings;
                if (typeName.charAt(0) == '[') {
                    // Union type
                    if (fieldType == null) {
                        GraphQLNamedType newType = null;
                        // Field is within the subtypes
                        for (String subTypeName : field.getObjectTypeNames()) {
                            GraphQLImplementingType subtype = (GraphQLImplementingType) graphQLSchema.getType(subTypeName);
                            GraphQLNamedType type = unwrapAll(subtype.getField(fieldName).getType());
                            if (newType == null) {
                                newType = type;
                                String mappedFieldPart = typeNameToFieldMapping.get(subTypeName).get(fieldName);
                                fetch = appendPath(fetch, mappedFieldPart);
                            } else if (newType != type) {
                                throw new IllegalArgumentException("Selection " + fqFieldName + " has multiple result types [" + type + "] and [" + newType + "]");
                            }
                        }
                        currentType = newType;
                        continue;
                    } else {
                        fieldMappings = typeNameToFieldMapping.get(currentTypeName);
                        currentTypeName = typeName;
                    }
                } else {
                    fieldMappings = typeNameToFieldMapping.get(typeName);
                }
                if (fieldMappings == null) {
                    // The type of the GraphQL field is not of the expected type, which could mean that we messed up
                    // in the schema building (unlikely), or that the user overwrote the type in a custom schema.
                    // In that case, let's just fetch the attribute fully as it is, since the user probably wrote a
                    // data fetcher to fetch the desired representation based on this data
                    break;
                }
                String mappedFieldPart = fieldMappings.get(fieldName);
                if (mappedFieldPart == null) {
                    // fieldName cannot be mapped to an entity view field, probably because it's a non-DB field with a default -> ignore the whole field
                    continue OUTER;
                }
                if (!typeName.equals(currentTypeName)
                        && typeNameToViewType.get(typeName) != typeNameToViewType.get(currentTypeName) ) {
                    fieldType = getFieldType((GraphQLNamedType) graphQLSchema.getType(typeName), fieldName);
                }
                currentType = unwrapAll(fieldType);
                currentTypeName = currentType.getName();
                defaultFetchMappings = typeNameToDefaultFetchMappings.get(currentTypeName);
                fetch = appendPath(fetch, mappedFieldPart);
            }
            if (!fetch.isEmpty()) {
                if (fetches == null) {
                    setting.fetch(fetch);
                    selectedFieldCache.putIfAbsent(fqFieldName, new String[]{ fetch });
                } else {
                    fetches.add(fetch);
                    for (String f : fetches) {
                        setting.fetch(f);
                    }
                    selectedFieldCache.putIfAbsent(fqFieldName, fetches.toArray(new String[0]));
                }
            }
        }
    }

    private SelectedField getSelectedField(List<SelectedField> fields, String qualifiedName) {
        for (SelectedField field : fields) {
            if (field.getFullyQualifiedName().equals(qualifiedName)) {
                return field;
            }
        }
        throw new IllegalArgumentException("No such field: " + qualifiedName + " in " + fields);
    }

    private static boolean matches(DefaultFetchMapping defaultFetchMapping, List<SelectedField> fields) {
        if (defaultFetchMapping.getIfFieldSelected().isEmpty()) {
            return true;
        }
        for (SelectedField field : fields) {
            if (defaultFetchMapping.getIfFieldSelected().equals(field.getName())) {
                return true;
            }
        }

        return false;
    }

    private GraphQLOutputType getFieldType(GraphQLNamedType currentType, String fieldName) {
        if (currentType instanceof GraphQLImplementingType) {
            GraphQLFieldDefinition currentTypeField = ((GraphQLImplementingType) currentType).getField(fieldName);
            return currentTypeField == null ? null : currentTypeField.getType();
        } else {
            GraphQLUnionType currentUnionType = (GraphQLUnionType) currentType;
            GraphQLOutputType resolvedFieldType = null;
            for (GraphQLNamedOutputType type : currentUnionType.getTypes()) {
                GraphQLOutputType fieldType = getFieldType(type, fieldName);
                if (resolvedFieldType == null) {
                    resolvedFieldType = fieldType;
                } else if (fieldType != null && resolvedFieldType != fieldType) {
                    throw new IllegalArgumentException("GraphQL type [" + currentType.getName() + "] has field [" + fieldName + "] with multiple result types [" + fieldType + "] and [" + resolvedFieldType + "]");
                }
            }
            return resolvedFieldType;
        }
    }

    private String appendPath(String fetch, String field) {
        if (fetch.isEmpty()) {
            return field;
        } else {
            return fetch + "." + field;
        }
    }

    /**
     * Deserializes the given Base64 encoded cursor to a {@link GraphQLCursor} object.
     *
     * @param beforeCursor The Base64 encoded cursor
     * @return a new cursor
     */
    protected GraphQLCursor deserialize(String beforeCursor) {
        try (ObjectInputStream ois = new GraphQLCursorObjectInputStream(Base64.getDecoder().wrap(new ByteArrayInputStream(beforeCursor.getBytes())), serializableBasicTypes)) {
            int offset = ois.read();
            int pageSize = ois.read();
            Serializable[] tuple = (Serializable[]) ois.readObject();
            return new GraphQLCursor(offset, pageSize, tuple);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read cursor", e);
        }
    }

    /**
     * Serializes the given cursor components to a byte array.
     *
     * @param offset The offset
     * @param pageSize The page size
     * @param tuple The tuple
     * @return the serialized form of the cursor
     */
    protected byte[] serializeCursor(int offset, int pageSize, Serializable[] tuple) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.write(offset);
            oos.write(pageSize);
            oos.writeObject(tuple);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Returns the entity view class for the given GraphQL type name.
     *
     * @param typeName The GraphQL type name
     * @return the entity view class or <code>null</code>
     */
    public Class<?> getEntityViewClass(String typeName) {
        ManagedViewType<?> entityViewClass = typeNameToViewType.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return entityViewClass.getJavaType();
    }

    /**
     * @author Christian Beikov
     * @since 1.6.2
     */
    private static class TypeRootCacheKey {
        private final GraphQLType baseType;
        private final String root;

        public TypeRootCacheKey(GraphQLType baseType, String root) {
            this.baseType = baseType;
            this.root = root;
        }

        @Override
        public boolean equals(Object o) {
            if (getClass() != o.getClass()) {
                return false;
            }
            TypeRootCacheKey that = (TypeRootCacheKey) o;
            return baseType.equals(that.baseType) && root.equals(that.root);
        }

        @Override
        public int hashCode() {
            int result = baseType.hashCode();
            result = 31 * result + root.hashCode();
            return result;
        }
    }

    /**
     * Returns a relay connection from the given result list.
     *
     * @param list the result list
     * @return the relay connection
     */
    public <T> Connection<T> createRelayConnection(List<T> list) {
        List<Edge<T>> edges = new ArrayList<>(list.size());
        boolean hasPreviousPage;
        boolean hasNextPage;
        KeysetPage keysetPage;
        if (list instanceof PagedList<?>) {
            PagedList<T> data = (PagedList<T>) list;
            hasPreviousPage = data.getFirstResult() != 0;
            hasNextPage = data.getTotalSize() == -1 || data.getFirstResult() + data.getMaxResults() < data.getTotalSize();
            keysetPage = data.getKeysetPage();
        } else {
            hasPreviousPage = true;
            hasNextPage = true;
            keysetPage = null;
        }
        if (keysetPage == null) {
            for (int i = 0; i < list.size(); i++) {
                edges.add(new DefaultEdge<>(list.get(i), new DefaultConnectionCursor(Integer.toString(i + 1))));
            }
        } else {
            PagedList<T> data = (PagedList<T>) list;
            List<Keyset> keysets = keysetPage.getKeysets();
            int listSize = list.size();
            if (listSize != 0 && keysets.size() != listSize) {
                int end = listSize - 1;
                edges.add(new DefaultEdge<>(list.get(0), new DefaultConnectionCursor(Base64.getEncoder().encodeToString(serializeCursor(data.getFirstResult(), data.getMaxResults(), keysetPage.getLowest().getTuple())))));
                for (int i = 1; i < end; i++) {
                    T node = list.get(i);
                    edges.add(new DefaultEdge<>(node, new DefaultConnectionCursor(Integer.toString(i + 1))));
                }
                edges.add(new DefaultEdge<>(list.get(end), new DefaultConnectionCursor(Base64.getEncoder().encodeToString(serializeCursor(data.getFirstResult(), data.getMaxResults(), keysetPage.getHighest().getTuple())))));
            } else {
                for (int i = 0; i < list.size(); i++) {
                    T node = list.get(i);
                    edges.add(new DefaultEdge<>(node, new DefaultConnectionCursor(Base64.getEncoder().encodeToString(serializeCursor(data.getFirstResult(), data.getMaxResults(), keysets.get(i).getTuple())))));
                }
            }
        }
        PageInfo pageInfo;
        if (edges.isEmpty()) {
            pageInfo = new DefaultPageInfo(null, null, hasPreviousPage, hasNextPage);
        } else {
            pageInfo = new DefaultPageInfo(edges.get(0).getCursor(), edges.get(edges.size() - 1).getCursor(), hasPreviousPage, hasNextPage);
        }
        return new DefaultConnection<>(edges, pageInfo);
    }

}
