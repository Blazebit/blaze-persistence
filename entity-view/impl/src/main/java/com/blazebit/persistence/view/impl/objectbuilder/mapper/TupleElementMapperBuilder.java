/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.impl.ExpressionUtils;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.objectbuilder.SecondaryMapper;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TupleElementMapperBuilder implements ServiceProvider {

    private static final ExpressionTupleElementMapper NULL_MAPPER = new ExpressionTupleElementMapper(null, "NULL", null, null, null, new String[0]);
    private final int mapperIndex;
    private final String aliasPrefix;
    private final String mappingPrefix;
    private final String idPrefix;
    private final Map<String, Type<?>> rootTypes;
    private final EntityMetamodel metamodel;
    private final ExpressionFactory ef;
    private final String constraint;
    private final Integer subtypeIndex;
    private final List<TupleElementMapper> mappers;
    private final List<String> parameterMappings;
    private final List<SecondaryMapper> secondaryMapper;
    private final TupleTransformatorFactory tupleTransformatorFactory;

    public TupleElementMapperBuilder(int mapperIndex, String constraint, Integer subtypeIndex, String aliasPrefix, String mappingPrefix, String idPrefix, EntityType<?> treatType, EntityMetamodel metamodel, ExpressionFactory ef, Map<String, Type<?>> rootTypes) {
        this(mapperIndex, constraint, subtypeIndex, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, new ArrayList<TupleElementMapper>(), new ArrayList<String>(), new ArrayList<SecondaryMapper>(), new TupleTransformatorFactory(), rootTypes);
    }

    public TupleElementMapperBuilder(int mapperIndex, String constraint, Integer subtypeIndex, String aliasPrefix, String mappingPrefix, String idPrefix, EntityType<?> treatType, EntityMetamodel metamodel, ExpressionFactory ef,
                                     List<TupleElementMapper> mappers, List<String> parameterMappings, List<SecondaryMapper> secondaryMapper, TupleTransformatorFactory tupleTransformatorFactory, Map<String, Type<?>> rootTypes) {
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
        this.rootTypes = rootTypes;
        this.metamodel = metamodel;
        this.ef = ef;
        this.mappers = mappers;
        this.parameterMappings = parameterMappings;
        this.secondaryMapper = secondaryMapper;
        this.tupleTransformatorFactory = tupleTransformatorFactory;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        if (serviceClass == ExpressionFactory.class) {
            return (T) ef;
        }
        return null;
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

    public void addSecondaryMapper(SecondaryMapper mapper) {
        secondaryMapper.add(mapper);
    }

    public void addSecondaryMappers(SecondaryMapper[] mappers) {
        Collections.addAll(this.secondaryMapper, mappers);
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

    public String getMapping(Expression expression, Class<?> expressionType) {
        if (expressionType == null) {
            return getMapping(expression);
        }

        ManagedType<?> managedType = metamodel.getManagedType(expressionType);
        Set<SingularAttribute<?, ?>> idAttributes;
        if (managedType == null || !JpaMetamodelUtils.isIdentifiable(managedType) || (idAttributes = JpaMetamodelUtils.getIdAttributes((IdentifiableType<?>) managedType)).size() > 1) {
            return getMapping(expression);
        }

        javax.persistence.metamodel.SingularAttribute<?, ?> idAttr = idAttributes.iterator().next();
        if (ExpressionUtils.isEmptyOrThis(expression)) {
            return getMapping(mappingPrefix, idAttr.getName());
        } else {
            return getMapping(expression) + '.' + idAttr.getName();
        }
    }

    public String getMapping() {
        return mappingPrefix;
    }

    public String getMapping(String mapping) {
        return getMapping(mappingPrefix, mapping);
    }

    public String getMapping(SubqueryAttribute<?, ?> subqueryAttribute) {
        StringBuilder sb = new StringBuilder();
        subqueryAttribute.renderSubqueryExpression(mappingPrefix, this, sb);
        return sb.toString().intern();
    }

    public String getMapping(Expression expression) {
        StringBuilder sb = new StringBuilder();
        if (mappingPrefix != null && !mappingPrefix.isEmpty()) {
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(ef, mappingPrefix, null, null, rootTypes.keySet(), true, false);
            generator.setQueryBuffer(sb);
            expression.accept(generator);
        } else {
            sb.append(expression);
        }
        return sb.toString().intern();
    }

    private String getMapping(String prefixParts, String mapping) {
        StringBuilder sb = new StringBuilder();
        applyMapping(sb, prefixParts, mapping);
        return sb.toString().intern();
    }

    private void applyMapping(StringBuilder sb, String prefixParts, String mapping) {
        if (mapping.isEmpty()) {
            if (prefixParts != null && !prefixParts.isEmpty()) {
                sb.append(AbstractAttribute.stripThisFromMapping(prefixParts));
            }

            return;
        }
        if (prefixParts != null && !prefixParts.isEmpty()) {
            Expression expr = ef.createSimpleExpression(mapping, false, false, true);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(ef, prefixParts, null, null, rootTypes.keySet(), true, false);
            generator.setQueryBuffer(sb);
            expr.accept(generator);
        } else {
            sb.append(mapping);
        }
    }

    public String getIdMapping(MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(idPrefix, mappingAttribute);
    }

    public String getMapping(MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(mappingPrefix, mappingAttribute);
    }

    public String getKeyMapping(MapAttribute<?, ?, ?> mappingAttribute) {
        return getKeyMapping(getMapping((MappingAttribute<?, ?>) mappingAttribute), mappingAttribute);
    }

    public String getKeyMapping(String prefix, MapAttribute<?, ?, ?> mappingAttribute) {
        StringBuilder sb = new StringBuilder();
        mappingAttribute.renderKeyMapping(prefix, this, sb);
        return sb.toString().intern();
    }

    public String getIndexMapping(ListAttribute<?, ?> mappingAttribute) {
        return getIndexMapping(getMapping((MappingAttribute<?, ?>) mappingAttribute), mappingAttribute);
    }

    public String getIndexMapping(String prefix, ListAttribute<?, ?> mappingAttribute) {
        StringBuilder sb = new StringBuilder();
        mappingAttribute.renderIndexMapping(prefix, this, sb);
        return sb.toString().intern();
    }

    private String getMapping(String prefixParts, MappingAttribute<?, ?> mappingAttribute) {
        StringBuilder sb = new StringBuilder();
        mappingAttribute.renderMapping(prefixParts, this, sb);
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
