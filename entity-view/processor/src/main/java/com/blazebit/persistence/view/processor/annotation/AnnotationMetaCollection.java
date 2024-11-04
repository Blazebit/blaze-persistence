/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    protected final String elementCollectionJavaType;
    private final String collectionType;
    private final String implementationTypeString;

    public AnnotationMetaCollection(AnnotationMetaEntityView parent, Element element, String collectionType, String collectionJavaType, String elementCollectionJavaType, String elementType, String realElementType, Context context) {
        super(parent, element, elementType, realElementType, context);
        this.elementCollectionJavaType = elementCollectionJavaType;
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
        if (elementCollectionJavaType == null) {
            this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(collectionJavaType) + "<" + getHostingEntity().importType(realElementType) + ">";
        } else {
            this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(collectionJavaType) + "<" + getHostingEntity().importType(elementCollectionJavaType) + "<" + getHostingEntity().importType(elementType) + ">>";
        }
    }

    @Override
    public void appendDefaultValue(StringBuilder sb, boolean createEmpty, boolean createConstructor, ImportContext importContext) {
        if (createEmpty) {
            if (isParameter()) {
                sb.append("new ").append(importCollectionType(importContext)).append("<>()");
            } else {
                sb.append("(").append(getImplementationTypeString()).append(") (").append(collectionJavaType).append("<?>) ");
                sb.append(importContext.importType(getDerivedTypeName() + MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)).append('.')
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
            super.appendDefaultValue(sb, createEmpty, createConstructor, importContext);
        }
    }

    @Override
    public boolean isCreateEmptyFlatViews() {
        return false;
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
    public void appendMetamodelAttributeType(StringBuilder sb, ImportContext importContext) {
        sb.append(importContext.importType(getMetaType()))
                .append('<')
                .append(importContext.importType(getHostingEntity().getQualifiedName()))
                .append(", ");
        if (elementCollectionJavaType != null) {
            sb.append(importContext.importType(getModelType())).append(", ");
        }
        appendElementType(sb, importContext);
        sb.append('>');
    }

    @Override
    public void appendElementType(StringBuilder sb, ImportContext importContext) {
        if (elementCollectionJavaType == null) {
            sb.append(importContext.importType(getModelType()));
        } else {
            sb.append(importContext.importType(elementCollectionJavaType))
                    .append('<')
                    .append(importContext.importType(getModelType()))
                    .append('>');
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

    @Override
    public boolean isMultiCollection() {
        return elementCollectionJavaType != null;
    }
}
