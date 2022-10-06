/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.view.processor.serialization.MetaSerializationField;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MetaAttribute {

    void appendElementType(StringBuilder sb, ImportContext importContext);

    void appendMetamodelAttributeType(StringBuilder sb, ImportContext importContext);

    void appendMetamodelAttributeDeclarationString(StringBuilder sb, ImportContext importContext);

    void appendMetamodelAttributeNameDeclarationString(StringBuilder sb, ImportContext importContext);

    void appendImplementationAttributeDeclarationString(StringBuilder sb);

    void appendImplementationAttributeGetterAndSetterString(StringBuilder sb, Context context);

    void appendBuilderAttributeDeclarationString(StringBuilder sb);

    void appendBuilderAttributeGetterAndSetterString(StringBuilder sb);

    boolean isPrimitive();

    boolean isParameter();

    Set<Modifier> getGetterModifiers();

    String getGetterPackageName();

    TypeKind getTypeKind();

    MetaSerializationField getSerializationField();

    MetaEntityView getSubviewElement();

    MappingKind getKind();

    String getMapping();

    int getAttributeIndex();

    void setAttributeIndex(int attributeIndex);

    int getDirtyStateIndex();

    void setDirtyStateIndex(int attributeIndex);

    String getSetterName();

    boolean supportsDirtyTracking();

    void appendDefaultValue(StringBuilder sb, boolean createEmpty, boolean createConstructor, ImportContext importContext);

    boolean isCreateEmptyFlatViews();

    String getMetaType();

    String getPropertyName();

    String getGetterName();

    String getModelType();

    String getDeclaredJavaType();

    String getConvertedModelType();

    String getGeneratedTypePrefix();

    String getImplementationTypeString();

    String getBuilderImplementationTypeString();

    Collection<AttributeFilter> getFilters();

    MetaEntityView getHostingEntity();

    boolean isSubview();

    boolean isMultiCollection();

    boolean isSynthetic();

    boolean isMutable();

    boolean isSelf();
}
