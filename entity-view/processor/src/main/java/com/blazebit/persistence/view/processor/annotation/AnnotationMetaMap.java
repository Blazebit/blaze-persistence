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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.Element;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaMap extends AnnotationMetaCollection {

    private final String keyType;
    private final String realKeyType;
    private final boolean isKeySubview;
    private final String generatedKeyTypePrefix;
    private final String implementationTypeString;
    private final String defaultValue;

    public AnnotationMetaMap(AnnotationMetaEntityView parent, Element element, String collectionType,
                             String collectionJavaType, String keyType, String realKeyType, String elementType, String realElementType, Context context) {
        super(parent, element, collectionType, collectionJavaType, elementType, realElementType, context);
        this.keyType = keyType;
        this.realKeyType = realKeyType;
        this.isKeySubview = isSubview(realKeyType, context);
        if (isKeySubview) {
            this.generatedKeyTypePrefix = TypeUtils.getDerivedTypeName(context.getElementUtils().getTypeElement(keyType));
        } else {
            this.generatedKeyTypePrefix = keyType;
        }
        this.implementationTypeString = getHostingEntity().importType(collectionJavaType) + "<" + getHostingEntity().importType(realKeyType) + ", " + getHostingEntity().importType(getRealType()) + ">";
        this.defaultValue = computeDefaultValue();
    }

    public String getKeyType() {
        return keyType;
    }

    public String getGeneratedKeyTypePrefix() {
        return generatedKeyTypePrefix;
    }

    public boolean isKeySubview() {
        return isKeySubview;
    }

    @Override
    public String getImplementationTypeString() {
        return implementationTypeString;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    private String computeDefaultValue() {
        // TODO: recording?
        switch (collectionJavaType) {
            case Constants.MAP:
                if (ordered) {
                    return "new " + getHostingEntity().importType(Constants.LINKED_HASH_MAP) + "<>()";
                } else if (sorted) {
                    return "new " + getHostingEntity().importType(Constants.TREE_MAP) + "<>()";
                } else {
                    return "new " + getHostingEntity().importType(Constants.HASH_MAP) + "<>()";
                }
            case Constants.SORTED_MAP:
                return "new " + getHostingEntity().importType(Constants.TREE_MAP) + "<>()";
            case Constants.NAVIGABLE_MAP:
                return "new " + getHostingEntity().importType(Constants.TREE_MAP) + "<>()";
            default:
                return "new " + getHostingEntity().importType(Constants.HASH_MAP) + "<>()";
        }
    }

    @Override
    public void appendMetamodelAttributeDeclarationString(StringBuilder sb) {
        sb.append("    public static volatile ")
                .append(getHostingEntity().metamodelImportType(getMetaType()))
                .append("<")
                .append(getHostingEntity().importType(getHostingEntity().getQualifiedName()))
                .append(", ")
                .append(getHostingEntity().importType(keyType))
                .append(", ")
                .append(getHostingEntity().importType(getType()))
                .append("> ")
                .append(getPropertyName())
                .append(";");
    }

}
