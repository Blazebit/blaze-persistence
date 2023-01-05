/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MetaEntityView {

    boolean hasEmptyConstructor();

    boolean hasSelfConstructor();

    boolean hasSubviews();

    boolean isValid();

    boolean isUpdatable();

    boolean isCreatable();

    boolean isAllSupportDirtyTracking();

    int getMutableAttributeCount();

    String getSimpleName();

    String getQualifiedName();

    String getDerivedTypeName();

    List<JavaTypeVariable> getTypeVariables();

    String getPackageName();

    String getBaseSuperclass();

    String getEntityClass();

    String getJpaManagedBaseClass();

    String getEntityVersionAttributeName();

    EntityViewLifecycleMethod getPostCreateForReflection();

    EntityViewLifecycleMethod getPostLoad();

    Map<String, ForeignPackageType> getForeignPackageSuperTypes();

    List<String> getForeignPackageSuperTypeVariables();

    MetaAttribute getIdMember();

    MetaAttribute getVersionMember();

    Collection<MetaConstructor> getConstructors();

    Collection<MetaAttribute> getMembers();

    Collection<EntityViewSpecialMemberMethod> getSpecialMembers();

    ImportContext getMetamodelImportContext();

    ImportContext getRelationImportContext();

    ImportContext getMultiRelationImportContext();

    ImportContext getImplementationImportContext();

    ImportContext getBuilderImportContext();

    String importType(String fqcn);

    String importTypeExceptMetamodel(String fqcn);

    String metamodelImportType(String fqcn);

    String relationImportType(String fqcn);

    String multiRelationImportType(String fqcn);

    String implementationImportType(String fqcn);

    String builderImportType(String fqcn);

    TypeElement getTypeElement();

    ElementKind getElementKind();

    Set<Modifier> getModifiers();

    Element[] getOriginatingElements();

    Map<String, String> getOptionalParameters();

    Map<String, ViewFilter> getViewFilters();

    String getSafeTypeVariable(String typeVariable);

    int getDefaultDirtyMask();

    boolean hasCustomEqualsOrHashCodeMethod();

    boolean hasCustomToStringMethod();
}
