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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.ImportContext;
import com.blazebit.persistence.view.processor.MetaEntityView;
import com.blazebit.persistence.view.processor.MetamodelClassWriter;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaMap extends AnnotationMetaCollection {

    private final String keyType;
    private final String realKeyType;
    private final MetaEntityView keySubviewElement;
    private final String generatedKeyTypePrefix;
    private final String implementationTypeString;

    public AnnotationMetaMap(AnnotationMetaEntityView parent, Element element, String collectionType,
                             String collectionJavaType, String elementCollectionJavaType, String keyType, String realKeyType, String elementType, String realElementType, Context context) {
        super(parent, element, collectionType, collectionJavaType, elementCollectionJavaType, elementType, realElementType, context);
        this.keyType = keyType;
        this.realKeyType = realKeyType;
        TypeElement subviewElement = getSubview(keyType, context);
        if (subviewElement == null) {
            this.keySubviewElement = null;
        } else {
            String subviewFqcn = subviewElement.getQualifiedName().toString();
            MetaEntityView subviewEntityView = context.getMetaEntityViewMap().get(subviewFqcn);
            if (subviewEntityView == null) {
                subviewEntityView = new AnnotationMetaEntityView(subviewElement, context);
            }
            this.keySubviewElement = subviewEntityView;
        }
        if (keySubviewElement != null) {
            this.generatedKeyTypePrefix = TypeUtils.getDerivedTypeName(context.getTypeElement(keyType));
        } else {
            this.generatedKeyTypePrefix = keyType;
        }
        if (elementCollectionJavaType == null) {
            this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(collectionJavaType) + "<" + getHostingEntity().importType(realKeyType) + ", " + getHostingEntity().importType(realElementType) + ">";
        } else {
            this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(collectionJavaType) + "<" + getHostingEntity().importType(realKeyType) + ", " + getHostingEntity().importType(elementCollectionJavaType) + "<" + getHostingEntity().importType(elementType) + ">>";
        }
    }

    public String getKeyType() {
        return keyType;
    }

    public String getGeneratedKeyTypePrefix() {
        return generatedKeyTypePrefix;
    }

    public boolean isKeySubview() {
        return keySubviewElement != null;
    }

    public MetaEntityView getKeySubviewElement() {
        return keySubviewElement;
    }

    @Override
    public String getImplementationTypeString() {
        return implementationTypeString;
    }

    @Override
    public void appendDefaultValue(StringBuilder sb, boolean createEmpty, boolean createConstructor, ImportContext importContext) {
        if (createEmpty) {
            if (isParameter()) {
                sb.append("new ").append(importCollectionType(importContext)).append("<>()");
            } else {

                sb.append("(").append(getImplementationTypeString()).append(") (").append(collectionJavaType).append("<?, ?>) ");
                sb.append(importContext.importType(getDerivedTypeName() + MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)).append('.')
                        .append(getPropertyName());
                if (isSubview()) {
                    sb.append(".attr()");
                }
                sb.append(".getMapInstantiator().");
                if (getDirtyStateIndex() == -1) {
                    sb.append("createMap(0)");
                } else {
                    sb.append("createRecordingMap(0)");
                }
            }
        } else {
            super.appendDefaultValue(sb, createEmpty, createConstructor, importContext);
        }
    }

    @Override
    public boolean isCreateEmptyFlatViews() {
        return false;
    }

    private String importCollectionType(ImportContext importContext) {
        switch (collectionJavaType) {
            case Constants.MAP:
                if (ordered) {
                    return importContext.importType(Constants.LINKED_HASH_MAP);
                } else if (sorted) {
                    return importContext.importType(Constants.TREE_MAP);
                } else {
                    return importContext.importType(Constants.HASH_MAP);
                }
            case Constants.SORTED_MAP:
                return importContext.importType(Constants.TREE_MAP);
            case Constants.NAVIGABLE_MAP:
                return importContext.importType(Constants.TREE_MAP);
            default:
                return importContext.importType(Constants.HASH_MAP);
        }
    }

    @Override
    public void appendMetamodelAttributeType(StringBuilder sb, ImportContext importContext) {
        sb.append(importContext.importType(getMetaType()))
                .append('<')
                .append(importContext.importType(getHostingEntity().getQualifiedName()))
                .append(", ")
                .append(importContext.importType(keyType))
                .append(", ");
        if (elementCollectionJavaType != null) {
            sb.append(importContext.importType(getModelType())).append(", ");
        }
        appendElementType(sb, importContext);
        sb.append('>');
    }

}
