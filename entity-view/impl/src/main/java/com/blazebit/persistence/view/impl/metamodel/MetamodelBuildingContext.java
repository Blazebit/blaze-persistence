/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.BasicUserTypeRegistry;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.Attribute;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MetamodelBuildingContext {

    public BasicUserTypeRegistry getBasicUserTypeRegistry();

    public Collection<ViewMapping> getViewMappings();

    public ViewMapping getViewMapping(Class<?> entityViewClass);

    public <X> Type<X> getBasicType(ViewMapping viewMapping, java.lang.reflect.Type type, Class<?> classType, Set<Class<?>> possibleTypes);

    public <X> Map<Class<?>, TypeConverter<?, X>> getTypeConverter(Class<X> type);

    /**
     * Returns the possible target types for the mapping based on the given entity class.
     * If the mapping does not allow to determine the type, <code>null</code> is returned.
     *
     * @param entityClass The entity class
     * @param rootAttribute
     * @param mapping The mapping annotation
     * @return The possible target types or <code>null</code>
     */
    public List<ScalarTargetResolvingExpressionVisitor.TargetType> getPossibleTargetTypes(Class<?> entityClass, Attribute<?, ?> rootAttribute, Annotation mapping, Map<String, javax.persistence.metamodel.Type<?>> rootTypes);

    public Map<String, JpqlFunction> getJpqlFunctions();

    public EntityMetamodel getEntityMetamodel();

    public JpaProvider getJpaProvider();

    public DbmsDialect getDbmsDialect();

    public ExpressionFactory getExpressionFactory();

    public ExpressionFactory getTypeValidationExpressionFactory();

    public ExpressionFactory getTypeExtractionExpressionFactory();

    public MacroConfigurationExpressionFactory createMacroAwareExpressionFactory(String viewRoot);

    public boolean isDisallowOwnedUpdatableSubview();

    public boolean isStrictCascadingCheck();

    public boolean isErrorOnInvalidPluralSetter();

    public boolean isCreateEmptyFlatViews();

    public ProxyFactory getProxyFactory();

    public FlushMode getFlushMode(Class<?> clazz, FlushMode defaultValue);

    public FlushStrategy getFlushStrategy(Class<?> clazz, FlushStrategy defaultValue);

    public void addError(String error);

    public boolean hasErrors();

    public boolean isEntityView(Class<?> clazz);

    public Set<Class<?>> findSubtypes(Class<?> entityViewClass);

    public Set<Class<?>> findSupertypes(Class<?> entityViewClass);

    public void addManagedViewType(ViewMapping viewMapping, EmbeddableOwner embeddableMapping, ManagedViewTypeImplementor<?> managedViewType);

    public ManagedViewTypeImplementor<?> getManagedViewType(ViewMapping viewMapping, EmbeddableOwner embeddableMapping);

    public void finishViewType(ManagedViewTypeImplementor<?> managedViewType);

    public void onViewTypeFinished(ManagedViewTypeImplementor<?> managedViewType, Runnable listener);

    public Map<Class<?>, CTEProvider> getCteProviders();

    void checkMultisetSupport(List<AbstractAttribute<?, ?>> parents, AbstractAttribute<?, ?> attribute, BasicUserType<?> userType);

    void checkMultisetSupport(AbstractAttribute<?, ?> attribute, BasicUserType<?> userType);
}
