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

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MetamodelBuildingContext {

    public Collection<ViewMapping> getViewMappings();

    public ViewMapping getViewMapping(Class<?> entityViewClass);

    public <X> Type<X> getBasicType(ViewMapping viewMapping, java.lang.reflect.Type type, Class<?> classType, Annotation mapping);

    public <X> Map<Class<?>, TypeConverter<?, X>> getTypeConverter(Class<X> type);

    public Class<?> getEntityModelType(Class<?> entityClass, Annotation mapping);

    public Map<String, JpqlFunction> getJpqlFunctions();

    public EntityMetamodel getEntityMetamodel();

    public JpaProvider getJpaProvider();

    public ExpressionFactory getExpressionFactory();

    public ExpressionFactory createMacroAwareExpressionFactory();

    public ExpressionFactory createMacroAwareExpressionFactory(String viewRoot);

    public ProxyFactory getProxyFactory();

    public FlushMode getFlushMode(Class<?> clazz, FlushMode defaultValue);

    public FlushStrategy getFlushStrategy(Class<?> clazz, FlushStrategy defaultValue);

    public void addError(String error);

    public boolean hasErrors();

    public boolean isEntityView(Class<?> clazz);

    public Set<Class<?>> findSubtypes(Class<?> entityViewClass);
}
