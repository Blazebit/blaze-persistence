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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.EmbeddableEntityView;
import com.blazebit.persistence.view.metamodel.EmbeddableViewType;

import javax.persistence.metamodel.EmbeddableType;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class EmbeddableViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements EmbeddableViewType<X> {


    public EmbeddableViewTypeImpl(Class<? extends X> clazz, MetamodelBuildingContext context) {
        super(clazz, getEntityClass(clazz, context), context);
    }
    
    private static Class<?> getEntityClass(Class<?> clazz, MetamodelBuildingContext context) {
        EmbeddableEntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EmbeddableEntityView.class);

        if (entityViewAnnot == null) {
            context.addError("Could not find any EmbeddableEntityView annotation for the class '" + clazz.getName() + "'");
            return null;
        }

        Class<?> entityClass = entityViewAnnot.value();

        if (!(context.getEntityMetamodel().getManagedType(entityClass) instanceof EmbeddableType<?>)) {
            context.addError("The class which is referenced by the EmbeddableEntityView annotation of the class '" + clazz.getName() + "' is not an embeddable!");
            return null;
        }
        
        return entityClass;
    }

}
