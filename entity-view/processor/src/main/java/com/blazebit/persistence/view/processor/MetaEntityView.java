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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    String getPackageName();

    String getBaseSuperclass();

    String getEntityClass();

    String getJpaManagedBaseClass();

    Element getEntityVersionAttribute();

    ExecutableElement getPostCreate();

    ExecutableElement getPostLoad();

    Map<String, TypeElement> getForeignPackageSuperTypes();

    List<TypeMirror> getForeignPackageSuperTypeVariables();

    MetaAttribute getIdMember();

    MetaAttribute getVersionMember();

    Collection<MetaConstructor> getConstructors();

    Collection<MetaAttribute> getMembers();

    Collection<ExecutableElement> getSpecialMembers();

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

    Element[] getOriginatingElements();

    Map<String, TypeElement> getOptionalParameters();

    Map<String, ViewFilter> getViewFilters();

    String getSafeTypeVariable(String typeVariable);

    int getDefaultDirtyMask();
}
