/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class Constants {
    public static final String ENTITY_VIEW_MANAGER = "com.blazebit.persistence.view.EntityViewManager";
    public static final String STATIC_METAMODEL = "com.blazebit.persistence.view.StaticMetamodel";

    public static final String ENTITY_VIEW = "com.blazebit.persistence.view.EntityView";
    public static final String ID_MAPPING = "com.blazebit.persistence.view.IdMapping";
    public static final String COLLECTION_MAPPING = "com.blazebit.persistence.view.CollectionMapping";
    public static final String SUBQUERY = "com.blazebit.persistence.view.MappingSubquery";
    public static final String PARAMETER = "com.blazebit.persistence.view.MappingParameter";
    public static final String SINGULAR = "com.blazebit.persistence.view.MappingSingular";

    public static final String SINGULAR_ATTRIBUTE = "com.blazebit.persistence.view.metamodel.SingularAttribute";
    public static final String LIST_ATTRIBUTE = "com.blazebit.persistence.view.metamodel.ListAttribute";
    public static final String SET_ATTRIBUTE = "com.blazebit.persistence.view.metamodel.SetAttribute";
    public static final String COLLECTION_ATTRIBUTE = "com.blazebit.persistence.view.metamodel.CollectionAttribute";
    public static final String MAP_ATTRIBUTE = "com.blazebit.persistence.view.metamodel.MapAttribute";

    public static final String LIST = "java.util.List";
    public static final String SET = "java.util.Set";
    public static final String COLLECTION = "java.util.Collection";
    public static final String MAP = "java.util.Map";
    public static final String SORTED_SET = "java.util.SortedSet";
    public static final String NAVIGABLE_SET = "java.util.NavigableSet";
    public static final String SORTED_MAP = "java.util.SortedMap";
    public static final String NAVIGABLE_MAP = "java.util.NavigableMap";

    public static final String SORTED = "java.util.Sorted";
    public static final String NAVIGABLE = "java.util.Navigable";

    public static final String ARRAY_LIST = "java.util.ArrayList";
    public static final String HASH_SET = "java.util.HashSet";
    public static final String LINKED_HASH_SET = "java.util.LinkedHashSet";
    public static final String HASH_MAP = "java.util.HashMap";
    public static final String LINKED_HASH_MAP = "java.util.LinkedHashMap";
    public static final String TREE_SET = "java.util.TreeSet";
    public static final String TREE_MAP = "java.util.TreeMap";

    public static final Map<String, String> COLLECTIONS = new HashMap<>();
    public static final Set<String> SPECIAL = new HashSet<>();

    static {
        COLLECTIONS.put(COLLECTION, COLLECTION_ATTRIBUTE);
        COLLECTIONS.put(SET, SET_ATTRIBUTE);
        COLLECTIONS.put(LIST, LIST_ATTRIBUTE);
        COLLECTIONS.put(MAP, MAP_ATTRIBUTE);

        COLLECTIONS.put(SORTED_SET, SET_ATTRIBUTE);
        COLLECTIONS.put(SORTED_MAP, MAP_ATTRIBUTE);

        SPECIAL.add(ENTITY_VIEW_MANAGER);
    }

    private Constants() {
    }
}
