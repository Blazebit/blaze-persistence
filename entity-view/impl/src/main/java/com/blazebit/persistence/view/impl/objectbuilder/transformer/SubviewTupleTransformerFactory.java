/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Map;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubviewTupleTransformerFactory implements TupleTransformerFactory {

    private final String attributePath;
    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final boolean updatable;
    private final boolean nullIfEmpty;

    public SubviewTupleTransformerFactory(String attributePath, ViewTypeObjectBuilderTemplate<Object[]> template, boolean updatable, boolean nullIfEmpty) {
        this.attributePath = attributePath;
        this.template = template;
        this.updatable = updatable;
        this.nullIfEmpty = nullIfEmpty;
    }

    @Override
    public int getConsumeStartIndex() {
        return template.getTupleOffset() + 1;
    }

    @Override
    public int getConsumeEndIndex() {
        return template.getTupleOffset() + template.getMappers().length;
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        ObjectBuilder<Object[]> objectBuilder = template.createObjectBuilder(parameterHolder, optionalParameters, entityViewConfiguration, 0, true, nullIfEmpty);
        if (updatable) {
            return new UpdatableSubviewTupleTransformer(template, objectBuilder, nullIfEmpty);
        } else {
            return new SubviewTupleTransformer(template, objectBuilder);
        }
    }

}
