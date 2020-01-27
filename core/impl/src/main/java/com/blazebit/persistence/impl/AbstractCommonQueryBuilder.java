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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseSubqueryBuilder;
import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DefaultKeyset;
import com.blazebit.persistence.From;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.Path;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.WindowBuilder;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.keyset.KeysetBuilderImpl;
import com.blazebit.persistence.impl.keyset.KeysetLink;
import com.blazebit.persistence.impl.keyset.KeysetManager;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.impl.query.AbstractCustomQuery;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.DefaultQuerySpecification;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.ObjectBuilderTypedQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.impl.transform.ExpressionTransformerGroup;
import com.blazebit.persistence.impl.transform.OuterFunctionVisitor;
import com.blazebit.persistence.impl.transform.SimpleTransformerGroup;
import com.blazebit.persistence.impl.transform.SizeTransformationVisitor;
import com.blazebit.persistence.impl.transform.SizeTransformerGroup;
import com.blazebit.persistence.impl.transform.SubqueryRecursiveExpressionVisitor;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.AliasReplacementVisitor;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionCopyContextMap;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.NumericType;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.Subquery;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.spi.SetOperationType;
import com.blazebit.persistence.spi.ValuesStrategy;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @param <QueryResultType> The query result type
 * @param <BuilderType> The concrete builder type
 * @param <SetReturn> The builder type that should be returned on set operations
 * @param <SubquerySetReturn> The builder type that should be returned on subquery set operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractCommonQueryBuilder<QueryResultType, BuilderType, SetReturn, SubquerySetReturn, FinalSetReturn extends BaseFinalSetOperationBuilderImpl<?, ?, ?>> implements ServiceProvider, ConfigurationSource {

    public static final String ID_PARAM_NAME = "ids";

    protected final MainQuery mainQuery;
    protected final QueryContext queryContext;
    /* This might change when transitioning to a set operation */
    protected boolean isMainQuery;
    
    protected final CriteriaBuilderFactoryImpl cbf;
    protected final EntityManager em;
    protected final DbmsStatementType statementType;
    protected final Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities = new HashMap<Class<?>, Map<String, DbmsModificationState>>(0);
    
    protected final ParameterManager parameterManager;
    protected final SelectManager<QueryResultType> selectManager;
    protected final WhereManager<BuilderType> whereManager;
    protected final HavingManager<BuilderType> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    protected final WindowManager<BuilderType> windowManager;
    protected final KeysetManager keysetManager;
    protected final ResolvingQueryGenerator queryGenerator;
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    protected final EmbeddableSplittingVisitor embeddableSplittingVisitor;
    protected final FunctionalDependencyAnalyzerVisitor functionalDependencyAnalyzerVisitor;

    // This builder will be passed in when using set operations
    protected FinalSetReturn finalSetOperationBuilder;
    protected boolean setOperationEnded;

    protected final AliasManager aliasManager;
    protected final ExpressionFactory expressionFactory;

    // Mutable state
    protected Class<QueryResultType> resultType;
    protected int firstResult = 0;
    protected int maxResults = Integer.MAX_VALUE;
    protected boolean fromClassExplicitlySet = false;

    protected final List<ExpressionTransformerGroup<?>> transformerGroups;

    // Cache
    protected String cachedQueryString;
    protected String cachedExternalQueryString;
    protected ResolvedExpression[] cachedGroupByIdentifierExpressions;
    protected Set<JoinNode> keyRestrictedLeftJoins;
    protected boolean hasGroupBy = false;
    protected boolean needsCheck = true;
    protected boolean hasCollections = false;
    // Fetch owner's are evaluated during implicit joining
    protected Set<JoinNode> nodesToFetch;

    private boolean checkSetBuilderEnded = true;
    private boolean implicitJoinsApplied = false;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    @SuppressWarnings("unchecked")
    protected AbstractCommonQueryBuilder(AbstractCommonQueryBuilder<QueryResultType, ?, ?, ?, ?> builder) {
        this.mainQuery = builder.mainQuery;
        this.queryContext = builder.queryContext;
        this.isMainQuery = builder.isMainQuery;
        this.cbf = builder.cbf;
        this.statementType = builder.statementType;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<BuilderType>) builder.whereManager;
        this.havingManager = (HavingManager<BuilderType>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.keysetManager = builder.keysetManager;
        this.joinManager = builder.joinManager;
        this.windowManager = (WindowManager<BuilderType>) builder.windowManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.finalSetOperationBuilder = (FinalSetReturn) builder.finalSetOperationBuilder;
        this.subqueryInitFactory = builder.subqueryInitFactory;
        this.embeddableSplittingVisitor = builder.embeddableSplittingVisitor;
        this.functionalDependencyAnalyzerVisitor = builder.functionalDependencyAnalyzerVisitor;
        this.aliasManager = builder.aliasManager;
        this.expressionFactory = builder.expressionFactory;
        this.transformerGroups = builder.transformerGroups;
        this.resultType = builder.resultType;
    }

    /**
     * Create fully copy of builder. Intended for CTEs only.
     *
     * @param builder
     * @param joinManagerMapping
     * @param copyContext
     */
    @SuppressWarnings("unchecked")
    protected AbstractCommonQueryBuilder(AbstractCommonQueryBuilder<QueryResultType, ?, ?, ?, ?> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        this.mainQuery = mainQuery;
        if (builder.isMainQuery) {
            mainQuery.cteManager.init(this);
        }
        this.queryContext = queryContext;
        this.isMainQuery = builder.isMainQuery;
        this.statementType = builder.statementType;
        this.cbf = mainQuery.cbf;
        this.parameterManager = mainQuery.parameterManager;
        this.em = mainQuery.em;

        this.aliasManager = new AliasManager(queryContext.getParent().aliasManager);
        this.expressionFactory = builder.expressionFactory;
        this.queryGenerator = new ResolvingQueryGenerator(cbf.getMetamodel(), this.aliasManager, parameterManager, mainQuery.parameterTransformerFactory, mainQuery.jpaProvider, mainQuery.registeredFunctions);
        this.joinManager = new JoinManager(mainQuery, this, queryGenerator, this.aliasManager, queryContext.getParent().joinManager, expressionFactory);
        this.fromClassExplicitlySet = builder.fromClassExplicitlySet;

        this.subqueryInitFactory = joinManager.getSubqueryInitFactory();
        SplittingVisitor splittingVisitor = new SplittingVisitor(mainQuery.metamodel, mainQuery.jpaProvider, this.aliasManager);
        this.embeddableSplittingVisitor = new EmbeddableSplittingVisitor(mainQuery.metamodel, mainQuery.jpaProvider, this.aliasManager, splittingVisitor);
        GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor = new GroupByExpressionGatheringVisitor(false, this.aliasManager, mainQuery.dbmsDialect);
        this.functionalDependencyAnalyzerVisitor = new FunctionalDependencyAnalyzerVisitor(mainQuery.metamodel, splittingVisitor, mainQuery.jpaProvider, this.aliasManager);

        this.windowManager = new WindowManager<>(queryGenerator, parameterManager, subqueryInitFactory);
        this.whereManager = new WhereManager<>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager, subqueryInitFactory, mainQuery.jpaProvider, this.aliasManager, embeddableSplittingVisitor, groupByExpressionGatheringVisitor);
        this.havingManager = new HavingManager<>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory, groupByExpressionGatheringVisitor);

        this.selectManager = new SelectManager<>(queryGenerator, parameterManager, this, this.joinManager, this.aliasManager, subqueryInitFactory, expressionFactory, mainQuery.jpaProvider, mainQuery, groupByExpressionGatheringVisitor, builder.resultType);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, subqueryInitFactory, selectManager, this.joinManager, this.aliasManager, embeddableSplittingVisitor, functionalDependencyAnalyzerVisitor, mainQuery.metamodel, mainQuery.jpaProvider, groupByExpressionGatheringVisitor);
        this.keysetManager = new KeysetManager(this, queryGenerator, parameterManager, mainQuery.jpaProvider, mainQuery.dbmsDialect);

        final SizeTransformationVisitor sizeTransformationVisitor = new SizeTransformationVisitor(mainQuery, subqueryInitFactory, joinManager, mainQuery.jpaProvider);
        this.transformerGroups = Arrays.<ExpressionTransformerGroup<?>>asList(
                new SimpleTransformerGroup(new OuterFunctionVisitor(joinManager)),
                new SimpleTransformerGroup(new SubqueryRecursiveExpressionVisitor()),
                new SizeTransformerGroup(sizeTransformationVisitor, orderByManager, selectManager, joinManager, groupByManager));
        this.resultType = builder.resultType;

        applyFrom(builder, isMainQuery, true, true, Collections.<ClauseType>emptySet(), Collections.<JoinNode>emptySet(), joinManagerMapping, copyContext);
    }
    
    protected AbstractCommonQueryBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, FinalSetReturn finalSetOperationBuilder, boolean implicitFromClause) {
        if (mainQuery == null) {
            throw new NullPointerException("mainQuery");
        }
        if (statementType == null) {
            throw new NullPointerException("statementType");
        }
        if (resultClazz == null) {
            throw new NullPointerException("resultClazz");
        }
        
        this.mainQuery = mainQuery;
        if (isMainQuery) {
            mainQuery.cteManager.init(this);
        }
        this.queryContext = queryContext;
        this.isMainQuery = isMainQuery;
        this.statementType = statementType;
        this.cbf = mainQuery.cbf;
        this.parameterManager = mainQuery.parameterManager;
        this.em = mainQuery.em;

        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;
        this.queryGenerator = new ResolvingQueryGenerator(cbf.getMetamodel(), this.aliasManager, parameterManager, mainQuery.parameterTransformerFactory, mainQuery.jpaProvider, mainQuery.registeredFunctions);
        this.joinManager = new JoinManager(mainQuery, this, queryGenerator, this.aliasManager, parentJoinManager, expressionFactory);

        if (implicitFromClause) {
            // set defaults
            if (alias != null) {
                // If the user supplies an alias, the intention is clear
                fromClassExplicitlySet = true;
            }

            EntityType<QueryResultType> type = mainQuery.metamodel.getEntity(resultClazz);
            if (type == null) {
                // the result class might not be an entity
                if (fromClassExplicitlySet) {
                    // If the intention was to use that as from clause, we have to throw an exception
                    throw new IllegalArgumentException("The class [" + resultClazz.getName() + "] is not an entity and therefore can't be aliased!");
                }
            } else {
                this.joinManager.addRoot(type, alias);
            }
        }

        this.subqueryInitFactory = joinManager.getSubqueryInitFactory();
        SplittingVisitor splittingVisitor = new SplittingVisitor(mainQuery.metamodel, mainQuery.jpaProvider, this.aliasManager);
        this.embeddableSplittingVisitor = new EmbeddableSplittingVisitor(mainQuery.metamodel, mainQuery.jpaProvider, this.aliasManager, splittingVisitor);
        GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor = new GroupByExpressionGatheringVisitor(false, this.aliasManager, mainQuery.dbmsDialect);
        this.functionalDependencyAnalyzerVisitor = new FunctionalDependencyAnalyzerVisitor(mainQuery.metamodel, splittingVisitor, mainQuery.jpaProvider, this.aliasManager);

        this.windowManager = new WindowManager<>(queryGenerator, parameterManager, subqueryInitFactory);
        this.whereManager = new WhereManager<>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager, subqueryInitFactory, mainQuery.jpaProvider, this.aliasManager, embeddableSplittingVisitor, groupByExpressionGatheringVisitor);
        this.havingManager = new HavingManager<>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory, groupByExpressionGatheringVisitor);

        this.selectManager = new SelectManager<>(queryGenerator, parameterManager, this, this.joinManager, this.aliasManager, subqueryInitFactory, expressionFactory, mainQuery.jpaProvider, mainQuery, groupByExpressionGatheringVisitor, resultClazz);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, subqueryInitFactory, selectManager, this.joinManager, this.aliasManager, embeddableSplittingVisitor, functionalDependencyAnalyzerVisitor, mainQuery.metamodel, mainQuery.jpaProvider, groupByExpressionGatheringVisitor);
        this.keysetManager = new KeysetManager(this, queryGenerator, parameterManager, mainQuery.jpaProvider, mainQuery.dbmsDialect);

        final SizeTransformationVisitor sizeTransformationVisitor = new SizeTransformationVisitor(mainQuery, subqueryInitFactory, joinManager, mainQuery.jpaProvider);
        this.transformerGroups = Arrays.<ExpressionTransformerGroup<?>>asList(
                new SimpleTransformerGroup(new OuterFunctionVisitor(joinManager)),
                new SimpleTransformerGroup(new SubqueryRecursiveExpressionVisitor()),
                new SizeTransformerGroup(sizeTransformationVisitor, orderByManager, selectManager, joinManager, groupByManager));
        this.resultType = resultClazz;
        
        this.finalSetOperationBuilder = finalSetOperationBuilder;
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, FinalSetReturn finalSetOperationBuilder, boolean implicitFromClause, AliasManager parentAliasManager, JoinManager parentJoinManager) {
        this(mainQuery, queryContext, isMainQuery, statementType, resultClazz, alias, parentAliasManager, parentJoinManager, mainQuery.expressionFactory, finalSetOperationBuilder, implicitFromClause);
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        this(mainQuery, queryContext, isMainQuery, statementType, resultClazz, alias, null, null, mainQuery.expressionFactory, finalSetOperationBuilder, true);
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias) {
        this(mainQuery, queryContext, isMainQuery, statementType, resultClazz, alias, null);
    }

    abstract AbstractCommonQueryBuilder<QueryResultType, BuilderType, SetReturn, SubquerySetReturn, FinalSetReturn> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext);

    ExpressionCopyContext applyFrom(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, boolean copyMainQuery, boolean copySelect, boolean fixedSelect, Set<ClauseType> clauseExclusions, Set<JoinNode> alwaysIncludedNodes, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        if (copyMainQuery) {
            copyContext = new ExpressionCopyContextMap(parameterManager.copyFrom(builder.parameterManager));
            mainQuery.cteManager.applyFrom(builder.mainQuery.cteManager, joinManagerMapping, copyContext);
        }

        joinManagerMapping.put(builder.joinManager, joinManager);
        aliasManager.applyFrom(builder.aliasManager);
        Map<JoinNode, JoinNode> nodeMapping = joinManager.applyFrom(builder.joinManager, clauseExclusions, alwaysIncludedNodes, copyContext);
        windowManager.applyFrom(builder.windowManager, copyContext);
        whereManager.applyFrom(builder.whereManager, copyContext);
        havingManager.applyFrom(builder.havingManager, copyContext);
        groupByManager.applyFrom(builder.groupByManager, clauseExclusions, copyContext);
        orderByManager.applyFrom(builder.orderByManager, copyContext);

        setFirstResult(builder.firstResult);
        setMaxResults(builder.maxResults);

        // TODO: select aliases that are ordered by?
        // TODO: set operations?

        if (copySelect) {
            selectManager.setDefaultSelect(nodeMapping, builder.selectManager.getSelectInfos(), copyContext);
            if (fixedSelect) {
                selectManager.unsetDefaultSelect();
            }
        }
        // No need to copy the finalSetOperationBuilder as that is only necessary for further builders which isn't possible after copying
        collectParameters();
        return copyContext;
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return cbf;
    }

    public DbmsStatementType getStatementType() {
        return statementType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (CriteriaBuilderFactory.class.equals(serviceClass)) {
            return (T) cbf;
        } else if (ConfigurationSource.class.equals(serviceClass)) {
            return (T) this;
        } else if (EntityManager.class.equals(serviceClass)) {
            return (T) em;
        } else if (DbmsDialect.class.equals(serviceClass)) {
            return (T) mainQuery.dbmsDialect;
        } else if (SubqueryExpressionFactory.class.equals(serviceClass)) {
            return (T) mainQuery.subqueryExpressionFactory;
        } else if (ExpressionFactory.class.equals(serviceClass)) {
            return (T) mainQuery.expressionFactory;
        } else if (JoinOnBuilder.class.equals(serviceClass)) {
            // TODO: We should think of a better way to expose a where builder to clients as an on builder
            // TODO: Setting the expression via this does not clear the cache
            return (T) whereManager.startOnBuilder(this);
        }
        
        return cbf.getService(serviceClass);
    }

    @SuppressWarnings("unchecked")
    public BuilderType registerMacro(String macroName, JpqlMacro jpqlMacro) {
        prepareForModification(null);
        this.mainQuery.registerMacro(macroName, jpqlMacro);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setProperty(String propertyName, String propertyValue) {
        prepareForModification(null);
        this.mainQuery.getMutableQueryConfiguration().setProperty(propertyName, propertyValue);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setProperties(Map<String, String> properties) {
        prepareForModification(null);
        this.mainQuery.getMutableQueryConfiguration().setProperties(properties);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setCacheable(boolean cacheable) {
        this.mainQuery.getMutableQueryConfiguration().setCacheable(cacheable);
        return (BuilderType) this;
    }

    public boolean isCacheable() {
        return this.mainQuery.getQueryConfiguration().isCacheable();
    }
    
    public Map<String, String> getProperties() {
        return this.mainQuery.getQueryConfiguration().getProperties();
    }

    public String getProperty(String name) {
        return this.mainQuery.getQueryConfiguration().getProperty(name);
    }

    @SuppressWarnings("unchecked")
    public StartOngoingSetOperationCTECriteriaBuilder<BuilderType, LeafOngoingFinalSetOperationCTECriteriaBuilder<BuilderType>> withStartSet(Class<?> cteClass) {
        Boolean inlineCtesEnabled = mainQuery.getQueryConfiguration().getInlineCtesEnabled();
        return mainQuery.cteManager.withStartSet(cteClass, (BuilderType) this, inlineCtesEnabled == null ? !mainQuery.dbmsDialect.supportsWithClause() : inlineCtesEnabled, null, null);
    }

    public StartOngoingSetOperationCTECriteriaBuilder<BuilderType, LeafOngoingFinalSetOperationCTECriteriaBuilder<BuilderType>> withStartSet(Class<?> cteClass, boolean inline) {
        return withStartSet(cteClass, inline, false);
    }

    @SuppressWarnings("unchecked")
    public StartOngoingSetOperationCTECriteriaBuilder<BuilderType, LeafOngoingFinalSetOperationCTECriteriaBuilder<BuilderType>> withStartSet(Class<?> cteClass, boolean inline, boolean lateral) {
        if (!inline && !mainQuery.dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause, so the CTE must be inlined!");
        }
        prepareForModification(ClauseType.CTE);
        return mainQuery.cteManager.withStartSet(cteClass, (BuilderType) this, inline, lateral ? aliasManager : null, lateral ? joinManager : null);
    }

    public boolean hasCte(Class<?> cte) {
        return mainQuery.cteManager.hasCte(cte);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass) {
        Boolean inlineCtesEnabled = mainQuery.getQueryConfiguration().getInlineCtesEnabled();
        return with(cteClass, inlineCtesEnabled == null ? !mainQuery.dbmsDialect.supportsWithClause() : inlineCtesEnabled);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass, CriteriaBuilder<?> criteriaBuilder) {
        Boolean inlineCtesEnabled = mainQuery.getQueryConfiguration().getInlineCtesEnabled();
        return with(cteClass, criteriaBuilder, inlineCtesEnabled == null ? !mainQuery.dbmsDialect.supportsWithClause() : inlineCtesEnabled);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass, boolean inline) {
        return with(cteClass, (String) null, inline, null, false, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass, CriteriaBuilder<?> criteriaBuilder, boolean inline) {
        return with(cteClass, criteriaBuilder, inline, null, false, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public <X> FullSelectCTECriteriaBuilder<X> with(Class<?> cteClass, String name, boolean inline, JoinManager inlineOwner, boolean lateral, X result) {
        if (!inline && !mainQuery.dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause, so the CTE must be inlined!");
        }
        prepareForModification(ClauseType.CTE);
        return mainQuery.cteManager.with(cteClass, name, result, inline, inlineOwner, lateral ? aliasManager : null, lateral ? joinManager : null);
    }

    @SuppressWarnings("unchecked")
    public <X> FullSelectCTECriteriaBuilder<X> with(Class<?> cteClass, CriteriaBuilder<?> criteriaBuilder, boolean inline, JoinManager inlineOwner, boolean lateral, X result) {
        if (!inline && !mainQuery.dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause, so the CTE must be inlined!");
        }

        prepareForModification(ClauseType.CTE);
        return mainQuery.cteManager.with(cteClass, result, (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) criteriaBuilder, inline, inlineOwner, lateral ? aliasManager : null, lateral ? joinManager : null);
    }

    public BuilderType withCtesFrom(CTEBuilder<?> cteBuilder) {
        MainQuery mainQuery = ((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) cteBuilder).mainQuery;
        if (this.mainQuery == mainQuery) {
            throw new IllegalStateException("Can't copy the CTEs from itself back into itself!");
        }
        if (!mainQuery.cteManager.getCtes().isEmpty()) {
            if (!mainQuery.dbmsDialect.supportsWithClause()) {
                for (CTEInfo cte : mainQuery.cteManager.getCtes()) {
                    if (!cte.inline) {
                        throw new UnsupportedOperationException("The database does not support the with clause!");
                    }
                }
            }

            prepareForModification(ClauseType.CTE);
            ExpressionCopyContext copyContext = new ExpressionCopyContextMap(this.parameterManager.copyFrom(mainQuery.parameterManager));
            this.mainQuery.cteManager.applyFrom(mainQuery.cteManager, new IdentityHashMap<JoinManager, JoinManager>(), copyContext);
        }
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public SelectRecursiveCTECriteriaBuilder<BuilderType> withRecursive(Class<?> cteClass) {
        if (!mainQuery.dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }

        prepareForModification(ClauseType.CTE);
        return mainQuery.cteManager.withRecursive(cteClass, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public ReturningModificationCriteriaBuilderFactory<BuilderType> withReturning(Class<?> cteClass) {
        if (!mainQuery.dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        if (!mainQuery.dbmsDialect.supportsModificationQueryInWithClause()) {
            throw new UnsupportedOperationException("The database does not support modification queries in the with clause!");
        }

        prepareForModification(ClauseType.CTE);
        return mainQuery.cteManager.withReturning(cteClass, (BuilderType) this);
    }
    
    public SetReturn union() {
        return addSetOperation(SetOperationType.UNION);
    }

    public SetReturn unionAll() {
        return addSetOperation(SetOperationType.UNION_ALL);
    }

    public SetReturn intersect() {
        return addSetOperation(SetOperationType.INTERSECT);
    }

    public SetReturn intersectAll() {
        return addSetOperation(SetOperationType.INTERSECT_ALL);
    }

    public SetReturn except() {
        return addSetOperation(SetOperationType.EXCEPT);
    }

    public SetReturn exceptAll() {
        return addSetOperation(SetOperationType.EXCEPT_ALL);
    }

    public SubquerySetReturn startUnion() {
        return addSubquerySetOperation(SetOperationType.UNION);
    }

    public SubquerySetReturn startUnionAll() {
        return addSubquerySetOperation(SetOperationType.UNION_ALL);
    }

    public SubquerySetReturn startIntersect() {
        return addSubquerySetOperation(SetOperationType.INTERSECT);
    }

    public SubquerySetReturn startIntersectAll() {
        return addSubquerySetOperation(SetOperationType.INTERSECT_ALL);
    }

    public SubquerySetReturn startExcept() {
        return addSubquerySetOperation(SetOperationType.EXCEPT);
    }

    public SubquerySetReturn startExceptAll() {
        return addSubquerySetOperation(SetOperationType.EXCEPT_ALL);
    }

    public SubquerySetReturn startSet() {
        return addSubquerySetOperation(null);
    }
    
    private SetReturn addSetOperation(SetOperationType type) {
        // We support set operations in subqueries since we use custom functions
        if (!(this instanceof BaseSubqueryBuilder<?>)) {
            mainQuery.assertSupportsAdvancedSql("Illegal use of SET operation!");
        }
        prepareForModification(ClauseType.SELECT);
        this.setOperationEnded = true;
        // We only check non-empty queries since empty ones will be replaced
        if (!isEmpty()) {
            prepareAndCheck();
        }
        FinalSetReturn finalSetOperationBuilder = this.finalSetOperationBuilder;
        
        if (finalSetOperationBuilder == null) {
            finalSetOperationBuilder = createFinalSetOperationBuilder(type, false);
            finalSetOperationBuilder.setOperationManager.setStartQueryBuilder(this);
            this.finalSetOperationBuilder = finalSetOperationBuilder;
        } else {
            SetOperationManager oldOperationManager = finalSetOperationBuilder.setOperationManager;
            if (oldOperationManager.getOperator() == null) {
                oldOperationManager.setOperator(type);
            } else if (oldOperationManager.getOperator() != type) {
                // Put existing set operands into a sub builder and use the sub builder as new start
                FinalSetReturn subFinalSetOperationBuilder = createFinalSetOperationBuilder(oldOperationManager.getOperator(), false);
                subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(oldOperationManager.getStartQueryBuilder());
                subFinalSetOperationBuilder.setOperationManager.getSetOperations().addAll(oldOperationManager.getSetOperations());
                oldOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
                oldOperationManager.getSetOperations().clear();
                oldOperationManager.setOperator(type);
            }
        }
        
        SetReturn setOperand = createSetOperand(finalSetOperationBuilder);
        finalSetOperationBuilder.setOperationManager.addSetOperation((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) setOperand);
        return setOperand;
    }
    
    private SubquerySetReturn addSubquerySetOperation(SetOperationType type) {
        prepareForModification(ClauseType.SELECT);
        this.setOperationEnded = true;
        // We only check non-empty queries since empty ones will be replaced
        if (!isEmpty()) {
            prepareAndCheck();
        }
        FinalSetReturn parentFinalSetOperationBuilder = this.finalSetOperationBuilder;
        
        if (parentFinalSetOperationBuilder == null) {
            parentFinalSetOperationBuilder = createFinalSetOperationBuilder(type, false);
            parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(this);
            this.finalSetOperationBuilder = parentFinalSetOperationBuilder;
            this.needsCheck = true;
        } else {
            SetOperationManager oldParentOperationManager = finalSetOperationBuilder.setOperationManager;

            if (oldParentOperationManager.getOperator() == null) {
                oldParentOperationManager.setOperator(type);
            } else if (oldParentOperationManager.getOperator() != type) {
                // Put existing set operands into a sub builder and use the sub builder as new start
                FinalSetReturn subFinalSetOperationBuilder = createFinalSetOperationBuilder(oldParentOperationManager.getOperator(), false);
                subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(oldParentOperationManager.getStartQueryBuilder());
                subFinalSetOperationBuilder.setOperationManager.getSetOperations().addAll(oldParentOperationManager.getSetOperations());
                oldParentOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
                oldParentOperationManager.getSetOperations().clear();
                oldParentOperationManager.setOperator(type);
            }
        }
        
        FinalSetReturn finalSetOperationBuilder = createFinalSetOperationBuilder(type, true);
        SubquerySetReturn subquerySetOperand = createSubquerySetOperand(finalSetOperationBuilder, parentFinalSetOperationBuilder);
        finalSetOperationBuilder.setOperationManager.setStartQueryBuilder((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subquerySetOperand);
        
        if (type != null) {
            parentFinalSetOperationBuilder.setOperationManager.addSetOperation(finalSetOperationBuilder);
        } else {
            parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(finalSetOperationBuilder);
        }

        return subquerySetOperand;
    }
    
    protected FinalSetReturn createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }
    
    protected SetReturn createSetOperand(FinalSetReturn baseQueryBuilder) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }
    
    protected SubquerySetReturn createSubquerySetOperand(FinalSetReturn baseQueryBuilder, FinalSetReturn resultFinalSetOperationBuilder) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }

    public BuilderType from(String correlationPath) {
        return from(correlationPath, null);
    }

    public BuilderType from(String correlationPath, String alias) {
        if (!(this instanceof SubqueryBuilder<?>)) {
            throw new IllegalStateException("Cannot use a correlation path in a non-subquery!");
        }
        prepareForModification(ClauseType.JOIN);
        joinManager.addRoot(correlationPath, alias, false);
        return (BuilderType) this;
    }

    public BuilderType from(Class<?> clazz) {
        return from(clazz, null);
    }

    public BuilderType from(Class<?> clazz, String alias) {
        return from(clazz, alias, null);
    }

    public BuilderType from(EntityType<?> entityType) {
        return from(entityType, null);
    }

    public BuilderType from(EntityType<?> entityType, String alias) {
        return from(entityType, alias, null);
    }

    public BuilderType fromCte(Class<?> clazz, String cteName) {
        return fromCte(clazz, cteName, null);
    }

    public BuilderType fromCte(Class<?> clazz, String cteName, String alias) {
        return from(clazz, alias, null);
    }

    public BuilderType fromOld(Class<?> clazz) {
        return fromOld(clazz, null);
    }

    public BuilderType fromOld(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.OLD);
    }

    public BuilderType fromNew(Class<?> clazz) {
        return fromNew(clazz, null);
    }

    public BuilderType fromNew(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.NEW);
    }

    public <T> BuilderType fromValues(Class<T> valueClass, String alias, Collection<T> values) {
        BuilderType result = fromValues(valueClass, alias, values.size());
        setParameter(alias, values);
        return result;
    }

    public BuilderType fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values) {
        BuilderType result = fromValues(entityBaseClass, attributeName, alias, values.size());
        setParameter(alias, values);
        return result;
    }

    public <T> BuilderType fromIdentifiableValues(Class<T> valueClass, String alias, Collection<T> values) {
        BuilderType result = fromIdentifiableValues(valueClass, alias, values.size());
        setParameter(alias, values);
        return result;
    }

    public <T> BuilderType fromIdentifiableValues(Class<T> valueClass, String identifierAttribute, String alias, Collection<T> values) {
        BuilderType result = fromIdentifiableValues(valueClass, identifierAttribute, alias, values.size());
        setParameter(alias, values);
        return result;
    }

    public BuilderType fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount) {
        return fromIdentifiableValues(valueClass, null, alias, valueCount);
    }

    public BuilderType fromIdentifiableValues(Class<?> valueClass, String identifierAttribute, String alias, int valueCount) {
        prepareForModification(ClauseType.JOIN);
        if (!fromClassExplicitlySet) {
            // When from is explicitly called we have to revert the implicit root
            if (joinManager.getRoots().size() > 0) {
                joinManager.removeRoot();
            }
        }

        ManagedType<?> type = mainQuery.metamodel.getManagedType(valueClass);
        if (!JpaMetamodelUtils.isIdentifiable(type)) {
            throw new IllegalArgumentException("Only identifiable types allowed!");
        }

        joinManager.addRootValues(valueClass, valueClass, alias, valueCount, null, null, true, true, identifierAttribute, null, null, null);
        fromClassExplicitlySet = true;

        return (BuilderType) this;
    }

    public FullSelectCTECriteriaBuilder<BuilderType> fromSubquery(Class<?> cteClass) {
        return fromSubquery(cteClass, null);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> fromSubquery(Class<?> cteClass, String alias) {
        alias = fromInternal(cteClass, alias);
        return with(cteClass, alias, true, joinManager, false, (BuilderType) this);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> fromEntitySubquery(Class<?> cteClass) {
        return fromEntitySubquery(cteClass, null);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> fromEntitySubquery(Class<?> cteClass, String alias) {
        return fromEntitySubquery(cteClass, alias, null);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> fromEntitySubquery(Class<?> cteClass, String alias, String subqueryAlias) {
        return bindEntityAttributes(alias, subqueryAlias, fromSubquery(cteClass, alias), false, true);
    }

    private <X extends FullSelectCTECriteriaBuilder<?>> X bindEntityAttributes(String alias, String subqueryAlias, X builder, boolean forbidAlias, boolean bindFrom) {
        AbstractCTECriteriaBuilder<?, ?, ?, ?> criteriaBuilder = (AbstractCTECriteriaBuilder<?, ?, ?, ?>) builder;
        if (forbidAlias) {
            criteriaBuilder.aliasManager.setForbiddenAlias(alias);
        }
        if (bindFrom) {
            builder.from(criteriaBuilder.cteType, subqueryAlias == null ? alias : subqueryAlias);
        }
        for (Map.Entry<String, ExtendedAttribute<?, ?>> entry : criteriaBuilder.attributeEntries.entrySet()) {
            if (!JpaMetamodelUtils.isAssociation(entry.getValue().getAttribute())) {
                builder.bind(entry.getKey()).select(entry.getKey());
            }
        }
        return builder;
    }

    public BuilderType fromValues(Class<?> valueClass, String alias, int valueCount) {
        ManagedType<?> type = mainQuery.metamodel.getManagedType(valueClass);
        if (type == null) {
            String sqlType = mainQuery.dbmsDialect.getSqlType(valueClass);
            if (sqlType == null) {
                throw new IllegalArgumentException("The basic type " + valueClass.getSimpleName() + " has no column type registered in the DbmsDialect '" + mainQuery.dbmsDialect.getClass().getName() + "'! Register a column type or consider using the fromValues variant that extracts column types from entity attributes!");
            }
            String typeName = cbf.getNamedTypes().get(valueClass);
            if (typeName == null) {
                throw new IllegalArgumentException("Unsupported non-managed type for VALUES clause: " + valueClass.getName() + ". You can register the type via com.blazebit.persistence.spi.CriteriaBuilderConfiguration.registerNamedType. Please report this so that we can add the type definition as well!");
            }

            String castedParameter = mainQuery.dbmsDialect.cast("?", sqlType);
            ExtendedAttribute valuesLikeAttribute = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, ValuesEntity.class).getAttribute("value");

            prepareFromModification();
            joinManager.addRootValues(ValuesEntity.class, valueClass, alias, valueCount, typeName, castedParameter, false, true, "value", valuesLikeAttribute, null, null);
        } else if (type instanceof EntityType<?>) {
            prepareFromModification();
            joinManager.addRootValues(valueClass, valueClass, alias, valueCount, null, null, false, true, null, null, null, null);
        } else {
            ExtendedManagedType<?> extendedManagedType = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, valueClass);
            Map.Entry<? extends EntityType<?>, String> entry = extendedManagedType.getEmbeddableSingularOwner();
            boolean singular = true;
            if (entry == null) {
                singular = false;
                entry = extendedManagedType.getEmbeddablePluralOwner();
            }
            if (entry == null) {
                throw new IllegalArgumentException("Unsupported use of embeddable type [" + valueClass + "] for values clause! Use the entity type and fromIdentifiableValues instead or introduce a CTE entity containing just the embeddable to be able to query it!");
            }
            Class<?> valueHolderEntityClass = entry.getKey().getJavaType();
            String valuesLikeAttributeName = entry.getValue();
            ExtendedAttribute valuesLikeAttribute = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, valueHolderEntityClass).getAttribute(valuesLikeAttributeName);
            prepareFromModification();
            joinManager.addRootValues(valueHolderEntityClass, valueClass, alias, valueCount, null, null, false, singular, valuesLikeAttributeName, valuesLikeAttribute, null, null);
        }

        fromClassExplicitlySet = true;
        return (BuilderType) this;
    }

    public BuilderType fromValues(Class<?> entityBaseClass, String originalAttributeName, String alias, int valueCount) {
        ExtendedManagedType<?> extendedManagedType = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, entityBaseClass);
        String keyFunction = "key(";
        String indexFunction = "index(";
        String attributeName = originalAttributeName;
        ExtendedAttribute<?, ?> valuesLikeAttribute;
        Class<?> elementClass;
        boolean index = false;
        boolean singular = false;
        String valuesLikeClause;
        String qualificationExpression;
        if (attributeName.regionMatches(true, 0, keyFunction, 0, keyFunction.length())) {
            attributeName = attributeName.substring(keyFunction.length(), attributeName.length() - 1);
            valuesLikeAttribute = extendedManagedType.getAttribute(attributeName);
            index = true;
            if (valuesLikeAttribute.getAttributePath().size() > 1) {
                ExtendedAttribute<?, ?> superAttr = extendedManagedType.getAttribute(valuesLikeAttribute.getAttributePathString().substring(0, valuesLikeAttribute.getAttributePathString().lastIndexOf('.')));
                elementClass = JpaMetamodelUtils.resolveKeyClass(superAttr.getElementClass(), (MapAttribute<?, ?, ?>) valuesLikeAttribute.getAttribute());
            } else {
                elementClass = JpaMetamodelUtils.resolveKeyClass(entityBaseClass, (MapAttribute<?, ?, ?>) valuesLikeAttribute.getAttribute());
            }
            valuesLikeClause = "KEY(" + ((EntityType<?>) extendedManagedType.getType()).getName() + "." + attributeName + ")";
            qualificationExpression = "KEY";
        } else if (attributeName.regionMatches(true, 0, indexFunction, 0, indexFunction.length())) {
            attributeName = attributeName.substring(indexFunction.length(), attributeName.length() - 1);
            valuesLikeAttribute = extendedManagedType.getAttribute(attributeName);
            index = true;
            elementClass = Integer.class;
            valuesLikeClause = "INDEX(" + ((EntityType<?>) extendedManagedType.getType()).getName() + "." + attributeName + ")";
            qualificationExpression = "INDEX";
        } else {
            valuesLikeAttribute = extendedManagedType.getAttribute(attributeName);
            elementClass = valuesLikeAttribute.getElementClass();
            valuesLikeClause = ((EntityType<?>) extendedManagedType.getType()).getName() + "." + attributeName;
            qualificationExpression = null;
        }
        if (valuesLikeAttribute.getAttribute() instanceof SingularAttribute<?, ?>) {
            singular = true;
            if (((SingularAttribute<?, ?>) valuesLikeAttribute.getAttribute()).getType() instanceof BasicType<?>) {
                if (valuesLikeAttribute.getColumnTypes().length != 1) {
                    throw new IllegalArgumentException("Unsupported VALUES clause use with multi-column attribute type " + Arrays.toString(valuesLikeAttribute.getColumnTypes()) + "! Consider creating a synthetic type like a @CTE entity to hold this attribute and use that type via fromIdentifiableValues instead!");
                }
                return fromValuesLike(entityBaseClass, elementClass, valuesLikeAttribute.getColumnTypes()[0], alias, valueCount, valuesLikeClause, valuesLikeAttribute, true, null);
            }
        } else if (index) {
            Map<String, String> keyColumnTypes = valuesLikeAttribute.getJoinTable().getKeyColumnTypes();
            if (keyColumnTypes.size() != 1) {
                throw new IllegalArgumentException("Unsupported VALUES clause use with multi-column attribute type " + keyColumnTypes.values() + "! Consider creating a synthetic type like a @CTE entity to hold this attribute and use that type via fromIdentifiableValues instead!");
            }
            String columnType = keyColumnTypes.values().iterator().next();
            return fromValuesLike(entityBaseClass, elementClass, columnType, alias, valueCount, valuesLikeClause, valuesLikeAttribute, false, qualificationExpression);
        } else {
            if (((PluralAttribute<?, ?, ?>) valuesLikeAttribute.getAttribute()).getElementType() instanceof BasicType<?>) {
                if (valuesLikeAttribute.getColumnTypes().length != 1) {
                    throw new IllegalArgumentException("Unsupported VALUES clause use with multi-column attribute type " + Arrays.toString(valuesLikeAttribute.getColumnTypes()) + "! Consider creating a synthetic type like a @CTE entity to hold this attribute and use that type via fromIdentifiableValues instead!");
                }
                return fromValuesLike(entityBaseClass, elementClass, valuesLikeAttribute.getColumnTypes()[0], alias, valueCount, valuesLikeClause, valuesLikeAttribute, false, null);
            }
        }
        return fromValuesLike(entityBaseClass, elementClass, null, alias, valueCount, valuesLikeClause, valuesLikeAttribute, singular, null);
    }

    private BuilderType fromValuesLike(Class<?> valueHolderEntityClass, Class<?> valueClass, String sqlType, String alias, int valueCount, String valuesLikeClause, ExtendedAttribute<?, ?> valuesLikeAttribute, boolean valueLikeAttributeSingular, String qualificationExpression) {
        prepareFromModification();
        String castedParameter = sqlType == null ? null : mainQuery.dbmsDialect.cast("?", sqlType);
        joinManager.addRootValues(valueHolderEntityClass, valueClass, alias, valueCount, null, castedParameter, false, valueLikeAttributeSingular, valuesLikeAttribute.getAttributePathString(), valuesLikeAttribute, valuesLikeClause, qualificationExpression);
        fromClassExplicitlySet = true;

        return (BuilderType) this;
    }

    private String fromInternal(Class<?> entityClass, String alias) {
        EntityType<?> type = mainQuery.metamodel.entity(entityClass);
        return fromInternal(type, alias);
    }

    private String fromInternal(EntityType<?> type, String alias) {
        prepareFromModification();
        String finalAlias = joinManager.addRoot(type, alias);
        fromClassExplicitlySet = true;
        return finalAlias;
    }

    private BuilderType from(Class<?> clazz, String alias, DbmsModificationState state) {
        EntityType<?> type = mainQuery.metamodel.entity(clazz);
        return from(type, alias, state);
    }

    @SuppressWarnings("unchecked")
    private BuilderType from(EntityType<?> type, String alias, DbmsModificationState state) {
        String finalAlias = fromInternal(type, alias);

        // Handle old and new references
        if (state != null) {
            mainQuery.assertSupportsAdvancedSql("Illegal use of modification state clause OLD/NEW!");
            Class<?> clazz = type.getJavaType();
            Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(clazz);
            if (versionEntities == null) {
                versionEntities = new HashMap<>(1);
                explicitVersionEntities.put(clazz, versionEntities);
            }
            
            versionEntities.put(finalAlias, state);
        }
        
        return (BuilderType) this;
    }

    private void prepareFromModification() {
        prepareForModification(ClauseType.JOIN);
        if (!fromClassExplicitlySet) {
            // When from is explicitly called we have to revert the implicit root
            if (joinManager.getRoots().size() > 0) {
                joinManager.removeRoot();
            }
        }
    }

    public Set<From> getRoots() {
        return new LinkedHashSet<From>(joinManager.getRoots());
    }

    public JoinNode getRoot() {
        return joinManager.getRootNodeOrFail("This should never happen. Please report this error!");
    }

    public JoinNode getFrom(String alias) {
        AliasInfo info = aliasManager.getAliasInfo(alias);
        if (info == null || !(info instanceof JoinAliasInfo)) {
            return null;
        }

        return ((JoinAliasInfo) info).getJoinNode();
    }

    public JoinNode getFromByPath(String path) {
        if (path == null || path.isEmpty()) {
            JoinNode node = joinManager.getRootNodeOrFail("No or multiple query roots, can't find single root!");
            return node;
        }
        PathExpression pathExpression = expressionFactory.createPathExpression(path);
        joinManager.implicitJoin(pathExpression, true, true, null, null, new HashSet<String>(), false, false, true, false);
        return (JoinNode) pathExpression.getBaseNode();
    }

    public Path getPath(String path) {
        if (path == null || path.isEmpty()) {
            JoinNode node = joinManager.getRootNodeOrFail("No or multiple query roots, can't find single root!");
            return new SimplePathReference(node, null, node.getType());
        }
        PathExpression pathExpression = expressionFactory.createPathExpression(path);
        joinManager.implicitJoin(pathExpression, true, true, null, null, new HashSet<String>(), false, false, true, false);
        return (Path) pathExpression.getPathReference();
    }

    public boolean isEmpty() {
        return joinManager.getRoots().isEmpty()
                || (
                        !fromClassExplicitlySet
                        && joinManager.getRoots().size() == 1
                        && joinManager.getRoots().get(0).getNodes().isEmpty()
                        && joinManager.getRoots().get(0).getTreatedJoinNodes().isEmpty()
                        && joinManager.getRoots().get(0).getEntityJoinNodes().isEmpty()
                )
            ;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return (BuilderType) this;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public EntityMetamodel getMetamodel() {
        return mainQuery.metamodel;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameter(String name, Object value) {
        parameterManager.satisfyParameter(name, value);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameter(String name, Calendar value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, value, temporalType);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameter(String name, Date value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, value, temporalType);
        return (BuilderType) this;
    }

    public boolean containsParameter(String name) {
        return parameterManager.containsParameter(name);
    }

    public boolean isParameterSet(String name) {
        return parameterManager.isParameterSet(name);
    }

    public Parameter<?> getParameter(String name) {
        return parameterManager.getParameter(name);
    }

    public Set<? extends Parameter<?>> getParameters() {
        return parameterManager.getParameters();
    }

    public Object getParameterValue(String name) {
        return parameterManager.getParameterValue(name);
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameterType(String name, Class<?> type) {
        parameterManager.setParameterType(name, type);
        return (BuilderType) this;
    }

    /*
     * Select methods
     */
    @SuppressWarnings("unchecked")
    public BuilderType distinct() {
        prepareForModification(ClauseType.SELECT);
        selectManager.distinct();
        return (BuilderType) this;
    }

    public CaseWhenStarterBuilder<BuilderType> selectCase() {
        return selectCase(null);
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public CaseWhenStarterBuilder<BuilderType> selectCase(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectCase((BuilderType) this, selectAlias);
    }

    public SimpleCaseWhenStarterBuilder<BuilderType> selectSimpleCase(String expression) {
        return selectSimpleCase(expression, null);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public SimpleCaseWhenStarterBuilder<BuilderType> selectSimpleCase(String caseOperandExpression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSimpleCase((BuilderType) this, selectAlias, expressionFactory.createSimpleExpression(caseOperandExpression, false));
    }

    public BuilderType select(String expression) {
        return select(expression, null);
    }

    @SuppressWarnings("unchecked")
    public BuilderType select(String expression, String selectAlias) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        selectManager.select(expr, selectAlias);
        if (selectManager.getSelectInfos().size() > 1) {
            // TODO: don't know if we should override this here
            resultType = (Class<QueryResultType>) Tuple.class;
        }
        return (BuilderType) this;
    }

    public SubqueryInitiator<BuilderType> selectSubquery() {
        return selectSubquery((String) null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> selectSubquery(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSubquery((BuilderType) this, selectAlias);
    }

    public SubqueryInitiator<BuilderType> selectSubquery(String subqueryAlias, String expression) {
        return selectSubquery(subqueryAlias, expression, (String) null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (subqueryAlias == null) {
            throw new NullPointerException("subqueryAlias");
        }
        if (subqueryAlias.isEmpty()) {
            throw new IllegalArgumentException("subqueryAlias");
        }
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (!expression.contains(subqueryAlias)) {
            throw new IllegalArgumentException("Expression [" + expression + "] does not contain subquery alias [" + subqueryAlias + "]");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSubquery((BuilderType) this, subqueryAlias, expressionFactory.createSimpleExpression(expression, false), selectAlias);
    }

    public MultipleSubqueryInitiator<BuilderType> selectSubqueries(String expression) {
        return selectSubqueries(null, expression);
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> selectSubqueries(String selectAlias, String expression) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSubqueries((BuilderType) this, expressionFactory.createSimpleExpression(expression, false), selectAlias);
    }

    public SubqueryBuilder<BuilderType> selectSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return selectSubquery(null, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> selectSubquery(String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (criteriaBuilder == null) {
            throw new NullPointerException("criteriaBuilder");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSubquery((BuilderType) this, selectAlias, criteriaBuilder);
    }

    public SubqueryBuilder<BuilderType> selectSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return selectSubquery(subqueryAlias, expression, null, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> selectSubquery(String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (subqueryAlias == null) {
            throw new NullPointerException("subqueryAlias");
        }
        if (subqueryAlias.isEmpty()) {
            throw new IllegalArgumentException("subqueryAlias");
        }
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (criteriaBuilder == null) {
            throw new NullPointerException("criteriaBuilder");
        }
        if (!expression.contains(subqueryAlias)) {
            throw new IllegalArgumentException("Expression [" + expression + "] does not contain subquery alias [" + subqueryAlias + "]");
        }
        verifyBuilderEnded();
        prepareForModification(ClauseType.SELECT);
        return selectManager.selectSubquery((BuilderType) this, subqueryAlias, expressionFactory.createSimpleExpression(expression, false), selectAlias, criteriaBuilder);
    }

    /*
     * Where methods
     */
    public RestrictionBuilder<BuilderType> where(String expression) {
        prepareForModification(ClauseType.WHERE);
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return whereManager.restrict((BuilderType) this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereCase() {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictCase((BuilderType) this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereSimpleCase(String expression) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictSimpleCase((BuilderType) this, expressionFactory.createSimpleExpression(expression, false));
    }

    public WhereOrBuilder<BuilderType> whereOr() {
        prepareForModification(ClauseType.WHERE);
        return whereManager.whereOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereExists() {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereNotExists() {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictNotExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictExists((BuilderType) this, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictNotExists((BuilderType) this, criteriaBuilder);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery() {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrict((BuilderType) this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery(String subqueryAlias, String expression) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrict((BuilderType) this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubqueries(String expression) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrictSubqueries((BuilderType) this, expression);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrict((BuilderType) this, criteriaBuilder);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.WHERE);
        return whereManager.restrict((BuilderType) this, subqueryAlias, expression, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public BuilderType whereExpression(String expression) {
        prepareForModification(ClauseType.WHERE);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        whereManager.restrictExpression(predicate);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> whereExpressionSubqueries(String expression) {
        prepareForModification(ClauseType.WHERE);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return whereManager.restrictExpressionSubqueries((BuilderType) this, predicate);
    }

    @SuppressWarnings("unchecked")
    public BuilderType setWhereExpression(String expression) {
        prepareForModification(ClauseType.WHERE);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        whereManager.restrictSetExpression(predicate);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> setWhereExpressionSubqueries(String expression) {
        prepareForModification(ClauseType.WHERE);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return whereManager.restrictSetExpressionSubqueries((BuilderType) this, predicate);
    }

    /*
     * Group by methods
     */
    @SuppressWarnings("unchecked")
    public BuilderType groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType groupBy(String expression) {
        prepareForModification(ClauseType.GROUP_BY);
        Expression expr;
        if (mainQuery.getQueryConfiguration().isCompatibleModeEnabled()) {
            expr = expressionFactory.createPathExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression, false);
        }
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (BuilderType) this;
    }

    /*
     * Having methods
     */
    public RestrictionBuilder<BuilderType> having(String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return havingManager.restrict((BuilderType) this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingCase() {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictCase((BuilderType) this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingSimpleCase(String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictSimpleCase((BuilderType) this, expressionFactory.createSimpleExpression(expression, false));
    }

    public HavingOrBuilder<BuilderType> havingOr() {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingExists() {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingNotExists() {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> havingExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((BuilderType) this, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> havingNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((BuilderType) this, criteriaBuilder);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery() {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict((BuilderType) this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery(String subqueryAlias, String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict((BuilderType) this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubqueries(String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictSubqueries((BuilderType) this, expression);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> havingSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict((BuilderType) this, criteriaBuilder);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> havingSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict((BuilderType) this, subqueryAlias, expression, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public BuilderType havingExpression(String expression) {
        prepareForModification(ClauseType.HAVING);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        havingManager.restrictExpression(predicate);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> havingExpressionSubqueries(String expression) {
        prepareForModification(ClauseType.HAVING);
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return havingManager.restrictExpressionSubqueries((BuilderType) this, predicate);
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setHavingExpression(String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        havingManager.restrictSetExpression(predicate);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> setHavingExpressionSubqueries(String expression) {
        prepareForModification(ClauseType.HAVING);
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return havingManager.restrictSetExpressionSubqueries((BuilderType) this, predicate);
    }

    /*
     * Order by methods
     */
    public BuilderType orderByDesc(String expression) {
        return orderBy(expression, false, false);
    }

    public BuilderType orderByAsc(String expression) {
        return orderBy(expression, true, false);
    }

    public BuilderType orderByDesc(String expression, boolean nullFirst) {
        return orderBy(expression, false, nullFirst);
    }

    public BuilderType orderByAsc(String expression, boolean nullFirst) {
        return orderBy(expression, true, nullFirst);
    }

    public BuilderType orderBy(String expression, boolean ascending) {
        return orderBy(expression, ascending, false);
    }

    @SuppressWarnings("unchecked")
    public BuilderType orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr;
        if (mainQuery.getQueryConfiguration().isCompatibleModeEnabled()) {
            expr = expressionFactory.createPathExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression, false);
        }
        orderBy(expr, ascending, nullFirst);
        return (BuilderType) this;
    }

    private void orderBy(Expression expression, boolean ascending, boolean nullFirst) {
        prepareForModification(ClauseType.ORDER_BY);
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
    }

    /*
     * Window methods
     */
    public WindowBuilder<BuilderType> window(String name) {
        prepareForModification(ClauseType.WINDOW);
        verifyBuilderEnded();
        return windowManager.window(name, (BuilderType) this);
    }

    protected void verifySetBuilderEnded() {
        if (finalSetOperationBuilder != null) {
            if (!setOperationEnded) {
                throw new IllegalStateException("Set operation builder not properly ended!");
            }
        }
    }

    protected void verifyBuilderEnded() {
        if (isMainQuery) {
            mainQuery.cteManager.verifyBuilderEnded();
        }
        
        whereManager.verifyBuilderEnded();
        keysetManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
        joinManager.verifyBuilderEnded();
        windowManager.verifyBuilderEnded();
    }

    /*
     * Join methods
     */
    public BuilderType innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    public BuilderType innerJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER);
    }

    public BuilderType leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    public BuilderType leftJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT);
    }

    public BuilderType rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    public BuilderType rightJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT);
    }

    @SuppressWarnings("unchecked")
    public BuilderType join(String path, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, false);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType joinDefault(String path, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, true);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String path, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinDefaultOn(String path, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, true);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(Class<?> clazz, String alias, JoinType type) {
        return joinOn(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, type);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String base, Class<?> entityClass, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(base, alias, type);
        if (entityClass == null) {
            throw new NullPointerException("entityClass");
        }
        return joinManager.joinOn((BuilderType) this, base, entityClass, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(EntityType<?> entityType, String alias, JoinType type) {
        return joinOn(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), entityType, alias, type);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String base, EntityType<?> entityType, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(base, alias, type);
        if (entityType == null) {
            throw new NullPointerException("entityType");
        }
        return joinManager.joinOn((BuilderType) this, base, entityType, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnSubquery(Class<?> clazz, String alias, JoinType type) {
        return joinOnSubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnSubquery(String base, Class<?> entityClass, String alias, JoinType type) {
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(base, alias, type);
        if (entityClass == null) {
            throw new NullPointerException("entityClass");
        }
        return with(entityClass, alias, true, joinManager, false, joinManager.joinOn((BuilderType) this, base, entityClass, alias, type, false));
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnEntitySubquery(Class<?> clazz, String alias, JoinType type) {
        return joinOnEntitySubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, null, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnEntitySubquery(String base, Class<?> entityClass, String alias, JoinType type) {
        return joinOnEntitySubquery(base, entityClass, alias, null, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias, JoinType type) {
        return joinOnEntitySubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, subqueryAlias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type) {
        return bindEntityAttributes(alias, subqueryAlias, joinOnSubquery(base, entityClass, alias, type), false, true);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnSubquery(Class<?> clazz, String alias, JoinType type) {
        return joinLateralOnSubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnSubquery(String base, Class<?> entityClass, String alias, JoinType type) {
        if (type != JoinType.INNER && type != JoinType.LEFT) {
            throw new IllegalArgumentException("Lateral joins are only possible with inner or left joins!");
        }
        if (mainQuery.dbmsDialect.getLateralStyle() == LateralStyle.NONE) {
            throw new IllegalStateException("The dbms dialect does not support lateral joins!");
        }
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(base, alias, type);
        if (entityClass == null) {
            throw new NullPointerException("entityClass");
        }
        return with(entityClass, alias, true, joinManager, true, joinManager.joinOn((BuilderType) this, base, entityClass, alias, type, true));
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias, JoinType type) {
        if (type != JoinType.INNER && type != JoinType.LEFT) {
            throw new IllegalArgumentException("Lateral joins are only possible with inner or left joins!");
        }
        if (mainQuery.dbmsDialect.getLateralStyle() == LateralStyle.NONE) {
            throw new IllegalStateException("The dbms dialect does not support lateral joins!");
        }
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(correlationPath, alias, type);
        return joinManager.joinOn((BuilderType) this, correlationPath, alias, subqueryAlias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias, JoinType type) {
        return joinLateralOnEntitySubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, subqueryAlias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type) {
        return bindEntityAttributes(alias, subqueryAlias, joinLateralOnSubquery(base, entityClass, alias, type), true, true);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> joinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias, JoinType type) {
        return bindEntityAttributes(alias, subqueryAlias, joinLateralOnSubquery(correlationPath, alias, subqueryAlias, type), true, false);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralSubquery(Class<?> clazz, String alias, JoinType type) {
        return joinLateralSubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralSubquery(String base, Class<?> entityClass, String alias, JoinType type) {
        if (type != JoinType.INNER && type != JoinType.LEFT) {
            throw new IllegalArgumentException("Lateral joins are only possible with inner or left joins!");
        }
        if (mainQuery.dbmsDialect.getLateralStyle() == LateralStyle.NONE) {
            throw new IllegalStateException("The dbms dialect does not support lateral joins!");
        }
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(base, alias, type);
        if (entityClass == null) {
            throw new NullPointerException("entityClass");
        }
        return with(entityClass, alias, true, joinManager, true, joinManager.joinOn((BuilderType) this, base, entityClass, alias, type, true).onExpression("1=1").end());
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralSubquery(String correlationPath, String alias, String subqueryAlias, JoinType type) {
        if (type != JoinType.INNER && type != JoinType.LEFT) {
            throw new IllegalArgumentException("Lateral joins are only possible with inner or left joins!");
        }
        if (mainQuery.dbmsDialect.getLateralStyle() == LateralStyle.NONE) {
            throw new IllegalStateException("The dbms dialect does not support lateral joins!");
        }
        prepareForModification(ClauseType.JOIN);
        checkJoinPreconditions(correlationPath, alias, type);
        return joinManager.join((BuilderType) this, correlationPath, alias, subqueryAlias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralEntitySubquery(Class<?> clazz, String alias, String subqueryAlias, JoinType type) {
        return joinLateralEntitySubquery(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, subqueryAlias, type);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type) {
        return bindEntityAttributes(alias, subqueryAlias, joinLateralSubquery(base, entityClass, alias, type), true, true);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> joinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias, JoinType type) {
        return bindEntityAttributes(alias, subqueryAlias, joinLateralSubquery(correlationPath, alias, subqueryAlias, type), true, false);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(Class<?> clazz, String alias) {
        return joinOn(clazz, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(String base, Class<?> clazz, String alias) {
        return joinOn(base, clazz, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(EntityType<?> entityType, String alias) {
        return joinOn(entityType, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(String base, EntityType<?> entityType, String alias) {
        return joinOn(base, entityType, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnSubquery(Class<?> clazz, String alias) {
        return joinOnSubquery(clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnSubquery(String base, Class<?> clazz, String alias) {
        return joinOnSubquery(base, clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnEntitySubquery(Class<?> clazz, String alias) {
        return joinOnEntitySubquery(clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnEntitySubquery(String base, Class<?> clazz, String alias) {
        return joinOnEntitySubquery(base, clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinOnEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnSubquery(Class<?> clazz, String alias) {
        return joinLateralOnSubquery(clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnSubquery(String base, Class<?> clazz, String alias) {
        return joinLateralOnSubquery(base, clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralOnSubquery(correlationPath, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> innerJoinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(correlationPath, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralSubquery(Class<?> clazz, String alias) {
        return joinLateralSubquery(clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralSubquery(String base, Class<?> clazz, String alias) {
        return joinLateralSubquery(base, clazz, alias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralSubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralSubquery(correlationPath, alias, subqueryAlias, JoinType.INNER);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> innerJoinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(correlationPath, alias, subqueryAlias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(EntityType<?> entityType, String alias) {
        return joinOn(entityType, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(String base, EntityType<?> entityType, String alias) {
        return joinOn(base, entityType, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(Class<?> clazz, String alias) {
        return joinOn(clazz, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(String base, Class<?> clazz, String alias) {
        return joinOn(base, clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnSubquery(Class<?> clazz, String alias) {
        return joinOnSubquery(clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnSubquery(String base, Class<?> clazz, String alias) {
        return joinOnSubquery(base, clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnEntitySubquery(Class<?> clazz, String alias) {
        return joinOnEntitySubquery(clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnEntitySubquery(String base, Class<?> clazz, String alias) {
        return joinOnEntitySubquery(base, clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinOnEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnSubquery(Class<?> clazz, String alias) {
        return joinLateralOnSubquery(clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnSubquery(String base, Class<?> clazz, String alias) {
        return joinLateralOnSubquery(base, clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralOnSubquery(correlationPath, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> leftJoinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralOnEntitySubquery(correlationPath, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralSubquery(Class<?> clazz, String alias) {
        return joinLateralSubquery(clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralSubquery(String base, Class<?> clazz, String alias) {
        return joinLateralSubquery(base, clazz, alias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralSubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralSubquery(correlationPath, alias, subqueryAlias, JoinType.LEFT);
    }

    public FullSelectCTECriteriaBuilder<BuilderType> leftJoinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias) {
        return joinLateralEntitySubquery(correlationPath, alias, subqueryAlias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(Class<?> clazz, String alias) {
        return joinOn(clazz, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(String base, Class<?> clazz, String alias) {
        return joinOn(base, clazz, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(EntityType<?> entityType, String alias) {
        return joinOn(entityType, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(String base, EntityType<?> entityType, String alias) {
        return joinOn(base, entityType, alias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnSubquery(Class<?> clazz, String alias) {
        return joinOnSubquery(clazz, alias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnSubquery(String base, Class<?> clazz, String alias) {
        return joinOnSubquery(base, clazz, alias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnEntitySubquery(Class<?> clazz, String alias) {
        return joinOnEntitySubquery(clazz, alias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnEntitySubquery(String base, Class<?> clazz, String alias) {
        return joinOnEntitySubquery(base, clazz, alias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnEntitySubquery(Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(clazz, alias, subqueryAlias, JoinType.RIGHT);
    }

    public FullSelectCTECriteriaBuilder<JoinOnBuilder<BuilderType>> rightJoinOnEntitySubquery(String base, Class<?> clazz, String alias, String subqueryAlias) {
        return joinOnEntitySubquery(base, clazz, alias, subqueryAlias, JoinType.RIGHT);
    }

    private void checkJoinPreconditions(String path, String alias, JoinType type) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (alias == null) {
            throw new NullPointerException("alias");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException("Empty alias");
        }
        verifyBuilderEnded();
    }
    
    protected boolean isJoinRequiredForSelect() {
        return true;
    }

    protected JoinVisitor applyImplicitJoins(JoinVisitor parentVisitor) {
        if (implicitJoinsApplied) {
            return null;
        }

        // The first thing we need to do, is reorder values clauses without joins to the end of the from clause roots
        // This is an ugly integration detail, but to me, this seems to be the only way to support the values clause in all situations
        // We put the values clauses without joins to the end because we will add synthetic predicates to the where clause
        // This is necessary in order to have a single query that has all parameters which will simplify a lot
        // If a values clause has a join, we will inject the synthetic predicate into that joins on clause which will preserve the correct order
        // We just have to hope that Hibernate will not reorder roots when translating to SQL, if it does, this will break..
        // NOTE: If it turns out to be problematic, I can imagine introducing a synthetic SELECT item that is removed in the end for this purpose
        joinManager.reorderSimpleValuesClauses();

        final JoinVisitor joinVisitor = new JoinVisitor(mainQuery, windowManager, parentVisitor, joinManager, parameterManager, !mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions());
        joinVisitor.setFromClause(ClauseType.JOIN);
        joinManager.acceptVisitor(joinVisitor);
        // carry out implicit joins
        joinVisitor.setFromClause(ClauseType.SELECT);
        // There might be clauses for which joins are not required
        joinVisitor.setJoinRequired(isJoinRequiredForSelect());
        selectManager.acceptVisitor((SelectInfoVisitor) joinVisitor);
        joinVisitor.setJoinRequired(true);

        // Only the main query has fetch owners
        if (isMainQuery) {
            StringBuilder sb = null;
            Set<JoinNode> fetchOwners = selectManager.collectFetchOwners();
            nodesToFetch = new HashSet<>();
            // Add all parents of the fetchable nodes to nodesToFetch until the fetch owner is reached
            // If we reach a root before a fetch owner, the fetch owner is missing
            final List<JoinNode> fetchableNodes = joinVisitor.getFetchableNodes();
            for (int i = 0; i < fetchableNodes.size(); i++) {
                JoinNode fetchableNode = fetchableNodes.get(i);
                while (!fetchOwners.contains(fetchableNode)) {
                    nodesToFetch.add(fetchableNode);
                    if (fetchableNode.getParent() == null) {
                        if (sb == null) {
                            sb = new StringBuilder();
                            sb.append("Some join nodes specified fetch joining but their fetch owners weren't included in the select clause! Missing fetch owners: [");
                        } else {
                            sb.append(", ");
                        }
                        sb.append(fetchableNode.getAlias());
                        break;
                    }
                    fetchableNode = fetchableNode.getParent();
                    // We don't care about treated nodes specifically when fetching as they aren't declarable directly, but only the "main" node
                    if (fetchableNode.isTreatedJoinNode()) {
                        fetchableNode = ((TreatedJoinAliasInfo) fetchableNode.getAliasInfo()).getTreatedJoinNode();
                    }
                }
            }
            if (sb != null) {
                sb.append("]");
                throw new IllegalStateException(sb.toString());
            }
        } else {
            nodesToFetch = Collections.emptySet();
        }

        joinVisitor.setFromClause(ClauseType.WHERE);
        whereManager.acceptVisitor(joinVisitor);
        joinVisitor.setFromClause(ClauseType.GROUP_BY);
        groupByManager.acceptVisitor(joinVisitor);

        joinVisitor.setFromClause(ClauseType.HAVING);
        havingManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(false);

        joinVisitor.setFromClause(ClauseType.ORDER_BY);
        orderByManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(true);
        // No need to implicit join again if no mutation occurs
        implicitJoinsApplied = true;
        return joinVisitor;
    }

    void inlineSelectAlias(String selectAlias, Expression expression) {
        final AliasReplacementVisitor aliasReplacementVisitor = new AliasReplacementVisitor(expression, selectAlias);
        ExpressionModifierVisitor<ExpressionModifier> expressionModifierVisitor = new ExpressionModifierVisitor<ExpressionModifier>() {
            @Override
            public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
                Expression expr = expressionModifier.get();
                Expression newExpr = expr.accept(aliasReplacementVisitor);
                if (expr != newExpr) {
                    expressionModifier.set(newExpr);
                }
            }
        };
        joinManager.apply(expressionModifierVisitor);
        selectManager.apply(expressionModifierVisitor);
        whereManager.apply(expressionModifierVisitor);
        havingManager.apply(expressionModifierVisitor);
        groupByManager.apply(expressionModifierVisitor);
        orderByManager.apply(expressionModifierVisitor);
    }

    protected void implicitJoinWhereClause() {
        final JoinVisitor joinVisitor = new JoinVisitor(mainQuery, windowManager, null, joinManager, parameterManager, !mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions());
        joinVisitor.setJoinRequired(true);
        joinVisitor.setFromClause(ClauseType.WHERE);
        whereManager.acceptVisitor(joinVisitor);
    }

    protected void collectParameters() {
        ParameterRegistrationVisitor parameterRegistrationVisitor = parameterManager.getParameterRegistrationVisitor();
        ClauseType oldClauseType = parameterRegistrationVisitor.getClauseType();
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterRegistrationVisitor.getQueryBuilder();
        try {
            parameterRegistrationVisitor.setQueryBuilder(this);
            parameterRegistrationVisitor.setClauseType(ClauseType.SELECT);
            selectManager.acceptVisitor(parameterRegistrationVisitor);
            parameterRegistrationVisitor.setClauseType(ClauseType.JOIN);
            joinManager.acceptVisitor(new OnClauseJoinNodeVisitor(parameterRegistrationVisitor));
            parameterRegistrationVisitor.setClauseType(ClauseType.WHERE);
            whereManager.acceptVisitor(parameterRegistrationVisitor);
            parameterRegistrationVisitor.setClauseType(ClauseType.GROUP_BY);
            groupByManager.acceptVisitor(parameterRegistrationVisitor);
            parameterRegistrationVisitor.setClauseType(ClauseType.HAVING);
            havingManager.acceptVisitor(parameterRegistrationVisitor);
            parameterRegistrationVisitor.setClauseType(ClauseType.ORDER_BY);
            orderByManager.acceptVisitor(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(oldClauseType);
            parameterRegistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    protected void applyVisitor(VisitorAdapter expressionVisitor) {
        selectManager.acceptVisitor(expressionVisitor);
        joinManager.acceptVisitor(new OnClauseJoinNodeVisitor(expressionVisitor));
        whereManager.acceptVisitor(expressionVisitor);
        groupByManager.acceptVisitor(expressionVisitor);
        havingManager.acceptVisitor(expressionVisitor);
        orderByManager.acceptVisitor(expressionVisitor);
    }

    public void applyExpressionTransformersAndBuildGroupByClauses(boolean addsGroupBy, JoinVisitor joinVisitor) {
        groupByManager.resetCollected();
        groupByManager.collectGroupByClauses(joinVisitor);

        orderByManager.splitEmbeddables(joinVisitor);

        int size = transformerGroups.size();
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            ExpressionTransformerGroup<ExpressionModifier> transformerGroup = (ExpressionTransformerGroup<ExpressionModifier>) transformerGroups.get(i);
            transformerGroup.applyExpressionTransformer(joinManager);
            transformerGroup.applyExpressionTransformer(selectManager);
            transformerGroup.applyExpressionTransformer(whereManager);
            transformerGroup.applyExpressionTransformer(groupByManager);
            transformerGroup.applyExpressionTransformer(havingManager);
            transformerGroup.applyExpressionTransformer(orderByManager);

            transformerGroup.afterTransformationGroup();
        }

        // After all transformations are done, we can finally check if aggregations are used
        hasGroupBy = groupByManager.hasCollectedGroupByClauses();
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(selectManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(joinManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(whereManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(orderByManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(havingManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE));

        if (hasGroupBy || addsGroupBy) {
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromSelectEnabled()) {
                selectManager.buildGroupByClauses(cbf.getMetamodel(), groupByManager, hasGroupBy, joinVisitor);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromHavingEnabled()) {
                havingManager.buildGroupByClauses(groupByManager, hasGroupBy, joinVisitor);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromOrderByEnabled()) {
                orderByManager.buildGroupByClauses(groupByManager, hasGroupBy, joinVisitor);
            }
        }

        for (int i = 0; i < size; i++) {
            ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
            transformerGroup.afterAllTransformations();
        }
    }

    public Class<QueryResultType> getResultType() {
        return resultType;
    }

    public String getQueryString() {
        prepareAndCheck();
        return getExternalQueryString();
    }
    
    protected String getBaseQueryStringWithCheck(StringBuilder lateralSb, JoinNode lateralJoinNode) {
        prepareAndCheck();
        return getBaseQueryString(lateralSb, lateralJoinNode);
    }

    protected final TypedQuery<QueryResultType> getTypedQueryForFinalOperationBuilder() {
        try {
            checkSetBuilderEnded = false;
            return getTypedQuery(null, null);
        } finally {
            checkSetBuilderEnded = true;
        }
    }

    protected TypedQuery<QueryResultType> getTypedQuery(StringBuilder lateralSb, JoinNode lateralJoinNode) {
        // NOTE: This must happen first because it generates implicit joins
        String baseQueryString = getBaseQueryStringWithCheck(lateralSb, lateralJoinNode);
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();
        final boolean needsSqlReplacement = isMainQuery && mainQuery.cteManager.hasCtes() || joinManager.hasEntityFunctions() || !keyRestrictedLeftJoins.isEmpty() || !isMainQuery && hasLimit();
        if (!needsSqlReplacement) {
            TypedQuery<QueryResultType> baseQuery = createTypedQuery(baseQueryString);
            parameterManager.parameterizeQuery(baseQuery);
            return baseQuery;
        }

        TypedQuery<QueryResultType> baseQuery = createTypedQuery(baseQueryString);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        String limit = null;
        String offset = null;

        // The main query will handle that separately
        if (!isMainQuery && lateralSb == null) {
            if (firstResult != 0) {
                offset = Integer.toString(firstResult);
            }
            if (maxResults != Integer.MAX_VALUE) {
                limit = Integer.toString(maxResults);
            }
        }
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, Collections.EMPTY_SET);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery, isMainQuery);
        boolean shouldRenderCteNodes = lateralSb == null && renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterManager.getParameters(), parameterListNames, limit, offset, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<QueryResultType> query = new CustomSQLTypedQuery<QueryResultType>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        // The main query will use the native mechanism for limit/offset
        if (isMainQuery && lateralSb == null) {
            if (firstResult != 0) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != Integer.MAX_VALUE) {
                query.setMaxResults(maxResults);
            }
        }

        parameterManager.parameterizeQuery(query);
        return applyObjectBuilder(query);
    }

    protected List<String> getKeyRestrictedLeftJoinAliases(Query baseQuery, Set<JoinNode> keyRestrictedLeftJoins, Set<ClauseType> clauseExclusions) {
        List<String> keyRestrictedLeftJoinAliases = new ArrayList<String>();
        if (!keyRestrictedLeftJoins.isEmpty()) {
            for (JoinNode node : keyRestrictedLeftJoins) {
                if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && !node.isCardinalityMandatory()) {
                    continue;
                }
                // The alias of the target entity table
                String sqlAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, node.getAliasInfo().getAlias());
                keyRestrictedLeftJoinAliases.add(sqlAlias);
            }
        }
        return keyRestrictedLeftJoinAliases;
    }

    protected List<EntityFunctionNode> getEntityFunctionNodes(Query baseQuery, boolean embeddedToMainQuery) {
        return getEntityFunctionNodes(baseQuery, embeddedToMainQuery, joinManager.getEntityFunctionNodes(), joinManager.getLateInlineNodes());
    }

    protected List<EntityFunctionNode> getEntityFunctionNodes(Query baseQuery, boolean embeddedToMainQuery, List<JoinNode> valuesNodes, List<JoinNode> lateInlineNodes) {
        List<EntityFunctionNode> entityFunctionNodes = new ArrayList<>();

        DbmsDialect dbmsDialect = mainQuery.dbmsDialect;
        ValuesStrategy strategy = dbmsDialect.getValuesStrategy();
        String dummyTable = dbmsDialect.getDummyTable();

        for (JoinNode node : valuesNodes) {
            Class<?> clazz = node.getInternalEntityType().getJavaType();
            String valueClazzAttributeName = node.getValuesLikeAttribute();
            int valueCount = node.getValueCount();
            boolean identifiableReference = node.getNodeType() instanceof EntityType<?> && node.getValuesIdNames() != null;
            String rootAlias = node.getAlias();
            String castedParameter = node.getValuesCastedParameter();
            String[] attributes = node.getValuesAttributes();

            // We construct an example query representing the values clause with a SELECT clause that selects the fields in the right order which we need to construct SQL
            // that uses proper aliases and filters null values which are there in the first place to pad up parameters in case we don't reach the desired value count
            StringBuilder valuesSb = new StringBuilder(20 + valueCount * attributes.length * 3);
            Query valuesExampleQuery = getValuesExampleQuery(clazz, valueCount, identifiableReference, valueClazzAttributeName, rootAlias, castedParameter, attributes, valuesSb, strategy, dummyTable, node);

            String exampleQuerySql = mainQuery.cbf.getExtendedQuerySupport().getSql(mainQuery.em, valuesExampleQuery);
            String exampleQuerySqlAlias = mainQuery.cbf.getExtendedQuerySupport().getSqlAlias(mainQuery.em, valuesExampleQuery, "e");
            String exampleQueryCollectionSqlAlias = null;
            if (!node.isValueClazzAttributeSingular()) {
                exampleQueryCollectionSqlAlias = mainQuery.cbf.getExtendedQuerySupport().getSqlAlias(mainQuery.em, valuesExampleQuery, node.getValueClazzAlias("e_"));
            }
            StringBuilder whereClauseSb = new StringBuilder(exampleQuerySql.length());
            String filterNullsTableAlias = "fltr_nulls_tbl_als_";
            String valuesAliases = getValuesAliases(exampleQuerySqlAlias, attributes.length, exampleQuerySql, whereClauseSb, filterNullsTableAlias, strategy, dummyTable);
            boolean filterNulls = mainQuery.getQueryConfiguration().isValuesClauseFilterNullsEnabled();

            if (strategy == ValuesStrategy.SELECT_VALUES) {
                valuesSb.insert(0, valuesAliases);
                valuesSb.append(')');
                valuesAliases = null;
            } else if (strategy == ValuesStrategy.SELECT_UNION) {
                valuesSb.insert(0, valuesAliases);
                if (!filterNulls) {
                    // We must order by all values and use a limit in such a case
                    valuesSb.insert(0, "(select * from ");
                    valuesSb.append(") val_tmp_ order by ");
                    if (dbmsDialect.isNullSmallest()) {
                        for (int i = 0; i < attributes.length; i++) {
                            valuesSb.append(i + 1);
                            valuesSb.append(',');
                        }
                    } else {
                        for (int i = 0; i < attributes.length; i++) {
                            dbmsDialect.appendOrderByElement(valuesSb, new DefaultOrderByElement(null, i + 1, true, true, true), null);
                            valuesSb.append(',');
                        }
                    }
                    valuesSb.setCharAt(valuesSb.length() - 1, ' ');
                    dbmsDialect.appendExtendedSql(valuesSb, DbmsStatementType.SELECT, false, true, null, Integer.toString(valueCount + 1), "1", null, null);
                }
                valuesSb.append(')');
                valuesAliases = null;
            }

            if (filterNulls) {
                valuesSb.insert(0, "(select * from ");
                valuesSb.append(' ');
                valuesSb.append(filterNullsTableAlias);
                if (valuesAliases != null) {
                    valuesSb.append(valuesAliases);
                    valuesAliases = null;
                }
                valuesSb.append(whereClauseSb);
                valuesSb.append(')');
            }

            String valuesClause = valuesSb.toString();
            String valuesTableSqlAlias = exampleQuerySqlAlias;
            String valuesTableJoin = null;
            String pluralCollectionTableAlias = null;
            String pluralTableAlias = null;
            String syntheticPredicate = exampleQuerySql.substring(SqlUtils.indexOfWhere(exampleQuerySql) + " where ".length());
            if (baseQuery != null) {
                valuesTableSqlAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, node.getAlias());
                syntheticPredicate = syntheticPredicate.replace(exampleQuerySqlAlias, valuesTableSqlAlias);
                if (exampleQueryCollectionSqlAlias != null) {
                    pluralTableAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, node.getValueClazzAlias(node.getAlias() + "_"));
                    syntheticPredicate = syntheticPredicate.replace(exampleQueryCollectionSqlAlias, pluralTableAlias);
                    String baseQuerySql = cbf.getExtendedQuerySupport().getSql(em, baseQuery);
                    int[] indexRange = SqlUtils.indexOfFullJoin(baseQuerySql, pluralTableAlias);
                    String baseTableAlias = " " + valuesTableSqlAlias + " ";
                    int baseTableAliasIndex = baseQuerySql.indexOf(baseTableAlias);
                    int fullJoinStartIndex = baseTableAliasIndex + baseTableAlias.length();
                    if (fullJoinStartIndex != indexRange[0]) {
                        // TODO: find out pluralCollectionTableAlias
                        String onClause = " on ";
                        int onClauseIndex = baseQuerySql.indexOf(onClause, fullJoinStartIndex);
                        int[] collectionTableIndexRange = SqlUtils.rtrimBackwardsToFirstWhitespace(baseQuerySql, onClauseIndex);
                        pluralCollectionTableAlias = baseQuerySql.substring(collectionTableIndexRange[0], collectionTableIndexRange[1]);
                    }
                    valuesTableJoin = baseQuerySql.substring(fullJoinStartIndex, indexRange[1]);
                }
            }

            entityFunctionNodes.add(new EntityFunctionNode(valuesClause, valuesAliases, node.getInternalEntityType().getName(), valuesTableSqlAlias, pluralCollectionTableAlias, pluralTableAlias, valuesTableJoin, syntheticPredicate, false));
        }

        // We assume to have to select via a union to apply aliases correctly when the values strategy requires that
        boolean selectUnion = strategy == ValuesStrategy.SELECT_UNION;
        boolean lateralStyle = dbmsDialect.getLateralStyle() == LateralStyle.LATERAL;
        for (JoinNode lateInlineNode : lateInlineNodes) {
            CTEInfo cteInfo = lateInlineNode.getInlineCte();
            String aliases;
            StringBuilder aliasesSb = new StringBuilder();
            if (selectUnion) {
                aliasesSb.append("select ");
                for (int i = 0; i < cteInfo.columnNames.size(); i++) {
                    aliasesSb.append("null ");
                    aliasesSb.append(cteInfo.columnNames.get(i)).append(',');
                }
                aliasesSb.setCharAt(aliasesSb.length() - 1, ' ');
                aliasesSb.append(" from ").append(dummyTable).append(" where 1=0 union all ");
                aliases = null;
            } else {
                aliasesSb.append('(');
                for (int i = 0; i < cteInfo.columnNames.size(); i++) {
                    aliasesSb.append(cteInfo.columnNames.get(i)).append(',');
                }
                aliasesSb.setCharAt(aliasesSb.length() - 1, ')');
                aliases = aliasesSb.toString();
                aliasesSb = null;
            }

            String subquery;
            if (lateInlineNode.isLateral()) {
                // We need to wrap the lateral subquery into a temporary HQL subquery to fake the correlation
                // Then, we extract the sql part of the actual subquery
                StringBuilder lateralExampleQueryString = new StringBuilder();
                buildLateralExampleQueryString(lateralExampleQueryString);
                String sql = getQuerySpecification(cteInfo.nonRecursiveCriteriaBuilder.getLateralQuery(lateralExampleQueryString, lateralStyle ? null : lateInlineNode)).getSql();
                int start = SqlUtils.indexOfWhere(sql);
                while (sql.charAt(start) != '(') {
                    start++;
                }

                String prefix;
                if (dbmsDialect.getLateralStyle() == LateralStyle.LATERAL) {
                    prefix = "lateral (";
                } else if (lateInlineNode.getJoinType() == JoinType.INNER) {
                    prefix = "cross apply (";
                } else {
                    prefix = "outer apply (";
                }
                if (aliasesSb == null) {
                    subquery = prefix + sql.substring(start + 1, sql.lastIndexOf(')')) + ")";
                } else {
                    aliasesSb.insert(0, prefix).append('(').append(sql, start + 1, sql.lastIndexOf(')')).append(')').append(')');
                    subquery = aliasesSb.toString();
                }
            } else {
                if (aliasesSb == null) {
                    subquery = "(" + getQuerySpecification(cteInfo.nonRecursiveCriteriaBuilder.getQuery(embeddedToMainQuery && baseQuery == null)).getSql() + ")";
                } else {
                    aliasesSb.insert(0, "(").append('(').append(getQuerySpecification(cteInfo.nonRecursiveCriteriaBuilder.getQuery(embeddedToMainQuery && baseQuery == null)).getSql()).append(')').append(')');
                    subquery = aliasesSb.toString();
                }
            }
            String cteTableSqlAlias = baseQuery == null ? "" : cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, lateInlineNode.getAlias());
            entityFunctionNodes.add(new EntityFunctionNode(subquery, aliases, cteInfo.cteType.getName(), cteTableSqlAlias, null, null, null, null, lateInlineNode.isLateral()));
        }

        return entityFunctionNodes;
    }

    private String getValuesAliases(String tableAlias, int attributeCount, String exampleQuerySql, StringBuilder whereClauseSb, String filterNullsTableAlias, ValuesStrategy strategy, String dummyTable) {
        int startIndex =  SqlUtils.indexOfSelect(exampleQuerySql);
        int endIndex = exampleQuerySql.indexOf(" from ");

        StringBuilder sb;

        if (strategy == ValuesStrategy.VALUES) {
            sb = new StringBuilder((endIndex - startIndex) - (tableAlias.length() + 3) * attributeCount);
            sb.append('(');
        } else if (strategy == ValuesStrategy.SELECT_VALUES) {
            sb = new StringBuilder(endIndex - startIndex);
            sb.append("(select ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            sb = new StringBuilder((endIndex - startIndex) - (tableAlias.length() + 3) * attributeCount);
            sb.append("(select ");
        } else {
            throw new IllegalArgumentException("Unsupported values strategy: " + strategy);
        }

        whereClauseSb.append(" where");
        String[] columnNames = SqlUtils.getSelectItemColumns(exampleQuerySql, startIndex);

        for (int i = 0; i < attributeCount; i++) {
            whereClauseSb.append(' ');
            if (i > 0) {
                whereClauseSb.append("or ");
            }
            whereClauseSb.append(filterNullsTableAlias);
            whereClauseSb.append('.');
            whereClauseSb.append(columnNames[i]);
            whereClauseSb.append(" is not null");

            if (strategy == ValuesStrategy.SELECT_VALUES) {
                // TODO: This naming is actually H2 specific
                sb.append('c');
                sb.append(i + 1);
                sb.append(' ');
            } else if (strategy == ValuesStrategy.SELECT_UNION) {
                sb.append("null as ");
            }

            sb.append(columnNames[i]);
            sb.append(',');
        }

        if (strategy == ValuesStrategy.VALUES) {
            sb.setCharAt(sb.length() - 1, ')');
        } else if (strategy == ValuesStrategy.SELECT_VALUES) {
            sb.setCharAt(sb.length() - 1, ' ');
            sb.append(" from ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            sb.setCharAt(sb.length() - 1, ' ');
            if (dummyTable != null) {
                sb.append(" from ");
                sb.append(dummyTable);
            }
        }

        return sb.toString();
    }

    private Query getValuesExampleQuery(Class<?> clazz, int valueCount, boolean identifiableReference, String valueClazzAttributeName, String prefix, String castedParameter, String[] attributes, StringBuilder valuesSb, ValuesStrategy strategy, String dummyTable, JoinNode valuesNode) {
        String[] attributeParameter = new String[attributes.length];
        // This size estimation roughly assumes a maximum attribute name length of 15
        StringBuilder sb = new StringBuilder(50 + valueCount * prefix.length() * attributes.length * 50);
        sb.append("SELECT ");

        if (clazz == ValuesEntity.class) {
            sb.append("e.");
            attributeParameter[0] = mainQuery.dbmsDialect.needsCastParameters() ? castedParameter : "?";
            sb.append(attributes[0]);
            sb.append(',');
        } else {
            Map<String, ExtendedAttribute> mapping =  mainQuery.metamodel.getManagedType(ExtendedManagedType.class, clazz).getAttributes();
            StringBuilder paramBuilder = new StringBuilder();
            for (int i = 0; i < attributes.length; i++) {
                ExtendedAttribute entry;
                if (valuesNode.isValueClazzSimpleValue()) {
                    entry = mapping.get(valueClazzAttributeName);
                } else {
                    entry = mapping.get(attributes[i]);
                }
                Attribute attribute = entry.getAttribute();
                String[] columnTypes;
                if (valuesNode.getQualificationExpression() == null) {
                    columnTypes = entry.getColumnTypes();
                } else {
                    Collection<String> types = entry.getJoinTable().getKeyColumnTypes().values();
                    columnTypes = types.toArray(new String[types.size()]);
                }
                attributeParameter[i] = getCastedParameters(paramBuilder, mainQuery.dbmsDialect, columnTypes);

                // When the class for which we want a VALUES clause has *ToOne relations, we need to put their ids into the select
                // otherwise we would fetch all of the types attributes, but the VALUES clause can only ever contain the id
                if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC &&
                        attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED &&
                        valuesNode.getQualificationExpression() == null) {
                    ManagedType<?> managedAttributeType = mainQuery.metamodel.getManagedType(entry.getElementClass());
                    if (managedAttributeType == null || mainQuery.jpaProvider.needsElementCollectionIdCutoff() && valuesNode.getValuesLikeAttribute() != null && mapping.get(valuesNode.getValuesLikeAttribute()).getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                        sb.append("e");
                        if (valuesNode.isValueClazzAttributeSingular()) {
                            sb.append('.');
                            sb.append(attributes[i]);
                        } else {
                            sb.append('_');
                            sb.append(valueClazzAttributeName.replace('.', '_'));
                            sb.append(attributes[i], valueClazzAttributeName.length(), attributes[i].length());
                        }
                    } else {
                        for (Attribute<?, ?> attributeTypeIdAttribute : JpaMetamodelUtils.getIdAttributes((IdentifiableType<?>) managedAttributeType)) {
                            sb.append("e");
                            if (valuesNode.isValueClazzAttributeSingular()) {
                                sb.append('.');
                                sb.append(attributes[i]);
                            } else {
                                sb.append('_');
                                sb.append(valueClazzAttributeName.replace('.', '_'));
                                sb.append(attributes[i], valueClazzAttributeName.length(), attributes[i].length());
                            }
                            sb.append('.');
                            sb.append(attributeTypeIdAttribute.getName());
                        }
                    }
                } else {
                    if (valuesNode.getQualificationExpression() != null) {
                        sb.append(valuesNode.getQualificationExpression()).append('(');
                    }
                    sb.append("e");
                    if (valuesNode.isValueClazzAttributeSingular()) {
                        sb.append('.');
                        sb.append(attributes[i]);
                    } else {
                        sb.append('_');
                        sb.append(valueClazzAttributeName.replace('.', '_'));
                        sb.append(attributes[i], valueClazzAttributeName.length(), attributes[i].length());
                    }
                    if (valuesNode.getQualificationExpression() != null) {
                        sb.append('_').append(valuesNode.getQualificationExpression().toLowerCase()).append(')');
                    }
                }

                sb.append(',');
            }
        }

        sb.setCharAt(sb.length() - 1, ' ');
        sb.append("FROM ");
        sb.append(clazz.getName());
        sb.append(" e");
        if (!valuesNode.isValueClazzAttributeSingular()) {
            sb.append(" LEFT JOIN e.");
            if (valuesNode.isValueClazzSimpleValue()) {
                sb.append(valueClazzAttributeName);
            } else {
                sb.append(valuesNode.getValuesLikeAttribute());
            }
            sb.append(" e_");
            if (valuesNode.isValueClazzSimpleValue()) {
                sb.append(valueClazzAttributeName.replace('.', '_'));
            } else {
                sb.append(valuesNode.getValuesLikeAttribute().replace('.', '_'));
            }
            if (valuesNode.getQualificationExpression() != null) {
                sb.append('_').append(valuesNode.getQualificationExpression().toLowerCase());
            }
        }
        sb.append(" WHERE ");
        joinManager.renderPlaceholderRequiringPredicate(sb, valuesNode, "e", false, true);

        if (strategy == ValuesStrategy.SELECT_VALUES || strategy == ValuesStrategy.VALUES) {
            valuesSb.append("(VALUES ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            // Nothing to do here
        } else {
            throw new IllegalArgumentException("Unsupported values strategy: " + strategy);
        }

        for (int i = 0; i < valueCount; i++) {
            if (strategy == ValuesStrategy.SELECT_UNION) {
                valuesSb.append(" union all select ");
            } else {
                valuesSb.append('(');
            }

            for (int j = 0; j < attributes.length; j++) {
                valuesSb.append(attributeParameter[j]);
                valuesSb.append(',');
            }

            if (strategy == ValuesStrategy.SELECT_UNION) {
                valuesSb.setCharAt(valuesSb.length() - 1, ' ');
                if (dummyTable != null) {
                    valuesSb.append("from ");
                    valuesSb.append(dummyTable);
                    valuesSb.append(' ');
                }
            } else {
                valuesSb.setCharAt(valuesSb.length() - 1, ')');
                valuesSb.append(',');
            }
        }

        if (strategy == ValuesStrategy.SELECT_UNION) {
            valuesSb.setCharAt(valuesSb.length() - 1, ' ');
        } else {
            valuesSb.setCharAt(valuesSb.length() - 1, ')');
        }

        String exampleQueryString = sb.toString();
        return mainQuery.em.createQuery(exampleQueryString);
    }

    private static String getCastedParameters(StringBuilder sb, DbmsDialect dbmsDialect, String[] types) {
        sb.setLength(0);
        if (dbmsDialect.needsCastParameters()) {
            for (int i = 0; i < types.length; i++) {
                sb.append(dbmsDialect.cast("?", types[i]));
                sb.append(',');
            }
        } else {
            for (int i = 0; i < types.length; i++) {
                sb.append("?,");
            }
        }

        return sb.substring(0, sb.length() - 1);
    }

    protected boolean renderCteNodes(boolean isSubquery) {
        return isMainQuery && !isSubquery;
    }

    protected List<CTENode> getCteNodes(boolean isSubquery) {
        List<CTENode> cteNodes = new ArrayList<CTENode>();
        // NOTE: Delete statements could cause CTEs to be generated for the cascading deletes
        if (!isMainQuery || isSubquery || !mainQuery.dbmsDialect.supportsWithClause() || !mainQuery.cteManager.hasCtes() && statementType != DbmsStatementType.DELETE || statementType != DbmsStatementType.SELECT && !mainQuery.dbmsDialect.supportsWithClauseInModificationQuery()) {
            return cteNodes;
        }

        StringBuilder sb = new StringBuilder();

        for (CTEInfo cteInfo : mainQuery.cteManager.getCtes()) {
            if (!cteInfo.inline) {
                // Build queries and add as participating queries
                Map<DbmsModificationState, String> modificationStates = cteInfo.nonRecursiveCriteriaBuilder.getModificationStates(explicitVersionEntities);
                Query nonRecursiveQuery = cteInfo.nonRecursiveCriteriaBuilder.getQuery(modificationStates);
                QuerySpecification<?> nonRecursiveQuerySpecification = getQuerySpecification(nonRecursiveQuery);
                Map<String, String> nonRecursiveTableNameRemappings = null;

                if (nonRecursiveQuery instanceof CustomSQLQuery) {
                    // EntityAlias -> CteName
                    nonRecursiveTableNameRemappings = cteInfo.nonRecursiveCriteriaBuilder.getModificationStateRelatedTableNameRemappings(explicitVersionEntities);
                }

                Query recursiveQuery;
                QuerySpecification<?> recursiveQuerySpecification = null;
                Map<String, String> recursiveTableNameRemappings = null;
                if (cteInfo.recursive) {
                    modificationStates = cteInfo.nonRecursiveCriteriaBuilder.getModificationStates(explicitVersionEntities);
                    recursiveQuery = cteInfo.recursiveCriteriaBuilder.getQuery(modificationStates);

                    if (!mainQuery.dbmsDialect.supportsJoinsInRecursiveCte() && cteInfo.recursiveCriteriaBuilder.joinManager.hasNonEmulatableJoins()) {
                        throw new IllegalStateException("The dbms dialect does not support joins in the recursive part of a CTE!");
                    }

                    recursiveQuerySpecification = getQuerySpecification(recursiveQuery);
                    if (recursiveQuery instanceof CustomSQLQuery) {
                        // EntityAlias -> CteName
                        recursiveTableNameRemappings = cteInfo.recursiveCriteriaBuilder.getModificationStateRelatedTableNameRemappings(explicitVersionEntities);
                    }
                }

                String cteName = cteInfo.cteType.getName();
                final List<String> columnNames = cteInfo.columnNames;
                String head;
                String[] aliases;

                if (mainQuery.dbmsDialect.supportsWithClauseHead()) {
                    sb.setLength(0);
                    sb.append(cteName);
                    sb.append('(');

                    for (int i = 0; i < columnNames.size(); i++) {
                        String column = columnNames.get(i);
                        if (i != 0) {
                            sb.append(", ");
                        }

                        sb.append(column);
                    }

                    sb.append(')');
                    head = sb.toString();
                    aliases = null;
                } else {
                    sb.setLength(0);
                    sb.append(cteName);
                    List<String> list = new ArrayList<>(columnNames.size());

                    for (int i = 0; i < columnNames.size(); i++) {
                        String[] columns = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, cteInfo.cteType.getJavaType()).getAttribute(columnNames.get(i)).getColumnNames();
                        for (String column : columns) {
                            list.add(column);
                        }
                    }

                    head = sb.toString();
                    aliases = list.toArray(new String[list.size()]);
                }

                String nonRecursiveWithClauseSuffix = null;
                if (!cteInfo.recursive && !mainQuery.dbmsDialect.supportsNonRecursiveWithClause()) {
                    sb.setLength(0);
                    sb.append(" UNION ALL ");
                    sb.append("SELECT ");

                    sb.append("NULL");

                    for (int i = 1; i < columnNames.size(); i++) {
                        sb.append(", ");
                        sb.append("NULL");
                    }

                    sb.append(" FROM DUAL WHERE 1=0");
                    nonRecursiveWithClauseSuffix = sb.toString();
                }

                cteNodes.add(new CTENode(
                        cteInfo.name,
                        cteInfo.cteType.getName(),
                        head,
                        aliases,
                        cteInfo.unionAll,
                        nonRecursiveQuerySpecification,
                        recursiveQuerySpecification,
                        nonRecursiveTableNameRemappings,
                        recursiveTableNameRemappings,
                        nonRecursiveWithClauseSuffix
                ));
            }
        }

        return cteNodes;
    }

    protected Query getLateralQuery(StringBuilder lateralSb, JoinNode lateralJoinNode) {
        return getTypedQuery(lateralSb, lateralJoinNode);
    }

    protected Query getQuery(boolean embeddedToMainQuery) {
        return getTypedQuery(null, null);
    }

    public Query getQuery() {
        return getQuery(false);
    }
    
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        return getQuery(false);
    }
    
    @SuppressWarnings("unchecked")
    protected TypedQuery<QueryResultType> createTypedQuery(String queryString) {
        TypedQuery<QueryResultType> query = (TypedQuery<QueryResultType>) em.createQuery(queryString, selectManager.getExpectedQueryResultType());
        if (firstResult != 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            query.setMaxResults(maxResults);
        }
        if (isCacheable()) {
            mainQuery.jpaProvider.setCacheable(query);
        }

        return applyObjectBuilder(query);
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<BuilderType> beforeKeyset() {
        prepareForModification(ClauseType.WHERE);
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.PREVIOUS));
    }

    public BuilderType beforeKeyset(Serializable... values) {
        return beforeKeyset(new DefaultKeyset(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType beforeKeyset(Keyset keyset) {
        prepareForModification(ClauseType.WHERE);
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.PREVIOUS));
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<BuilderType> afterKeyset() {
        prepareForModification(ClauseType.WHERE);
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.NEXT));
    }

    public BuilderType afterKeyset(Serializable... values) {
        return afterKeyset(new DefaultKeyset(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType afterKeyset(Keyset keyset) {
        prepareForModification(ClauseType.WHERE);
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.NEXT));
        return (BuilderType) this;
    }

    protected String getBaseQueryString(StringBuilder lateralSb, JoinNode lateralJoinNode) {
        if (lateralSb != null) {
            return buildLateralBaseQueryString(lateralSb, lateralJoinNode);
        }
        if (cachedQueryString == null) {
            cachedQueryString = buildBaseQueryString(false, false);
        }

        return cachedQueryString;
    }

    protected String getExternalQueryString() {
        if (cachedExternalQueryString == null) {
            cachedExternalQueryString = buildExternalQueryString();
        }

        return cachedExternalQueryString;
    }

    protected Set<JoinNode> getKeyRestrictedLeftJoins() {
        if (needsCheck) {
            throw new IllegalStateException("Can't access key restricted left joins when query builder wasn't checked yet!");
        }
        if (keyRestrictedLeftJoins == null) {
            keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        }
        return keyRestrictedLeftJoins;
    }

    protected void prepareForModification(ClauseType changedClause) {
        if (setOperationEnded) {
            throw new IllegalStateException("Modifications to a query after connecting with a set operation is not allowed!");
        }
        needsCheck = true;
        cachedQueryString = null;
        cachedExternalQueryString = null;
        cachedGroupByIdentifierExpressions = null;
        keyRestrictedLeftJoins = null;
        implicitJoinsApplied = false;
        if (changedClause == null || changedClause == ClauseType.WHERE) {
            functionalDependencyAnalyzerVisitor.reset();
        }
    }

    protected void prepareAndCheckCtes() {
        final List<JoinNode> lateInlineNodes = joinManager.getLateInlineNodes();
        lateInlineNodes.clear();
        joinManager.acceptVisitor(new JoinNodeVisitor() {
            @Override
            public void visit(JoinNode node) {
                Class<?> cteType = node.getJavaType();
                if (cteType != null) {
                    CTEInfo cte = mainQuery.cteManager.getCte(cteType, node.getAlias(), joinManager);
                    if (cte == null) {
                        cte = mainQuery.cteManager.getCte(cteType);
                    }
                    if (cte != null) {
                        if (cte.inline) {
                            node.setInlineCte(cte);
                            lateInlineNodes.add(node);
                        } else {
                            node.setInlineCte(null);
                        }
                    }
                    // Except for VALUES clause from nodes, every cte type must be defined
                    if (node.getValueCount() == 0 && mainQuery.metamodel.getCte(cteType) != null) {
                        if (cte == null) {
                            throw new IllegalStateException("Usage of CTE '" + cteType.getName() + "' without definition!");
                        }
                    }
                }
            }
        });
    }

    protected void prepareSelect() {
        selectManager.wrapPlainParameters();
    }

    protected void prepareAndCheck() {
        if (checkSetBuilderEnded) {
            verifySetBuilderEnded();
        }
        if (!needsCheck) {
            return;
        }

        verifyBuilderEnded();
        prepareAndCheckCtes();
        prepareSelect();
        // resolve unresolved aliases, object model etc.
        // we must do implicit joining at the end because we can only do
        // the aliases resolving at the end and alias resolving must happen before
        // the implicit joins
        // it makes no sense to do implicit joining before this point, since
        // the user can call the api in arbitrary orders
        // so where("b.c").join("a.b") but also
        // join("a.b", "b").where("b.c")
        // in the first case
        JoinVisitor joinVisitor = applyImplicitJoins(null);
        applyExpressionTransformersAndBuildGroupByClauses(false, joinVisitor);
        analyzeConstantifiedJoinNodes();
        hasCollections = joinManager.hasCollections();

        if (keysetManager.hasKeyset()) {
            // The last order by expression must be unique, otherwise keyset scrolling wouldn't work
            List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(hasCollections, whereManager.rootPredicate.getPredicate(), hasGroupBy ? Arrays.asList(getGroupByIdentifierExpressions()) : Collections.<ResolvedExpression>emptyList(), joinVisitor);
            if (!orderByExpressions.get(orderByExpressions.size() - 1).isResultUnique()) {
                throw new IllegalStateException("The order by items of the query builder are not guaranteed to produce unique tuples! Consider also ordering by the entity identifier!");
            }
            keysetManager.initialize(orderByExpressions);
        }

        // No need to do all that stuff again if no mutation occurs
        needsCheck = false;
    }

    protected void analyzeConstantifiedJoinNodes() {
        final ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector = functionalDependencyAnalyzerVisitor.getConstantifiedJoinNodeAttributeCollector();
        final JoinNode firstRootNode = joinManager.getRoots().get(0);
        constantifiedJoinNodeAttributeCollector.collectConstantifiedJoinNodeAttributes(whereManager.rootPredicate.getPredicate(), firstRootNode, true);
        joinManager.acceptVisitor(new JoinNodeVisitor() {
            @Override
            public void visit(JoinNode node) {
                if (node.getOnPredicate() != null) {
                    constantifiedJoinNodeAttributeCollector.collectConstantifiedJoinNodeAttributes(node.getOnPredicate(), firstRootNode, node.getJoinType() == JoinType.INNER);
                }
            }
        });
    }

    protected ResolvedExpression[] getGroupByIdentifierExpressions() {
        if (cachedGroupByIdentifierExpressions == null) {
            Set<ResolvedExpression> resolvedExpressions = groupByManager.getCollectedGroupByClauses().keySet();
            cachedGroupByIdentifierExpressions = resolvedExpressions.toArray(new ResolvedExpression[resolvedExpressions.size()]);
        }
        return cachedGroupByIdentifierExpressions;
    }

    protected void buildLateralExampleQueryString(StringBuilder sb) {
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        queryGenerator.setExternalRepresentation(false);
        try {
            sb.append("SELECT 1");
            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            joinManager.buildClause(sb, EnumSet.noneOf(ClauseType.class), null, false, false, false, true, false, optionalWhereClauseConjuncts, whereClauseConjuncts, null, explicitVersionEntities, nodesToFetch, Collections.EMPTY_SET);
        } finally {
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
        }
    }

    protected String buildLateralBaseQueryString(StringBuilder sbSelectFrom, JoinNode lateralJoinNode) {
        sbSelectFrom.append(" WHERE ");
        if (hasLimit()) {
            sbSelectFrom.append(mainQuery.jpaProvider.getCustomFunctionInvocation("LIMIT", 1));
            sbSelectFrom.append('(');
        } else {
            sbSelectFrom.append("EXISTS(");
        }
        buildBaseQueryString(sbSelectFrom, false, false, lateralJoinNode);
        if (hasLimit()) {
            final boolean hasFirstResult = firstResult != 0;
            final boolean hasMaxResults = maxResults != Integer.MAX_VALUE;
            sbSelectFrom.append(')');
            if (hasMaxResults) {
                sbSelectFrom.append(',').append(maxResults);
            }
            if (hasFirstResult) {
                sbSelectFrom.append(',').append(firstResult);
            }
            sbSelectFrom.append(')');
            sbSelectFrom.append(" is not null");
        } else {
            sbSelectFrom.append(')');
        }
        return sbSelectFrom.toString();
    }

    public Expression asExpression(boolean externalRepresentation, boolean embeddedToMainQuery) {
        return asExpression(this, externalRepresentation, embeddedToMainQuery);
    }

    protected Expression asExpression(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, boolean externalRepresentation, boolean embeddedToMainQuery) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            return queryBuilder.asExpression(externalRepresentation, embeddedToMainQuery);
        }

        final String queryString = queryBuilder.buildBaseQueryString(externalRepresentation, embeddedToMainQuery);
        Expression expression = new SubqueryExpression(new Subquery() {
            @Override
            public String getQueryString() {
                return queryString;
            }
        });
        if (externalRepresentation) {
            return expression;
        }

        if (queryBuilder.joinManager.hasEntityFunctions()) {
            for (EntityFunctionNode node : queryBuilder.getEntityFunctionNodes(null, isMainQuery)) {
                List<Expression> arguments = new ArrayList<>(6);
                arguments.add(new StringLiteral("ENTITY_FUNCTION"));
                arguments.add(expression);

                String subquery = node.getSubquery();
                String aliases = node.getAliases();
                String syntheticPredicate = node.getSyntheticPredicate();

                // TODO: this is a hibernate specific integration detail
                // Replace the subview subselect that is generated for this subselect
                String entityName = node.getEntityName();
                arguments.add(new StringLiteral(entityName));
                arguments.add(new StringLiteral(subquery));
                arguments.add(new StringLiteral(aliases == null ? "" : aliases));
                arguments.add(new StringLiteral(syntheticPredicate == null ? "" : syntheticPredicate));

                expression = new FunctionExpression("FUNCTION", arguments);
            }
        }

        if (queryBuilder.hasLimit()) {
            final boolean hasFirstResult = queryBuilder.getFirstResult() != 0;
            final boolean hasMaxResults = queryBuilder.getMaxResults() != Integer.MAX_VALUE;
            List<Expression> arguments = new ArrayList<>(2);
            arguments.add(new StringLiteral("LIMIT"));
            arguments.add(expression);

            if (!hasMaxResults) {
                throw new IllegalArgumentException("First result without max results is not supported!");
            } else {
                arguments.add(new NumericLiteral(Integer.toString(queryBuilder.getMaxResults()), NumericType.INTEGER));
            }

            if (hasFirstResult) {
                arguments.add(new NumericLiteral(Integer.toString(queryBuilder.getFirstResult()), NumericType.INTEGER));
            }

            expression = new FunctionExpression("FUNCTION", arguments);
        }

        return expression;
    }

    protected String buildBaseQueryString(boolean externalRepresentation, boolean embeddedToMainQuery) {
        StringBuilder sbSelectFrom = new StringBuilder();
        buildBaseQueryString(sbSelectFrom, externalRepresentation, embeddedToMainQuery, null);
        return sbSelectFrom.toString();
    }

    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, boolean embeddedToMainQuery, JoinNode lateralJoinNode) {
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        queryGenerator.setExternalRepresentation(externalRepresentation);
        try {
            appendSelectClause(sbSelectFrom, externalRepresentation);
            List<String> whereClauseEndConjuncts = this instanceof Subquery ? new ArrayList<String>() : null;

            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false, externalRepresentation, false, false, embeddedToMainQuery, optionalWhereClauseConjuncts, whereClauseConjuncts, whereClauseEndConjuncts, explicitVersionEntities, nodesToFetch, Collections.EMPTY_SET);

            appendWhereClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts, whereClauseEndConjuncts, lateralJoinNode);
            appendGroupByClause(sbSelectFrom);
            appendWindowClause(sbSelectFrom, externalRepresentation);
            appendOrderByClause(sbSelectFrom);
            if (externalRepresentation && !isMainQuery) {
                applyJpaLimit(sbSelectFrom);
            }
        } finally {
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
        }
    }

    protected String buildExternalQueryString() {
        StringBuilder sbSelectFrom = new StringBuilder();
        buildExternalQueryString(sbSelectFrom);
        return sbSelectFrom.toString();
    }

    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        if (isMainQuery) {
            mainQuery.cteManager.buildClause(sbSelectFrom);
        }
        buildBaseQueryString(sbSelectFrom, true, false, null);
    }

    protected void appendSelectClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        selectManager.buildSelect(sbSelectFrom, false, externalRepresentation);
    }

    protected void appendWhereClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        queryGenerator.setExternalRepresentation(externalRepresentation);
        try {
            appendWhereClause(sbSelectFrom, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), null);
        } finally {
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
        }
    }

    protected void appendWhereClause(StringBuilder sbSelectFrom, List<String> whereClauseConjuncts, List<String> optionalWhereClauseConjuncts, List<String> whereClauseEndConjuncts, JoinNode lateralJoinNode) {
        KeysetLink keysetLink = keysetManager.getKeysetLink();
        if (keysetLink == null || keysetLink.getKeysetMode() == KeysetMode.NONE || keysetLink.getKeyset().getTuple() == null) {
            int initialLength = sbSelectFrom.length();
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts, whereClauseEndConjuncts);
            if (sbSelectFrom.length() == initialLength && lateralJoinNode != null) {
                sbSelectFrom.append(" WHERE ");
                boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
                queryGenerator.setExternalRepresentation(false);
                try {
                    lateralJoinNode.getOnPredicate().accept(queryGenerator);
                } finally {
                    queryGenerator.setExternalRepresentation(originalExternalRepresentation);
                }
            }
        } else {
            sbSelectFrom.append(" WHERE ");

            if (whereManager.hasPredicates()) {
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts, whereClauseEndConjuncts);
                sbSelectFrom.append(" AND ");
            }

            int positionalOffset = parameterManager.getPositionalOffset();
            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom, positionalOffset);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom, positionalOffset);
            }

            if (lateralJoinNode != null && lateralJoinNode.getOnPredicate() != null) {
                sbSelectFrom.append(" AND ");
                boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
                queryGenerator.setExternalRepresentation(false);
                try {
                    lateralJoinNode.getOnPredicate().accept(queryGenerator);
                } finally {
                    queryGenerator.setExternalRepresentation(originalExternalRepresentation);
                }
            }
        }
    }

    protected void appendGroupByClause(StringBuilder sbSelectFrom) {
        if (hasGroupBy) {
            groupByManager.buildGroupBy(sbSelectFrom);
            havingManager.buildClause(sbSelectFrom);
        }
    }

    protected void appendWindowClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        if (externalRepresentation) {
            windowManager.buildWindow(sbSelectFrom);
        }
    }

    protected void appendOrderByClause(StringBuilder sbSelectFrom) {
        orderByManager.buildOrderBy(sbSelectFrom, false, false, false, false);
    }
    
    protected Map<DbmsModificationState, String> getModificationStates(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        return null;
    }
    
    protected Map<String, String> getModificationStateRelatedTableNameRemappings(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        return null;
    }

    protected static boolean isEmpty(Set<JoinNode> joinNodes, Set<ClauseType> clauseExclusions) {
        for (JoinNode node : joinNodes) {
            if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && !node.isCardinalityMandatory()) {
                continue;
            }
            return false;
        }

        return true;
    }

    private QuerySpecification<?> getQuerySpecification(Query query) {
        if (query instanceof AbstractCustomQuery<?>) {
            return ((AbstractCustomQuery<?>) query).getQuerySpecification();
        }
        return new DefaultQuerySpecification(statementType, query, em, parameterManager.getParameterListNames(query), cbf.getExtendedQuerySupport());
    }

    protected boolean hasLimit() {
        return firstResult != 0 || maxResults != Integer.MAX_VALUE;
    }
    
    protected void applyJpaLimit(StringBuilder sbSelectFrom) {
        if (hasLimit()) {
            sbSelectFrom.append(" LIMIT ");
            sbSelectFrom.append(maxResults);
            
            if (firstResult > 0) {
                sbSelectFrom.append(" OFFSET ");
                sbSelectFrom.append(firstResult);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected final TypedQuery<QueryResultType> applyObjectBuilder(TypedQuery<?> query) {
        ObjectBuilder<QueryResultType> selectObjectBuilder = selectManager.getSelectObjectBuilder();
        if (selectObjectBuilder != null) {
            return  new ObjectBuilderTypedQuery<>(query, selectObjectBuilder);
        } else {
            return (TypedQuery<QueryResultType>) query;
        }
    }
    // TODO: needs equals-hashCode implementation
}
