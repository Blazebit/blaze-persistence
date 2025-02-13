/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
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
public class CollectionMultisetTupleTransformerFactory implements TupleTransformerFactory {

    private final int startIndex;
    private final String mapping;
    private final String attributePath;
    private final String multisetResultAlias;
    private final BasicUserTypeStringSupport<Object>[] fieldConverters;
    private final TypeConverter<Object, Object> elementConverter;
    private final ContainerAccumulator<Object> containerAccumulator;
    private final boolean dirtyTracking;
    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final ViewTypeObjectBuilderTemplate<Object[]> indexTemplate;
    private final TupleTransformerFactory subviewTupleTransformerFactory;
    private final TupleTransformerFactory indexSubviewTupleTransformerFactory;
    private final BasicUserTypeStringSupport<?> valueBasicTypeSupport;
    private final BasicUserTypeStringSupport<?> indexBasicTypeSupport;
    private final boolean hasSelectOrSubselectFetchedAttributes;

    public CollectionMultisetTupleTransformerFactory(int startIndex, String mapping, String attributePath, String multisetResultAlias, TypeConverter<Object, Object> elementConverter, ContainerAccumulator<?> containerAccumulator, boolean dirtyTracking, ViewTypeObjectBuilderTemplate<Object[]> template,
                                                     ViewTypeObjectBuilderTemplate<Object[]> indexTemplate, boolean hasSelectOrSubselectFetchedAttributes, TupleTransformerFactory subviewTupleTransformerFactory, TupleTransformerFactory indexSubviewTupleTransformerFactory, BasicUserTypeStringSupport<?> valueBasicTypeSupport, BasicUserTypeStringSupport<?> indexBasicTypeSupport) {
        this.startIndex = startIndex;
        this.mapping = mapping;
        this.attributePath = attributePath;
        this.multisetResultAlias = multisetResultAlias;
        this.elementConverter = elementConverter;
        this.containerAccumulator = (ContainerAccumulator<Object>) containerAccumulator;
        this.dirtyTracking = dirtyTracking;
        this.template = template;
        this.indexTemplate = indexTemplate;
        this.hasSelectOrSubselectFetchedAttributes = hasSelectOrSubselectFetchedAttributes;
        this.indexSubviewTupleTransformerFactory = indexSubviewTupleTransformerFactory;
        this.valueBasicTypeSupport = valueBasicTypeSupport;
        this.indexBasicTypeSupport = indexBasicTypeSupport;
        List<BasicUserTypeStringSupport<Object>> fieldConverters = new ArrayList<>();
        TupleElementMapper[] mappers;
        if (template == null) {
            fieldConverters.add((BasicUserTypeStringSupport<Object>) valueBasicTypeSupport);
        } else {
            mappers = template.getMappers();
            for (int i = 0; i < mappers.length; i++) {
                fieldConverters.add(mappers[i].getBasicTypeStringSupport());
            }
        }
        if (indexTemplate == null) {
            if (indexBasicTypeSupport != null) {
                fieldConverters.add((BasicUserTypeStringSupport<Object>) indexBasicTypeSupport);
            }
        } else {
            mappers = indexTemplate.getMappers();
            for (int i = 0; i < mappers.length; i++) {
                fieldConverters.add(mappers[i].getBasicTypeStringSupport());
            }
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
            return NullTupleTransformer.forMultiset(startIndex);
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
        TupleTransformator tupleTransformator = template == null ? null : template.getTupleTransformatorFactory().create(parameterHolder, optionalParameters, entityViewConfiguration);
        TupleTransformer subviewTupleTransformer = subviewTupleTransformerFactory == null ? null : subviewTupleTransformerFactory.create(parameterHolder, optionalParameters, entityViewConfiguration);
        TupleTransformer indexSubviewTupleTransformer = indexSubviewTupleTransformerFactory == null ? null : indexSubviewTupleTransformerFactory.create(parameterHolder, optionalParameters, entityViewConfiguration);
        return new MultisetTupleTransformer(startIndex, hasSelectOrSubselectFetchedAttributes, tupleTransformator, subviewTupleTransformer, indexSubviewTupleTransformer, indexBasicTypeSupport == null ? -1 : fieldConverters.length - 1, fieldConverters, elementConverter, containerAccumulator, dirtyTracking);
    }

}
