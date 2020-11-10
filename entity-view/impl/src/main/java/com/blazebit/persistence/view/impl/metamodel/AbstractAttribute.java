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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.parser.AliasReplacementVisitor;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.NumericType;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.impl.CollectionJoinMappingGathererExpressionVisitor;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor.TargetType;
import com.blazebit.persistence.view.impl.StaticCorrelationProvider;
import com.blazebit.persistence.view.impl.StaticPathCorrelationProvider;
import com.blazebit.persistence.view.impl.SubqueryProviderHelper;
import com.blazebit.persistence.view.impl.TypeExtractingCorrelationBuilder;
import com.blazebit.persistence.view.impl.UpdatableExpressionVisitor;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.ListCollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.ListFactory;
import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.OrderedCollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.OrderedMapInstantiator;
import com.blazebit.persistence.view.impl.collection.OrderedSetCollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.PluralObjectFactory;
import com.blazebit.persistence.view.impl.collection.SetFactory;
import com.blazebit.persistence.view.impl.collection.SortedMapInstantiator;
import com.blazebit.persistence.view.impl.collection.SortedSetCollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.SortedSetFactory;
import com.blazebit.persistence.view.impl.collection.UnorderedMapInstantiator;
import com.blazebit.persistence.view.impl.collection.UnorderedSetCollectionInstantiator;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.NullFilteringCollectionAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.SimpleCollectionAccumulator;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.OrderByItem;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {

    protected static final String[] EMPTY = new String[0];
    private static final String THIS = "this";
    private static final Pattern PREFIX_THIS_REPLACE_PATTERN = Pattern.compile("([^a-zA-Z0-9\\.])this\\.");

    protected final ManagedViewTypeImplementor<X> declaringType;
    protected final Class<Y> javaType;
    protected final Class<?> convertedJavaType;
    protected final String mapping;
    protected final Expression mappingExpression;
    protected final String[] fetches;
    protected final FetchStrategy fetchStrategy;
    protected final int batchSize;
    protected final List<OrderByItem> orderByItems;
    protected final String limitExpression;
    protected final String offsetExpression;
    protected final SubqueryProviderFactory subqueryProviderFactory;
    protected final Class<? extends SubqueryProvider> subqueryProvider;
    protected final String subqueryExpression;
    protected final Expression subqueryResultExpression;
    protected final String subqueryAlias;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final Class<? extends CorrelationProvider> correlationProvider;
    protected final String correlationBasis;
    protected final String correlationResult;
    protected final Class<?> correlated;
    protected final String correlationKeyAlias;
    protected final String correlationExpression;
    protected final Expression correlationBasisExpression;
    protected final Expression correlationResultExpression;
    protected final MappingType mappingType;
    protected final boolean id;
    protected final javax.persistence.metamodel.Attribute<?, ?> updateMappableAttribute;
    private final List<TargetType> possibleTargetTypes;
    private final List<TargetType> possibleIndexTargetTypes;

    @SuppressWarnings("unchecked")
    public AbstractAttribute(ManagedViewTypeImplementor<X> declaringType, AttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        Class<Y> javaType = null;
        try {
            javaType = (Class<Y>) mapping.getJavaType(context, embeddableMapping);
            if (javaType == null) {
                context.addError("The attribute type is not resolvable at the " + mapping.getErrorLocation());
            }
        } catch (IllegalArgumentException ex) {
            context.addError("An error occurred while trying to resolve the attribute type at the " + mapping.getErrorLocation());
        }

        this.possibleTargetTypes = mapping.getPossibleTargetTypes(context);
        this.possibleIndexTargetTypes = mapping.getPossibleIndexTargetTypes(context);
        Integer defaultBatchSize = mapping.getDefaultBatchSize();
        int batchSize;
        if (defaultBatchSize == null || defaultBatchSize == -1) {
            batchSize = -1;
        } else if (defaultBatchSize < 1) {
            context.addError("Illegal batch fetch size lower than 1 defined at '" + mapping.getErrorLocation() + "'!");
            batchSize = Integer.MIN_VALUE;
        } else {
            batchSize = defaultBatchSize;
        }

        String limitExpression;
        String offsetExpression;
        List<OrderByItem> orderByItems;
        if (mapping.getLimitExpression() == null) {
            limitExpression = null;
            offsetExpression = null;
            orderByItems = Collections.emptyList();
        } else {
            limitExpression = mapping.getLimitExpression();
            offsetExpression = mapping.getOffsetExpression();
            if (offsetExpression == null || offsetExpression.isEmpty()) {
                offsetExpression = "0";
            }
            List<String> orderByItemExpressions = mapping.getOrderByItems();
            orderByItems = parseOrderByItems(orderByItemExpressions);
        }

        this.declaringType = declaringType;
        this.javaType = javaType;
        this.convertedJavaType = getConvertedType(declaringType.getJavaType(), mapping.getType(context, embeddableMapping).getConvertedType(), javaType);
        Annotation mappingAnnotation = mapping.getMapping();

        String mappingString = null;
        Expression mappingExpression = null;
        String[] fetches = EMPTY;
        FetchStrategy fetchStrategy = FetchStrategy.JOIN;
        SubqueryProviderFactory subqueryProviderFactory = null;
        Class<? extends SubqueryProvider> subqueryProvider = null;
        boolean id = false;
        javax.persistence.metamodel.Attribute<?, ?> updateMappableAttribute = null;
        String subqueryExpression = null;
        Expression subqueryResultExpression = null;
        String subqueryAlias = null;
        String correlationBasis = null;
        String correlationResult = null;
        Class<? extends CorrelationProvider> correlationProvider = null;
        CorrelationProviderFactory correlationProviderFactory = null;
        Class<?> correlated = null;
        String correlationKeyAlias = null;
        String correlationExpression = null;
        Expression correlationBasisExpression = null;
        Expression correlationResultExpression = null;

        if (mappingAnnotation instanceof IdMapping) {
            mappingString = ((IdMapping) mappingAnnotation).value();
            mappingExpression = createSimpleExpression(mappingString, mapping, context, ExpressionLocation.MAPPING);
            batchSize = -1;
            limitExpression = null;
            offsetExpression = null;
            orderByItems = Collections.emptyList();
            fetchStrategy = FetchStrategy.JOIN;
            id = true;
            updateMappableAttribute = getUpdateMappableAttribute(context, mappingExpression);
            this.mappingType = MappingType.BASIC;
        } else if (mappingAnnotation instanceof Mapping) {
            Mapping m = (Mapping) mappingAnnotation;
            mappingString = m.value();
            mappingExpression = createSimpleExpression(mappingString, mapping, context, ExpressionLocation.MAPPING);
            fetches = m.fetches();
            fetchStrategy = m.fetch();
            updateMappableAttribute = getUpdateMappableAttribute(context, mappingExpression);
            this.mappingType = MappingType.BASIC;
            if (fetchStrategy != FetchStrategy.JOIN || limitExpression != null) {
                ExtendedManagedType<?> managedType = context.getEntityMetamodel().getManagedType(ExtendedManagedType.class, declaringType.getJpaManagedType());
                ExtendedAttribute<?, ?> attribute = managedType.getOwnedAttributes().get(mappingString);

                correlationKeyAlias = "__correlationAlias";
                String correlationPath = null;
                // The special case when joining the association results in a different join than when doing it through entity joins
                // This might be due to a @Where annotation being present on the association
                if (fetchStrategy == FetchStrategy.SELECT && attribute != null && attribute.hasJoinCondition()) {
                    correlated = declaringType.getEntityClass();
                    correlationExpression = "this IN __correlationAlias";
                    correlationResult = mappingString;
                    correlationResultExpression = mappingExpression;
                } else {
                    // If the mapping is a deep path expression i.e. contains a dot but no parenthesis, we try to find a mapped by attribute by a prefix
                    int index;
                    if (attribute == null && (index = mappingString.indexOf('.')) != -1 && mappingString.indexOf('(') == -1
                            && (attribute = managedType.getOwnedAttributes().get(mappingString.substring(0, index))) != null && !StringUtils.isEmpty(attribute.getMappedBy()) && !attribute.hasJoinCondition()) {
                        correlated = attribute.getElementClass();
                        correlationExpression = attribute.getMappedBy() + " IN __correlationAlias";
                        correlationResult = mappingString.substring(index + 1);
                        if (mappingExpression instanceof PathExpression) {
                            correlationResultExpression = ((PathExpression) mappingExpression).withoutFirst();
                        } else {
                            correlationResultExpression = new PathExpression();
                        }
                    } else if (attribute != null && !StringUtils.isEmpty(attribute.getMappedBy()) && !attribute.hasJoinCondition()) {
                        correlated = attribute.getElementClass();
                        correlationExpression = attribute.getMappedBy() + " IN __correlationAlias";
                        correlationResult = "";
                        correlationResultExpression = new PathExpression();
                    } else {
                        correlated = declaringType.getEntityClass();
                        correlationExpression = "this IN __correlationAlias";
                        correlationResult = mappingString;
                        correlationResultExpression = mappingExpression;
                        // When using @Limit in combination with JOIN fetching, we need to adapt the correlation expression when array expressions are used
                        if (fetchStrategy == FetchStrategy.JOIN && !orderByItems.isEmpty() && mappingExpression instanceof PathExpression) {
                            PathExpression pathExpression = (PathExpression) mappingExpression;
                            int arrayIndex = pathExpression.getExpressions().size() - 1;
                            do {
                                if (pathExpression.getExpressions().get(arrayIndex) instanceof ArrayExpression) {
                                    break;
                                }
                                arrayIndex--;
                            } while (arrayIndex > 0);

                            // If we encounter an array, we must correlate the path as a whole instead
                            if (arrayIndex != -1) {
                                correlated = null;
                                correlationPath = mappingString;
                                correlationResult = "";
                                correlationResultExpression = new PathExpression();
                            }
                        }
                    }
                }
                correlationBasis = "this";
                correlationBasisExpression = new PathExpression(new PropertyExpression("this"));
                if (correlated == null) {
                    correlationProviderFactory = new StaticPathCorrelationProvider(correlationPath, declaringType.getEntityViewRootTypes().keySet());
                } else {
                    correlationProviderFactory = new StaticCorrelationProvider(correlated, correlationKeyAlias, correlationExpression, createPredicate(correlationExpression, mapping, context, ExpressionLocation.CORRELATION_EXPRESSION), declaringType.getEntityViewRootTypes().keySet());
                }
                // Having a correlated class being set would only cause validation which is unnecessary since this is fully generated
                correlated = null;
            }
        } else if (mappingAnnotation instanceof MappingParameter) {
            mappingString = ((MappingParameter) mappingAnnotation).value();
            fetchStrategy = FetchStrategy.JOIN;
            batchSize = -1;
            limitExpression = null;
            offsetExpression = null;
            this.mappingType = MappingType.PARAMETER;
        } else if (mappingAnnotation instanceof Self) {
            mappingString = "NULL";
            mappingExpression = NullExpression.INSTANCE;
            batchSize = -1;
            limitExpression = null;
            offsetExpression = null;
            orderByItems = Collections.emptyList();
            this.mappingType = MappingType.PARAMETER;
        } else if (mappingAnnotation instanceof MappingSubquery) {
            MappingSubquery mappingSubquery = (MappingSubquery) mappingAnnotation;
            subqueryProvider = mappingSubquery.value();
            subqueryProviderFactory = SubqueryProviderHelper.getFactory(subqueryProvider);
            fetchStrategy = FetchStrategy.JOIN;
            batchSize = -1;
            limitExpression = null;
            offsetExpression = null;
            orderByItems = Collections.emptyList();
            this.mappingType = MappingType.SUBQUERY;
            subqueryExpression = mappingSubquery.expression();
            subqueryAlias = mappingSubquery.subqueryAlias();
            subqueryResultExpression = createSimpleExpression(subqueryExpression, mapping, context, ExpressionLocation.SUBQUERY_EXPRESSION);

            if (!subqueryExpression.isEmpty() && subqueryAlias.isEmpty()) {
                context.addError("The subquery alias is empty although the subquery expression is not " + mapping.getErrorLocation());
            }
            if (subqueryProvider.getEnclosingClass() != null && !Modifier.isStatic(subqueryProvider.getModifiers())) {
                context.addError("The subquery provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + mapping.getErrorLocation());
            }
        } else if (mappingAnnotation instanceof MappingCorrelated) {
            MappingCorrelated mappingCorrelated = (MappingCorrelated) mappingAnnotation;
            fetches = mappingCorrelated.fetches();
            fetchStrategy = mappingCorrelated.fetch();

            if (fetchStrategy != FetchStrategy.SELECT) {
                batchSize = -1;
            }
            this.mappingType = MappingType.CORRELATED;
            correlationBasis = mappingCorrelated.correlationBasis();
            correlationResult = mappingCorrelated.correlationResult();
            correlationProvider = mappingCorrelated.correlator();
            correlationBasisExpression = createSimpleExpression(correlationBasis, mapping, context, ExpressionLocation.CORRELATION_BASIS);
            correlationResultExpression = createSimpleExpression(correlationResult, mapping, context, ExpressionLocation.CORRELATION_RESULT);

            if (correlationProvider.getEnclosingClass() != null && !Modifier.isStatic(correlationProvider.getModifiers())) {
                context.addError("The correlation provider is defined as non-static inner class. Make it static, otherwise it can't be instantiated: " + mapping.getErrorLocation());
            }
            correlationProviderFactory = CorrelationProviderHelper.getFactory(correlationProvider);
            ScalarTargetResolvingExpressionVisitor resolver = new ScalarTargetResolvingExpressionVisitor(declaringType.getJpaManagedType(), context.getEntityMetamodel(), context.getJpqlFunctions(), declaringType.getEntityViewRootTypes());
            javax.persistence.metamodel.Type<?> type = TypeExtractingCorrelationBuilder.extractType(correlationProviderFactory, "_alias", context, resolver);
            correlated = type == null ? null : type.getJavaType();
        } else if (mappingAnnotation instanceof MappingCorrelatedSimple) {
            MappingCorrelatedSimple mappingCorrelated = (MappingCorrelatedSimple) mappingAnnotation;
            fetches = mappingCorrelated.fetches();
            fetchStrategy = mappingCorrelated.fetch();

            if (fetchStrategy != FetchStrategy.SELECT) {
                batchSize = -1;
            }
            this.mappingType = MappingType.CORRELATED;
            correlationBasis = mappingCorrelated.correlationBasis();
            correlationResult = mappingCorrelated.correlationResult();
            correlated = mappingCorrelated.correlated();
            correlationKeyAlias = mappingCorrelated.correlationKeyAlias();
            correlationExpression = mappingCorrelated.correlationExpression();
            correlationBasisExpression = createSimpleExpression(correlationBasis, mapping, context, ExpressionLocation.CORRELATION_BASIS);
            correlationResultExpression = createSimpleExpression(correlationResult, mapping, context, ExpressionLocation.CORRELATION_RESULT);
            correlationProviderFactory = new StaticCorrelationProvider(correlated, correlationKeyAlias, correlationExpression, createPredicate(correlationExpression, mapping, context, ExpressionLocation.CORRELATION_EXPRESSION), declaringType.getEntityViewRootTypes().keySet());

            if (mappingCorrelated.correlationBasis().isEmpty()) {
                context.addError("Illegal empty correlation basis in the " + mapping.getErrorLocation());
            }
            if (!(declaringType instanceof ViewType<?>) && (fetchStrategy == FetchStrategy.SELECT || fetchStrategy == FetchStrategy.SUBSELECT)) {
                // This check is not perfect, but good enough since we also check it at runtime
                if (mappingCorrelated.correlationExpression().toUpperCase().contains("EMBEDDING_VIEW")) {
                    context.addError("The use of EMBEDDING_VIEW in the correlation for '" + mapping.getErrorLocation() + "' is illegal because the embedding view type '" + declaringType.getJavaType().getName() + "' does not declare a @IdMapping!");
                }
            }
        } else {
            context.addError("No mapping annotation could be found " + mapping.getErrorLocation());
            this.mappingType = null;
        }

        if (limitExpression != null && fetchStrategy == FetchStrategy.MULTISET && context.getDbmsDialect().getLateralStyle() == LateralStyle.NONE && !context.getDbmsDialect().supportsWindowFunctions()) {
            context.addError("The use of the MULTISET fetch strategy with a limit in the '" + mapping.getErrorLocation() + "' requires lateral joins or window functions which are unsupported by the DBMS!");
        }

        this.mapping = mappingString;
        this.mappingExpression = mappingExpression;
        this.fetches = fetches;
        this.fetchStrategy = fetchStrategy;
        this.batchSize = batchSize;
        this.orderByItems = orderByItems;
        this.limitExpression = limitExpression;
        this.offsetExpression = offsetExpression;
        this.subqueryProviderFactory = subqueryProviderFactory;
        this.subqueryProvider = subqueryProvider;
        this.id = id;
        this.updateMappableAttribute = updateMappableAttribute;
        this.subqueryExpression = subqueryExpression;
        this.subqueryResultExpression = subqueryResultExpression;
        this.subqueryAlias = subqueryAlias;
        this.correlationBasis = correlationBasis;
        this.correlationResult = correlationResult;
        this.correlationProvider = correlationProvider;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlated = correlated;
        this.correlationKeyAlias = correlationKeyAlias;
        this.correlationExpression = correlationExpression;
        this.correlationBasisExpression = correlationBasisExpression;
        this.correlationResultExpression = correlationResultExpression;
    }

    public static List<OrderByItem> parseOrderByItems(List<String> orderByItemExpressions) {
        List<OrderByItem> orderByItems = new ArrayList<>(orderByItemExpressions.size());
        for (int i = 0; i < orderByItemExpressions.size(); i++) {
            String expression = orderByItemExpressions.get(i);
            String upperExpression = expression.toUpperCase();
            boolean ascending = true;
            boolean nullsFirst = false;
            if (upperExpression.endsWith(" NULLS LAST")) {
                upperExpression = upperExpression.substring(0, upperExpression.length() - " NULLS LAST".length());
            } else if (upperExpression.endsWith(" NULLS FIRST")) {
                nullsFirst = true;
                upperExpression = upperExpression.substring(0, upperExpression.length() - " NULLS FIRST".length());
            }
            if (upperExpression.endsWith(" ASC")) {
                upperExpression = upperExpression.substring(0, upperExpression.length() - " ASC".length());
            } else if (upperExpression.endsWith(" DESC")) {
                ascending = false;
                upperExpression = upperExpression.substring(0, upperExpression.length() - " DESC".length());
            }
            expression = expression.substring(0, upperExpression.length());
            orderByItems.add(new OrderByItem(expression, ascending, nullsFirst));
        }
        return Collections.unmodifiableList(orderByItems);
    }

    protected static Expression createSimpleExpression(String expression, AttributeMapping mapping, MetamodelBuildingContext context, ExpressionLocation expressionLocation) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        try {
            return context.getTypeValidationExpressionFactory().createSimpleExpression(expression, false, expressionLocation == ExpressionLocation.SUBQUERY_EXPRESSION, true);
        } catch (SyntaxErrorException ex) {
            context.addError("Syntax error in " + expressionLocation + " '" + expression + "' of the " + mapping.getErrorLocation() + ": " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            context.addError("An error occurred while trying to resolve the " + expressionLocation + " of the " + mapping.getErrorLocation() + ": " + ex.getMessage());
        }
        return null;
    }

    private static Predicate createPredicate(String expression, AttributeMapping mapping, MetamodelBuildingContext context, ExpressionLocation expressionLocation) {
        try {
            return context.getTypeValidationExpressionFactory().createBooleanExpression(expression, false);
        } catch (SyntaxErrorException ex) {
            context.addError("Syntax error in " + expressionLocation + " '" + expression + "' of the " + mapping.getErrorLocation() + ": " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            context.addError("An error occurred while trying to resolve the " + expressionLocation + " of the " + mapping.getErrorLocation() + ": " + ex.getMessage());
        }
        return null;
    }

    private static Class<?> getConvertedType(Class<?> declaringClass, java.lang.reflect.Type convertedType, Class<?> javaType) {
        if (convertedType == null) {
            return javaType;
        }
        return ReflectionUtils.resolveType(declaringClass, convertedType);
    }

    private javax.persistence.metamodel.Attribute<?, ?> getUpdateMappableAttribute(MetamodelBuildingContext context, Expression mappingExpression) {
        if (mappingExpression != null) {
            try {
                UpdatableExpressionVisitor visitor = new UpdatableExpressionVisitor(context.getEntityMetamodel(), declaringType.getEntityClass(), true, declaringType.getEntityViewRootTypes());
                mappingExpression.accept(visitor);
                Iterator<javax.persistence.metamodel.Attribute<?, ?>> iterator = visitor.getPossibleTargets().keySet().iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                }
            } catch (Exception ex) {
                // Don't care about the actual exception as that will be thrown anyway when validating the expressions later
            }
        }

        return null;
    }

    public static String stripThisFromMapping(String mapping) {
        return replaceThisFromMapping(mapping, "");
    }

    public static String replaceThisFromMapping(String mapping, String root) {
        if (mapping == null) {
            return null;
        }
        mapping = mapping.trim();
        if (mapping.startsWith(THIS)) {
            // Special case when the mapping start with "this"
            if (mapping.length() == THIS.length()) {
                // Return the empty string if it essentially equals "this"
                return root;
            }
            if (root.isEmpty()) {
                char nextChar = mapping.charAt(THIS.length());
                if (nextChar == '.') {
                    // Only replace if it isn't a prefix
                    mapping = mapping.substring(THIS.length() + 1);
                }
            } else {
                mapping = root + mapping.substring(THIS.length());
            }
        }

        String replacement;
        if (root.isEmpty()) {
            replacement = "$1";
        } else {
            replacement = "$1" + root + ".";
        }
        mapping = PREFIX_THIS_REPLACE_PATTERN.matcher(mapping)
                .replaceAll(replacement);

        return mapping;
    }

    public final void renderSubqueryExpression(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, subqueryResultExpression, subqueryAlias, serviceProvider, sb);
    }

    public final void renderSubqueryExpression(String parent, String subqueryExpression, String subqueryAlias, ServiceProvider serviceProvider, StringBuilder sb) {
        ExpressionFactory ef = serviceProvider.getService(ExpressionFactory.class);
        Expression expr = ef.createSimpleExpression(subqueryExpression, false, false, true);
        renderExpression(parent, expr, subqueryAlias, serviceProvider, sb);
    }

    public final void renderCorrelationBasis(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, correlationBasisExpression, null, serviceProvider, sb);
    }

    public final void renderCorrelationResult(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, correlationResultExpression, null, serviceProvider, sb);
    }

    public final void renderMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, mappingExpression, null, serviceProvider, sb);
    }

    protected final void renderExpression(String parent, Expression expression, String aliasToSkip, ServiceProvider serviceProvider, StringBuilder sb) {
        if (parent != null && !parent.isEmpty()) {
            ExpressionFactory ef = serviceProvider.getService(ExpressionFactory.class);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(ef, parent, aliasToSkip, aliasToSkip, declaringType.getEntityViewRootTypes().keySet(), true, false);
            generator.setQueryBuffer(sb);
            expression.accept(generator);
        } else {
            sb.append(expression);
        }
    }

    @Override
    public <T extends LimitBuilder<?> & OrderByBuilder<?>> void renderLimit(String parent, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, T builder) {
        if (limitExpression != null) {
            ExpressionFactory expressionFactory = ((ServiceProvider) builder).getService(ExpressionFactory.class);
            declaringType.createLimiter(expressionFactory, parent, limitExpression, offsetExpression, orderByItems).apply(parameterHolder, optionalParameters, builder);
        }
    }

    /**
     * Collects all mappings that involve the use of a collection attribute for duplicate usage checks.
     *
     * @param managedType The JPA type against which to evaluate the mapping
     * @param context The metamodel context
     * @return The mappings which contain collection attribute uses
     */
    public Map<String, Boolean> getCollectionJoinMappings(ManagedType<?> managedType, MetamodelBuildingContext context) {
        if (mappingExpression == null || isQueryParameter() || getFetchStrategy() != FetchStrategy.JOIN) {
            // Subqueries and parameters can't be checked. When a collection is remapped to a singular attribute, we don't check it
            // When using a non-join fetch strategy, we also don't care about the collection join mappings
            return Collections.emptyMap();
        }
        
        CollectionJoinMappingGathererExpressionVisitor visitor = new CollectionJoinMappingGathererExpressionVisitor(managedType, context.getEntityMetamodel());
        mappingExpression.accept(visitor);
        Map<String, Boolean> mappings = new HashMap<>();
        boolean aggregate = getAttributeType() == AttributeType.SINGULAR;
        
        for (String s : visitor.getPaths()) {
            mappings.put(s, aggregate);
        }
        
        return mappings;
    }

    public boolean hasJoinFetchedCollections() {
        return getFetchStrategy() == FetchStrategy.JOIN && (
                isCollection() || getElementType() instanceof ManagedViewTypeImpl<?> && ((ManagedViewTypeImplementor<?>) getElementType()).hasJoinFetchedCollections());
    }

    public boolean hasSelectOrSubselectFetchedAttributes() {
        return getFetchStrategy() == FetchStrategy.SELECT || getFetchStrategy() == FetchStrategy.SUBSELECT || (
                getElementType() instanceof ManagedViewTypeImpl<?> && ((ManagedViewTypeImplementor<?>) getElementType()).hasSelectOrSubselectFetchedAttributes());
    }

    public boolean hasJpaManagedAttributes() {
        return getElementType() instanceof BasicTypeImpl<?> && ((BasicTypeImpl<?>) getElementType()).isJpaManaged() ||
                getElementType() instanceof ManagedViewTypeImpl<?> && ((ManagedViewTypeImplementor<?>) getElementType()).hasJpaManagedAttributes();
    }

    protected String determineIndexMapping(AttributeMapping mapping) {
        String indexMapping = null;
        if (mapping.getMappingIndex() != null) {
            indexMapping = mapping.getMappingIndex().value();
            if (indexMapping.isEmpty()) {
                indexMapping = "INDEX(" + getMapping() + ")";
            }
        }
        return indexMapping;
    }

    protected String determineKeyMapping(AttributeMapping mapping) {
        String keyMapping = null;
        if (mapping.getMappingIndex() != null) {
            keyMapping = mapping.getMappingIndex().value();
        }
        if (keyMapping == null || keyMapping.isEmpty()) {
            keyMapping = "KEY(this)";
        }
        return keyMapping;
    }

    protected boolean determineForcedUnique(MetamodelBuildingContext context) {
        if (isCollection() && getMapping() != null && getMapping().indexOf('.') == -1 && getMappingIndexExpression() == null && getKeyMappingExpression() == null) {
            ExtendedManagedType<?> managedType = context.getEntityMetamodel().getManagedType(ExtendedManagedType.class, getDeclaringType().getJpaManagedType());
            ExtendedAttribute<?, ?> attribute = managedType.getOwnedAttributes().get(getMapping());
            if (attribute != null && attribute.getAttribute() instanceof javax.persistence.metamodel.PluralAttribute<?, ?, ?>) {
                // TODO: we should add that information to ExtendedAttribute
                return (((javax.persistence.metamodel.PluralAttribute<?, ?, ?>) attribute.getAttribute()).getCollectionType() != javax.persistence.metamodel.PluralAttribute.CollectionType.MAP)
                        && (!StringUtils.isEmpty(attribute.getMappedBy()) || !attribute.isBag())
                        && (attribute.getJoinTable() == null || attribute.getJoinTable().getKeyColumnMappings() == null)
                        && !MetamodelUtils.isIndexedList(context.getEntityMetamodel(), managedType.getType().getJavaType(), mappingExpression, mapping);
            }
        }

        return false;
    }

    public javax.persistence.metamodel.Attribute<?, ?> getUpdateMappableAttribute() {
        return updateMappableAttribute;
    }

    protected boolean isFilterNulls() {
        return !isCorrelated() || getCorrelationProviderFactory() != null;
    }

    public boolean isUpdateMappable() {
        // Since we can cascade correlated views, we consider them update mappable
        return hasDirtyStateIndex() || updateMappableAttribute != null;
    }

    public Class<?> getCorrelated() {
        return correlated;
    }

    public String getCorrelationKeyAlias() {
        return correlationKeyAlias;
    }

    public String getCorrelationExpression() {
        return correlationExpression;
    }

    public Predicate getCorrelationPredicate() {
        if (correlationProviderFactory instanceof StaticCorrelationProvider) {
            return ((StaticCorrelationProvider) correlationProviderFactory).getCorrelationPredicate();
        } else if (correlationProviderFactory instanceof StaticPathCorrelationProvider) {
            return ((StaticPathCorrelationProvider) correlationProviderFactory).getCorrelationPredicate();
        } else {
            return null;
        }
    }

    public abstract boolean needsDirtyTracker();

    public abstract boolean hasDirtyStateIndex();

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static enum ExpressionLocation {
        MAPPING("mapping expression"),
        MAPPING_INDEX("mapping index expression"),
        SUBQUERY_EXPRESSION("subquery expression"),
        CORRELATION_BASIS("correlation basis"),
        CORRELATION_RESULT("correlation result"),
        CORRELATION_EXPRESSION("correlation expression");

        private final String location;

        ExpressionLocation(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location;
        }
    }

    private static boolean isCompatible(TargetType t, Class<?> targetType, Class<?> targetElementType, boolean updatable, boolean singular) {
        if (t.hasCollectionJoin()) {
            return isCompatible(t.getLeafMethod(), t.getLeafBaseClass(), t.getLeafBaseValueClass(), targetType, targetElementType, updatable, singular);
        } else {
            Class<?> entityAttributeElementType = getElementTypeOrNull(t, singular);
            return isCompatible(t.getLeafMethod(), t.getLeafBaseClass(), entityAttributeElementType, targetType, targetElementType, updatable, singular);
        }
    }

    private static Class<?> getElementTypeOrNull(TargetType t, boolean singular) {
        if (singular && t.getLeafMethod() != null && t.getLeafBaseClass() == t.getLeafBaseValueClass() && (Collection.class.isAssignableFrom(t.getLeafBaseClass()) || Map.class.isAssignableFrom(t.getLeafBaseClass()))) {
            Member javaMember = t.getLeafMethod().getJavaMember();
            Class<?> elementClass;
            if (javaMember instanceof Field) {
                Class<?>[] resolvedFieldTypeArguments = ReflectionUtils.getResolvedFieldTypeArguments(t.getLeafMethod().getDeclaringType().getJavaType(), (Field) javaMember);
                elementClass = resolvedFieldTypeArguments[resolvedFieldTypeArguments.length - 1];
            } else if (javaMember instanceof Method) {
                Class<?>[] resolvedMethodReturnTypeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(t.getLeafMethod().getDeclaringType().getJavaType(), (Method) javaMember);
                elementClass = resolvedMethodReturnTypeArguments[resolvedMethodReturnTypeArguments.length - 1];
            } else {
                elementClass = null;
            }
            if (elementClass != t.getLeafBaseValueClass()) {
                return elementClass;
            }
        }
        return null;
    }

    /**
     * Checks if <code>entityAttributeType</code> with an optional element type <code>entityAttributeElementType</code>
     * can be mapped to <code>targetType</code> with the optional element type <code>viewAttributeElementType</code> and the given <code>updatable</code> config.
     *
     * A <code>entityAttributeType</code> of <code>NULL</code> represents the <i>any type</i> which makes it always compatible i.e. returning <code>true</code>.
     * A type is compatible if the source types given by <code>entityAttributeType</code>/<code>entityAttributeElementType</code>
     * are subtypes of the target types <code>targetType</code>/<code>viewAttributeElementType</code>.
     *
     * A source collection type it is also compatible with non-collection targets if the source element type is a subtype of the target type.
     * A source non-collection type is also compatible with a collection target if the source type is a subtype of the target element type.
     *
     *
     * @param attribute The resolved JPA attribute
     * @param entityAttributeType The source type
     * @param entityAttributeElementType The optional source element type
     * @param viewAttributeType The target type
     * @param viewAttributeElementType The optional target element type
     * @param updatable Whether this is a check for an updatable attribute
     * @param singular Whether the view attribute is singular
     * @return True if mapping from <code>entityAttributeType</code>/<code>entityAttributeElementType</code> to <code>targetType</code>/<code>viewAttributeElementType</code> is possible
     */
    private static boolean isCompatible(javax.persistence.metamodel.Attribute<?, ?> attribute, Class<?> entityAttributeType, Class<?> entityAttributeElementType, Class<?> viewAttributeType, Class<?> viewAttributeElementType, boolean updatable, boolean singular) {
        // Null is the marker for ANY TYPE
        if (entityAttributeType == null) {
            return true;
        }

        if (updatable) {
            if (entityAttributeElementType != null) {
                if (viewAttributeElementType != null) {
                    if (singular) {
                        return viewAttributeType == entityAttributeType && viewAttributeElementType == entityAttributeElementType;
                    } else {
                        // Mapping a plural entity attribute to a plural view attribute

                        // Indexed lists or maps must map to indexed lists or maps again if they want to be updatable
                        if (attribute instanceof ListAttribute<?, ?>) {
                            if (!List.class.isAssignableFrom(viewAttributeType)) {
                                return false;
                            }
                        } else if (attribute instanceof MapAttribute<?, ?, ?>) {
                            if (!Map.class.isAssignableFrom(viewAttributeType)) {
                                return false;
                            }
                        } else if (!Collection.class.isAssignableFrom(viewAttributeType)) {
                            // For all other plural attributes, we allow any collection type
                            return false;
                        }
                        return viewAttributeElementType == entityAttributeElementType;
                    }
                } else {
                    if (singular) {
                        return viewAttributeType == entityAttributeType;
                    } else {
                        // Mapping a plural entity attribute to a singular view attribute
                        return viewAttributeType == entityAttributeElementType;
                    }
                }
            } else {
                if (viewAttributeElementType != null) {
                    // Mapping a singular entity attribute to a plural view attribute
                    return viewAttributeElementType == entityAttributeType;
                } else {
                    // Mapping a singular entity attribute to a singular view attribute
                    return viewAttributeType == entityAttributeType;
                }
            }
        } else {
            if (entityAttributeElementType != null) {
                if (viewAttributeElementType != null) {
                    // Mapping a plural entity attribute to a plural view attribute
                    // Either entityAttributeType is a subtype of target type, or it is a subtype of map and the target type a subtype of Collection
                    // This allows mapping Map<?, Entity> to List<Subview>
                    // Anyway the entityAttributeElementType must be a subtype of the viewAttributeElementType
                    return (viewAttributeType.isAssignableFrom(entityAttributeType) || !singular && Map.class.isAssignableFrom(entityAttributeType) && Collection.class.isAssignableFrom(viewAttributeType))
                            && viewAttributeElementType.isAssignableFrom(entityAttributeElementType);
                } else {
                    // Mapping a plural entity attribute to a singular view attribute
                    return viewAttributeType.isAssignableFrom(entityAttributeElementType);
                }
            } else {
                if (viewAttributeElementType != null) {
                    // Mapping a singular entity attribute to a plural view attribute
                    return viewAttributeElementType.isAssignableFrom(entityAttributeType);
                } else {
                    // Mapping a singular entity attribute to a singular view attribute
                    return viewAttributeType.isAssignableFrom(entityAttributeType);
                }
            }
        }
    }

    private static void validateTypesCompatible(ManagedType<?> managedType, Expression expression, Class<?> targetType, Class<?> targetElementType, boolean updatable, boolean singular, Map<String, javax.persistence.metamodel.Type<?>> rootTypes, MetamodelBuildingContext context, ExpressionLocation expressionLocation, String location) {
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedType, context.getEntityMetamodel(), context.getJpqlFunctions(), rootTypes);

        try {
            expression.accept(visitor);
        } catch (IllegalArgumentException ex) {
            context.addError("An error occurred while trying to resolve the " + expressionLocation + " of the " + location + ": " + ex.getMessage());
        }

        validateTypesCompatible(visitor.getPossibleTargetTypes(), targetType, targetElementType, updatable, singular, context, expressionLocation, location);
    }

    private static void validateTypesCompatible(List<TargetType> possibleTargets, Class<?> targetType, Class<?> targetElementType, boolean updatable, boolean singular, MetamodelBuildingContext context, ExpressionLocation expressionLocation, String location) {
        final Class<?> expressionType = targetType;
        if (!possibleTargets.isEmpty()) {
            boolean error = true;
            for (TargetType t : possibleTargets) {
                if (isCompatible(t, targetType, targetElementType, updatable, singular)) {
                    error = false;
                    break;
                }
            }

            if (error) {
                if (targetType.isPrimitive()) {
                    targetType = ReflectionUtils.getObjectClassOfPrimitve(targetType);
                } else {
                    targetType = ReflectionUtils.getPrimitiveClassOfWrapper(targetType);
                }

                if (targetType != null) {
                    for (TargetType t : possibleTargets) {
                        if (isCompatible(t, targetType, targetElementType, updatable, singular)) {
                            error = false;
                            break;
                        }
                    }
                }
            }

            if (error) {
                context.addError(typeCompatibilityError(possibleTargets, expressionType, targetElementType, expressionLocation, location));
            }
        }
    }

    private static String typeCompatibilityError(List<TargetType> possibleTargets, Class<?> targetType, Class<?> targetElementType, ExpressionLocation expressionLocation, String location) {
        StringBuilder sb = new StringBuilder();
        sb.append("The resolved possible types ");
        sb.append('[');
        for (TargetType t : possibleTargets) {
            sb.append(t.getLeafBaseClass().getName());
            if (t.getLeafBaseValueClass() != null && t.getLeafBaseClass() != t.getLeafBaseValueClass()) {
                sb.append('<');
                sb.append(t.getLeafBaseValueClass().getName());
                sb.append('>');
            }
            sb.append(", ");
        }

        sb.setLength(sb.length() - 2);
        sb.append(']');
        sb.append(" are not assignable to the given expression type '");
        sb.append(targetType.getName());
        if (targetElementType != null && targetElementType != targetType) {
            sb.append('<');
            sb.append(targetElementType.getName());
            sb.append('>');
        }
        sb.append("' of the ");
        sb.append(expressionLocation);
        sb.append(" declared by the ");
        sb.append(location);
        sb.append("!");
        return sb.toString();
    }

    public void checkAttribute(ManagedType<?> managedType, MetamodelBuildingContext context) {
        Class<?> expressionType = getJavaType();
        Class<?> keyType = null;
        Class<?> elementType = null;
        ManagedType<?> elementManagedType = context.getEntityMetamodel().getManagedType(getElementType().getJavaType());
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(elementManagedType, context.getEntityMetamodel(), context.getJpqlFunctions(), declaringType.getEntityViewRootTypes());

        if (fetches.length != 0) {
            if (elementManagedType == null) {
                context.addError("Specifying fetches for non-entity attribute type [" + Arrays.toString(fetches) + "] at the " + getLocation() + " is not allowed!");
            } else {
                for (int i = 0; i < fetches.length; i++) {
                    final String fetch = fetches[i];
                    final String errorLocation;
                    if (fetches.length == 1) {
                        errorLocation = "the fetch expression";
                    } else {
                        errorLocation = "the " + (i + 1) + ". fetch expression";
                    }
                    visitor.clear();

                    try {
                        // Validate the fetch expression parses
                        context.getExpressionFactory().createPathExpression(fetch).accept(visitor);
                    } catch (SyntaxErrorException ex) {
                        try {
                            context.getExpressionFactory().createSimpleExpression(fetch, false, false, true);
                            // The used expression is not usable for fetches
                            context.addError("Invalid fetch expression '" + fetch + "' of the " + getLocation() + ". Simplify the fetch expression to a simple path expression. Encountered error: " + ex.getMessage());
                        } catch (SyntaxErrorException ex2) {
                            // This is a real syntax error
                            context.addError("Syntax error in " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                        }
                    } catch (IllegalArgumentException ex) {
                        context.addError("An error occurred while trying to resolve the " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                    }
                }
            }
        }

        if (limitExpression != null) {
            try {
                Expression inItemExpression = context.getTypeValidationExpressionFactory().createInItemExpression(limitExpression);
                if (!(inItemExpression instanceof ParameterExpression) && !(inItemExpression instanceof NumericLiteral) || inItemExpression instanceof NumericLiteral && ((NumericLiteral) inItemExpression).getNumericType() != NumericType.INTEGER) {
                    context.addError("Syntax error in the limit expression '" + limitExpression + "' of the " + getLocation() + ": The expression must be a integer literal or a parameter expression");
                }
            } catch (SyntaxErrorException ex) {
                context.addError("Syntax error in the limit expression '" + limitExpression + "' of the " + getLocation() + ": " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                context.addError("An error occurred while trying to resolve the limit expression '" + limitExpression + "' of the " + getLocation() + ": " + ex.getMessage());
            }
            try {
                Expression inItemExpression = context.getTypeValidationExpressionFactory().createInItemExpression(offsetExpression);
                if (!(inItemExpression instanceof ParameterExpression) && !(inItemExpression instanceof NumericLiteral) || inItemExpression instanceof NumericLiteral && ((NumericLiteral) inItemExpression).getNumericType() != NumericType.INTEGER) {
                    context.addError("Syntax error in the offset expression '" + offsetExpression + "' of the " + getLocation() + ": The expression must be a integer literal or a parameter expression");
                }
            } catch (SyntaxErrorException ex) {
                context.addError("Syntax error in the offset expression '" + offsetExpression + "' of the " + getLocation() + ": " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                context.addError("An error occurred while trying to resolve the offset expression '" + offsetExpression + "' of the " + getLocation() + ": " + ex.getMessage());
            }
            for (int i = 0; i < orderByItems.size(); i++) {
                OrderByItem orderByItem = orderByItems.get(i);
                String expression = orderByItem.getExpression();
                try {
                    visitor.clear();
                    context.getTypeValidationExpressionFactory().createSimpleExpression(expression, false, false, true).accept(visitor);
                } catch (SyntaxErrorException ex) {
                    context.addError("Syntax error in the " + (i + 1) + "th order by expression '" + expression + "' of the " + getLocation() + ": " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    context.addError("An error occurred while trying to resolve the " + (i + 1) + "th order by expression '" + expression + "' of the " + getLocation() + ": " + ex.getMessage());
                }
            }
        }

        if (fetchStrategy == FetchStrategy.MULTISET) {
            if (getElementType() instanceof ManagedViewTypeImplementor<?> && ((ManagedViewTypeImplementor<?>) getElementType()).hasJpaManagedAttributes()) {
                context.addError("Using the MULTISET fetch strategy is not allowed when the subview contains attributes with entity types. MULTISET at the " + getLocation() + " is not allowed!");
            } else if (getElementType() instanceof BasicTypeImpl<?> && ((BasicTypeImpl<?>) getElementType()).isJpaManaged()) {
                context.addError("Using the MULTISET fetch strategy is not allowed with entity types. MULTISET at the " + getLocation() + " is not allowed!");
            }
        }

        Expression indexExpression = null;
        if (isCollection()) {
            elementType = getElementType().getJavaType();

            if (!isUpdatable()) {
                // Updatable collection attributes are specially handled in the type compatibility check
                if (isIndexed()) {
                    if (getCollectionType() == PluralAttribute.CollectionType.MAP) {
                        expressionType = Collection.class;
                        keyType = getKeyType().getJavaType();
                    } else {
                        expressionType = Collection.class;
                        keyType = Integer.class;
                    }
                } else {
                    // We can assign e.g. a Set to a List, so let's use the common supertype
                    expressionType = Collection.class;
                }
            }

            if (isIndexed()) {
                if (getCollectionType() == PluralAttribute.CollectionType.MAP) {
                    indexExpression = getKeyMappingExpression();

                    String[] keyFetches = getKeyFetches();
                    if (keyFetches.length != 0) {
                        ManagedType<?> managedKeyType = context.getEntityMetamodel().getManagedType(getKeyType().getJavaType());
                        if (managedKeyType == null) {
                            context.addError("Specifying key fetches for non-entity attribute key type [" + Arrays.toString(keyFetches) + "] at the " + getLocation() + " is not allowed!");
                        } else {
                            ScalarTargetResolvingExpressionVisitor keyVisitor = new ScalarTargetResolvingExpressionVisitor(managedKeyType, context.getEntityMetamodel(), context.getJpqlFunctions(), declaringType.getEntityViewRootTypes());
                            for (int i = 0; i < keyFetches.length; i++) {
                                final String fetch = keyFetches[i];
                                final String errorLocation;
                                if (keyFetches.length == 1) {
                                    errorLocation = "the key fetch expression";
                                } else {
                                    errorLocation = "the " + (i + 1) + ". key fetch expression";
                                }
                                keyVisitor.clear();

                                try {
                                    // Validate the fetch expression parses
                                    context.getExpressionFactory().createPathExpression(fetch).accept(keyVisitor);
                                } catch (SyntaxErrorException ex) {
                                    try {
                                        context.getExpressionFactory().createSimpleExpression(fetch, false, false, true);
                                        // The used expression is not usable for fetches
                                        context.addError("Invalid key fetch expression '" + fetch + "' of the " + getLocation() + ". Simplify the key fetch expression to a simple path expression. Encountered error: " + ex.getMessage());
                                    } catch (SyntaxErrorException ex2) {
                                        // This is a real syntax error
                                        context.addError("Syntax error in " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                                    }
                                } catch (IllegalArgumentException ex) {
                                    context.addError("An error occurred while trying to resolve the " + errorLocation + " '" + fetch + "' of the " + getLocation() + ": " + ex.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    indexExpression = getMappingIndexExpression();
                }
            }
        }

        if (isSubview()) {
            ManagedViewTypeImplementor<?> subviewType = (ManagedViewTypeImplementor<?>) getElementType();

            if (isCollection()) {
                elementType = subviewType.getEntityClass();
            } else {
                expressionType = subviewType.getEntityClass();
            }
        } else {
            // If we determined, that the java type is a basic type, let's double check if the user didn't do something wrong
            Class<?> elementJavaType = getElementType().getJavaType();
            if ((elementJavaType.getModifiers() & Modifier.ABSTRACT) != 0) {
                // If the element type has an entity view annotation, although it is considered basic, we throw an error as this means, the view was probably not registered
                if (!isQueryParameter() && AnnotationUtils.findAnnotation(elementJavaType, EntityView.class) != null && getElementType().getConvertedType() == null) {
                    context.addError("The element type '" + elementJavaType.getName() + "' is considered basic although the class is annotated with @EntityView. Add a type converter or add the java class to the entity view configuration! Problematic attribute " + getLocation());
                }
            }
        }
        if (isKeySubview()) {
            keyType = ((ManagedViewTypeImplementor<?>) getKeyType()).getEntityClass();
        }

        if (keyType != null) {
            validateTypesCompatible(possibleIndexTargetTypes, keyType, null, isUpdatable(), true, context, ExpressionLocation.MAPPING_INDEX, getLocation());
        }

        if (isCorrelated()) {
            // Validate that resolving "correlationBasis" on "managedType" is valid
            validateTypesCompatible(managedType, correlationBasisExpression, Object.class, null, false, true, declaringType.getEntityViewRootTypes(), context, ExpressionLocation.CORRELATION_BASIS, getLocation());

            if (correlated != null) {
                // Validate that resolving "correlationResult" on "correlated" is compatible with "expressionType" and "elementType"
                validateTypesCompatible(possibleTargetTypes, expressionType, elementType, false, !isCollection(), context, ExpressionLocation.CORRELATION_RESULT, getLocation());
                Predicate correlationPredicate = getCorrelationPredicate();
                if (correlationPredicate != null) {
                    ExpressionFactory ef = context.getTypeValidationExpressionFactory();
                    // First we need to prefix the correlation basis expression with an alias because we use that in the predicate
                    PrefixingQueryGenerator prefixingQueryGenerator = new PrefixingQueryGenerator(ef, correlationKeyAlias, null, null, declaringType.getEntityViewRootTypes().keySet(), false, false);
                    prefixingQueryGenerator.setQueryBuffer(new StringBuilder());
                    correlationBasisExpression.accept(prefixingQueryGenerator);
                    // Next we replace the plain alias usage with the prefixed correlation basis expression
                    AliasReplacementVisitor aliasReplacementVisitor = new AliasReplacementVisitor(ef.createSimpleExpression(prefixingQueryGenerator.getQueryBuffer().toString()), correlationKeyAlias);
                    correlationPredicate = correlationPredicate.copy(ExpressionCopyContext.EMPTY);
                    correlationPredicate.accept(aliasReplacementVisitor);
                    // Finally we validate that the expression
                    try {
                        Map<String, javax.persistence.metamodel.Type<?>> rootTypes = new HashMap<>(declaringType.getEntityViewRootTypes());
                        rootTypes.put(correlationKeyAlias, managedType);
                        ScalarTargetResolvingExpressionVisitor correlationVisitor = new ScalarTargetResolvingExpressionVisitor(elementManagedType, context.getEntityMetamodel(), context.getJpqlFunctions(), rootTypes);
                        correlationPredicate.accept(correlationVisitor);
                    } catch (SyntaxErrorException ex) {
                        context.addError("Syntax error in the condition expression '" + correlationPredicate + "' of the " + getLocation() + ": " + ex.getMessage());
                    } catch (IllegalArgumentException ex) {
                        context.addError("An error occurred while trying to resolve the condition expression '" + correlationPredicate + "' of the " + getLocation() + ": " + ex.getMessage());
                    }
                }
            }
        } else if (isSubquery()) {
            if (subqueryExpression != null && !subqueryExpression.isEmpty()) {
                // If a converter is applied, we already know that there was a type match with the underlying type
                if (getElementType().getConvertedType() == null) {
                    validateTypesCompatible(possibleTargetTypes, expressionType, elementType, false, !isCollection(), context, ExpressionLocation.SUBQUERY_EXPRESSION, getLocation());
                }
            }
        } else if (!isQueryParameter()) {
            // Forcing singular via @MappingSingular
            if (!isCollection() && (Collection.class.isAssignableFrom(expressionType) || Map.class.isAssignableFrom(expressionType))) {
                Class<?>[] typeArguments = getTypeArguments();
                elementType = typeArguments[typeArguments.length - 1];
            }

            // If a converter is applied, we already know that there was a type match with the underlying type
            if (getElementType().getConvertedType() == null) {
                // Validate that resolving "mapping" on "managedType" is compatible with "expressionType" and "elementType"
                validateTypesCompatible(possibleTargetTypes, expressionType, elementType, isUpdatable(), !isCollection(), context, ExpressionLocation.MAPPING, getLocation());
            }

            if (isMutable() && (declaringType.isUpdatable() || declaringType.isCreatable())) {
                UpdatableExpressionVisitor updatableVisitor = new UpdatableExpressionVisitor(context.getEntityMetamodel(), managedType.getJavaType(), isUpdatable(), declaringType.getEntityViewRootTypes());
                try {
                    // NOTE: Not supporting "this" here because it doesn't make sense to have an updatable mapping that refers to this
                    // The only thing that might be interesting is supporting "this" when we support cascading as properties could be nested
                    // But not sure yet if the embeddable attributes would then be modeled as "updatable".
                    // I guess these attributes are not "updatable" but that probably depends on the decision regarding collections as they have a similar problem
                    // A collection itself might not be "updatable" but it's elements could be. This is roughly the same problem
                    mappingExpression.accept(updatableVisitor);
                    Map<javax.persistence.metamodel.Attribute<?, ?>, javax.persistence.metamodel.Type<?>> possibleTargets = updatableVisitor.getPossibleTargets();

                    if (possibleTargets.size() > 1) {
                        context.addError("Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets);
                    }

                    // TODO: maybe allow to override this per-attribute?
                    if (isDisallowOwnedUpdatableSubview()) {
                        for (Type<?> updateCascadeAllowedSubtype : getUpdateCascadeAllowedSubtypes()) {
                            ManagedViewType<?> managedViewType = (ManagedViewType<?>) updateCascadeAllowedSubtype;
                            if (managedViewType.isUpdatable()) {
                                context.addError("Invalid use of @UpdatableEntityView type '" + managedViewType.getJavaType().getName() + "' for the " + getLocation() + ". Consider using a read-only view type instead or use @AllowUpdatableEntityViews! " +
                                        "For further information on this topic, please consult the documentation https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#updatable-mappings-subview");
                            }
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    context.addError("There is an error for the " + getLocation() + ": " + ex.getMessage());
                }
                if (isUpdatable()) {
                    if (isCollection() && getElementCollectionType() != null) {
                        context.addError("The use of a multi-collection i.e. List<Collection<?>> or Map<?, Collection<?>> at the " + getLocation() + " is unsupported for updatable collections!");
                    }
                }
                if ((isUpdatable() || isKeySubview() && ((ManagedViewTypeImplementor<?>) getKeyType()).isUpdatable()) && indexExpression != null) {
                    boolean invalid;
                    if (getCollectionType() == PluralAttribute.CollectionType.MAP) {
                        invalid = !(indexExpression instanceof MapKeyExpression) || !"this".equals(((MapKeyExpression) indexExpression).getPath().getPath());
                    } else {
                        invalid = !(indexExpression instanceof ListIndexExpression) || !"this".equals(((ListIndexExpression) indexExpression).getPath().getPath());
                    }
                    if (invalid) {
                        context.addError("The @MappingIndex at the " + getLocation() + " is a complex mapping and can thus not be updatable!");
                    }
                }
            }
        }
    }

    protected abstract boolean isDisallowOwnedUpdatableSubview();

    public void checkNestedAttribute(List<AbstractAttribute<?, ?>> parents, ManagedType<?> managedType, MetamodelBuildingContext context, boolean hasMultisetParent) {
        if (hasMultisetParent) {
            if (!isQueryParameter() && getElementType() instanceof BasicTypeImpl<?>) {
                context.checkMultisetSupport(parents, this, ((BasicTypeImpl<?>) getElementType()).getUserType());
            }
        } else {
            hasMultisetParent = fetchStrategy == FetchStrategy.MULTISET;
        }
        if (!parents.isEmpty()) {
            if (getDeclaringType().getMappingType() == Type.MappingType.FLAT_VIEW) {
                // When this attribute is part of a flat view
                if (isCollection() && getFetchStrategy() == FetchStrategy.JOIN) {
                    // And is a join fetched collection
                    // We need to ensure it has at least one non-embedded parent
                    // Otherwise there is no identity which we can use to correlate the collection elements to
                    for (int i = parents.size() - 1; i >= 0; i--) {
                        AbstractAttribute<?, ?> parentAttribute = parents.get(i);
                        // If a parent attribute is a non-indexed collection, we bail out because that's an error
                        if (parentAttribute.isCollection() && !parentAttribute.isIndexed()) {
                            String path = parents.get(0).getDeclaringType().getJavaType().getName();
                            for (i = 0; i < parents.size(); i++) {
                                path += " > " + parents.get(i).getLocation();
                            }
                            context.addError("Illegal mapping of join fetched collection for the " + getLocation() + " via the path: " + path + ". Join fetched collections in flat views are only allowed for when the flat view is contained in an indexed collections or in a view.");
                            break;
                        }
                        // If the parent is a view with identity, having the collection is ok and we are done here
                        if (parentAttribute.getDeclaringType().getMappingType() == Type.MappingType.VIEW) {
                            break;
                        }
                    }
                }
            }
        }

        // Go into subtypes for nested checking
        if (isSubview()) {
            Map<ManagedViewTypeImplementor<?>, String> inheritanceSubtypeMappings = elementInheritanceSubtypeMappings();
            if (inheritanceSubtypeMappings.isEmpty()) {
                context.addError("Illegal empty inheritance subtype mappings for the " + getLocation() + ". Remove the @MappingInheritance annotation, set the 'onlySubtypes' attribute to false or add a @MappingInheritanceSubtype element!");
            }
            for (ManagedViewTypeImplementor<?> subviewType : inheritanceSubtypeMappings.keySet()) {
                parents.add(this);
                subviewType.checkNestedAttributes(parents, context, hasMultisetParent);
                parents.remove(parents.size() - 1);
            }

        }
        if (isKeySubview()) {
            Map<ManagedViewTypeImplementor<?>, String> inheritanceSubtypeMappings = keyInheritanceSubtypeMappings();
            if (inheritanceSubtypeMappings.isEmpty()) {
                context.addError("Illegal empty inheritance subtype mappings for the " + getLocation() + ". Remove the @MappingInheritance annotation, set the 'onlySubtypes' attribute to false or add a @MappingInheritanceSubtype element!");
            }
            for (ManagedViewTypeImplementor<?> subviewType : inheritanceSubtypeMappings.keySet()) {
                parents.add(this);
                subviewType.checkNestedAttributes(parents, context, hasMultisetParent);
                parents.remove(parents.size() - 1);
            }
        }
    }

    protected boolean isEmbedded() {
        return getDeclaringType().getMappingType() == Type.MappingType.FLAT_VIEW && "this".equals(mapping);
    }

    @SuppressWarnings("rawtypes")
    protected abstract Class[] getTypeArguments();

    public abstract String getLocation();

    public abstract boolean isUpdatable();

    public abstract boolean isMutable();

    public abstract String getMappedBy();

    public abstract boolean isUpdateCascaded();

    public abstract Set<Type<?>> getUpdateCascadeAllowedSubtypes();

    protected abstract boolean isIndexed();

    protected abstract boolean isSorted();

    protected abstract boolean isForcedUnique();

    protected abstract boolean isElementCollectionOrdered();

    protected abstract boolean isElementCollectionSorted();

    protected abstract boolean isElementCollectionForcedUnique();

    protected abstract PluralAttribute.CollectionType getCollectionType();

    protected abstract PluralAttribute.ElementCollectionType getElementCollectionType();

    public abstract Type<?> getElementType();

    protected abstract Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypeMappings();

    protected String[] getKeyFetches() {
        return EMPTY;
    }

    public Expression getKeyMappingExpression() {
        return null;
    }

    public Expression getMappingIndexExpression() {
        return null;
    }

    protected abstract Type<?> getKeyType();

    protected abstract Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypeMappings();

    protected abstract boolean isKeySubview();

    public abstract Set<Class<?>> getAllowedSubtypes();

    public abstract Set<Class<?>> getParentRequiringUpdateSubtypes();

    public abstract Set<Class<?>> getParentRequiringCreateSubtypes();

    public abstract boolean isOptimizeCollectionActionsEnabled();

    public abstract ContainerAccumulator<?> getContainerAccumulator();

    public abstract CollectionInstantiatorImplementor<?, ?> getCollectionInstantiator();

    public abstract MapInstantiatorImplementor<?, ?> getMapInstantiator();

    protected final ContainerAccumulator<?> createValueContainerAccumulator(Comparator<Object> comparator) {
        if (getElementCollectionType() == null) {
            return null;
        }
        boolean forcedUnique = false;
        PluralObjectFactory<? extends Collection<?>> instance;
        //CHECKSTYLE:OFF: FallThrough
        switch (getElementCollectionType()) {
            case COLLECTION:
            case LIST:
                forcedUnique = isElementCollectionForcedUnique();
                instance = ListFactory.INSTANCE;
                break;
            case SET:
                if (!isElementCollectionSorted()) {
                    instance = SetFactory.INSTANCE;
                    break;
                }
            case SORTED_SET:
                if (comparator == null) {
                    instance = SortedSetFactory.INSTANCE;
                } else {
                    instance = new SortedSetFactory(comparator);
                    comparator = null;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported element collection type: " + getElementCollectionType());
        }
        //CHECKSTYLE:ON: FallThrough
        if (isFilterNulls()) {
            return new NullFilteringCollectionAccumulator(instance, forcedUnique, comparator);
        } else {
            return new SimpleCollectionAccumulator(instance, forcedUnique, comparator);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final CollectionInstantiatorImplementor<?, ?> createCollectionInstantiator(MetamodelBuildingContext context, PluralObjectFactory<? extends Collection<?>> collectionFactory, boolean indexed, boolean sorted, boolean ordered, Comparator comparator) {
        if (indexed) {
            if (isForcedUnique()) {
                context.addError("Forcing uniqueness for indexed attribute is invalid at the " + getLocation());
            }
            if (comparator != null) {
                context.addError("Comparator can't be defined for indexed attribute at the " + getLocation());
            }
            return new ListCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), true, isOptimizeCollectionActionsEnabled(), false, context.isStrictCascadingCheck(), null);
        } else {
            if (sorted) {
                return new SortedSetCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck(), comparator);
            } else {
                if (getCollectionType() == PluralAttribute.CollectionType.SET) {
                    if (comparator != null) {
                        context.addError("Comparator can't be defined for non-sorted set attribute at the " + getLocation());
                    }
                    if (ordered) {
                        return new OrderedSetCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck());
                    } else {
                        return new UnorderedSetCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck());
                    }
                } else if (getCollectionType() == PluralAttribute.CollectionType.LIST) {
                    return new ListCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), false, isOptimizeCollectionActionsEnabled(), isForcedUnique(), context.isStrictCascadingCheck(), comparator);
                } else {
                    return new OrderedCollectionInstantiator((PluralObjectFactory<Collection<?>>) collectionFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), isForcedUnique(), context.isStrictCascadingCheck(), comparator);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final MapInstantiatorImplementor<?, ?> createMapInstantiator(MetamodelBuildingContext context, PluralObjectFactory<? extends Map<?, ?>> mapFactory, boolean sorted, boolean ordered, Comparator comparator) {
        if (sorted) {
            return new SortedMapInstantiator((PluralObjectFactory<Map<?, ?>>) mapFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck(), comparator);
        } else if (ordered) {
            return new OrderedMapInstantiator((PluralObjectFactory<Map<?, ?>>) mapFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck());
        } else {
            return new UnorderedMapInstantiator((PluralObjectFactory<Map<?, ?>>) mapFactory, getAllowedSubtypes(), getParentRequiringUpdateSubtypes(), getParentRequiringCreateSubtypes(), isUpdatable(), isOptimizeCollectionActionsEnabled(), context.isStrictCascadingCheck());
        }
    }

    @Override
    public final MappingType getMappingType() {
        return mappingType;
    }

    public final boolean isQueryParameter() {
        return mappingType == MappingType.PARAMETER;
    }

    public final boolean isId() {
        return id;
    }

    public final SubqueryProviderFactory getSubqueryProviderFactory() {
        return subqueryProviderFactory;
    }

    public final Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    public final String getSubqueryExpression() {
        return subqueryExpression;
    }

    public final String getSubqueryAlias() {
        return subqueryAlias;
    }

    public CorrelationProviderFactory getCorrelationProviderFactory() {
        return correlationProviderFactory;
    }

    public final Class<? extends CorrelationProvider> getCorrelationProvider() {
        return correlationProvider;
    }

    public final String getCorrelationBasis() {
        return correlationBasis;
    }

    public final String getCorrelationResult() {
        return correlationResult;
    }

    public Expression getCorrelationBasisExpression() {
        return correlationBasisExpression;
    }

    public Expression getCorrelationResultExpression() {
        return correlationResultExpression;
    }

    public final FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public final int getBatchSize() {
        return batchSize;
    }

    public final List<OrderByItem> getOrderByItems() {
        return orderByItems;
    }

    public final String getLimitExpression() {
        return limitExpression;
    }

    public final String getOffsetExpression() {
        return offsetExpression;
    }

    public final String getMapping() {
        return mapping;
    }

    @Override
    public final boolean isSubquery() {
        return mappingType == MappingType.SUBQUERY;
    }

    @Override
    public final ManagedViewTypeImplementor<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public final Class<Y> getJavaType() {
        return javaType;
    }

    @Override
    public Class<?> getConvertedJavaType() {
        return convertedJavaType;
    }

    @Override
    public final String[] getFetches() {
        return fetches;
    }
}
