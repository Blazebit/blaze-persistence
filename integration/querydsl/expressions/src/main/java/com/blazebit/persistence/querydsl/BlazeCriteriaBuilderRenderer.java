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

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingSetOperationBuilder;
import com.blazebit.persistence.BaseSubqueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DistinctBuilder;
import com.blazebit.persistence.FinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.FinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.From;
import com.blazebit.persistence.FromBaseBuilder;
import com.blazebit.persistence.FromBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.GroupByBuilder;
import com.blazebit.persistence.HavingBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.OngoingSetOperationBuilder;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.Queryable;
import com.blazebit.persistence.SelectBaseCTECriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SetOperationBuilder;
import com.blazebit.persistence.StartOngoingSetOperationBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.WindowBuilder;
import com.blazebit.persistence.WindowContainerBuilder;
import com.blazebit.persistence.WindowFrameBetweenBuilder;
import com.blazebit.persistence.WindowFrameBuilder;
import com.blazebit.persistence.WindowFrameExclusionBuilder;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.querydsl.core.JoinExpression;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.CollectionExpressionBase;
import com.querydsl.jpa.JPAQueryMixin;
import com.querydsl.jpa.JPQLTemplates;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.blazebit.persistence.querydsl.JPQLNextOps.BIND;
import static com.blazebit.persistence.querydsl.JPQLNextOps.LEFT_NESTED_SET_OPERATIONS;
import static com.blazebit.persistence.querydsl.JPQLNextOps.SET_UNION;
import static com.blazebit.persistence.querydsl.JPQLNextOps.WITH_RECURSIVE_ALIAS;
import static com.blazebit.persistence.querydsl.SetOperationFlag.getSetOperationFlag;

