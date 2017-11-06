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

import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.spi.EntityViewMapping;

import java.util.Map;
import java.util.Set;

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

    void initializeViewMappings(MetamodelBuildingContext context, AttributeMapping originatingAttributeMapping);

    ManagedViewTypeImplementor<?> getManagedViewType(MetamodelBuildingContext context);

    MethodAttributeMapping getIdAttribute();

    MethodAttributeMapping getVersionAttribute();

    boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping);
}
