/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EmptyFlatViewCreation;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.MultiCollectionMapping;
import com.blazebit.persistence.view.metamodel.PluralAttribute;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractAnnotationAttributeMappingReader {

    protected final MetamodelBootContext context;

    public AbstractAnnotationAttributeMappingReader(MetamodelBootContext context) {
        this.context = context;
    }

    protected void applyCommonMappings(AttributeMapping attributeMapping, AnnotatedElement annotatedElement) {
        CollectionMapping collectionMapping = annotatedElement.getAnnotation(CollectionMapping.class);

        Class<?> collectionType = attributeMapping.getDeclaredType();
        if (collectionMapping != null && collectionMapping.ignoreIndex() && Map.class.isAssignableFrom(collectionType)) {
            context.addError("Illegal ignoreIndex mapping for the " + attributeMapping.getErrorLocation());
        }

        if (collectionMapping != null) {
            Class<? extends Comparator<?>> comparatorClass;
            Class<?> c = collectionMapping.comparator();
            if (c == Comparator.class) {
                comparatorClass = null;
            } else {
                comparatorClass = (Class<? extends Comparator<?>>) c;
            }
            if (comparatorClass != null || MetamodelUtils.isSorted(collectionType)) {
                if (collectionMapping.ignoreIndex()) {
                    context.addError("Illegal ignoreIndex mapping for the sorted " + attributeMapping.getErrorLocation());
                }
                if (collectionMapping.ordered()) {
                    context.addError("Illegal ordered mapping for the sorted " + attributeMapping.getErrorLocation());
                }
                attributeMapping.setContainerSorted(comparatorClass);
            } else if (collectionType == List.class) {
                // List types have to be resolved during building against the metamodel
                // Except if the ignore index flag is set
                if (collectionMapping.ignoreIndex()) {
                    attributeMapping.setContainerDefault();
                }
            } else if (collectionMapping.ordered()) {
                attributeMapping.setContainerOrdered();
            } else {
                attributeMapping.setContainerDefault();
            }
            attributeMapping.setForceUniqueness(collectionMapping.forceUnique());
        } else {
            // List types have to be resolved during building against the metamodel
            if (collectionType != List.class) {
                if (MetamodelUtils.isSorted(collectionType)) {
                    attributeMapping.setContainerSorted(null);
                } else {
                    attributeMapping.setContainerDefault();
                }
            }
        }

        MultiCollectionMapping multiCollectionMapping = annotatedElement.getAnnotation(MultiCollectionMapping.class);

        PluralAttribute.ElementCollectionType elementCollectionType = attributeMapping.getElementCollectionType();
        if (multiCollectionMapping != null && elementCollectionType == null) {
            context.addError("Illegal @MultiCollectionMapping mapping on non-multi collection " + attributeMapping.getErrorLocation());
        }

        if (multiCollectionMapping != null) {
            Class<? extends Comparator<?>> comparatorClass;
            Class<?> c = multiCollectionMapping.comparator();
            if (c == Comparator.class) {
                comparatorClass = null;
            } else {
                comparatorClass = (Class<? extends Comparator<?>>) c;
            }
            if (comparatorClass != null || elementCollectionType == PluralAttribute.ElementCollectionType.SORTED_SET) {
                if (multiCollectionMapping.ordered()) {
                    context.addError("Illegal ordered mapping for the sorted element collection " + attributeMapping.getErrorLocation());
                }
                attributeMapping.setElementCollectionSorted(comparatorClass);
            } else if (multiCollectionMapping.ordered()) {
                attributeMapping.setElementCollectionOrdered();
            } else {
                attributeMapping.setElementCollectionDefault();
            }
            attributeMapping.setElementCollectionForceUniqueness(multiCollectionMapping.forceUnique());
        } else if (elementCollectionType == PluralAttribute.ElementCollectionType.SORTED_SET) {
            attributeMapping.setContainerSorted(null);
        } else if (elementCollectionType != null) {
            attributeMapping.setContainerDefault();
        }

        BatchFetch batchFetch = annotatedElement.getAnnotation(BatchFetch.class);
        if (batchFetch != null) {
            attributeMapping.setDefaultBatchSize(batchFetch.size());
        }

        EmptyFlatViewCreation emptyFlatViewCreation = annotatedElement.getAnnotation(EmptyFlatViewCreation.class);
        if (emptyFlatViewCreation != null) {
            attributeMapping.setCreateEmptyFlatViews(emptyFlatViewCreation.value());
        }

        Limit limit = annotatedElement.getAnnotation(Limit.class);
        if (limit != null) {
            attributeMapping.setLimit(limit.limit(), limit.offset(), Arrays.asList(limit.order()));
        }
    }

    protected PluralAttribute.ElementCollectionType getElementCollectionType(Class<?> elementType) {
        if (List.class.isAssignableFrom(elementType)) {
            return PluralAttribute.ElementCollectionType.LIST;
        } else if (SortedSet.class.isAssignableFrom(elementType)) {
            return PluralAttribute.ElementCollectionType.SORTED_SET;
        } else if (Set.class.isAssignableFrom(elementType)) {
            return PluralAttribute.ElementCollectionType.SET;
        } else {
            return PluralAttribute.ElementCollectionType.COLLECTION;
        }
    }
}
