/*
 * Copyright 2014 - 2020 Blazebit.
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MetaAttribute {

    void appendMetamodelAttributeType(StringBuilder sb, ImportContext importContext);

    void appendMetamodelAttributeDeclarationString(StringBuilder sb);

    void appendMetamodelAttributeNameDeclarationString(StringBuilder sb);

    void appendImplementationAttributeDeclarationString(StringBuilder sb);

    void appendImplementationAttributeGetterAndSetterString(StringBuilder sb, Context context);

    void appendBuilderAttributeDeclarationString(StringBuilder sb);

    void appendBuilderAttributeGetterAndSetterString(StringBuilder sb);

    boolean isPrimitive();

    Element getElement();

    TypeMirror getTypeMirror();

    TypeElement getSubviewElement();

    MappingKind getKind();

    String getMapping();

    int getAttributeIndex();

    void setAttributeIndex(int attributeIndex);

    int getDirtyStateIndex();

    void setDirtyStateIndex(int attributeIndex);

    Element getSetter();

    boolean supportsDirtyTracking();

    void appendDefaultValue(StringBuilder sb, boolean createEmpty, boolean createConstructor, ImportContext importContext);

    boolean isCreateEmptyFlatViews();

    String getMetaType();

    String getPropertyName();

    String getType();

    String getRealType();

    String getGeneratedTypePrefix();

    String getImplementationTypeString();

    Collection<AttributeFilter> getFilters();

    MetaEntityView getHostingEntity();

    boolean isSubview();

    boolean isSynthetic();

    boolean isMutable();

    boolean isSelf();
}
