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

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DefaultKeyset;
import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewSetting;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private final Map<String, Class<?>> typeNameToClass;
    private final Set<String> serializableBasicTypes;

    private final String pageSizeName;
    private final String offsetName;
    private final String beforeCursorName;
    private final String afterCursorName;
    private final String totalCountName;
    private final String pageElementsName;
    private final String pageElementObjectName;
    private final String elementCursorName;

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     * It uses the GraphQL Relay specification names for accessing page info fields for paginated settings.
     *
     * @param typeNameToClass The mapping from GraphQL type names to entity view class names
     * @param serializableBasicTypes The whitelist of allowed serializable basic types to use for cursor deserialization
     */
    public GraphQLEntityViewSupport(Map<String, Class<?>> typeNameToClass, Set<String> serializableBasicTypes) {
        this(typeNameToClass, serializableBasicTypes, PAGE_SIZE_NAME, OFFSET_NAME, BEFORE_CURSOR_NAME, AFTER_CURSOR_NAME, TOTAL_COUNT_NAME, EDGES_NAME, EDGE_NODE_NAME, EDGE_CURSOR_NAME);
    }

    /**
     * Creates a new {@link GraphQLEntityViewSupport} instance with the given type name to class mapping and serializable basic type whitelist.
     * @param typeNameToClass The mapping from GraphQL type names to entity view class names
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
    public GraphQLEntityViewSupport(Map<String, Class<?>> typeNameToClass, Set<String> serializableBasicTypes, String pageSizeName, String offsetName, String beforeCursorName, String afterCursorName, String totalCountName, String pageElementsName, String pageElementObjectName, String elementCursorName) {
        this.pageSizeName = pageSizeName;
        this.offsetName = offsetName;
        this.beforeCursorName = beforeCursorName;
        this.afterCursorName = afterCursorName;
        this.totalCountName = totalCountName;
        this.pageElementsName = pageElementsName;
        this.typeNameToClass = typeNameToClass;
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
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        String typeName = getElementTypeName(dataFetchingEnvironment, elementRoot);
        Class<?> entityViewClass = typeNameToClass.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return createPaginatedSetting((Class<T>) entityViewClass, dataFetchingEnvironment, elementRoot);
    }

    /**
     * Like calling {{@link #createSetting(Class, DataFetchingEnvironment, String)}} with the configured page elements name.
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
     * Like calling {{@link #createSetting(Class, DataFetchingEnvironment, String)}} with the configured page elements name.
     *
     * @param entityViewClass The entity view class
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> createPaginatedSetting(Class<T> entityViewClass, DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        String objectRoot;
        if (pageElementObjectName == null || pageElementObjectName.isEmpty()) {
            objectRoot = elementRoot;
        } else if (elementRoot == null || elementRoot.isEmpty()) {
            objectRoot = pageElementObjectName;
        } else {
            objectRoot = elementRoot + "/" + pageElementObjectName;
        }
        EntityViewSetting<T, PaginatedCriteriaBuilder<T>> setting = (EntityViewSetting<T, PaginatedCriteriaBuilder<T>>) (EntityViewSetting<?, ?>) createSetting(entityViewClass, dataFetchingEnvironment, objectRoot);

        Map<String, List<Field>> map = dataFetchingEnvironment.getSelectionSet().get();

        if (!map.containsKey(totalCountName)) {
            setting.setProperty(ConfigurationProperties.PAGINATION_DISABLE_COUNT_QUERY, Boolean.TRUE);
        }

        if (elementCursorName != null && !elementCursorName.isEmpty()) {
            String elementCursorPath;
            if (elementRoot == null || elementRoot.isEmpty()) {
                elementCursorPath = elementCursorName;
            } else {
                elementCursorPath = elementRoot + "/" + elementCursorName;
            }

            if (map.containsKey(elementCursorPath)) {
                setting.setProperty(ConfigurationProperties.PAGINATION_EXTRACT_ALL_KEYSETS, Boolean.TRUE);
            }
        }
        return setting;
    }

    /**
     * Like calling {{@link #createSetting(DataFetchingEnvironment, String)}} with an empty element root.
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
     * Like calling {{@link #createSetting(Class, DataFetchingEnvironment, String)}} with the explicit entity view class.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @param elementRoot The field at which to find the elements for fetch extraction
     * @param <T> The entity view type
     * @return the entity view setting
     */
    public <T> EntityViewSetting<T, CriteriaBuilder<T>> createSetting(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        String typeName = getElementTypeName(dataFetchingEnvironment, elementRoot);
        Class<?> entityViewClass = typeNameToClass.get(typeName);
        if (entityViewClass == null) {
            throw new IllegalArgumentException("No entity view type is registered for the name: " + typeName);
        }
        return createSetting((Class<T>) entityViewClass, dataFetchingEnvironment, elementRoot);
    }

    public String getElementTypeName(DataFetchingEnvironment dataFetchingEnvironment, String elementRoot) {
        GraphQLType type = dataFetchingEnvironment.getFieldTypeInfo().getType();
        if (type instanceof GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getWrappedType();
        }
        if (type instanceof GraphQLList) {
            type = ((GraphQLList) type).getWrappedType();
        }
        if (type instanceof GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getWrappedType();
        }

        String[] parts = elementRoot.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (type instanceof GraphQLObjectType) {
                if (parts[i].length() > 0) {
                    type = ((GraphQLObjectType) type).getFieldDefinition(parts[i]).getType();
                }
                if (type instanceof GraphQLNonNull) {
                    type = ((GraphQLNonNull) type).getWrappedType();
                }
                if (type instanceof GraphQLList) {
                    type = ((GraphQLList) type).getWrappedType();
                }
                if (type instanceof GraphQLNonNull) {
                    type = ((GraphQLNonNull) type).getWrappedType();
                }
            } else {
                throw new IllegalArgumentException("The element root part '" + parts[i] + "' wasn't found on type: " + type);
            }
        }

        return type.getName();
    }

    /**
     * Like calling {{@link #createSetting(Class, DataFetchingEnvironment, String)}} with an empty element root.
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
        EntityViewSetting<T, CriteriaBuilder<T>> setting;
        boolean forceUseKeyset = false;
        if (keysetPage == null) {
            setting = EntityViewSetting.create(entityViewClass);
        } else {
            Integer pageSize = dataFetchingEnvironment.getArgument(pageSizeName);
            Integer offset = dataFetchingEnvironment.getArgument(offsetName);
            Integer last = dataFetchingEnvironment.getArgument(RELAY_LAST_NAME);

            if (pageSize == null) {
                pageSize = Integer.MAX_VALUE;
            } else if (pageSize < 0) {
                throw new RuntimeException("Illegal negative " + pageSizeName + " parameter: " + pageSize);
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
                        offset = pageSize - last;
                        pageSize = last;
                    } else {
                        offset += pageSize - last;
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
            setting = (EntityViewSetting<T, CriteriaBuilder<T>>) (EntityViewSetting<?, ?>) EntityViewSetting.create(entityViewClass, offset == null ? 0 : (int) offset, (int) pageSize);
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
     * as well as deserializing before or afterCursors.
     *
     * @param dataFetchingEnvironment The GraphQL data fetching environment
     * @return the {@link KeysetPage} or <code>null</code>
     */
    public KeysetPage extractKeysetPage(DataFetchingEnvironment dataFetchingEnvironment) {
        Integer pageSize = dataFetchingEnvironment.getArgument(pageSizeName);
        Integer last = dataFetchingEnvironment.getArgument(RELAY_LAST_NAME);
        String beforeCursor = dataFetchingEnvironment.getArgument(beforeCursorName);
        String afterCursor = dataFetchingEnvironment.getArgument(afterCursorName);
        if (pageSize == null && last == null && beforeCursor == null && afterCursor == null) {
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
            } else if (pageSize != null) {
                keysetPage = new DefaultKeysetPage(0, pageSize, null, null);
            } else {
                // Keyset with empty tuple is a special case for traversing the result list in reverse order
                keysetPage = new DefaultKeysetPage(0, last, new DefaultKeyset(null), null);
            }

            return keysetPage;
        }
    }

    /**
     * Like {{@link #applyFetches(DataFetchingEnvironment, EntityViewSetting, String)}} but with an empty element root.
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
     * @param elementRoot The element root
     */
    public void applyFetches(DataFetchingEnvironment dataFetchingEnvironment, EntityViewSetting<?, ?> setting, String elementRoot) {
        String prefix = elementRoot == null || elementRoot.isEmpty() ? "" : elementRoot + "/";
        Collection<String> keys = dataFetchingEnvironment.getSelectionSet().get().keySet();
        StringBuilder sb = new StringBuilder();
        OUTER: for (String key : keys) {
            sb.setLength(0);
            for (int i = 0; i < key.length(); i++) {
                final char c = key.charAt(i);
                if (i < prefix.length()) {
                    if (c != prefix.charAt(i)) {
                        continue OUTER;
                    } else {
                        continue;
                    }
                }
                if (c == '/') {
                    sb.append('.');
                } else {
                    sb.append(c);
                }
            }
            if (sb.length() > 0) {
                setting.fetch(sb.toString());
            }
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
     * Returns the entity view class for the given GraphQL type name.
     *
     * @param typeName The GraphQL type name
     * @return the entity view class or <code>null</code>
     */
    public Class<?> getEntityViewClass(String typeName) {
        return typeNameToClass.get(typeName);
    }

}
