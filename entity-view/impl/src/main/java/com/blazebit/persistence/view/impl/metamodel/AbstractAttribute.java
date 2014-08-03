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

import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 *
 * @author Christian
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {
    
    protected final ViewType<X> declaringType;
    protected final Class<Y> javaType;
    protected final String mapping;
    protected final Class<? extends SubqueryProvider> subqueryProvider;
    protected final boolean mappingParameter;
    protected final boolean subqueryMapping;
    protected final boolean subview;
    
    public AbstractAttribute(ViewType<X> declaringType, Class<Y> javaType, Annotation mapping, Set<Class<?>> entityViews) {
        this.declaringType = declaringType;
        this.javaType = javaType;
        this.subview = entityViews.contains(javaType);
        
        if (mapping instanceof Mapping) {
            this.mapping = ((Mapping) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = false;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = true;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingSubquery) {
            this.mapping = null;
            this.subqueryProvider = ((MappingSubquery) mapping).value();
            this.mappingParameter = false;
            this.subqueryMapping = true;
        } else {
            throw new IllegalArgumentException("No mapping annotation could be found!");
        }
    }
    
    public PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean isQueryParameter() {
        return mappingParameter;
    }

    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    @Override
    public boolean isSubquery() {
        return subqueryMapping;
    }

    @Override
    public boolean isSubview() {
        return subview;
    }

    @Override
    public ViewType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Class<Y> getJavaType() {
        return javaType;
    }

    public String getMapping() {
        return mapping;
    }
}