/**
 * A class for rendering a {@link BlazeJPAQuery} to a {@link CriteriaBuilder}
 * @param <T> Query result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class BlazeCriteriaBuilderRenderer<T> {

    private static final Logger LOG = Logger.getLogger(BlazeCriteriaBuilderRenderer.class.getName());

    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private final EntityManager entityManager;
    private final JPQLNextSerializer serializer;
    private final Map<Object, String> constantToLabel = new IdentityHashMap<>();
    private Map<Expression<?>, String> subQueryToLabel = new IdentityHashMap<>();
    private final List<SubqueryInitiator<?>> subqueryInitiatorStack = new ArrayList<SubqueryInitiator<?>>();
    private List<Path<?>> cteAliases;
    private CriteriaBuilder<T> criteriaBuilder;

    public BlazeCriteriaBuilderRenderer(CriteriaBuilderFactory criteriaBuilderFactory, EntityManager entityManager, JPQLTemplates templates) {
        this.serializer = new JPQLNextExpressionSerializer(templates, entityManager);
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityManager = entityManager;
    }

    public Queryable<T, ?> render(Expression<?> expression) {
        this.criteriaBuilder = (CriteriaBuilder) criteriaBuilderFactory.create(entityManager, Object.class);
        return (Queryable<T, ?>) serializeSubQuery(this.criteriaBuilder, expression);
    }

    private Object serializeSubQuery(Object criteriaBuilder, Expression<?> expression) {
        Object result = expression.accept(new Visitor<Object, Object>() {

            @Override
            public Object visit(Constant<?> constant, Object criteriaBuilder) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object visit(FactoryExpression<?> factoryExpression, Object criteriaBuilder) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object visit(Operation<?> setOperation, Object criteriaBuilder) {
                Expression<?> lhs = setOperation.getArg(0);
                SubQueryExpression<?> lhsSubquery = lhs.accept(GetSubQueryVisitor.INSTANCE, null);
                SetOperationFlag setOperationFlag = lhsSubquery != null ? getSetOperationFlag(lhsSubquery.getMetadata()) : null;
                boolean lhsNestedSet = setOperationFlag != null && LEFT_NESTED_SET_OPERATIONS.contains(setOperation.getOperator());

                if (lhsNestedSet) {
                    if (criteriaBuilder instanceof StartOngoingSetOperationBuilder) {
                        StartOngoingSetOperationBuilder<?, ?, ?> ob = (StartOngoingSetOperationBuilder<?, ?, ?>) criteriaBuilder;
                        criteriaBuilder = ob.startSet();
                    } else if (criteriaBuilder instanceof SubqueryInitiator) {
                        SubqueryInitiator<?> subqueryInitiator = (SubqueryInitiator<?>) criteriaBuilder;
                        criteriaBuilder = subqueryInitiator.startSet();
                    } else {
                        criteriaBuilder = criteriaBuilderFactory.startSet(entityManager, Object.class);
                    }

                    criteriaBuilder = setOperationFlag.getFlag().accept(this, criteriaBuilder);

                    if (criteriaBuilder instanceof OngoingSetOperationBuilder) {
                        criteriaBuilder = ((OngoingSetOperationBuilder<?, ?, ?>) criteriaBuilder).endSetWith();
                        renderOrderBy(lhsSubquery.getMetadata(), (OrderByBuilder<?>) criteriaBuilder);
                        renderModifiers(lhsSubquery.getMetadata().getModifiers(), (LimitBuilder<?>) criteriaBuilder);
                        criteriaBuilder = ((BaseOngoingFinalSetOperationBuilder) criteriaBuilder).endSet();
                    } else {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    criteriaBuilder = lhs.accept(this, criteriaBuilder);
                }

                Expression<?> rhs = setOperation.getArg(1);
                SubQueryExpression<?> rhsSubquery = rhs.accept(GetSubQueryVisitor.INSTANCE, null);
                setOperationFlag = rhsSubquery != null ? getSetOperationFlag(rhsSubquery.getMetadata()) : null;
                boolean isNestedSet = setOperationFlag != null;
                SetOperationBuilder<?,?> setOperationBuilder = (SetOperationBuilder<?,?>) criteriaBuilder;

                switch ((JPQLNextOps) setOperation.getOperator()) {
                    //CHECKSTYLE:OFF: FallThrough
                    case SET_UNION:
                    case LEFT_NESTED_SET_UNION:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startUnion() : setOperationBuilder.union();
                        break;
                    case SET_UNION_ALL:
                    case LEFT_NESTED_SET_UNION_ALL:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startUnionAll() : setOperationBuilder.unionAll();
                        break;
                    case SET_EXCEPT:
                    case LEFT_NESTED_SET_EXCEPT:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startExcept() : setOperationBuilder.except();
                        break;
                    case SET_EXCEPT_ALL:
                    case LEFT_NESTED_SET_EXCEPT_ALL:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startExceptAll() : setOperationBuilder.exceptAll();
                        break;
                    case SET_INTERSECT:
                    case LEFT_NESTED_SET_INTERSECT:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startIntersect() : setOperationBuilder.intersect();
                        break;
                    case SET_INTERSECT_ALL:
                    case LEFT_NESTED_SET_INTERSECT_ALL:
                        criteriaBuilder = isNestedSet ?
                                setOperationBuilder.startIntersectAll() : setOperationBuilder.intersectAll();
                        break;
                    default:
                        throw new UnsupportedOperationException("No support for set operation " + setOperation.getOperator());
                    //CHECKSTYLE:ON: FallThrough
                }

                if (isNestedSet) {
                    criteriaBuilder = setOperationFlag.getFlag().accept(this, criteriaBuilder);

                    if (criteriaBuilder instanceof OngoingSetOperationBuilder) {
                        criteriaBuilder = ((OngoingSetOperationBuilder<?, ?, ?>) criteriaBuilder).endSetWith();
                        renderOrderBy(rhsSubquery.getMetadata(), (OrderByBuilder<?>) criteriaBuilder);
                        renderModifiers(rhsSubquery.getMetadata().getModifiers(), (LimitBuilder<?>) criteriaBuilder);
                        criteriaBuilder = ((BaseOngoingFinalSetOperationBuilder) criteriaBuilder).endSet();
                    } else {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    criteriaBuilder = rhs.accept(this, criteriaBuilder);
                }

                return criteriaBuilder;
            }

            @Override
            public Object visit(ParamExpression<?> paramExpression, Object criteriaBuilder) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object visit(Path<?> path, Object criteriaBuilder) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object visit(SubQueryExpression<?> subQuery, Object criteriaBuilder) {
                QueryMetadata subQueryMetadata = subQuery.getMetadata();

                SetOperationFlag setOperationFlag = getSetOperationFlag(subQueryMetadata);
                if (setOperationFlag != null) {
                    return setOperationFlag.getFlag().accept(this, criteriaBuilder);
                }

                renderCTEs(subQueryMetadata);

                criteriaBuilder = renderJoins(subQueryMetadata, (FromBaseBuilder) criteriaBuilder);
                criteriaBuilder = renderNamedWindows(subQueryMetadata, (WindowContainerBuilder) criteriaBuilder);
                renderDistinct(subQueryMetadata, (DistinctBuilder<?>) criteriaBuilder);
                renderWhere(subQueryMetadata, (WhereBuilder<?>) criteriaBuilder);
                renderGroupBy(subQueryMetadata, (GroupByBuilder<?>) criteriaBuilder);
                renderHaving(subQueryMetadata, (HavingBuilder<?>) criteriaBuilder);

                Expression<?> select = subQueryMetadata.getProjection();
                if (select instanceof FactoryExpression<?> && criteriaBuilder instanceof FullQueryBuilder<?, ?>) {
                    FactoryExpression<T> factoryExpression = (FactoryExpression<T>) select;
                    FullQueryBuilder<?, ?> fullQueryBuilder = (FullQueryBuilder<?, ?>) criteriaBuilder;
                    criteriaBuilder = fullQueryBuilder.selectNew(new FactoryExpressionObjectBuilder(factoryExpression));

                } else {
                    List<? extends Expression<?>> projection = expandProjection(subQueryMetadata.getProjection());

                    if (criteriaBuilder instanceof SelectBaseCTECriteriaBuilder) {
                        SelectBaseCTECriteriaBuilder<?> selectBaseCriteriaBuilder = (SelectBaseCTECriteriaBuilder<?>) criteriaBuilder;

                        boolean bindEntity = projection.size() == 1 && subQueryMetadata.getJoins().get(0).getTarget().accept(new JoinTargetAliasPathResolver(), null).equals(projection.get(0));

                        if (bindEntity) {
                            EntityMetamodel metamodel = criteriaBuilderFactory.getService(EntityMetamodel.class);
                            Path<?> pathExpression = (Path<?>) projection.get(0);

                            ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, pathExpression.getType());
                            Map<String, ? extends ExtendedAttribute<?, ?>> ownedSingularAttributes = managedType.getOwnedSingularAttributes();

                            for (Map.Entry<String, ? extends ExtendedAttribute<?,?>> ownedSingularAttribute : ownedSingularAttributes.entrySet()) {
                                String attributeName = ownedSingularAttribute.getKey();
                                ExtendedAttribute<?, ?> attribute = ownedSingularAttribute.getValue();

                                if (!JpaMetamodelUtils.isAssociation(attribute.getAttribute())) {
                                    final SelectBuilder<?> bindBuilder = selectBaseCriteriaBuilder.bind(attributeName);
                                    BeanPath<?> beanPath = new BeanPath<Object>(attribute.getElementClass(), pathExpression, attributeName);
                                    setExpressionSubqueries(beanPath, null,  bindBuilder, SelectBuilderExpressionSetter.INSTANCE);
                                }
                            }
                        } else {
                            for (int i = 0; i < projection.size(); i++) {
                                Expression<?> projExpression = projection.get(i);

                                BindResolverContext bindResolverContext = new BindResolverContext();
                                projExpression = projExpression.accept(BindResolver.INSTANCE, bindResolverContext);
                                Path<?> cteAttribute = bindResolverContext.getCteAttribute();
                                String alias = bindResolverContext.getAliasString();

                                if (cteAttribute == null && cteAliases != null) {
                                    cteAttribute = cteAliases.get(i);
                                }

                                if (cteAttribute != null) {
                                    Path<?> cteEntityPath = cteAttribute.getRoot();
                                    String relativeCteAttributePath = relativePathString(cteEntityPath, cteAttribute);
                                    final SelectBuilder<?> bindBuilder = selectBaseCriteriaBuilder.bind(relativeCteAttributePath);
                                    setExpressionSubqueries(projExpression, alias,  bindBuilder, SelectBuilderExpressionSetter.INSTANCE);
                                } else {
                                    throw new UnsupportedOperationException("Select statement should be bound to any CTE attribute");
                                }
                            }
                        }
                    } else {
                        for (Expression<?> selection : projection) {
                            renderSingleSelect(selection, (SelectBuilder<?>) criteriaBuilder);
                        }
                    }
                }

                renderOrderBy(subQueryMetadata, (OrderByBuilder<?>) criteriaBuilder);
                renderParameters(subQueryMetadata, (ParameterHolder<?>) criteriaBuilder);
                renderConstants((ParameterHolder<?>) criteriaBuilder);
                renderModifiers(subQueryMetadata.getModifiers(), (LimitBuilder<?>) criteriaBuilder);
                return criteriaBuilder;
            }

            @Override
            public Object visit(TemplateExpression<?> templateExpression, Object criteriaBuilder) {
                throw new UnsupportedOperationException();
            }
        }, criteriaBuilder);

        if (result instanceof BaseOngoingSetOperationBuilder) {
            result = ((BaseOngoingSetOperationBuilder<?, ?, ?>) result).endSet();
        }

        if ((result instanceof FinalSetOperationCriteriaBuilder ||
            result instanceof FinalSetOperationCTECriteriaBuilder ||
            result instanceof FinalSetOperationSubqueryBuilder) &&
            expression instanceof SubQueryExpression<?>) {
            QueryMetadata metadata = ((SubQueryExpression<?>) expression).getMetadata();
            renderOrderBy(metadata, (OrderByBuilder<?>) result);
            renderModifiers(metadata.getModifiers(), (LimitBuilder<?>) result);
        }

        return result;
    }

    private void renderCTEs(QueryMetadata subQueryMetadata) {
        for (QueryFlag queryFlag : subQueryMetadata.getFlags()) {
            Expression<?> flag = queryFlag.getFlag();
            Position position = queryFlag.getPosition();
            if (position == Position.WITH) {
                flag.accept(serializer, null);
            }
        }
    }

    private <W extends WindowContainerBuilder<W>> W renderNamedWindows(QueryMetadata subQueryMetadata, W windowContainerBuilder) {
        for (QueryFlag queryFlag : subQueryMetadata.getFlags()) {
            Expression<?> flag = queryFlag.getFlag();
            Position position = queryFlag.getPosition();
            if (position == Position.AFTER_HAVING) {
                windowContainerBuilder = renderWindowFlag(queryFlag, windowContainerBuilder);
            }
        }
        return windowContainerBuilder;
    }

    private <X extends WindowContainerBuilder<X>> X renderWindowFlag(QueryFlag queryFlag, WindowContainerBuilder<X> windowContainerBuilder) {
        return queryFlag.getFlag().accept(new WindowContainerBuilderDefaultVisitorImpl<X>(windowContainerBuilder), null);
    }

    private void renderModifiers(QueryModifiers modifiers, LimitBuilder<?> criteriaBuilder) {
        if (modifiers != null) {
            if (modifiers.getLimitAsInteger() != null) {
                criteriaBuilder.setMaxResults(modifiers.getLimitAsInteger());
            }
            if (modifiers.getOffsetAsInteger() != null) {
                criteriaBuilder.setFirstResult(modifiers.getOffsetAsInteger());
            }
        }
    }

    private void renderConstants(ParameterHolder<?> criteriaBuilder) {
        for (Map.Entry<Object, String> entry : constantToLabel.entrySet()) {
            criteriaBuilder.setParameter(entry.getValue(), entry.getKey());
        }
    }

    private void renderParameters(QueryMetadata metadata, ParameterHolder<?> criteriaBuilder) {
        for (Map.Entry<ParamExpression<?>, Object> entry : metadata.getParams().entrySet()) {
            criteriaBuilder.setParameter(entry.getKey().getName(), entry.getValue());
        }
    }

    private void renderOrderBy(QueryMetadata metadata, OrderByBuilder<?> criteriaBuilder) {
        for (OrderSpecifier<?> orderSpecifier : metadata.getOrderBy()) {
            renderOrderSpecifier(orderSpecifier, criteriaBuilder);
        }
    }

    private void renderHaving(QueryMetadata metadata, final HavingBuilder<?> criteriaBuilder) {
        if (metadata.getHaving() != null) {
            setExpressionSubqueries(metadata.getHaving(), null, criteriaBuilder, HavingBuilderExpressionSetter.INSTANCE);
        }
    }

    private void renderGroupBy(QueryMetadata metadata, GroupByBuilder<?> criteriaBuilder) {
        for (Expression<?> groupByExpression : metadata.getGroupBy()) {
            criteriaBuilder.groupBy(renderExpression(groupByExpression));
        }
    }

    private void renderWhere(QueryMetadata metadata, final WhereBuilder<?> criteriaBuilder) {
        if (metadata.getWhere() != null) {
            setExpressionSubqueries(metadata.getWhere(), null, criteriaBuilder, WhereBuilderExpressionSetter.INSTANCE);
        }
    }

    private void renderDistinct(QueryMetadata metadata, DistinctBuilder<?> criteriaBuilder) {
        if (metadata.isDistinct()) {
            criteriaBuilder.distinct();
        }
    }

    private <X extends FromBuilder<X>> X renderJoins(QueryMetadata metadata, FromBaseBuilder<X> fromBuilder) {
        X criteriaBuilder = null;
        for (final JoinExpression joinExpression : metadata.getJoins()) {
            boolean fetch = joinExpression.hasFlag(JPAQueryMixin.FETCH);
            boolean hasCondition = joinExpression.getCondition() != null;
            Expression<?> target = joinExpression.getTarget();
            String alias = null;

            if (target instanceof Operation<?>) {
                Operation<?> operation = (Operation<?>) target;
                if (operation.getOperator() == Ops.ALIAS) {
                    target = operation.getArg(0);
                    alias = ((Path<?>) operation.getArg(1)).getMetadata().getName();
                }
            }

            if (target instanceof ValuesExpression<?>) {
                ValuesExpression<?> valuesExpression = (ValuesExpression<?>) target;
                Class type = valuesExpression.getRoot().getType();
                String name = valuesExpression.getAlias().getMetadata().getName();
                Collection<?> elements = valuesExpression.getElements();

                if (! valuesExpression.getMetadata().isRoot()) {
                    String attribute = relativePathString(valuesExpression.getRoot(), valuesExpression);
                    if ( valuesExpression.isIdentifiable() ) {
                        criteriaBuilder = (X) fromBuilder.fromIdentifiableValues(type, attribute, name, elements);
                    } else {
                        criteriaBuilder = (X) fromBuilder.fromValues(type, attribute, name, elements);
                    }
                } else if ( valuesExpression.isIdentifiable() ) {
                    criteriaBuilder = (X) fromBuilder.fromIdentifiableValues(type, name, elements);
                } else {
                    criteriaBuilder = (X) fromBuilder.fromValues(type, name, elements);
                }
            } else if (target instanceof Path<?>) {
                Path<?> entityPath = (Path<?>) target;
                if (alias == null) {
                    alias = entityPath.getMetadata().getName();
                }
                boolean entityJoin = entityPath.getMetadata().isRoot();

                switch (joinExpression.getType()) {
                    case DEFAULT:
                        if (fromBuilder instanceof FromBuilder) {
                            criteriaBuilder = (X) fromBuilder;
                            From from = criteriaBuilder.getFrom(alias);

                            // TODO find a clean way to detect FROM clauses that are already set by fromEntitySubquery...
                            if (from != null) {
                                if (entityPath instanceof CollectionExpressionBase<?,?> &&
                                        ((CollectionExpressionBase<?, ?>) entityPath).getElementType().equals(from.getJavaType()) ||
                                        from.getJavaType().equals(entityPath.getType())) {
                                    break;
                                }
                            }
                        }

                        if (entityJoin) {
                            criteriaBuilder = fromBuilder.from(entityPath.getType(), alias);
                        } else {
                            String collectionExpression = renderExpression(entityPath);
                            if (fromBuilder instanceof BaseSubqueryBuilder) {
                                criteriaBuilder = (X) ((BaseSubqueryBuilder<?>) fromBuilder).from(collectionExpression, alias);
                            } else if (fromBuilder instanceof SubqueryInitiator<?>) {
                                criteriaBuilder = (X) ((SubqueryInitiator<?>) fromBuilder).from(collectionExpression, alias);
                            } else {
                                throw new IllegalArgumentException(collectionExpression + "  join not supported here");
                            }
                        }

                        break;
                    default:
                        JoinType joinType = getJoinType(joinExpression);

                        if (hasCondition && fetch) {
                            LOG.warning("Fetch is ignored due to on-clause");
                        }

                        if (entityJoin) {
                            if (!hasCondition) {
                                throw new IllegalStateException("No on-clause for entity join!");
                            }
                            final JoinOnBuilder<X> xJoinOnBuilder = criteriaBuilder.joinOn(entityPath.getType(), alias, joinType);
                            setExpressionSubqueries(joinExpression.getCondition(), null, xJoinOnBuilder, JoinOnBuilderExpressionSetter.INSTANCE);
                        } else if (!hasCondition) {
                            if (fetch) {
                                ((FullQueryBuilder<?, ?>) criteriaBuilder).joinDefault(renderExpression(entityPath), alias, joinType, fetch);
                            } else {
                                criteriaBuilder.joinDefault(renderExpression(entityPath), alias, joinType);
                            }
                        } else {
                            final JoinOnBuilder<X> xJoinOnBuilder = criteriaBuilder.joinOn(renderExpression(entityPath), alias, joinType);
                            setExpressionSubqueries(joinExpression.getCondition(), null, xJoinOnBuilder, JoinOnBuilderExpressionSetter.INSTANCE);
                        }

                        break;
                }

            } else if (target instanceof SubQueryExpression)  {
                switch (joinExpression.getType()) {
                    case DEFAULT: {
                        FullSelectCTECriteriaBuilder<X> xFullSelectCTECriteriaBuilder = fromBuilder.fromSubquery(target.getType(), alias);
                        Object o = serializeSubQuery(xFullSelectCTECriteriaBuilder, target);
                        criteriaBuilder = o instanceof FinalSetOperationCTECriteriaBuilder ?
                                ((FinalSetOperationCTECriteriaBuilder<X>) o).end() :
                                ((FullSelectCTECriteriaBuilder<X>) o).end();
                        break;
                    }
                    default: {
                        JoinType joinType = getJoinType(joinExpression);
                        boolean isLateral = joinExpression.hasFlag(AbstractBlazeJPAQuery.LATERAL);

                        if (fetch) {
                            LOG.warning("Fetch is ignored due to subquery entity join!");
                        }

                        SubQueryExpression<?> subQueryExpression = target.accept(new FirstSubqueryResolver(), null);
                        Path<?> fromPath = subQueryExpression.getMetadata().getJoins().get(0).getTarget().accept(new FirstSubqueryTargetPathResolver(), null);
                        boolean entityJoin = fromPath.getMetadata().isRoot();

                        if (hasCondition) {
                            FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinOnBuilderFullSelectCTECriteriaBuilder;

                            if (isLateral) {
                                String subqueryAlias = subQueryExpression.getMetadata().getJoins().get(0).getTarget().accept(new JoinTargetAliasPathResolver(), null).getMetadata().getName();
                                if (entityJoin) {
                                    joinOnBuilderFullSelectCTECriteriaBuilder = criteriaBuilder.joinLateralOnSubquery(target.getType(), alias, joinType);
                                } else {
                                    joinOnBuilderFullSelectCTECriteriaBuilder = criteriaBuilder.joinLateralOnSubquery(renderExpression(fromPath), alias, subqueryAlias, joinType);
                                }
                            } else {
                                if (!entityJoin) {
                                    throw new IllegalStateException("Entity join to association");
                                }
                                joinOnBuilderFullSelectCTECriteriaBuilder = criteriaBuilder.joinOnSubquery(target.getType(), alias, joinType);
                            }

                            Object o = serializeSubQuery(joinOnBuilderFullSelectCTECriteriaBuilder, target);
                            final JoinOnBuilder<X> joinOnBuilder = o instanceof FinalSetOperationCTECriteriaBuilder ?
                                    ((FinalSetOperationCTECriteriaBuilder<JoinOnBuilder<X>>) o).end() :
                                    ((FullSelectCTECriteriaBuilder<JoinOnBuilder<X>>) o).end();
                            criteriaBuilder = (X) setExpressionSubqueries(joinExpression.getCondition(), null, joinOnBuilder, JoinOnBuilderExpressionSetter.INSTANCE);
                        } else {
                            if (isLateral) {
                                FullSelectCTECriteriaBuilder<X> xFullSelectCTECriteriaBuilder;
                                String subqueryAlias = subQueryExpression.getMetadata().getJoins().get(0).getTarget().accept(new JoinTargetAliasPathResolver(), null).getMetadata().getName();

                                if (entityJoin) {
                                    xFullSelectCTECriteriaBuilder = criteriaBuilder.joinLateralSubquery(target.getType(), alias, joinType);
                                } else {
                                    xFullSelectCTECriteriaBuilder = criteriaBuilder.joinLateralSubquery(renderExpression(fromPath), alias, subqueryAlias, joinType);
                                }

                                Object o = serializeSubQuery(xFullSelectCTECriteriaBuilder, target);
                                criteriaBuilder = o instanceof FinalSetOperationCTECriteriaBuilder ?
                                        ((FinalSetOperationCTECriteriaBuilder<X>) o).end() :
                                        ((FullSelectCTECriteriaBuilder<X>) o).end();

                            } else {
                                throw new IllegalStateException("No on-clause for subquery entity join!");
                            }
                        }
                        break;
                    }
                }
            } else {
                throw new UnsupportedOperationException("Joins for " + target + " is not yet implemented");
            }
        }

        return criteriaBuilder;
    }

    private List<? extends Expression<?>> expandProjection(Expression<?> expr) {
        if (expr instanceof FactoryExpression) {
            return ((FactoryExpression<?>) expr).getArgs();
        } else {
            return Collections.singletonList(expr);
        }
    }

    private JoinType getJoinType(JoinExpression joinExpression) {
        switch (joinExpression.getType()) {
            case INNERJOIN:
            case JOIN:
                return JoinType.INNER;
            case LEFTJOIN:
                return JoinType.LEFT;
            case RIGHTJOIN:
                return JoinType.RIGHT;
            case FULLJOIN:
                return JoinType.FULL;
            default:
                throw new IllegalArgumentException("Join has no equivalent JoinType");
        }
    }

    private void renderOrderSpecifier(OrderSpecifier<?> orderSpecifier, OrderByBuilder<?> criteriaBuilder) {
        String orderExpression = renderExpression(orderSpecifier.getTarget());
        boolean ascending = orderSpecifier.isAscending();
        switch (orderSpecifier.getNullHandling()) {
            case Default:
                criteriaBuilder.orderBy(orderExpression, ascending);
                break;
            case NullsFirst:
                criteriaBuilder.orderBy(orderExpression, ascending, true);
                break;
            case NullsLast:
                criteriaBuilder.orderBy(orderExpression, ascending, false);
                break;
            default:
                throw new IllegalArgumentException("Null handling not implemented for " + orderSpecifier.getNullHandling());
        }
    }

    private void renderSingleSelect(Expression<?> select, final SelectBuilder<?> selectBuilder) {
        String alias = null;

        if (select instanceof Operation<?>) {
            Operation<?> operation = (Operation<?>) select;
            if (operation.getOperator() == Ops.ALIAS) {
                select = operation.getArg(0);
                alias = ((Path<?>) operation.getArg(1)).getMetadata().getName();
            }
        }

        setExpressionSubqueries(select, alias, selectBuilder, SelectBuilderExpressionSetter.INSTANCE);
    }

    private String renderExpression(Expression<?> select) {
        serializer.clearBuffer();
        select.accept(serializer, null);
        return serializer.takeBuffer();
    }

    private void pushSubqueryInitiator(SubqueryInitiator<?> subqueryInitiator) {
        subqueryInitiatorStack.add(subqueryInitiator);
    }

    private void popSubqueryInitiator() {
        subqueryInitiatorStack.remove(subqueryInitiatorStack.size() - 1);
    }

    private Map<Expression<?>, String> takeSubQueryToLabelMap() {
        Map<Expression<?>, String> subQueryToLabel = this.subQueryToLabel;
        if (subQueryToLabel.isEmpty()) {
            return Collections.emptyMap();
        }
        this.subQueryToLabel = new IdentityHashMap<>();
        return subQueryToLabel;
    }

    /**
     * Drop in for java.util.Function
     * @param <T> argument type
     * @param <R> return type
     */
    private interface Function<T, R> {

        // Remove when compiling against Java 8 in BP1.6

        /**
         * Drop in for java.util.Function, TODO: fix after JDK 8
         *
         * @param t argument
         * @return return value
         */
        R apply(T t);

    }

    /**
     * Helper class to serialize expressions for a certain type of builder.
     *
     * @param <B> Builder type
     * @param <X> Builder result type
     * @author Jan-Willem Gmelig Meyling
     */
    private interface ExpressionSetter<B, X> {

        /**
         * Set an expression without subqueries.
         *
         * @param builder the builder
         * @param expression the expression to set
         * @param alias an optional alias to use
         * @return the builder result
         */
        X setExpression(B builder, String expression, String alias);

        /**
         * Set an expression with subqueries.
         *
         * @param builder the builder
         * @param expression the expression to set
         * @param alias an optional alias to use
         * @return the builder result
         */
        MultipleSubqueryInitiator<? extends X> setExpressionSubqueries(B builder, String expression, String alias);

    }

    /**
     * Helper class to serialize expressions for SelectBuilder instances.
     *
     * @param <X> Builder result type
     * @author Jan-Willem Gmelig Meyling
     */
    private static class SelectBuilderExpressionSetter<X> implements ExpressionSetter<SelectBuilder<X>, X> {

        private static final SelectBuilderExpressionSetter INSTANCE = new SelectBuilderExpressionSetter();

        @Override
        public X setExpression(SelectBuilder<X> builder, String expression, String alias) {
            return alias != null ? builder.select(expression, alias) : builder.select(expression);
        }

        @Override
        public MultipleSubqueryInitiator<? extends X> setExpressionSubqueries(SelectBuilder<X> builder, String expression, String alias) {
            return alias != null ? builder.selectSubqueries(expression, alias) : builder.selectSubqueries(expression);
        }

    }

    /**
     * Helper class to serialize expressions for JoinOnBuilder instances.
     *
     * @param <X> Builder result type
     * @author Jan-Willem Gmelig Meyling
     */
    private static class JoinOnBuilderExpressionSetter<X> implements ExpressionSetter<JoinOnBuilder<X>, X> {

        private static final JoinOnBuilderExpressionSetter INSTANCE = new JoinOnBuilderExpressionSetter();

        @Override
        public X setExpression(JoinOnBuilder<X> builder, String expression, String alias) {
            return builder.setOnExpression(expression);
        }

        @Override
        public MultipleSubqueryInitiator<? extends X> setExpressionSubqueries(JoinOnBuilder<X> builder, String expression, String alias) {
            return builder.setOnExpressionSubqueries(expression);
        }

    }

    /**
     * Helper class to serialize expressions for HavingBuilder instances.
     *
     * @param <X> Builder result type
     * @author Jan-Willem Gmelig Meyling
     */
    private static class HavingBuilderExpressionSetter<X extends HavingBuilder<X>> implements ExpressionSetter<HavingBuilder<X>, X> {

        private static final HavingBuilderExpressionSetter INSTANCE = new HavingBuilderExpressionSetter();

        @Override
        public X setExpression(HavingBuilder<X> builder, String expression, String alias) {
            return builder.setHavingExpression(expression);
        }

        @Override
        public MultipleSubqueryInitiator<? extends X> setExpressionSubqueries(HavingBuilder<X> builder, String expression, String alias) {
            return builder.setHavingExpressionSubqueries(expression);
        }

    }

    /**
     * Helper class to serialize expressions for WhereBuilder instances.
     *
     * @param <X> Builder result type
     * @author Jan-Willem Gmelig Meyling
     */
    private static class WhereBuilderExpressionSetter<X extends WhereBuilder<X>> implements ExpressionSetter<WhereBuilder<X>, X> {

        private static final WhereBuilderExpressionSetter INSTANCE = new WhereBuilderExpressionSetter();

        @Override
        public X setExpression(WhereBuilder<X> builder, String expression, String alias) {
            return builder.setWhereExpression(expression);
        }

        @Override
        public MultipleSubqueryInitiator<? extends X> setExpressionSubqueries(WhereBuilder<X> builder, String expression, String alias) {
            return builder.setWhereExpressionSubqueries(expression);
        }

    }

    private <B, X> X setExpressionSubqueries(Expression<?> expression, String alias, B builder, ExpressionSetter<B, X> expressionSetter) {
        String expressionString = renderExpression(expression);
        Map<Expression<?>, String> subQueryToLabel = takeSubQueryToLabelMap();
        if (subQueryToLabel.isEmpty()) {
            return expressionSetter.setExpression(builder, expressionString, alias);
        } else {
            MultipleSubqueryInitiator<? extends X> subqueryInitiator = expressionSetter.setExpressionSubqueries(builder, expressionString, alias);
            for (Map.Entry<Expression<?>, String> entry : subQueryToLabel.entrySet()) {
                SubqueryInitiator<?> initiator = subqueryInitiator.with(entry.getValue());
                pushSubqueryInitiator(initiator);
                Object o = serializeSubQuery(initiator, entry.getKey());

                if (o instanceof SubqueryBuilder) {
                    o = ((SubqueryBuilder<?>) o).end();
                } else if (o instanceof FinalSetOperationSubqueryBuilder) {
                    o = ((FinalSetOperationSubqueryBuilder<?>) o).end();
                }

                assert subqueryInitiator == o : "Expected SubqueryInitiator to return original MultipleSubqueryInitiator";
                popSubqueryInitiator();
            }
            return subqueryInitiator.end();
        }
    }


    private static String relativePathString(Path<?> root, Path<?> path) {
        StringBuilder pathString = new StringBuilder(path.getMetadata().getName().length());
        while (path.getMetadata().getParent() != null && ! path.equals(root)) {
            if (pathString.length() > 0) {
                pathString.insert(0, '.');
            }
            pathString.insert(0, path.getMetadata().getName());
            path = path.getMetadata().getParent();
        }
        return pathString.toString();
    }

    /**
     * Visitor that parses {@link JPQLNextOps#WITH_ALIAS}.
     */
    private static class CteAttributesVisitor extends DefaultVisitorImpl<List<Path<?>>, List<Path<?>>> {

        @Override
        public List<Path<?>> visit(Operation<?> operation, List<Path<?>> cteAliases) {
            switch ((Ops) (operation.getOperator())) {
                case SINGLETON:
                    operation.getArg(0).accept(this, cteAliases);
                    break;
                case LIST:
                    visit(operation.getArgs(), cteAliases);
                    break;
                default:
                    break;
            }
            return cteAliases;
        }

        @Override
        public List<Path<?>> visit(Path<?> expr, List<Path<?>> context) {
            context.add(expr);
            return context;
        }

        private void visit(List<Expression<?>> exprs, List<Path<?>> context) {
            for (Expression<?> e : exprs) {
                e.accept(this, context);
            }
        }

    }

    public CriteriaBuilder<T> getCriteriaBuilder() {
        return criteriaBuilder;
    }

    /**
     * Visitor that extracts the alias for a join target expression.
     */
    private static class JoinTargetAliasPathResolver extends DefaultVisitorImpl<Path<?>, Void> {
        @Override
        public Path<?> visit(Operation<?> operation, Void aVoid) {
            return operation.getArg(1).accept(this, aVoid);
        }

        @Override
        public Path<?> visit(Path<?> path, Void aVoid) {
            return path;
        }
    }

    /**
     * Visitor that returns the first default join path for the subquery.
     */
    private static class FirstSubqueryTargetPathResolver extends DefaultVisitorImpl<Path<?>, Object> {
        @Override
        public Path<?> visit(Operation<?> operation, Object o) {
            return operation.getArg(0).accept(this, o);
        }

        @Override
        public Path<?> visit(Path<?> path, Object o) {
            return path;
        }

        @Override
        public Path<?> visit(SubQueryExpression<?> subQueryExpression, Object o) {
            return subQueryExpression.getMetadata().getJoins().get(0).getTarget().accept(this, o);
        }
    }

    /**
     * Visitor that traverses left hand side of set operations until the first subquery expression is found.
     */
    private static class FirstSubqueryResolver extends DefaultVisitorImpl<SubQueryExpression<?>, Object> {
        @Override
        public SubQueryExpression<?> visit(Operation<?> operation, Object o) {
            return operation.getArg(0).accept(this, o);
        }

        @Override
        public SubQueryExpression<?> visit(SubQueryExpression<?> subQueryExpression, Object o) {
            SetOperationFlag setOperationFlag = getSetOperationFlag(subQueryExpression.getMetadata());
            if (setOperationFlag != null) {
                return setOperationFlag.getFlag().accept(this, o);
            }
            return subQueryExpression;
        }
    }

    /**
     *  Object builder implementation to construct QueryDSL factory expression instances.
     */
    private class FactoryExpressionObjectBuilder implements ObjectBuilder<T> {
        private final FactoryExpression<T> factoryExpression;

        public FactoryExpressionObjectBuilder(FactoryExpression<T> factoryExpression) {
            this.factoryExpression = factoryExpression;
        }

        @Override
        public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
            for (Expression<?> arg : factoryExpression.getArgs()) {
                renderSingleSelect(arg, selectBuilder);
            }
        }

        @Override
        public T build(Object[] tuple) {
            return factoryExpression.newInstance(tuple);
        }

        @Override
        public List<T> buildList(List<T> list) {
            return list;
        }
    }

    /**
     * Get an expression as Operation. This is useful for expressions that do not implement Operation,
     * but actually are treated as Operation during visiting.
     */
    private static class GetOperationVisitor extends DefaultVisitorImpl<Operation<?>, Void> {

        public static final GetOperationVisitor INSTANCE = new GetOperationVisitor();

        @Override
        public Operation<?> visit(Operation<?> operation, Void aVoid) {
            return operation;
        }

    }

    /**
     * Get an expression as SubQuery. This is useful for expressions that do not implement SubQuery,
     * but actually are treated as SubQuery during visiting.
     */
    private static class GetSubQueryVisitor extends DefaultVisitorImpl<SubQueryExpression<?>, Void> {

        public static final GetSubQueryVisitor INSTANCE = new GetSubQueryVisitor();

        @Override
        public SubQueryExpression<?> visit(SubQueryExpression<?> subQueryExpression, Void aVoid) {
            return subQueryExpression;
        }
    }

    /**
     * Adjusted local instance of JPQLNextSerializer that ensures serialized parameters and constants
     * are stored in the rendering context.
     */
    private class JPQLNextExpressionSerializer extends JPQLNextSerializer {

        private final JPQLTemplates templates;

        public JPQLNextExpressionSerializer(JPQLTemplates templates, EntityManager entityManager) {
            super(templates, entityManager);
            this.templates = templates;
        }

        @Override
        public void visitConstant(Object constant) {
            // TODO Handle in case operations
            boolean wrap = templates.wrapConstant(constant);
            if (wrap) {
                append("(");
            }
            append(":");
            String label = constantToLabel.get(constant);
            if (label == null) {
                label = "param_" + constantToLabel.size();
                constantToLabel.put(constant, label);
            }
            append(label);
            if (wrap) {
                append(")");
            }
        }

        @Override
        public Void visit(ParamExpression<?> param, Void context) {
            append(":").append(param.getName());
            return null;
        }

        @Override
        public Void visit(SubQueryExpression<?> query, Void context) {
            renderSubQueryExpression(query);
            return null;
        }

        private void renderSubQueryExpression(Expression<?> query) {
            String label = subQueryToLabel.get(query);
            if (label == null) {
                label = "generatedSubquery_" + (subQueryToLabel.size() + 1);
                subQueryToLabel.put(query, label);
            }
            serializer.append(label);
        }

        @Override
        protected void visitOperation(final Class<?> type, final Operator operator, final List<? extends Expression<?>> args) {
            if (operator instanceof JPQLNextOps) {
                switch ((JPQLNextOps) operator) {
                    case WITH_RECURSIVE_ALIAS:
                    case WITH_ALIAS:
                        boolean recursive = operator == WITH_RECURSIVE_ALIAS;
                        Expression<?> withColumns = args.get(0);
                        Expression<?> subQueryExpression = args.get(1);
                        withColumns.accept(this, null);
                        Class<?> cteType = withColumns.getType();

                        if (recursive) {
                            Operation<?> unionOperation = subQueryExpression.accept(GetOperationVisitor.INSTANCE, null);
                            if (unionOperation == null) {
                                SubQueryExpression<?> setSubquery = subQueryExpression.accept(GetSubQueryVisitor.INSTANCE, null);
                                QueryFlag setFlag = getSetOperationFlag(setSubquery.getMetadata());
                                unionOperation = setFlag.getFlag().accept(GetOperationVisitor.INSTANCE, null);
                            }

                            SubQueryExpression<?> subQuery = (SubQueryExpression<?>) unionOperation.getArg(0);
                            SelectRecursiveCTECriteriaBuilder<?> baseCriteriaBuilder =
                                    (SelectRecursiveCTECriteriaBuilder<?>)
                                            serializeSubQuery(criteriaBuilder.withRecursive(cteType), subQuery);
                            SelectCTECriteriaBuilder<?> recursiveCriteriaBuilder = unionOperation.getOperator() == SET_UNION ?
                                    baseCriteriaBuilder.union() : baseCriteriaBuilder.unionAll();
                            subQuery = (SubQueryExpression<?>) unionOperation.getArg(1);
                            ((SelectCTECriteriaBuilder<?>) serializeSubQuery(recursiveCriteriaBuilder, subQuery)).end();
                        } else {
                            FullSelectCTECriteriaBuilder<?> cteBuilder = criteriaBuilder.with(cteType);

                            Object result = serializeSubQuery(cteBuilder, subQueryExpression);

                            if (result instanceof FinalSetOperationCTECriteriaBuilder) {
                                ((FinalSetOperationCTECriteriaBuilder<?>) result).end();
                            } else if (result instanceof FullSelectCTECriteriaBuilder) {
                                ((FullSelectCTECriteriaBuilder<?>) result).end();
                            }
                        }
                        return;
                    case WITH_RECURSIVE_COLUMNS:
                    case WITH_COLUMNS:
                        cteAliases = args.get(1).accept(new CteAttributesVisitor(), new ArrayList<Path<?>>());
                        return;
                    default:
                        break;
                }
            }

            // JPQLSerializer calls serialize transitively,
            if (operator == Ops.EXISTS && args.get(0) instanceof SubQueryExpression) {
                append("EXISTS (");
                renderSubQueryExpression(args.get(0));
                append(")");
                return;
            }

            super.visitOperation(type, operator, args);
        }

        @Override
        public Void visit(Operation<?> expr, Void context) {
            if (expr.getOperator() instanceof JPQLNextOps) {
                switch ((JPQLNextOps) expr.getOperator()) {
                    case SET_UNION:
                    case SET_UNION_ALL:
                    case SET_INTERSECT:
                    case SET_INTERSECT_ALL:
                    case SET_EXCEPT:
                    case SET_EXCEPT_ALL:
                    case LEFT_NESTED_SET_UNION:
                    case LEFT_NESTED_SET_UNION_ALL:
                    case LEFT_NESTED_SET_INTERSECT:
                    case LEFT_NESTED_SET_INTERSECT_ALL:
                    case LEFT_NESTED_SET_EXCEPT:
                    case LEFT_NESTED_SET_EXCEPT_ALL:
                        renderSubQueryExpression(expr);
                        return null;
                    default:
                        break;
                }
            }

            return super.visit(expr, context);
        }

    }

    /**
     * Visitor for rendering window expressions.
     *
     * @param <X> Concrete window builder
     */
    private class WindowContainerBuilderDefaultVisitorImpl<X extends WindowContainerBuilder<X>> extends DefaultVisitorImpl<X, Object> {

        private boolean between;
        private boolean frameStartMode;
        private final WindowContainerBuilder<X> windowContainerBuilder;
        private WindowBuilder<X> window;
        private WindowFrameBuilder<X> rows;
        private WindowFrameBetweenBuilder<X> windowFrameBetweenBuilder;
        private WindowFrameExclusionBuilder<X> windowFrameExclusionBuilder;

        public WindowContainerBuilderDefaultVisitorImpl(WindowContainerBuilder<X> windowContainerBuilder) {
            this.windowContainerBuilder = windowContainerBuilder;
            between = false;
            frameStartMode = true;
        }

        @Override
        public X visit(Operation<?> operation, Object o) {
            List<Expression<?>> arguments = operation.getArgs();
            if (operation.getOperator() instanceof JPQLNextOps) {
                switch ((JPQLNextOps) operation.getOperator()) {
                    case WINDOW_NAME:
                        window = windowContainerBuilder.window(((Constant) arguments.get(0)).getConstant().toString());
                        arguments.get(1).accept(this, o);
                        break;
                    case WINDOW_BASE:
                        throw new UnsupportedOperationException("Named window extension is not supported in WindowContainerBuilder");
                    case WINDOW_ORDER_BY:
                        operation.getArgs().get(0).accept(new DefaultVisitorImpl<Object, Object>() {
                            @Override
                            public Object visit(Operation<?> operation, Object o) {
                                if (operation.getOperator() == Ops.LIST) {
                                    operation.getArg(0).accept(this, null);
                                    operation.getArg(1).accept(this, null);
                                    return null;
                                } else {
                                    throw new UnsupportedOperationException();
                                }
                            }

                            @Override
                            public Object visit(TemplateExpression<?> templateExpression, Object o) {
                                String nullRendering = templateExpression.getArgs().get(2).toString();
                                if ("".equals(nullRendering)) {
                                    window = window.orderBy(renderExpression((Expression) templateExpression.getArg(0)), templateExpression.getArg(1).equals(Order.ASC));
                                } else if ("NULLS FIRST".equals(nullRendering)) {
                                    window = window.orderBy(renderExpression((Expression) templateExpression.getArg(0)), templateExpression.getArg(1).equals(Order.ASC), true);
                                } else {
                                    window = window.orderBy(renderExpression((Expression) templateExpression.getArg(0)), templateExpression.getArg(1).equals(Order.ASC), false);
                                }
                                return null;
                            }
                        }, null);
                        break;
                    case WINDOW_PARTITION_BY:
                        window = operation.getArgs().get(0).accept(new Visitor<WindowBuilder<X>, WindowBuilder<X>>() {
                            @Override
                            public WindowBuilder<X> visit(Constant<?> expr, WindowBuilder<X> window) {
                                return window.partitionBy(renderExpression(expr));
                            }

                            @Override
                            public WindowBuilder<X> visit(FactoryExpression<?> expr, WindowBuilder<X> window) {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public WindowBuilder<X> visit(Operation<?> operation, WindowBuilder<X> window) {
                                if (operation.getOperator() == Ops.LIST) {
                                    window = operation.getArg(0).accept(this, window);
                                    return operation.getArg(1).accept(this, window);
                                } else {
                                    return window.partitionBy(renderExpression(operation));
                                }
                            }

                            @Override
                            public WindowBuilder<X> visit(ParamExpression<?> expr, WindowBuilder<X> window) {
                                return window.partitionBy(renderExpression(expr));
                            }

                            @Override
                            public WindowBuilder<X> visit(Path<?> expr, WindowBuilder<X> window) {
                                return window.partitionBy(renderExpression(expr));
                            }

                            @Override
                            public WindowBuilder<X> visit(SubQueryExpression<?> expr, WindowBuilder<X> window) {
                                return window.partitionBy(renderExpression(expr));
                            }

                            @Override
                            public WindowBuilder<X> visit(TemplateExpression<?> expr, WindowBuilder<X> window) {
                                return window.partitionBy(renderExpression(expr));
                            }
                        }, window);
                        break;
                    case WINDOW_ROWS:
                        rows = window.rows();
                        arguments.get(0).accept(this, o);
                        break;
                    case WINDOW_RANGE:
                        rows = window.range();
                        arguments.get(0).accept(this, o);
                        break;
                    case WINDOW_GROUPS:
                        rows = window.groups();
                        arguments.get(0).accept(this, o);
                        break;
                    case WINDOW_BETWEEN:
                        between = true;
                        arguments.get(0).accept(this, o);
                        arguments.get(1).accept(this, o);
                        break;
                    case WINDOW_UNBOUNDED_PRECEDING:
                        assert frameStartMode;
                        assert rows != null;
                        if (between) {
                            windowFrameBetweenBuilder = rows.betweenUnboundedPreceding();
                        } else {
                            windowFrameExclusionBuilder = rows.unboundedPreceding();
                        }
                        frameStartMode = false;
                        break;
                    case WINDOW_PRECEDING:
                        if (frameStartMode) {
                            assert rows != null;
                            if (between) {
                                windowFrameBetweenBuilder = rows.betweenPreceding(renderExpression(arguments.get(0)));
                            } else {
                                windowFrameExclusionBuilder = rows.preceding(renderExpression(arguments.get(0)));
                            }
                            frameStartMode = false;
                        } else {
                            windowFrameExclusionBuilder = windowFrameBetweenBuilder.andPreceding(renderExpression(arguments.get(0)));
                        }
                        arguments.get(0).accept(this, o);
                        break;
                    case WINDOW_FOLLOWING:
                        if (frameStartMode) {
                            assert between;
                            assert rows != null;
                            windowFrameBetweenBuilder = rows.betweenFollowing(renderExpression(arguments.get(0)));
                            frameStartMode = false;
                        } else {
                            windowFrameExclusionBuilder = windowFrameBetweenBuilder.andFollowing(renderExpression(arguments.get(0)));
                        }
                        arguments.get(0).accept(this, o);
                        break;
                    case WINDOW_UNBOUNDED_FOLLOWING:
                        assert !frameStartMode;
                        windowFrameExclusionBuilder = windowFrameBetweenBuilder.andUnboundedFollowing();
                        break;
                    case WINDOW_CURRENT_ROW:
                        if (frameStartMode) {
                            assert rows != null;
                            if (between) {
                                windowFrameBetweenBuilder = rows.betweenCurrentRow();
                            } else {
                                windowFrameExclusionBuilder = rows.currentRow();
                            }
                            frameStartMode = false;
                        } else {
                            windowFrameExclusionBuilder = windowFrameBetweenBuilder.andCurrentRow();
                        }
                        break;
                    case WINDOW_DEFINITION_1:
                    case WINDOW_DEFINITION_2:
                    case WINDOW_DEFINITION_3:
                    case WINDOW_DEFINITION_4:
                        for (Expression<?> argument : arguments) {
                            argument.accept(this, o);
                        }
                        if (windowFrameExclusionBuilder != null) {
                            return windowFrameExclusionBuilder.end();
                        } else {
                            return window.end();
                        }
                    default:
                        break;
                }
            }
            return (X) windowContainerBuilder;
        }
    }


    /**
     * Context for the {@link BindResolver}
     *
     * @since 1.6.0
     */
    private static class BindResolverContext {

        private Path<?> alias;
        private Path<?> cteAttribute;

        public Path<?> getAlias() {
            return alias;
        }

        public String getAliasString() {
            return alias == null ? null : alias.getMetadata().getName();
        }

        public void setAlias(Path<?> alias) {
            this.alias = alias;
        }

        public Path<?> getCteAttribute() {
            return cteAttribute;
        }

        public void setCteAttribute(Path<?> cteAttribute) {
            this.cteAttribute = cteAttribute;
        }

    }

    /**
     * Resolve the bind and alias information for an expression.
     *
     * @since 1.6.0
     */
    private static class BindResolver implements Visitor<Expression<?>, BindResolverContext> {

        private static final BindResolver INSTANCE = new BindResolver();

        @Override
        public Expression<?> visit(Constant<?> expr, BindResolverContext context) {
            return expr;
        }

        @Override
        public Expression<?> visit(FactoryExpression<?> expr, BindResolverContext context) {
            return expr;
        }

        @Override
        public Expression<?> visit(Operation<?> expr, BindResolverContext context) {
            if (Ops.ALIAS.equals(expr.getOperator())) {
                context.setAlias((Path<?>) expr.getArg(1));
                return expr.getArg(0).accept(this, context);
            } else if (BIND.equals(expr.getOperator())) {
                context.setCteAttribute((Path<?>) expr.getArg(1));
                return expr.getArg(0).accept(this, context);
            } else {
                return expr;
            }
        }

        @Override
        public Expression<?> visit(ParamExpression<?> expr, BindResolverContext context) {
            return expr;
        }

        @Override
        public Expression<?> visit(Path<?> expr, BindResolverContext context) {
            return expr;
        }

        @Override
        public Expression<?> visit(SubQueryExpression<?> expr, BindResolverContext context) {
            return expr;
        }

        @Override
        public Expression<?> visit(TemplateExpression<?> expr, BindResolverContext context) {
            return expr;
        }

    }

}
