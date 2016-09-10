/*
 * Copyright 2014 Blazebit.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.EmbeddableEntityView;
import com.blazebit.persistence.view.metamodel.EmbeddableViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewMetamodelImpl implements ViewMetamodel {

    private final EntityMetamodel metamodel;
    private final Map<Class<?>, ViewType<?>> views;
    private final Map<Class<?>, EmbeddableViewType<?>> embeddableViews;
    private final Map<Class<?>, ManagedViewTypeImpl<?>> managedViews;

    public ViewMetamodelImpl(Set<Class<?>> entityViews, boolean validateExpressions, ExpressionFactory expressionFactory, Metamodel metamodel) {
        this.metamodel = new EntityMetamodel(metamodel);
        this.views = new HashMap<Class<?>, ViewType<?>>(entityViews.size());
        this.embeddableViews = new HashMap<Class<?>, EmbeddableViewType<?>>(entityViews.size());
        this.managedViews = new HashMap<Class<?>, ManagedViewTypeImpl<?>>(entityViews.size());

        Set<String> errors = new HashSet<String>();
        
        for (Class<?> entityViewClass : entityViews) {
            ManagedViewTypeImpl<?> managedView;
            
            if (!isEmbeddableViewType(entityViewClass)) {
                ViewTypeImpl<?> viewType = getViewType(entityViewClass, entityViews, this.metamodel, expressionFactory);
                views.put(entityViewClass, viewType);
                managedView = viewType;
            } else {
                EmbeddableViewTypeImpl<?> embeddableViewType = getEmbeddableViewType(entityViewClass, entityViews, this.metamodel, expressionFactory);
                embeddableViews.put(entityViewClass, embeddableViewType);
                managedView = embeddableViewType;
            }
            
            managedViews.put(entityViewClass, managedView);
        }
        
        if (validateExpressions) {
            for (ManagedViewType<?> t : managedViews.values()) {
                ((ManagedViewTypeImpl<?>) t).checkAttributes(managedViews, expressionFactory, this.metamodel, errors);
            }
        }
        
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("There are error(s) in entity views!");
            
            for (String error : errors) {
                sb.append('\n');
                sb.append(error);
            }
            
            throw new IllegalArgumentException(sb.toString());
        }

        // Check for circular dependencies
        for (ViewType<?> viewType : views.values()) {
            Set<ManagedViewType<?>> dependencies = new HashSet<ManagedViewType<?>>();
            dependencies.add(viewType);
            checkCircularDependencies(viewType, dependencies);
        }
    }

    private void checkCircularDependencies(ManagedViewType<?> viewType, Set<ManagedViewType<?>> dependencies) {
        for (MethodAttribute<?, ?> attr : viewType.getAttributes()) {
            if (attr.isSubview()) {
                ManagedViewType<?> subviewType;
                if (attr instanceof PluralAttribute<?, ?, ?>) {
                    subviewType = managedViews.get(((PluralAttribute<?, ?, ?>) attr).getElementType());
                } else {
                    subviewType = managedViews.get(attr.getJavaType());
                }
                if (dependencies.contains(subviewType)) {
                    throw new IllegalArgumentException("A circular dependency is introduced at the attribute '" + attr.getName() + "' of the view type '" + viewType.getJavaType().getName()
                        + "' in the following dependency set: " + Arrays.deepToString(dependencies.toArray()));
                }

                Set<ManagedViewType<?>> subviewDependencies = new HashSet<ManagedViewType<?>>(dependencies);
                subviewDependencies.add(subviewType);
                checkCircularDependencies(subviewType, subviewDependencies);
            }
        }
    }

    public EntityMetamodel getEntityMetamodel() {
        return metamodel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ViewType<X> view(Class<X> clazz) {
        return (ViewType<X>) views.get(clazz);
    }

    @Override
    public Set<ViewType<?>> getViews() {
        return new HashSet<ViewType<?>>(views.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ManagedViewType<X> managedView(Class<X> clazz) {
        return (ManagedViewType<X>) managedViews.get(clazz);
    }

    @Override
    public Set<ManagedViewType<?>> getManagedViews() {
        return new HashSet<ManagedViewType<?>>(managedViews.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> EmbeddableViewType<X> embeddableView(Class<X> clazz) {
        return (EmbeddableViewType<X>) embeddableViews.get(clazz);
    }

    @Override
    public Set<EmbeddableViewType<?>> getEmbeddableViews() {
        return new HashSet<EmbeddableViewType<?>>(embeddableViews.values());
    }

    private boolean isEmbeddableViewType(Class<?> entityViewClass) {
        return AnnotationUtils.findAnnotation(entityViewClass, EmbeddableEntityView.class) != null;
    }

    private ViewTypeImpl<?> getViewType(Class<?> entityViewClass, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        return new ViewTypeImpl<Object>(entityViewClass, entityViews, metamodel, expressionFactory);
    }

    private EmbeddableViewTypeImpl<?> getEmbeddableViewType(Class<?> entityViewClass, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        return new EmbeddableViewTypeImpl<Object>(entityViewClass, entityViews, metamodel, expressionFactory);
    }

}
