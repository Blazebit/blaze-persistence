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

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MetaAttribute {

    void appendMetamodelAttributeDeclarationString(StringBuilder sb);

    void appendMetamodelAttributeNameDeclarationString(StringBuilder sb);

    void appendImplementationAttributeDeclarationString(StringBuilder sb);

    void appendImplementationAttributeGetterAndSetterString(StringBuilder sb);

    void appendImplementationAttributeConstructorParameterString(StringBuilder sb);

    void appendImplementationAttributeConstructorAssignmentString(StringBuilder sb);

    void appendImplementationAttributeConstructorAssignmentDefaultString(StringBuilder sb);

    void appendBuilderAttributeDeclarationString(StringBuilder sb);

    void appendBuilderAttributeGetterAndSetterString(StringBuilder sb);

    boolean isPrimitive();

    Element getElement();

    String getDefaultValue();

    String getMetaType();

    String getPropertyName();

    String getType();

    String getRealType();

    String getGeneratedTypePrefix();

    String getImplementationTypeString();

    MetaEntityView getHostingEntity();

    boolean isSubview();
}
