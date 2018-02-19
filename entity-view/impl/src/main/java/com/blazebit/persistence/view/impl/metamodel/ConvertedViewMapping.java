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

import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConvertedViewMapping implements ViewMapping {

    private final ViewMapping delegate;
    private final TypeConverter<?, ?> converter;
    private final Type convertedType;
    private ManagedViewTypeImplementor convertedViewType;

    public ConvertedViewMapping(ViewMapping delegate, TypeConverter<?, ?> converter, Type convertedType) {
        this.delegate = delegate;
        this.converter = converter;
        this.convertedType = convertedType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ManagedViewTypeImplementor<?> getManagedViewType(MetamodelBuildingContext context) {
        if (convertedViewType != null) {
            return convertedViewType;
        }

        ManagedViewTypeImplementor<?> viewType = delegate.getManagedViewType(context);
        if (viewType instanceof FlatViewType<?>) {
            return convertedViewType = new ConvertedFlatViewType((FlatViewTypeImplementor<?>) viewType, convertedType, converter);
        } else {
            return convertedViewType = new ConvertedViewType((ViewTypeImplementor<?>) viewType, convertedType, converter);
        }
    }

    @Override
    public int compareTo(ViewMapping o) {
        if (o instanceof ConvertedViewMapping) {
            return delegate.compareTo(((ConvertedViewMapping) o).delegate);
        }
        return delegate.compareTo(o);
    }

    // Delegates

    @Override
    public InheritanceViewMapping getDefaultInheritanceViewMapping() {
        return delegate.getDefaultInheritanceViewMapping();
    }

    @Override
    public Integer getDefaultBatchSize() {
        return delegate.getDefaultBatchSize();
    }

    @Override
    public void setDefaultBatchSize(Integer defaultBatchSize) {
        delegate.setDefaultBatchSize(defaultBatchSize);
    }

    @Override
    public boolean isCreatable() {
        return delegate.isCreatable();
    }

    @Override
    public void setCreatable(boolean creatable) {
        delegate.setCreatable(creatable);
    }

    @Override
    public Method getPostCreateMethod() {
        return delegate.getPostCreateMethod();
    }

    @Override
    public List<Method> getSpecialMethods() {
        return delegate.getSpecialMethods();
    }

    @Override
    public void setSpecialMethods(List<Method> specialMethods) {
        delegate.setSpecialMethods(specialMethods);
    }

    @Override
    public void setPostCreateMethod(Method postCreateMethod) {
        delegate.setPostCreateMethod(postCreateMethod);
    }

    @Override
    public boolean isValidatePersistability() {
        return delegate.isValidatePersistability();
    }

    @Override
    public void setValidatePersistability(boolean validatePersistability) {
        delegate.setValidatePersistability(validatePersistability);
    }

    @Override
    public Set<String> getExcludedAttributes() {
        return delegate.getExcludedAttributes();
    }

    @Override
    public void setIdAttributeMapping(MethodAttributeMapping idAttribute) {
        delegate.setIdAttributeMapping(idAttribute);
    }

    @Override
    public void setVersionAttributeMapping(MethodAttributeMapping versionAttribute) {
        delegate.setVersionAttributeMapping(versionAttribute);
    }

    @Override
    public LockMode getResolvedLockMode() {
        return delegate.getResolvedLockMode();
    }

    @Override
    public Map<String, MethodAttributeMapping> getMethodAttributes() {
        return delegate.getMethodAttributes();
    }

    @Override
    public void addConstructor(ConstructorMapping constructorMapping) {
        delegate.addConstructor(constructorMapping);
    }

    @Override
    public Map<ParametersKey, ConstructorMapping> getConstructorMappings() {
        return delegate.getConstructorMappings();
    }

    @Override
    public String determineInheritanceMapping(MetamodelBuildingContext context) {
        return delegate.determineInheritanceMapping(context);
    }

    @Override
    public void setInheritanceMapping(String inheritanceMapping) {
        delegate.setInheritanceMapping(inheritanceMapping);
    }

    @Override
    public boolean isInheritanceSubtypesResolved() {
        return delegate.isInheritanceSubtypesResolved();
    }

    @Override
    public void setInheritanceSubtypesResolved(boolean inheritanceSubtypesResolved) {
        delegate.setInheritanceSubtypesResolved(inheritanceSubtypesResolved);
    }

    @Override
    public Set<Class<?>> getInheritanceSubtypeClasses() {
        return delegate.getInheritanceSubtypeClasses();
    }

    @Override
    public Set<ViewMapping> getInheritanceSubtypes() {
        return delegate.getInheritanceSubtypes();
    }

    @Override
    public Set<ViewMapping> getInheritanceSupertypes() {
        return delegate.getInheritanceSupertypes();
    }

    @Override
    public Set<InheritanceViewMapping> getInheritanceViewMappings() {
        return delegate.getInheritanceViewMappings();
    }

    @Override
    public void onInitializeViewMappingsFinished(Runnable finishListener) {
        delegate.onInitializeViewMappingsFinished(finishListener);
    }

    @Override
    public void initializeViewMappings(MetamodelBuildingContext context, Runnable finishListener) {
        delegate.initializeViewMappings(context, finishListener);
    }

    @Override
    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping, Class<?> excludeEntityViewClass, boolean reportError) {
        return delegate.validateDependencies(context, dependencies, originatingAttributeMapping, null, reportError);
    }

    @Override
    public Class<?> getEntityViewClass() {
        return delegate.getEntityViewClass();
    }

    @Override
    public Class<?> getEntityClass() {
        return delegate.getEntityClass();
    }

    @Override
    public void setEntityClass(Class<?> entityClass) {
        delegate.setEntityClass(entityClass);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public boolean isUpdatable() {
        return delegate.isUpdatable();
    }

    @Override
    public void setUpdatable(boolean updatable) {
        delegate.setUpdatable(updatable);
    }

    @Override
    public LockMode getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public void setLockMode(LockMode lockMode) {
        delegate.setLockMode(lockMode);
    }

    @Override
    public String getLockOwner() {
        return delegate.getLockOwner();
    }

    @Override
    public void setLockOwner(String lockOwner) {
        delegate.setLockOwner(lockOwner);
    }

    @Override
    public FlushMode getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushStrategy getFlushStrategy() {
        return delegate.getFlushStrategy();
    }

    @Override
    public void setFlushStrategy(FlushStrategy flushStrategy) {
        delegate.setFlushStrategy(flushStrategy);
    }

    @Override
    public MethodAttributeMapping getIdAttribute() {
        return delegate.getIdAttribute();
    }

    @Override
    public void setIdAttribute(EntityViewAttributeMapping idAttribute) {
        delegate.setIdAttribute(idAttribute);
    }

    @Override
    public MethodAttributeMapping getVersionAttribute() {
        return delegate.getVersionAttribute();
    }

    @Override
    public void setVersionAttribute(EntityViewAttributeMapping versionAttribute) {
        delegate.setVersionAttribute(versionAttribute);
    }

    @Override
    public Map<String, EntityViewAttributeMapping> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Map<String, EntityViewConstructorMapping> getConstructors() {
        return delegate.getConstructors();
    }
}
