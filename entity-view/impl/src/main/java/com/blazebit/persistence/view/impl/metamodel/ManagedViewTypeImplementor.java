/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ManagedViewTypeImplementor<X> extends ManagedViewType<X> {
    void checkAttributes(MetamodelBuildingContext context);

    void checkNestedAttributes(List<AbstractAttribute<?, ?>> parents, MetamodelBuildingContext context);

    LockMode getLockMode();

    ManagedType<?> getJpaManagedType();

    Set<AbstractMethodAttribute<? super X, ?>> getUpdateMappableAttributes();

    Map<ManagedViewTypeImplementor<? extends X>, String> getInheritanceSubtypeConfiguration();

    boolean hasJoinFetchedCollections();

    boolean hasSubtypes();

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getOverallInheritanceSubtypeConfiguration();

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getInheritanceSubtypeConfiguration(Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeMapping);

    ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X> getDefaultInheritanceSubtypeConfiguration();

    Map<Map<ManagedViewTypeImplementor<? extends X>, String>, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<X>> getInheritanceSubtypeConfigurations();

    AbstractMethodAttribute<?, ?> getMutableAttribute(int i);

    int getMutableAttributeCount();

    int getSubtypeIndex(ManagedViewTypeImplementor<? super X> inheritanceBase);

    List<Method> getSpecialMethods();
}
