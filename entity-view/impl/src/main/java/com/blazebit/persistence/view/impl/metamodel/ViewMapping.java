/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.metamodel.ViewRoot;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.EntityViewRootMapping;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ViewMapping extends Comparable<ViewMapping>, EntityViewMapping {

    InheritanceViewMapping getDefaultInheritanceViewMapping();

    Integer getDefaultBatchSize();

    void setDefaultBatchSize(Integer defaultBatchSize);

    Set<String> getExcludedAttributes();

    void setIdAttributeMapping(MethodAttributeMapping idAttribute);

    void setVersionAttributeMapping(MethodAttributeMapping versionAttribute);

    LockMode getResolvedLockMode();

    Map<String, MethodAttributeMapping> getMethodAttributes();

    void addConstructor(ConstructorMapping constructorMapping);

    Map<ParametersKey, ConstructorMapping> getConstructorMappings();

    String determineInheritanceMapping(MetamodelBuildingContext context);

    void setInheritanceMapping(String inheritanceMapping);

    boolean isInheritanceSubtypesResolved();

    void setInheritanceSubtypesResolved(boolean inheritanceSubtypesResolved);

    Set<Class<?>> getInheritanceSubtypeClasses();

    Set<ViewMapping> getInheritanceSubtypes();

    Set<ViewMapping> getInheritanceSupertypes();

    Set<InheritanceViewMapping> getInheritanceViewMappings();

    void onInitializeViewMappingsFinished(Runnable finishListener);

    boolean isCreatable(MetamodelBuildingContext context);

    void initializeViewMappings(MetamodelBuildingContext context, Runnable finishListener);

    ManagedType<?> getManagedType(MetamodelBuildingContext context);

    ManagedViewTypeImplementor<?> getManagedViewType(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping);

    MethodAttributeMapping getIdAttribute();

    MethodAttributeMapping getVersionAttribute();

    boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping, Class<?> excludeEntityViewClass, boolean reportError);

    List<Method> getSpecialMethods();

    void setSpecialMethods(List<Method> specialMethods);

    Set<Class<? extends CTEProvider>> getCteProviders();

    void setCteProviders(Set<Class<? extends CTEProvider>> cteProviders);

    Map<String, Class<? extends ViewFilterProvider>> getViewFilterProviders();

    void setViewFilterProviders(Map<String, Class<? extends ViewFilterProvider>> viewFilterProviders);

    Set<EntityViewRootMapping> getEntityViewRoots();

    void setEntityViewRoots(Set<EntityViewRootMapping> entityViewRoots);

    Set<ViewRoot> getViewRoots(MetamodelBuildingContext context);

    Map<String, Type<?>> getViewRootTypes(MetamodelBuildingContext context);
}
