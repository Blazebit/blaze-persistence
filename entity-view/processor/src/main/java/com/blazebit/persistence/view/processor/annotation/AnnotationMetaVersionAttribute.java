/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public class AnnotationMetaVersionAttribute extends AnnotationMetaAttribute {

    public AnnotationMetaVersionAttribute(AnnotationMetaEntityView parent, Element entityVersionAttribute, Context context) {
        super(parent, entityVersionAttribute, TypeUtils.getType(entityVersionAttribute, context), TypeUtils.getRealType(entityVersionAttribute, context), null, context, true);
    }

    @Override
    public final String getMetaType() {
        return Constants.SINGULAR_ATTRIBUTE;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }
}
