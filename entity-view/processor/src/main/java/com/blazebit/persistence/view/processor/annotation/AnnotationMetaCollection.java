/*
 * Copyright 2014 - 2019 Blazebit.
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
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class AnnotationMetaCollection extends AnnotationMetaAttribute {

    protected final boolean ordered;
    protected final boolean sorted;
    protected final String comparator;
    protected final String collectionJavaType;
    private final String collectionType;

    public AnnotationMetaCollection(AnnotationMetaEntityView parent, Element element, String collectionType, String collectionJavaType, String elementType, String realElementType) {
        super(parent, element, elementType, realElementType);
        boolean ordered = Constants.LIST.equals(collectionJavaType);
        boolean sorted = collectionJavaType.startsWith(Constants.SORTED) || collectionJavaType.startsWith(Constants.NAVIGABLE);
        String comparator = null;
        AnnotationMirror annotationMirror = TypeUtils.getAnnotationMirror(element, Constants.COLLECTION_MAPPING);
        if (annotationMirror != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                if ("ordered".equals(entry.getKey().getSimpleName())) {
                    ordered = Boolean.valueOf(entry.getValue().toString());
                } else if ("comparator".equals(entry.getKey().getSimpleName())) {
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
    }

    @Override
    protected String getImplementationTypeString() {
        return getHostingEntity().importType(collectionJavaType) + "<" + getHostingEntity().importType(getRealType()) + ">";
    }

    @Override
    protected String getDefaultValue() {
        switch (collectionJavaType) {
            case Constants.SET:
                if (ordered) {
                    return "new " + getHostingEntity().importType(Constants.LINKED_HASH_SET) + "<>()";
                } else if (sorted) {
                    return "new " + getHostingEntity().importType(Constants.TREE_SET) + "<>()";
                } else {
                    return "new " + getHostingEntity().importType(Constants.HASH_SET) + "<>()";
                }
            case Constants.SORTED_SET:
                return "new " + getHostingEntity().importType(Constants.TREE_SET) + "<>()";
            case Constants.NAVIGABLE_SET:
                return "new " + getHostingEntity().importType(Constants.TREE_SET) + "<>()";
            default:
                return "new " + getHostingEntity().importType(Constants.ARRAY_LIST) + "<>()";
        }
    }

    @Override
    public String getMetaType() {
        return collectionType;
    }
}
