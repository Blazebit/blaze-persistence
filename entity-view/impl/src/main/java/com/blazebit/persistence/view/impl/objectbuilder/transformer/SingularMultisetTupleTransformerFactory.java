/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformator;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularMultisetTupleTransformerFactory implements TupleTransformerFactory {

    private final int startIndex;
    private final String mapping;
    private final String attributePath;
    private final String multisetResultAlias;
    private final BasicUserTypeStringSupport<Object>[] fieldConverters;
    private final TypeConverter<Object, Object> elementConverter;
    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final TupleTransformerFactory subviewTupleTransformerFactory;
    private final boolean hasSelectOrSubselectFetchedAttributes;

    public SingularMultisetTupleTransformerFactory(int startIndex, String mapping, String attributePath, String multisetResultAlias, TypeConverter<Object, Object> elementConverter, ViewTypeObjectBuilderTemplate<Object[]> template, boolean hasSelectOrSubselectFetchedAttributes, TupleTransformerFactory subviewTupleTransformerFactory) {
        this.startIndex = startIndex;
        this.mapping = mapping;
        this.attributePath = attributePath;
        this.multisetResultAlias = multisetResultAlias;
        this.elementConverter = elementConverter;
        this.template = template;
        this.hasSelectOrSubselectFetchedAttributes = hasSelectOrSubselectFetchedAttributes;
        List<BasicUserTypeStringSupport<Object>> fieldConverters = new ArrayList<>(template.getMappers().length);
        TupleElementMapper[] mappers = template.getMappers();
        for (int i = 0; i < mappers.length; i++) {
            fieldConverters.add(mappers[i].getBasicTypeStringSupport());
        }
        this.fieldConverters = fieldConverters.toArray(new BasicUserTypeStringSupport[0]);
        this.subviewTupleTransformerFactory = subviewTupleTransformerFactory;
    }

    @Override
    public int getConsumeStartIndex() {
        return startIndex;
    }

    @Override
    public int getConsumeEndIndex() {
        return startIndex + 1;
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        if (!entityViewConfiguration.hasSubFetches(attributePath)) {
            return new NullTupleTransformer(template, startIndex);
        }
        if (mapping != null) {
            if (parameterHolder instanceof FullQueryBuilder<?, ?>) {
                FullQueryBuilder<?, ?> queryBuilder = (FullQueryBuilder<?, ?>) parameterHolder;
                if (hasSelectOrSubselectFetchedAttributes) {
                    queryBuilder = queryBuilder.copyCriteriaBuilder(Object[].class, false);
                    queryBuilder.innerJoin(mapping, multisetResultAlias);
                    parameterHolder = queryBuilder;
                }
                entityViewConfiguration = entityViewConfiguration.forSubview(queryBuilder, attributePath, entityViewConfiguration.getEmbeddingViewJpqlMacro());
            } else {
                throw new UnsupportedOperationException("Converting views with correlated attributes isn't supported!");
            }
        }
        TupleTransformator tupleTransformator = template.getTupleTransformatorFactory().create(parameterHolder, optionalParameters, entityViewConfiguration);
        TupleTransformer subviewTupleTransformer = subviewTupleTransformerFactory.create(parameterHolder, optionalParameters, entityViewConfiguration);
        return new SingularMultisetTupleTransformer(startIndex, hasSelectOrSubselectFetchedAttributes, tupleTransformator, subviewTupleTransformer, fieldConverters, elementConverter);
    }

}
