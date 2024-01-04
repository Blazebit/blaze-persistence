/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.util.LiteralFunctionTypeConverter;
import com.blazebit.persistence.parser.util.TypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.AttributeAccessor;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterManager {

    private static final String PREFIX = "param_";
    private static final Map<TypeConverter<?>, String> TEMPORAL_CONVERTER_LITERAL_FUNCTIONS;

    static {
        Map<TypeConverter<?>, String> literalFunctions = new HashMap<>();
        literalFunctions.put(TypeUtils.TIME_CONVERTER, "literal_time");
        literalFunctions.put(TypeUtils.DATE_CONVERTER, "literal_date");
        literalFunctions.put(TypeUtils.TIMESTAMP_CONVERTER, "literal_timestamp");
        literalFunctions.put(TypeUtils.DATE_TIMESTAMP_CONVERTER, "literal_util_date");
        literalFunctions.put(TypeUtils.CALENDAR_CONVERTER, "literal_calendar");
        TEMPORAL_CONVERTER_LITERAL_FUNCTIONS = literalFunctions;
    }

    private int counter;
    private final JpaProvider jpaProvider;
    private final EntityMetamodel entityMetamodel;
    private final Map<String, ParameterImpl<?>> parameters = new TreeMap<>();
    private final Map<String, String> valuesParameters = new TreeMap<>();
    private final ParameterRegistrationVisitor parameterRegistrationVisitor;
    private final ParameterUnregistrationVisitor parameterUnregistrationVisitor;
    private Map<javax.persistence.criteria.ParameterExpression<?>, String> criteriaNameMapping;
    private int positionalOffset = -1; // Records the last positional parameter index that was used

    public ParameterManager(JpaProvider jpaProvider, EntityMetamodel entityMetamodel) {
        this.jpaProvider = jpaProvider;
        this.entityMetamodel = entityMetamodel;
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(this);
        this.parameterUnregistrationVisitor = new ParameterUnregistrationVisitor(this);
    }

    public ParameterRegistrationVisitor getParameterRegistrationVisitor() {
        return parameterRegistrationVisitor;
    }

    public void collectParameterRegistrations(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, ClauseType clauseType) {
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterRegistrationVisitor.getQueryBuilder();
        ClauseType oldClauseType = parameterRegistrationVisitor.getClauseType();
        try {
            parameterRegistrationVisitor.setClauseType(clauseType);
            parameterRegistrationVisitor.setQueryBuilder(queryBuilder);
            queryBuilder.applyVisitor(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(oldClauseType);
            parameterRegistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    public void collectParameterRegistrations(Expression expression, ClauseType clauseType, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterRegistrationVisitor.getQueryBuilder();
        ClauseType oldClauseType = parameterRegistrationVisitor.getClauseType();
        try {
            parameterRegistrationVisitor.setClauseType(clauseType);
            parameterRegistrationVisitor.setQueryBuilder(queryBuilder);
            expression.accept(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(oldClauseType);
            parameterRegistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    public void collectParameterUnregistrations(Expression expression, ClauseType clauseType, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterUnregistrationVisitor.getQueryBuilder();
        ClauseType oldClauseType = parameterUnregistrationVisitor.getClauseType();
        try {
            parameterUnregistrationVisitor.setClauseType(clauseType);
            parameterUnregistrationVisitor.setQueryBuilder(queryBuilder);
            expression.accept(parameterUnregistrationVisitor);
        } finally {
            parameterUnregistrationVisitor.setClauseType(oldClauseType);
            parameterUnregistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    Map<String, String> copyFrom(ParameterManager parameterManager) {
        Map<String, String> parameterMapping = new HashMap<>(parameterManager.parameters.size());
        for (Map.Entry<String, ParameterImpl<?>> entry : parameterManager.parameters.entrySet()) {
            ParameterImpl<Object> param = (ParameterImpl<Object>) entry.getValue();
            Object paramValue = null;
            if (param.isValueSet()) {
                if (param.getParameterValue() == null) {
                    paramValue = param.getValue();
                } else {
                    paramValue = param.getParameterValue().copy();
                }
            }
            String oldParameterName = entry.getKey();
            String newParameterName;
            if (Character.isDigit(oldParameterName.charAt(0))) {
                this.positionalOffset++;
                newParameterName = Integer.toString(this.positionalOffset);
            } else if (param.isImplicit() && !(paramValue instanceof ValuesParameterWrapper)) {
                newParameterName = PREFIX + counter++;
            } else {
                ParameterImpl<Object> existingParameter = (ParameterImpl<Object>) parameters.get(oldParameterName);
                newParameterName = oldParameterName;
                if (existingParameter != null) {
                    if (existingParameter.getParameterType() != param.getParameterType()) {
                        throw new IllegalStateException("Can't apply parameters! Parameter '" + oldParameterName + "' with type '" + param.getParameterType() + "' is incompatible with existing type: " + existingParameter.getParameterType());
                    }
                    if (existingParameter.isCollectionValued() != param.isCollectionValued()) {
                        throw new IllegalStateException("Can't apply parameters! Parameter '" + oldParameterName + "' is collection valued in one query, but not the other!");
                    }
                    if (existingParameter.getTransformer() != param.getTransformer()) {
                        throw new IllegalStateException("Can't apply parameters! Parameter '" + oldParameterName + "' has a tranfsformer in one query, but not the other!");
                    }
                    if (param.isValueSet()) {
                        existingParameter.setValue(paramValue);
                    }
                    continue;
                }
            }

            parameterMapping.put(oldParameterName, newParameterName);
            addParameterMapping(newParameterName, paramValue, param.isImplicit());
        }

        for (Map.Entry<String, String> entry : parameterManager.valuesParameters.entrySet()) {
            if (this.valuesParameters.put(entry.getKey(), entry.getValue()) != null) {
                throw new IllegalArgumentException("Can't copy value parameters because of a name clash for value parameter with name: " + entry.getKey());
            }
        }

        return parameterMapping;
    }

    Set<String> getParameterListNames(Query q) {
        return getParameterListNames(q, null);
    }

    Set<String> getParameterListNames(Query q, String skippedParameterPrefix) {
        Set<String> parameterListNames = new HashSet<String>();
        collectParameterListNames(q, parameterListNames, skippedParameterPrefix);
        return parameterListNames;
    }

    void collectParameterListNames(Query q, Set<String> parameterListNames) {
        collectParameterListNames(q, parameterListNames, null);
    }

    void collectParameterListNames(Query q, Set<String> parameterListNames, String skippedParameterPrefix) {
        for (Parameter<?> p: q.getParameters()) {
            String parameterName = p.getName();
            // In case of positional parameters, we convert the position to a string and look it up instead
            if (parameterName == null) {
                if (criteriaNameMapping != null && p instanceof javax.persistence.criteria.ParameterExpression<?>) {
                    parameterName = criteriaNameMapping.get(p);
                } else {
                    parameterName = p.getPosition().toString();
                }
            } else if (skippedParameterPrefix != null && parameterName.startsWith(skippedParameterPrefix)) {
                continue;
            }
            ParameterImpl<?> parameter = getParameter(parameterName);
            if (parameter != null && parameter.isCollectionValued()) {
                parameterListNames.add(parameterName);
            }
        }
    }

    void parameterizeQuery(Query q) {
        parameterizeQuery(q, null);
    }

    void parameterizeQuery(Query q, String skippedParameterPrefix) {
        Set<String> requestedValueParameters = new HashSet<String>();
        for (Parameter<?> p : q.getParameters()) {
            String parameterName = p.getName();
            // In case of positional parameters, we convert the position to a string and look it up instead
            if (parameterName == null) {
                if (criteriaNameMapping != null && p instanceof javax.persistence.criteria.ParameterExpression<?>) {
                    parameterName = criteriaNameMapping.get(p);
                } else {
                    parameterName = p.getPosition().toString();
                }
            } else if (skippedParameterPrefix != null && parameterName.startsWith(skippedParameterPrefix)) {
                continue;
            }
            ParameterImpl<?> parameter = parameters.get(parameterName);
            if (parameter == null) {
                String valuesParameter = valuesParameters.get(parameterName);
                if (valuesParameter == null) {
                    throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
                }

                // Skip binding the sub-parameter, we will do that in one go at the end
                requestedValueParameters.add(valuesParameter);
                continue;
            }

            // If a query requests the values parameter directly, it is aware of handling it
            if (parameter.getParameterValue() instanceof ValuesParameterWrapper) {
                if (parameter.getValue() != null) {
                    q.setParameter(parameterName, parameter.getValue());
                }
            } else {
                parameter.bind(q);
            }
        }

        for (String parameterName : requestedValueParameters) {
            ParameterImpl<?> parameter = parameters.get(parameterName);
            parameter.bind(q);
        }
    }

    public ParameterImpl<?> getParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        return parameters.get(parameterName);
    }

    public Collection<Parameter<?>> getParameters() {
        return (Collection<Parameter<?>>) (Collection<?>) parameters.values();
    }

    public Collection<ParameterImpl<?>> getParameterImpls() {
        return parameters.values();
    }

    public Map<javax.persistence.criteria.ParameterExpression<?>, String> getCriteriaNameMapping() {
        return criteriaNameMapping;
    }

    public Map<String, String> getValuesParameters() {
        return Collections.unmodifiableMap(valuesParameters);
    }

    public Map<String, ParameterValueTransformer> getTransformers() {
        Map<String, ParameterValueTransformer> transformers = new HashMap<>();
        for (Map.Entry<String, ParameterImpl<?>> entry : parameters.entrySet()) {
            ParameterValueTransformer transformer = entry.getValue().getTransformer();
            if (transformer != null) {
                transformers.put(entry.getKey(), transformer);
            }
        }
        return transformers;
    }

    public Map<String, ValuesParameterBinder> getValuesBinders() {
        Map<String, ValuesParameterBinder> binders = new HashMap<>();
        for (Map.Entry<String, ParameterImpl<?>> entry : parameters.entrySet()) {
            ParameterValue value = entry.getValue().getParameterValue();
            if (value instanceof ValuesParameterWrapper) {
                binders.put(entry.getKey(), ((ValuesParameterWrapper) value).getBinder());
            }
        }
        return binders;
    }

    public boolean containsParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        return parameters.containsKey(parameterName);
    }

    public boolean isParameterSet(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        return parameter != null && parameter.getValue() != null;
    }

    public Object getParameterValue(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        return parameter.getValue();
    }

    public String getLiteralParameterValue(ParameterExpression expression, boolean renderEnumAsLiteral) {
        Object value = expression.getValue();
        if (value == null) {
            value = getParameterValue(expression.getName());
        }
        return getLiteralParameterValue(value, renderEnumAsLiteral);
    }

    public String getLiteralParameterValue(Object value, boolean renderEnumAsLiteral) {
        if (value != null) {
            final TypeConverter<Object> converter = (TypeConverter<Object>) TypeUtils.getConverter(value.getClass(), entityMetamodel.getEnumTypes().keySet());
            if (converter != null) {
                if (value instanceof Enum<?>) {
                    if (renderEnumAsLiteral) {
                        return converter.toString(value);
                    }
                } else if (converter instanceof LiteralFunctionTypeConverter<?>) {
                    if (TypeUtils.isTemporalConverter(converter)) {
                        if (jpaProvider.supportsTemporalLiteral()) {
                            return converter.toString(value);
                        }
                    }
                    String functionInvocation = jpaProvider.getCustomFunctionInvocation(((LiteralFunctionTypeConverter<?>) converter).getLiteralFunctionName(), 1);
                    String literalValue = converter.toString(value);
                    StringBuilder sb = new StringBuilder(functionInvocation.length() + literalValue.length() + 5);
                    sb.append(functionInvocation);
                    TypeUtils.STRING_CONVERTER.appendTo(literalValue, sb);
                    sb.append(')');
                    return sb.toString();
                } else {
                    return converter.toString(value);
                }
            }
        }

        return null;
    }

    public ParameterExpression addParameterExpression(Object o, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        String name = addParameter(o, o instanceof Collection, clause, queryBuilder);
        return new ParameterExpression(name, o, o instanceof Collection);
    }

    private String addParameter(Object o, boolean collectionValued, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (o == null) {
            throw new NullPointerException();
        }
        String name = PREFIX + counter++;
        parameters.put(name, new ParameterImpl<>(name, collectionValued, clause, queryBuilder, o));
        return name;
    }

    public void addParameterMapping(String parameterName, Object o, boolean implicit) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        Integer position = determinePositionalOffset(parameterName);
        if (position == null) {
            parameters.put(parameterName, new ParameterImpl<>(parameterName, o instanceof Collection, implicit, o));
        } else {
            parameters.put(parameterName, new ParameterImpl<>(position, o instanceof Collection, implicit, o));
        }
    }

    public void addParameterMapping(String parameterName, Object o, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        Integer position = determinePositionalOffset(parameterName);
        if (position == null) {
            parameters.put(parameterName, new ParameterImpl<>(parameterName, o instanceof Collection, clause, queryBuilder, o));
        } else {
            parameters.put(parameterName, new ParameterImpl<>(position, o instanceof Collection, clause, queryBuilder, o));
        }
    }

    public void registerParameterName(String parameterName, boolean collectionValued, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            Integer position = determinePositionalOffset(parameterName);
            if (position == null) {
                parameters.put(parameterName, new ParameterImpl<>(parameterName, collectionValued, false, clause, queryBuilder));
            } else {
                parameters.put(parameterName, new ParameterImpl<>(position, collectionValued, false, clause, queryBuilder));
            }
        } else {
            Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> builders = parameter.getClauseTypes().get(clause);
            if (builders == null) {
                builders = Collections.newSetFromMap(new IdentityHashMap<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, Boolean>());
                parameter.getClauseTypes().put(clause, builders);
            }
            builders.add(queryBuilder);
        }
    }

    private Integer determinePositionalOffset(String parameterName) {
        if (Character.isDigit(parameterName.charAt(0))) {
            int value = Integer.parseInt(parameterName);
            positionalOffset = Math.max(value, positionalOffset);
            return value;
        }
        return null;
    }

    public void unregisterParameterName(String parameterName, ClauseType clauseType, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter != null) {
            Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> builders = parameter.getClauseTypes().get(clauseType);
            if (builders != null) {
                builders.remove(queryBuilder);
                if (builders.isEmpty()) {
                    parameter.getClauseTypes().remove(clauseType);
                    if (parameter.getClauseTypes().isEmpty()) {
                        parameters.remove(parameterName);
                    }
                }
            }
        }
    }

    public void registerValuesParameter(String parameterName, Class<?> type, String[][] parameterNames, AttributeAccessor<Object, Object>[] pathExpressions, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Can't register parameter for VALUES clause because there already exists a parameter with the name: " + parameterName);
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, false, ClauseType.JOIN, queryBuilder, new ValuesParameterWrapper(type, parameterNames, pathExpressions)));
        for (int i = 0; i < parameterNames.length; i++) {
            for (int j = 0; j < parameterNames[i].length; j++) {
                valuesParameters.put(parameterNames[i][j], parameterName);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Object parameterValue) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(parameterValue);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Calendar value, TemporalType temporalType) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(new TemporalCalendarParameterWrapper(temporalType, value));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Date value, TemporalType temporalType) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(new TemporalDateParameterWrapper(temporalType, value));
    }

    @SuppressWarnings({ "unchecked" })
    @Deprecated
    public void setParameterType(String parameterName, Class<?> type) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        // TODO: maybe we should do some checks here?
        parameter.setParameterType((Class) type);
    }

    public <X> void registerCriteriaParameter(String parameterName, javax.persistence.criteria.ParameterExpression<X> parameterExpression) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<X> parameter = (ParameterImpl<X>) parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setCriteriaParameter(parameterExpression);
        if (criteriaNameMapping == null) {
            criteriaNameMapping = new HashMap<>();
        }
        criteriaNameMapping.put(parameterExpression, parameterName);
    }

    public int getPositionalOffset() {
        if (positionalOffset == -1) {
            return -1;
        }
        return positionalOffset + 1;
    }

    // TODO: needs equals-hashCode implementation

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static final class ParameterImpl<T> implements ExtendedParameter<T> {

        private final String name;
        private final Integer position;
        private final boolean collectionValued;
        private final boolean implicit;
        private final Map<ClauseType, Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>>> clauseTypes;
        private boolean usedInImplicitGroupBy;
        private Class<T> parameterType;
        private javax.persistence.criteria.ParameterExpression<T> criteriaParameter;
        private T value;
        private boolean valueSet;
        private ParameterValueTransformer transformer;

        public ParameterImpl(String name, boolean collectionValued, boolean implicit, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
            this.name = name;
            this.position = null;
            this.collectionValued = collectionValued;
            this.implicit = implicit;
            this.clauseTypes = new EnumMap<>(ClauseType.class);
            if (clause != null) {
                Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> builders = Collections.newSetFromMap(new IdentityHashMap<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, Boolean>());
                builders.add(queryBuilder);
                this.clauseTypes.put(clause, builders);
            }
        }

        public ParameterImpl(String name, boolean collectionValued, boolean implicit, T value) {
            this(name, collectionValued, implicit, null, null);
            setValue(value);
        }

        public ParameterImpl(String name, boolean collectionValued, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, T value) {
            this(name, collectionValued, true, clause, queryBuilder);
            setValue(value);
        }

        public ParameterImpl(int position, boolean collectionValued, boolean implicit, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
            this.name = null;
            this.position = position;
            this.collectionValued = collectionValued;
            this.implicit = implicit;
            this.clauseTypes = new EnumMap<>(ClauseType.class);
            if (clause != null) {
                Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> builders = Collections.newSetFromMap(new IdentityHashMap<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, Boolean>());
                builders.add(queryBuilder);
                this.clauseTypes.put(clause, builders);
            }
        }

        public ParameterImpl(int position, boolean collectionValued, boolean implicit, T value) {
            this(position, collectionValued, implicit, null, null);
            setValue(value);
        }

        public ParameterImpl(int position, boolean collectionValued, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, T value) {
            this(position, collectionValued, true, clause, queryBuilder);
            setValue(value);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getPosition() {
            return position;
        }

        @Override
        public boolean isCollectionValued() {
            return collectionValued;
        }

        public Map<ClauseType, Set<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>>> getClauseTypes() {
            return clauseTypes;
        }

        @Override
        public Class<T> getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class<T> parameterType) {
            this.parameterType = parameterType;
        }

        public javax.persistence.criteria.ParameterExpression<T> getCriteriaParameter() {
            return criteriaParameter;
        }

        public void setCriteriaParameter(javax.persistence.criteria.ParameterExpression<T> criteriaParameter) {
            this.criteriaParameter = criteriaParameter;
            this.parameterType = criteriaParameter.getParameterType();
        }

        public boolean isUsedInGroupBy() {
            return usedInImplicitGroupBy || clauseTypes.containsKey(ClauseType.GROUP_BY);
        }

        public boolean isUsedInImplicitGroupBy() {
            return usedInImplicitGroupBy;
        }

        public void setUsedInImplicitGroupBy(boolean usedInImplicitGroupBy) {
            this.usedInImplicitGroupBy = usedInImplicitGroupBy;
        }

        public ParameterValue getParameterValue() {
            if (value instanceof ParameterValue) {
                return (ParameterValue) value;
            }
            return null;
        }

        public boolean isImplicit() {
            return implicit;
        }

        public boolean isValueSet() {
            return valueSet;
        }

        @SuppressWarnings("unchecked")
        public T getValue() {
            if (value instanceof ParameterValue) {
                return (T) ((ParameterValue) value).getValue();
            }

            return value;
        }

        @SuppressWarnings({ "unchecked" })
        public void setValue(T value) {
            this.valueSet = true;
            if (transformer != null) {
                value = transform(value);
            }
            if (this.value instanceof ParameterValue) {
                this.value = (T) ((ParameterValue) this.value).withValue(value);
            } else {
                this.value = value;
                if (value != null) {
                    if (value instanceof ParameterValue) {
                        parameterType = (Class<T>) ((ParameterValue) value).getValueType();
                    } else {
                        parameterType = (Class<T>) value.getClass();
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private T transform(T value) {
            if (value instanceof Collection<?>) {
                Collection<?> values = (Collection<?>) value;
                List<Object> list = new ArrayList<>(values.size());
                for (Object o : values) {
                    list.add(transformer.transform(o));
                }
                return (T) list;
            } else {
                return (T) transformer.transform(value);
            }
        }

        public ParameterValueTransformer getTransformer() {
            return transformer;
        }

        public void setTransformer(ParameterValueTransformer transformer) {
            if (this.transformer == null) {
                this.transformer = transformer;
                if (valueSet) {
                    this.value = transform(value);
                }
            } else if (!this.transformer.equals(transformer)) {
                throw new IllegalStateException("Tried to set parameter value transformer [" + transformer + "] although a transformer [" + this.transformer + "] is already set for parameter: " + name);
            }
        }

        public void bind(Query q) {
            if (valueSet) {
                if (value instanceof ParameterValue) {
                    if (name == null) {
                        ((ParameterValue) value).bind(q, position);
                    } else {
                        ((ParameterValue) value).bind(q, name);
                    }
                } else {
                    if (name == null) {
                        q.setParameter(position, value);
                    } else {
                        q.setParameter(name, value);
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Parameter<?>)) {
                return false;
            }

            Parameter<?> parameter = (Parameter<?>) o;

            if (name != null ? !name.equals(parameter.getName()) : parameter.getName() != null) {
                return false;
            }
            return position != null ? position.equals(parameter.getPosition()) : parameter.getPosition() == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (position != null ? position.hashCode() : 0);
            return result;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static interface ParameterValue {

        public ParameterValue copy();

        public Class<?> getValueType();

        public Object getValue();

        public ParameterValue withValue(Object value);

        public void bind(Query query, String name);

        public void bind(Query query, int position);

    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static final class TemporalCalendarParameterWrapper implements ParameterValue {

        private final TemporalType type;
        private Calendar value;

        public TemporalCalendarParameterWrapper(TemporalType type, Calendar value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public ParameterValue copy() {
            Calendar newValue = null;
            if (value != null) {
                newValue = (Calendar) value.clone();
            }
            return new TemporalCalendarParameterWrapper(type, newValue);
        }

        @Override
        public Calendar getValue() {
            return value;
        }

        @Override
        public ParameterValue withValue(Object value) {
            this.value = (Calendar) value;
            return this;
        }

        @Override
        public Class<?> getValueType() {
            return Calendar.class;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, type);
        }

        @Override
        public void bind(Query query, int position) {
            query.setParameter(position, value, type);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static final class TemporalDateParameterWrapper implements ParameterValue {

        private final TemporalType type;
        private Date value;

        public TemporalDateParameterWrapper(TemporalType type, Date value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public ParameterValue copy() {
            Date newValue = null;
            if (value != null) {
                newValue = (Date) value.clone();
            }
            return new TemporalDateParameterWrapper(type, newValue);
        }

        @Override
        public Date getValue() {
            return value;
        }

        @Override
        public ParameterValue withValue(Object value) {
            this.value = (Date) value;
            return this;
        }

        @Override
        public Class<?> getValueType() {
            return Date.class;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, type);
        }

        @Override
        public void bind(Query query, int position) {
            query.setParameter(position, value, type);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static final class ValuesParameterWrapper implements ParameterValue {

        private final Class<?> type;
        private final ValuesParameterBinder binder;
        private Collection<Object> value;

        public ValuesParameterWrapper(Class<?> type, String[][] parameterNames, AttributeAccessor<Object, Object>[] pathExpressions) {
            this.type = type;
            this.binder = new ValuesParameterBinder(parameterNames, pathExpressions);
        }

        private ValuesParameterWrapper(Class<?> type, ValuesParameterBinder binder) {
            this.type = type;
            this.binder = binder;
        }

        @Override
        public ParameterValue copy() {
            Collection newValue = null;
            if (value != null) {
                newValue = new ArrayList(value);
            }
            return new ValuesParameterWrapper(type, binder).withValue(newValue);
        }

        public ValuesParameterBinder getBinder() {
            return binder;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Class<?> getValueType() {
            return Collection.class;
        }

        @Override
        public ParameterValue withValue(Object value) {
            if (value == null) {
                throw new IllegalArgumentException("null not allowed for VALUES parameter!");
            }
            if (!(value instanceof Collection<?>)) {
                throw new IllegalArgumentException("Value for VALUES parameter must be a collection! Unsupported type: " + value.getClass());
            }

            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) value;
            if (collection.size() > binder.size()) {
                throw new IllegalArgumentException("The size of the collection must be lower or equal to the specified size for the VALUES clause.");
            }
            // NOTE: be careful when changing this, there might be code that depends on this not being copied for performance
            this.value = collection;
            return this;
        }

        @Override
        public void bind(Query query, String name) {
            if (value == null) {
                throw new IllegalArgumentException("No values are bound for parameter with name: " + name);
            }

            binder.bind(query, value);
        }

        @Override
        public void bind(Query query, int position) {
            if (value == null) {
                throw new IllegalArgumentException("No values are bound for parameter with position: " + position);
            }

            binder.bind(query, value);
        }
    }
}
