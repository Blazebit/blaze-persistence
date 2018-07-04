/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConstrainedTupleElementMapper implements TupleElementMapper {

    private final Map.Entry<String, TupleElementMapper>[] mappers;
    private final Map.Entry<String, TupleElementMapper>[] subqueryMappers;
    private final String alias;

    @SuppressWarnings("unchecked")
    private ConstrainedTupleElementMapper(List<Map.Entry<String, TupleElementMapper>> mappers, List<Map.Entry<String, TupleElementMapper>> subqueryMappers, String alias) {
        this.mappers = mappers.toArray(new Map.Entry[mappers.size()]);
        this.subqueryMappers = subqueryMappers.toArray(new Map.Entry[subqueryMappers.size()]);
        this.alias = alias;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        StringBuilder sb = new StringBuilder();
        StringBuilderSelectBuilder selectBuilder = new StringBuilderSelectBuilder(sb);

        sb.append("CASE");
        for (Map.Entry<String, TupleElementMapper> entry : mappers) {
            if (entry.getKey() != null) {
                sb.append(" WHEN ");
                sb.append(entry.getKey());
                sb.append(" THEN ");
            } else {
                sb.append(" ELSE ");
            }
            entry.getValue().applyMapping(selectBuilder, parameterHolder, optionalParameters, embeddingViewJpqlMacro);
        }
        sb.append(" END");

        if (subqueryMappers.length == 0) {
            if (alias != null) {
                queryBuilder.select(sb.toString(), alias);
            } else {
                queryBuilder.select(sb.toString());
            }
        } else {
            MultipleSubqueryInitiator<?> initiator;
            if (alias != null) {
                initiator = queryBuilder.selectSubqueries(sb.toString(), alias);
            } else {
                initiator = queryBuilder.selectSubqueries(sb.toString());
            }

            for (Map.Entry<String, TupleElementMapper> entry : subqueryMappers) {
                selectBuilder.setInitiator(initiator.with(entry.getKey()));
                entry.getValue().applyMapping(selectBuilder, parameterHolder, optionalParameters, embeddingViewJpqlMacro);
            }

            initiator.end();
        }
    }

    public static void addMappers(List<TupleElementMapper> mappingList, List<String> parameterMappingList, TupleTransformatorFactory tupleTransformatorFactory, List<Map.Entry<String, TupleElementMapperBuilder>> builders) {
        List<List<Map.Entry<String, TupleElementMapper>>> subtypeMappersPerAttribute = new ArrayList<>();
        boolean first = true;

        // Transpose the subtype grouped attribute lists to attribute grouped subtype lists
        for (Map.Entry<String, TupleElementMapperBuilder> builderEntry : builders) {
            String constraint = builderEntry.getKey();
            TupleElementMapperBuilder mapperBuilder = builderEntry.getValue();

            // NOTE: we assume that constrained attributes have the same attribute types so we only use the transformators of the first mapper builder
            if (first) {
                tupleTransformatorFactory.add(mapperBuilder.getTupleTransformatorFactory());
                first = false;
            }

            int attributeIndex = 0;
            for (TupleElementMapper mapper : mapperBuilder.getMappers()) {
                List<Map.Entry<String, TupleElementMapper>> list = subtypeMappersPerAttribute.size() > attributeIndex ? subtypeMappersPerAttribute.get(attributeIndex) : null;
                if (list == null) {
                    list = new ArrayList<>();
                    subtypeMappersPerAttribute.add(attributeIndex, list);
                }

                list.add(new AbstractMap.SimpleEntry<>(constraint, mapper));
                attributeIndex++;
            }
        }

        // Split up subquery mappers from the rest since they need special handling
        for (List<Map.Entry<String, TupleElementMapper>> attributeEntry : subtypeMappersPerAttribute) {
            List<Map.Entry<String, TupleElementMapper>> mappers = new ArrayList<>();
            List<Map.Entry<String, TupleElementMapper>> subqueryMappers = new ArrayList<>();

            for (Map.Entry<String, TupleElementMapper> subtypeEntry : attributeEntry) {
                String constraint = subtypeEntry.getKey();
                TupleElementMapper mapper = subtypeEntry.getValue();
                if (mapper instanceof SubqueryTupleElementMapper) {
                    SubqueryTupleElementMapper subqueryMapper = (SubqueryTupleElementMapper) mapper;
                    String subqueryAlias = "_inheritance_subquery_" + subqueryMappers.size();
                    String subqueryExpression;
                    if (subqueryMapper.getSubqueryAlias() == null) {
                        subqueryExpression = subqueryAlias;
                    } else {
                        subqueryExpression = subqueryMapper.getSubqueryExpression().replaceAll(subqueryMapper.getSubqueryAlias(), subqueryAlias);
                    }

                    mappers.add(new AbstractMap.SimpleEntry<String, TupleElementMapper>(constraint, new ExpressionTupleElementMapper(subqueryExpression, subqueryMapper.getEmbeddingViewPath())));
                    subqueryMappers.add(new AbstractMap.SimpleEntry<>(subqueryAlias, mapper));
                } else {
                    mappers.add(new AbstractMap.SimpleEntry<>(constraint, mapper));
                }
            }

            // TODO: determine alias
            String alias = null;
            mappingList.add(new ConstrainedTupleElementMapper(mappers, subqueryMappers, alias));
            parameterMappingList.add(null);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class StringBuilderSelectBuilder implements SelectBuilder<Object> {

        private final StringBuilder sb;
        private SubqueryInitiator<Object> initiator;

        public StringBuilderSelectBuilder(StringBuilder sb) {
            this.sb = sb;
        }

        @SuppressWarnings("unchecked")
        public void setInitiator(SubqueryInitiator<?> initiator) {
            this.initiator = (SubqueryInitiator<Object>) initiator;
        }

        @Override
        public CaseWhenStarterBuilder<Object> selectCase() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CaseWhenStarterBuilder<Object> selectCase(String alias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SimpleCaseWhenStarterBuilder<Object> selectSimpleCase(String caseOperand) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SimpleCaseWhenStarterBuilder<Object> selectSimpleCase(String caseOperand, String alias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubqueryInitiator<Object> selectSubquery() {
            return initiator;
        }

        @Override
        public SubqueryInitiator<Object> selectSubquery(String alias) {
            return initiator;
        }

        @Override
        public SubqueryInitiator<Object> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
            return initiator;
        }

        @Override
        public SubqueryInitiator<Object> selectSubquery(String subqueryAlias, String expression) {
            return initiator;
        }

        @Override
        public MultipleSubqueryInitiator<Object> selectSubqueries(String expression, String selectAlias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultipleSubqueryInitiator<Object> selectSubqueries(String expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubqueryBuilder<Object> selectSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubqueryBuilder<Object> selectSubquery(String alias, FullQueryBuilder<?, ?> criteriaBuilder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubqueryBuilder<Object> selectSubquery(String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubqueryBuilder<Object> selectSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object select(String expression) {
            sb.append(expression);
            return this;
        }

        @Override
        public Object select(String expression, String alias) {
            sb.append(expression);
            return this;
        }
    }
}
