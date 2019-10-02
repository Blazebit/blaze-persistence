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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TupleElementMapperBuilder {

    private static final ExpressionTupleElementMapper NULL_MAPPER = new ExpressionTupleElementMapper("NULL", null, new String[0]);
    private final int mapperIndex;
    private final String aliasPrefix;
    private final String mappingPrefix;
    private final String idPrefix;
    private final EntityType<?> treatType;
    private final EntityMetamodel metamodel;
    private final ExpressionFactory ef;
    private final String constraint;
    private final Integer subtypeIndex;
    private final List<TupleElementMapper> mappers;
    private final List<String> parameterMappings;
    private final TupleTransformatorFactory tupleTransformatorFactory;

    public TupleElementMapperBuilder(int mapperIndex, String constraint, Integer subtypeIndex, String aliasPrefix, String mappingPrefix, String idPrefix, EntityType<?> treatType, EntityMetamodel metamodel, ExpressionFactory ef) {
        this(mapperIndex, constraint, subtypeIndex, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, new ArrayList<TupleElementMapper>(), new ArrayList<String>(), new TupleTransformatorFactory());
    }

    public TupleElementMapperBuilder(int mapperIndex, String constraint, Integer subtypeIndex, String aliasPrefix, String mappingPrefix, String idPrefix, EntityType<?> treatType, EntityMetamodel metamodel, ExpressionFactory ef, List<TupleElementMapper> mappers, List<String> parameterMappings, TupleTransformatorFactory tupleTransformatorFactory) {
        this.mapperIndex = mapperIndex;
        this.constraint = constraint;
        this.subtypeIndex = subtypeIndex;
        this.aliasPrefix = aliasPrefix;
        if (treatType != null) {
            this.mappingPrefix = "TREAT(" + mappingPrefix + " AS " + treatType.getName() + ")";
        } else {
            this.mappingPrefix = mappingPrefix;
        }
        this.idPrefix = idPrefix;
        this.treatType = treatType;
        this.metamodel = metamodel;
        this.ef = ef;
        this.mappers = mappers;
        this.parameterMappings = parameterMappings;
        this.tupleTransformatorFactory = tupleTransformatorFactory;
    }

    public String constraint() {
        return constraint;
    }

    public int mapperIndex() {
        return mapperIndex + mappers.size();
    }

    public void addMapper(TupleElementMapper mapper) {
        mappers.add(mapper);
        parameterMappings.add(null);
    }

    public void addMappers(TupleElementMapper[] mappers) {
        Collections.addAll(this.mappers, mappers);
        for (int i = 0; i < mappers.length; i++) {
            parameterMappings.add(null);
        }
    }

    public List<TupleElementMapper> getMappers() {
        return mappers;
    }

    public void addQueryParam(String paramName) {
        mappers.add(NULL_MAPPER);
        parameterMappings.add(paramName);
    }

    public String getAlias(String attributeName) {
        return getAlias(aliasPrefix, attributeName);
    }

    private String getAlias(String prefix, String attributeName) {
        if (prefix == null) {
            return attributeName.intern();
        } else {
            return (prefix + "_" + attributeName).intern();
        }
    }

    public String getAlias(Attribute<?, ?> attribute, boolean isKey) {
        return getAlias(aliasPrefix, attribute, isKey);
    }

    private String getAlias(String prefix, Attribute<?, ?> attribute, boolean isKey) {
        if (isKey) {
            prefix = prefix + "_key";
        }
        if (attribute instanceof MethodAttribute<?, ?>) {
            return getAlias(prefix, ((MethodAttribute<?, ?>) attribute).getName());
        } else {
            return getAlias(prefix, "$" + ((ParameterAttribute<?, ?>) attribute).getIndex());
        }
    }

    public String getMapping(String mapping, Class<?> expressionType) {
        return getMapping(mappingPrefix, mapping, expressionType);
    }

    private String getMapping(String prefixParts, String mapping, Class<?> expressionType) {
        if (expressionType == null) {
            return getMapping(prefixParts, mapping);
        }

        ManagedType<?> managedType = metamodel.getManagedType(expressionType);
        Set<SingularAttribute<?, ?>> idAttributes;
        if (managedType == null || !JpaMetamodelUtils.isIdentifiable(managedType) || (idAttributes = JpaMetamodelUtils.getIdAttributes((IdentifiableType<?>) managedType)).size() > 1) {
            return getMapping(prefixParts, mapping);
        }

        javax.persistence.metamodel.SingularAttribute<?, ?> idAttr = idAttributes.iterator().next();
        if (mapping.isEmpty()) {
            return getMapping(prefixParts, idAttr.getName());
        } else {
            return getMapping(prefixParts, mapping + '.' + idAttr.getName());
        }
    }

    public String getMapping(String mapping) {
        return getMapping(mappingPrefix, mapping);
    }

    public String getMappingWithSkipAlias(String mapping, String skippedAlias) {
        StringBuilder sb = new StringBuilder();
        applyMapping(sb, mappingPrefix, mapping, skippedAlias);
        return sb.toString().intern();
    }

    private String getMapping(String prefixParts, String mapping) {
        StringBuilder sb = new StringBuilder();
        applyMapping(sb, prefixParts, mapping, null);
        return sb.toString().intern();
    }

    private void applyMapping(StringBuilder sb, String prefixParts, String mapping, String skippedAlias) {
        if (mapping.isEmpty()) {
            if (prefixParts != null && !prefixParts.isEmpty()) {
                sb.append(AbstractAttribute.stripThisFromMapping(prefixParts));
            }

            return;
        }
        if (prefixParts != null && !prefixParts.isEmpty()) {
            Expression expr = ef.createSimpleExpression(mapping, false, false, true);
            EmbeddingViewJpqlMacro embeddingViewJpqlMacro = (EmbeddingViewJpqlMacro) ef.getDefaultMacroConfiguration().get("EMBEDDING_VIEW").getState()[0];
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(Collections.singletonList(prefixParts), embeddingViewJpqlMacro.getEmbeddingViewPath(), CorrelatedSubqueryEmbeddingViewJpqlMacro.CORRELATION_EMBEDDING_VIEW_ALIAS, skippedAlias);
            generator.setQueryBuffer(sb);
            expr.accept(generator);
        } else {
            sb.append(mapping);
        }
    }

    public String getMapping(MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(mappingPrefix, mappingAttribute);
    }

    private String getMapping(String prefixParts, MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(prefixParts, AbstractAttribute.stripThisFromMapping(mappingAttribute.getMapping()));
    }

    public String getIdMapping(MappingAttribute<?, ?> mappingAttribute, boolean isKey) {
        return getMapping(idPrefix, mappingAttribute, isKey);
    }

    public String getMapping(MappingAttribute<?, ?> mappingAttribute, boolean isKey) {
        return getMapping(mappingPrefix, mappingAttribute, isKey);
    }

    private String getMapping(String prefixParts, MappingAttribute<?, ?> mappingAttribute, boolean isKey) {
        StringBuilder sb = new StringBuilder();
        String mapping = AbstractAttribute.stripThisFromMapping(mappingAttribute.getMapping());
        if (isKey) {
            sb.append("KEY(");
        }

        applyMapping(sb, prefixParts, mapping, null);

        if (isKey) {
            sb.append(')');
        }
        return sb.toString().intern();
    }

    public String getJoinCorrelationAttributePath(String attributePath) {
        if (subtypeIndex == null) {
            return attributePath;
        } else {
            return attributePath + '_' + subtypeIndex;
        }
    }

    public TupleTransformatorFactory getTupleTransformatorFactory() {
        return tupleTransformatorFactory;
    }

    public void setTupleListTransformer(TupleListTransformer tupleListTransformer) {
        tupleTransformatorFactory.add(tupleListTransformer);
    }

    public void setTupleListTransformerFactory(TupleListTransformerFactory tupleListTransformerFactory) {
        tupleTransformatorFactory.add(tupleListTransformerFactory);
    }

    public void addTupleTransformerFactory(TupleTransformerFactory tupleTransformerFactory) {
        tupleTransformatorFactory.add(tupleTransformerFactory);
    }

    public void addTupleTransformatorFactory(TupleTransformatorFactory tupleTransformatorFactory) {
        this.tupleTransformatorFactory.add(tupleTransformatorFactory);
    }
}
