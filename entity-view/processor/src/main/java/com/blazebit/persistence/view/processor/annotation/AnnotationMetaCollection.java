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
import com.blazebit.persistence.view.processor.ImportContext;
import com.blazebit.persistence.view.processor.MetamodelClassWriter;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaCollection extends AnnotationMetaAttribute {

    protected final boolean ordered;
    protected final boolean sorted;
    protected final String comparator;
    protected final String collectionJavaType;
    private final String collectionType;
    private final String implementationTypeString;

    public AnnotationMetaCollection(AnnotationMetaEntityView parent, Element element, String collectionType, String collectionJavaType, String elementType, String realElementType, Context context) {
        super(parent, element, elementType, realElementType, context);
        boolean ordered = Constants.LIST.equals(collectionJavaType);
        boolean sorted = collectionJavaType.startsWith(Constants.SORTED) || collectionJavaType.startsWith(Constants.NAVIGABLE);
        String comparator = null;
        AnnotationMirror annotationMirror = TypeUtils.getAnnotationMirror(element, Constants.COLLECTION_MAPPING);
        if (annotationMirror != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                String name = entry.getKey().getSimpleName().toString();
                if ("ordered".equals(name)) {
                    ordered = Boolean.parseBoolean(entry.getValue().toString());
                } else if ("comparator".equals(name)) {
                    String s = entry.getValue().toString();
                    if (!Comparator.class.getName().equals(s)) {
                        comparator = s;
                        sorted = true;
                    }
                }
            }
        }
        this.ordered = ordered;
        this.sorted = sorted;
        this.comparator = comparator;
        this.collectionJavaType = collectionJavaType;
        this.collectionType = collectionType;
        this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(collectionJavaType) + "<" + getHostingEntity().importType(getRealType()) + ">";
    }

    @Override
    public void appendDefaultValue(StringBuilder sb, boolean createEmpty, ImportContext importContext) {
        if (createEmpty) {
            if (getElement().getKind() == ElementKind.PARAMETER) {
                sb.append("new ").append(importCollectionType(importContext)).append("<>()");
            } else {
                sb.append("(").append(getImplementationTypeString()).append(") (").append(collectionJavaType).append("<?>) ");
                sb.append(importContext.importType(TypeUtils.getDerivedTypeName(getHostingEntity().getTypeElement()) + MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)).append('.')
                        .append(getPropertyName());
                if (isSubview()) {
                    sb.append(".attr()");
                }
                sb.append(".getCollectionInstantiator().");
                if (getDirtyStateIndex() == -1) {
                    sb.append("createCollection(0)");
                } else {
                    sb.append("createRecordingCollection(0)");
                }
            }
        } else {
            super.appendDefaultValue(sb, createEmpty, importContext);
        }
    }

    private String importCollectionType(ImportContext importContext) {
        switch (collectionJavaType) {
            case Constants.SET:
                if (ordered) {
                    return importContext.importType(Constants.LINKED_HASH_SET);
                } else if (sorted) {
                    return importContext.importType(Constants.TREE_SET);
                } else {
                    return importContext.importType(Constants.HASH_SET);
                }
            case Constants.SORTED_SET:
                return importContext.importType(Constants.TREE_SET);
            case Constants.NAVIGABLE_SET:
                return importContext.importType(Constants.TREE_SET);
            default:
                return importContext.importType(Constants.ARRAY_LIST);
        }
    }

    @Override
    public String getImplementationTypeString() {
        return implementationTypeString;
    }

    @Override
    public String getMetaType() {
        return collectionType;
    }

    public boolean isIndexedList() {
        return Constants.LIST.equals(collectionJavaType);
    }
}
