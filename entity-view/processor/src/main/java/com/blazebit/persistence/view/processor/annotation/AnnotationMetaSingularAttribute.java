/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;

import javax.lang.model.element.Element;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaSingularAttribute extends AnnotationMetaAttribute {

    public AnnotationMetaSingularAttribute(AnnotationMetaEntityView parent, Element element, String type, String realType, String convertedModelType, Context context) {
        super(parent, element, type, realType, convertedModelType, context, false);
    }

    @Override
    public final String getMetaType() {
        return Constants.METHOD_SINGULAR_ATTRIBUTE;
    }
}
