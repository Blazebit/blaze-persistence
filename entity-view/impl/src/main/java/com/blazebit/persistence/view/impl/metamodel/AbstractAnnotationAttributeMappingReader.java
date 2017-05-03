/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.view.CollectionMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AbstractAnnotationAttributeMappingReader {

    protected final MetamodelBootContext context;

    public AbstractAnnotationAttributeMappingReader(MetamodelBootContext context) {
        this.context = context;
    }

    public void applyCollectionMapping(AttributeMapping attributeMapping, CollectionMapping collectionMapping) {
        Class<?> type = attributeMapping.getTypeClass();
        if (collectionMapping != null && collectionMapping.ignoreIndex() && Map.class.isAssignableFrom(type)) {
            context.addError("Illegal ignoreIndex mapping for the " + attributeMapping.getErrorLocation());
        }

        if (MetamodelUtils.isSorted(type)) {
            Class<? extends Comparator<?>> comparatorClass = null;
            if (collectionMapping != null) {
                Class<?> c = collectionMapping.comparator();
                if (c == Comparator.class) {
                    comparatorClass = null;
                } else {
                    comparatorClass = (Class<? extends Comparator<?>>) c;
                }
            }
            attributeMapping.setContainerSorted(comparatorClass);
        } else if (type == List.class) {
            // List types have to be resolved during building against the metamodel
            // Except if the ignore index flag is set
            if (collectionMapping != null && collectionMapping.ignoreIndex()) {
                attributeMapping.setContainerDefault();
            }
        } else if (collectionMapping != null && collectionMapping.ordered()) {
            attributeMapping.setContainerOrdered();
        } else {
            attributeMapping.setContainerDefault();
        }
    }
}
