/*
 * Copyright 2014 - 2021 Blazebit.
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
public class SingularMultisetTupleListTransformerFactory implements TupleListTransformerFactory {

    private final int startIndex;
    private final String mapping;
    private final String attributePath;
    private final String multisetResultAlias;
    private final BasicUserTypeStringSupport<Object>[] fieldConverters;
    private final TypeConverter<Object, Object> elementConverter;
    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final TupleTransformerFactory subviewTupleTransformerFactory;
    private final boolean hasSelectOrSubselectFetchedAttributes;

    public SingularMultisetTupleListTransformerFactory(int startIndex, String mapping, String attributePath, String multisetResultAlias, TypeConverter<Object, Object> elementConverter, ViewTypeObjectBuilderTemplate<Object[]> template, boolean hasSelectOrSubselectFetchedAttributes, TupleTransformerFactory subviewTupleTransformerFactory) {
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
    public int getConsumableIndex() {
        return startIndex;
    }

    @Override
    public TupleListTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        if (mapping != null) {
            if (parameterHolder instanceof FullQueryBuilder<?, ?>) {
                FullQueryBuilder<?, ?> queryBuilder = (FullQueryBuilder<?, ?>) parameterHolder;
                if (hasSelectOrSubselectFetchedAttributes) {
                    queryBuilder = queryBuilder.copy(Object[].class);
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
        return new SingularMultisetTupleListTransformer(startIndex, hasSelectOrSubselectFetchedAttributes, tupleTransformator, subviewTupleTransformer, fieldConverters, elementConverter);
    }

}
