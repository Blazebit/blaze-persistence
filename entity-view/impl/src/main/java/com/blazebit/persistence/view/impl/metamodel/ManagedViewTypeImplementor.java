/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.OrderByItem;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ManagedViewTypeImplementor<X> extends ManagedViewType<X> {

    ManagedViewTypeImplementor<X> getRealType();

    void checkAttributes(MetamodelBuildingContext context);

    void checkNestedAttributes(List<AbstractAttribute<?, ?>> parents, MetamodelBuildingContext context, boolean hasMultisetParent);

    NavigableMap<String, AbstractMethodAttribute<? super X, ?>> getRecursiveAttributes();

    NavigableMap<String, AbstractMethodAttribute<? super X, ?>> getRecursiveSubviewAttributes();

    LockMode getLockMode();

    ManagedType<?> getJpaManagedType();

    MappingConstructorImpl<X> getDefaultConstructor();

    Set<AbstractMethodAttribute<? super X, ?>> getUpdateMappableAttributes();

    Map<ManagedViewType<? extends X>, String> getInheritanceSubtypeConfiguration();

    boolean hasEmptyConstructor();

    boolean hasJoinFetchedCollections();

    boolean hasSelectOrSubselectFetchedAttributes();

    boolean hasJpaManagedAttributes();

    boolean hasSubtypes();

    boolean supportsInterfaceEquals();

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getOverallInheritanceSubtypeConfiguration();

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getInheritanceSubtypeConfiguration(Map<ManagedViewType<? extends X>, String> inheritanceSubtypeMapping);

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getDefaultInheritanceSubtypeConfiguration();

    Map<Map<ManagedViewType<? extends X>, String>, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X>> getInheritanceSubtypeConfigurations();

    AbstractMethodAttribute<?, ?> getMutableAttribute(int i);

    int getMutableAttributeCount();

    int getSubtypeIndex(ManagedViewTypeImplementor<? super X> inheritanceBase);

    List<Method> getSpecialMethods();

    Map<String, Type<?>> getEntityViewRootTypes();

    Limiter createLimiter(ExpressionFactory expressionFactory, String prefix, String limitExpression, String offsetExpression, List<OrderByItem> orderByItems);
}
