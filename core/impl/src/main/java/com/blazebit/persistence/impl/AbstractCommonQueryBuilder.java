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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
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
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.keyset.KeysetBuilderImpl;
import com.blazebit.persistence.impl.keyset.KeysetImpl;
import com.blazebit.persistence.impl.keyset.KeysetLink;
import com.blazebit.persistence.impl.keyset.KeysetManager;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.query.AbstractCustomQuery;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.DefaultQuerySpecification;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.ObjectBuilderTypedQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.transform.ExpressionTransformerGroup;
import com.blazebit.persistence.impl.transform.OuterFunctionVisitor;
import com.blazebit.persistence.impl.transform.SimpleTransformerGroup;
import com.blazebit.persistence.impl.transform.SizeTransformationVisitor;
import com.blazebit.persistence.impl.transform.SizeTransformerGroup;
import com.blazebit.persistence.impl.transform.SubqueryRecursiveExpressionVisitor;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @param <QueryResultType> The query result type
 * @param <BuilderType> The concrete builder type
 * @param <SetReturn> The builder type that should be returned on set operations
 * @param <SubquerySetReturn> The builder type that should be returned on subquery set operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCommonQueryBuilder<QueryResultType, BuilderType, SetReturn, SubquerySetReturn, FinalSetReturn extends BaseFinalSetOperationBuilderImpl<?, ?, ?>> implements ServiceProvider, ConfigurationSource {

    public static final String ID_PARAM_NAME = "ids";

    protected static final Logger LOG = Logger.getLogger(AbstractCommonQueryBuilder.class.getName());

    protected final MainQuery mainQuery;
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
    protected final KeysetManager keysetManager;
    protected final ResolvingQueryGenerator queryGenerator;
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    protected final GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor;
    
    // This builder will be passed in when using set operations
    protected FinalSetReturn finalSetOperationBuilder;
    protected boolean setOperationEnded;

    protected final DbmsDialect dbmsDialect;
    protected final JpaProvider jpaProvider;
    protected final Map<String, JpqlFunction> registeredFunctions;

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
    protected boolean hasGroupBy = false;
    protected boolean needsCheck = true;
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
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.finalSetOperationBuilder = (FinalSetReturn) builder.finalSetOperationBuilder;
        this.dbmsDialect = builder.dbmsDialect;
        this.jpaProvider = builder.jpaProvider;
        this.registeredFunctions = builder.registeredFunctions;
        this.subqueryInitFactory = builder.subqueryInitFactory;
        this.groupByExpressionGatheringVisitor = builder.groupByExpressionGatheringVisitor;
        this.aliasManager = builder.aliasManager;
        this.expressionFactory = builder.expressionFactory;
        this.transformerGroups = builder.transformerGroups;
        this.resultType = builder.resultType;
    }
    
    protected AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, FinalSetReturn finalSetOperationBuilder, boolean implicitFromClause) {
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
        this.isMainQuery = isMainQuery;
        this.statementType = statementType;
        this.cbf = mainQuery.cbf;
        this.parameterManager = mainQuery.parameterManager;
        this.em = mainQuery.em;
        this.dbmsDialect = mainQuery.dbmsDialect;
        this.jpaProvider = mainQuery.jpaProvider;
        this.registeredFunctions = mainQuery.registeredFunctions;

        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;
        this.queryGenerator = new ResolvingQueryGenerator(this.aliasManager, parameterManager, mainQuery.parameterTransformerFactory, mainQuery.metamodel, jpaProvider, registeredFunctions);
        this.joinManager = new JoinManager(mainQuery, queryGenerator, this.aliasManager, parentJoinManager, expressionFactory);

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
        this.groupByExpressionGatheringVisitor = new GroupByExpressionGatheringVisitor(false, dbmsDialect);

        this.whereManager = new WhereManager<BuilderType>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.havingManager = new HavingManager<BuilderType>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory, groupByExpressionGatheringVisitor);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager, subqueryInitFactory);

        this.selectManager = new SelectManager<QueryResultType>(queryGenerator, parameterManager, this.joinManager, this.aliasManager, subqueryInitFactory, expressionFactory, jpaProvider, mainQuery, groupByExpressionGatheringVisitor, resultClazz);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, subqueryInitFactory, this.aliasManager, jpaProvider, groupByExpressionGatheringVisitor);
        this.keysetManager = new KeysetManager(queryGenerator, parameterManager, jpaProvider, dbmsDialect);

        final SizeTransformationVisitor sizeTransformationVisitor = new SizeTransformationVisitor(mainQuery, subqueryInitFactory, joinManager, jpaProvider);
        this.transformerGroups = Arrays.<ExpressionTransformerGroup<?>>asList(
                new SimpleTransformerGroup(new OuterFunctionVisitor(joinManager)),
                new SimpleTransformerGroup(new SubqueryRecursiveExpressionVisitor()),
                new SizeTransformerGroup(sizeTransformationVisitor, orderByManager, selectManager, joinManager, groupByManager));
        this.resultType = resultClazz;
        
        this.finalSetOperationBuilder = finalSetOperationBuilder;
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, FinalSetReturn finalSetOperationBuilder, boolean implicitFromClause) {
        this(mainQuery, isMainQuery, statementType, resultClazz, alias, null, null, mainQuery.expressionFactory, finalSetOperationBuilder, implicitFromClause);
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        this(mainQuery, isMainQuery, statementType, resultClazz, alias, null, null, mainQuery.expressionFactory, finalSetOperationBuilder, true);
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias) {
        this(mainQuery, isMainQuery, statementType, resultClazz, alias, null);
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
            return (T) dbmsDialect;
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
        prepareForModification();
        this.mainQuery.registerMacro(macroName, jpqlMacro);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setProperty(String propertyName, String propertyValue) {
        prepareForModification();
        this.mainQuery.getMutableQueryConfiguration().setProperty(propertyName, propertyValue);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setProperties(Map<String, String> properties) {
        prepareForModification();
        this.mainQuery.getMutableQueryConfiguration().setProperties(properties);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setCacheable(boolean cacheable) {
        prepareForModification();
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
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }

        prepareForModification();
        return mainQuery.cteManager.withStartSet(cteClass, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }

        prepareForModification();
        return mainQuery.cteManager.with(cteClass, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SelectRecursiveCTECriteriaBuilder<BuilderType> withRecursive(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }

        prepareForModification();
        return mainQuery.cteManager.withRecursive(cteClass, (BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public ReturningModificationCriteriaBuilderFactory<BuilderType> withReturning(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        if (!dbmsDialect.supportsModificationQueryInWithClause()) {
            throw new UnsupportedOperationException("The database does not support modification queries in the with clause!");
        }

        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
        joinManager.addRoot(correlationPath, alias);
        return (BuilderType) this;
    }

    public BuilderType from(Class<?> clazz) {
        return from(clazz, null);
    }

    public BuilderType from(Class<?> clazz, String alias) {
        return from(clazz, alias, null);
    }

    public BuilderType fromCte(Class<?> clazz, String cteName) {
        return fromCte(clazz, null);
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

    public <T> BuilderType fromIdentifiableValues(Class<T> valueClass, String alias, Collection<T> values) {
        BuilderType result = fromIdentifiableValues(valueClass, alias, values.size());
        setParameter(alias, values);
        return result;
    }

    public BuilderType fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount) {
        prepareForModification();
        if (!fromClassExplicitlySet) {
            // When from is explicitly called we have to revert the implicit root
            if (joinManager.getRoots().size() > 0) {
                joinManager.removeRoot();
            }
        }

        Class<?> valuesClazz = valueClass;
        ManagedType<?> type = mainQuery.metamodel.getManagedType(valueClass);
        String treatFunction = null;
        String castedParameter = null;
        if (!(type instanceof IdentifiableType<?>)) {
            throw new IllegalArgumentException("Only identifiable types allowed!");
        }

        joinManager.addRootValues(valuesClazz, valueClass, alias, valueCount, treatFunction, castedParameter, true);
        fromClassExplicitlySet = true;

        return (BuilderType) this;
    }

    public BuilderType fromValues(Class<?> valueClass, String alias, int valueCount) {
        prepareForModification();
        if (!fromClassExplicitlySet) {
            // When from is explicitly called we have to revert the implicit root
            if (joinManager.getRoots().size() > 0) {
                joinManager.removeRoot();
            }
        }

        Class<?> valuesClazz = valueClass;
        ManagedType<?> type = mainQuery.metamodel.getManagedType(valueClass);
        String typeName = null;
        String castedParameter = null;
        if (type == null) {
            typeName = cbf.getNamedTypes().get(valueClass);
            if (typeName == null) {
                throw new IllegalArgumentException("Unsupported non-managed type for VALUES clause: " + valueClass.getName());
            }

            String sqlType = mainQuery.dbmsDialect.getSqlType(valueClass);
            castedParameter = mainQuery.dbmsDialect.cast("?", sqlType);
            valuesClazz = ValuesEntity.class;
        }
        joinManager.addRootValues(valuesClazz, valueClass, alias, valueCount, typeName, castedParameter, false);
        fromClassExplicitlySet = true;

        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    private BuilderType from(Class<?> clazz, String alias, DbmsModificationState state) {
        prepareForModification();
        if (!fromClassExplicitlySet) {
            // When from is explicitly called we have to revert the implicit root
            if (joinManager.getRoots().size() > 0) {
                joinManager.removeRoot();
            }
        }
        
        EntityType<?> type = mainQuery.metamodel.entity(clazz);
        String finalAlias = joinManager.addRoot(type, alias);
        fromClassExplicitlySet = true;
        
        // Handle old and new references
        if (state != null) {
            Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(clazz);
            if (versionEntities == null) {
                versionEntities = new HashMap<String, DbmsModificationState>(1);
                explicitVersionEntities.put(clazz, versionEntities);
            }
            
            versionEntities.put(finalAlias, state);
        }
        
        return (BuilderType) this;
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
        PathExpression pathExpression = expressionFactory.createPathExpression(path);
        joinManager.implicitJoin(pathExpression, true, null, null, false, false, true, false);
        return (JoinNode) pathExpression.getBaseNode();
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

    public Metamodel getMetamodel() {
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
        return selectManager.selectSimpleCase((BuilderType) this, selectAlias, expressionFactory.createCaseOperandExpression(caseOperandExpression));
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
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
        prepareForModification();
        return selectManager.selectSubquery((BuilderType) this, subqueryAlias, expressionFactory.createSimpleExpression(expression, false), selectAlias, criteriaBuilder);
    }

    /*
     * Where methods
     */
    public RestrictionBuilder<BuilderType> where(String expression) {
        prepareForModification();
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return whereManager.restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereCase() {
        prepareForModification();
        return whereManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereSimpleCase(String expression) {
        prepareForModification();
        return whereManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public WhereOrBuilder<BuilderType> whereOr() {
        prepareForModification();
        return whereManager.whereOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereExists() {
        prepareForModification();
        return whereManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereNotExists() {
        prepareForModification();
        return whereManager.restrictNotExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        return whereManager.restrictExists((BuilderType) this, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        return whereManager.restrictNotExists((BuilderType) this, criteriaBuilder);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery() {
        prepareForModification();
        return whereManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery(String subqueryAlias, String expression) {
        prepareForModification();
        return whereManager.restrict(this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubqueries(String expression) {
        prepareForModification();
        return whereManager.restrictSubqueries(this, expression);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        return whereManager.restrict(this, criteriaBuilder);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        return whereManager.restrict(this, subqueryAlias, expression, criteriaBuilder);
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setWhereExpression(String expression) {
        prepareForModification();
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        whereManager.restrictExpression(this, predicate);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> setWhereExpressionSubqueries(String expression) {
        prepareForModification();
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return whereManager.restrictExpressionSubqueries((BuilderType) this, predicate);
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
        prepareForModification();
        Expression expr;
        if (mainQuery.getQueryConfiguration().isCompatibleModeEnabled()) {
            expr = expressionFactory.createPathExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression, false);
            Set<Expression> collectedExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(expr);
            if (collectedExpressions.size() > 1 || collectedExpressions.iterator().next() != expr) {
                throw new RuntimeException("The complex group by expression [" + expression + "] is not supported by the underlying database. The valid sub-expressions are: " + collectedExpressions);
            }
        }
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (BuilderType) this;
    }

    /*
     * Having methods
     */
    public RestrictionBuilder<BuilderType> having(String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return havingManager.restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingCase() {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingSimpleCase(String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public HavingOrBuilder<BuilderType> havingOr() {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingExists() {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingNotExists() {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> havingExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((BuilderType) this, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<BuilderType> havingNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((BuilderType) this, criteriaBuilder);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery() {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery(String subqueryAlias, String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict(this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubqueries(String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictSubqueries(this, expression);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> havingSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict(this, criteriaBuilder);
    }

    public SubqueryBuilder<RestrictionBuilder<BuilderType>> havingSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrict(this, subqueryAlias, expression, criteriaBuilder);
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setHavingExpression(String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        havingManager.restrictExpression(this, predicate);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> setHavingExpressionSubqueries(String expression) {
        prepareForModification();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return havingManager.restrictExpressionSubqueries((BuilderType) this, predicate);
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

    @SuppressWarnings("unchecked")
    public BuilderType orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr;
        if (mainQuery.getQueryConfiguration().isCompatibleModeEnabled()) {
            expr = expressionFactory.createOrderByExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression, false);
        }
        orderBy(expr, ascending, nullFirst);
        return (BuilderType) this;
    }

    private void orderBy(Expression expression, boolean ascending, boolean nullFirst) {
        prepareForModification();
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
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
        prepareForModification();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, false);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType joinDefault(String path, String alias, JoinType type) {
        prepareForModification();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, true);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String path, String alias, JoinType type) {
        prepareForModification();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinDefaultOn(String path, String alias, JoinType type) {
        prepareForModification();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, true);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(Class<?> clazz, String alias, JoinType type) {
        return joinOn(joinManager.getRootNodeOrFail("An explicit base join node is required when multiple root nodes are used!").getAlias(), clazz, alias, type);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String base, Class<?> entityClass, String alias, JoinType type) {
        prepareForModification();
        checkJoinPreconditions(base, alias, type);
        if (entityClass == null) {
            throw new NullPointerException("entityClass");
        }
        return joinManager.joinOn((BuilderType) this, base, entityClass, alias, type);
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

    public JoinOnBuilder<BuilderType> leftJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(Class<?> clazz, String alias) {
        return joinOn(clazz, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(String base, Class<?> clazz, String alias) {
        return joinOn(base, clazz, alias, JoinType.LEFT);
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

    protected void applyImplicitJoins(JoinVisitor parentVisitor) {
        if (implicitJoinsApplied) {
            return;
        }

        final JoinVisitor joinVisitor = new JoinVisitor(mainQuery.parameterTransformerFactory, parentVisitor, joinManager, parameterManager, !jpaProvider.supportsSingleValuedAssociationIdExpressions());
        final List<JoinNode> fetchableNodes = new ArrayList<>();
        final JoinNodeVisitor joinNodeVisitor = new OnClauseJoinNodeVisitor(joinVisitor) {

            @Override
            public void visit(JoinNode node) {
                super.visit(node);
                node.registerDependencies();
                if (node.isFetch()) {
                    fetchableNodes.add(node);
                }
            }

        };
        joinVisitor.setFromClause(ClauseType.JOIN);
        joinManager.acceptVisitor(joinNodeVisitor);
        // carry out implicit joins
        joinVisitor.setFromClause(ClauseType.SELECT);
        // There might be clauses for which joins are not required
        joinVisitor.setJoinRequired(isJoinRequiredForSelect());
        selectManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinRequired(true);

        // Only the main query has fetch owners
        if (isMainQuery) {
            StringBuilder sb = null;
            Set<JoinNode> fetchOwners = selectManager.collectFetchOwners();
            nodesToFetch = new HashSet<>();
            // Add all parents of the fetchable nodes to nodesToFetch until the fetch owner is reached
            // If we reach a root before a fetch owner, the fetch owner is missing
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
    }

    protected void applyVisitor(VisitorAdapter expressionVisitor) {
        selectManager.acceptVisitor(expressionVisitor);
        joinManager.acceptVisitor(new OnClauseJoinNodeVisitor(expressionVisitor));
        whereManager.acceptVisitor(expressionVisitor);
        groupByManager.acceptVisitor(expressionVisitor);
        havingManager.acceptVisitor(expressionVisitor);
        orderByManager.acceptVisitor(expressionVisitor);
    }

    public void applyExpressionTransformers() {
        int size = transformerGroups.size();
        for (int i = 0; i < size; i++) {
            ExpressionTransformerGroup transformerGroup = transformerGroups.get(i);
            transformerGroup.applyExpressionTransformer(joinManager);
            transformerGroup.applyExpressionTransformer(selectManager);
            transformerGroup.applyExpressionTransformer(whereManager);
            transformerGroup.applyExpressionTransformer(groupByManager);
            transformerGroup.applyExpressionTransformer(havingManager);
            transformerGroup.applyExpressionTransformer(orderByManager);

            transformerGroup.afterGlobalTransformation();
        }

        // After all transformations are done, we can finally check if aggregations are used
        hasGroupBy = groupByManager.hasGroupBys();
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(selectManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(joinManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(whereManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(orderByManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(havingManager.acceptVisitor(AggregateDetectionVisitor.INSTANCE));
    }

    public Class<QueryResultType> getResultType() {
        return resultType;
    }

    public String getQueryString() {
        prepareAndCheck();
        return getExternalQueryString();
    }
    
    protected String getBaseQueryStringWithCheck() {
        prepareAndCheck();
        return getBaseQueryString();
    }

    protected final TypedQuery<QueryResultType> getTypedQueryForFinalOperationBuilder() {
        try {
            checkSetBuilderEnded = false;
            return getTypedQuery();
        } finally {
            checkSetBuilderEnded = true;
        }
    }

    protected TypedQuery<QueryResultType> getTypedQuery() {
        // NOTE: This must happen first because it generates implicit joins
        String baseQueryString = getBaseQueryStringWithCheck();
        // We can only use the query directly if we have no ctes, entity functions or hibernate bugs
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        final boolean needsSqlReplacement = isMainQuery && mainQuery.cteManager.hasCtes() || joinManager.hasEntityFunctions() || !keyRestrictedLeftJoins.isEmpty() || !isMainQuery && hasLimit();
        if (!needsSqlReplacement) {
            TypedQuery<QueryResultType> baseQuery = getTypedQuery(baseQueryString);
            parameterManager.parameterizeQuery(baseQuery);
            return baseQuery;
        }

        TypedQuery<QueryResultType> baseQuery = getTypedQuery(baseQueryString);
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        String limit = null;
        String offset = null;

        // The main query will handle that separately
        if (!isMainQuery) {
            if (firstResult != 0) {
                offset = Integer.toString(firstResult);
            }
            if (maxResults != Integer.MAX_VALUE) {
                limit = Integer.toString(maxResults);
            }
        }
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, Collections.EMPTY_SET);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new CustomQuerySpecification(
                this, baseQuery, parameterListNames, limit, offset, keyRestrictedLeftJoinAliases, entityFunctionNodes, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes
        );

        TypedQuery<QueryResultType> query = new CustomSQLTypedQuery<QueryResultType>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

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

    protected List<EntityFunctionNode> getEntityFunctionNodes(Query baseQuery) {
        List<EntityFunctionNode> entityFunctionNodes = new ArrayList<EntityFunctionNode>();
        for (JoinNode node : joinManager.getEntityFunctionNodes()) {
            String valuesClause = node.getValuesClause();
            String valuesAliases = node.getValuesAliases();

            String valuesTableSqlAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, node.getAlias());
            entityFunctionNodes.add(new EntityFunctionNode(valuesClause, valuesAliases, node.getType(), valuesTableSqlAlias, node.getValueQuery()));
        }
        return entityFunctionNodes;
    }

    protected boolean renderCteNodes(boolean isSubquery) {
        return isMainQuery && !isSubquery;
    }

    protected List<CTENode> getCteNodes(Query baseQuery, boolean isSubquery) {
        List<CTENode> cteNodes = new ArrayList<CTENode>();
        // NOTE: Delete statements could cause CTEs to be generated for the cascading deletes
        if (!isMainQuery || isSubquery || !dbmsDialect.supportsWithClause() || !mainQuery.cteManager.hasCtes() && statementType != DbmsStatementType.DELETE || statementType != DbmsStatementType.SELECT && !mainQuery.dbmsDialect.supportsWithClauseInModificationQuery()) {
            return cteNodes;
        }

        StringBuilder sb = new StringBuilder();

        for (CTEInfo cteInfo : mainQuery.cteManager.getCtes()) {
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

                if (!dbmsDialect.supportsJoinsInRecursiveCte() && cteInfo.recursiveCriteriaBuilder.joinManager.hasJoins()) {
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

            if (dbmsDialect.supportsWithClauseHead()) {
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
            if (!cteInfo.recursive && !dbmsDialect.supportsNonRecursiveWithClause()) {
                sb.setLength(0);
                sb.append("\nUNION ALL\n");
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

        return cteNodes;
    }

    protected Query getQuery() {
        return getTypedQuery();
    }
    
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        return getQuery();
    }
    
    @SuppressWarnings("unchecked")
    protected TypedQuery<QueryResultType> getTypedQuery(String queryString) {
        TypedQuery<QueryResultType> query = (TypedQuery<QueryResultType>) em.createQuery(queryString, selectManager.getExpectedQueryResultType());
        if (firstResult != 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            query.setMaxResults(maxResults);
        }
        if (isCacheable()) {
            jpaProvider.setCacheable(query);
        }

        return applyObjectBuilder(query);
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<BuilderType> beforeKeyset() {
        prepareForModification();
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.PREVIOUS));
    }

    public BuilderType beforeKeyset(Serializable... values) {
        return beforeKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType beforeKeyset(Keyset keyset) {
        prepareForModification();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.PREVIOUS));
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<BuilderType> afterKeyset() {
        prepareForModification();
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.NEXT));
    }

    public BuilderType afterKeyset(Serializable... values) {
        return afterKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType afterKeyset(Keyset keyset) {
        prepareForModification();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.NEXT));
        return (BuilderType) this;
    }

    protected String getBaseQueryString() {
        if (cachedQueryString == null) {
            cachedQueryString = buildBaseQueryString(false);
        }

        return cachedQueryString;
    }

    protected String getExternalQueryString() {
        if (cachedExternalQueryString == null) {
            cachedExternalQueryString = buildExternalQueryString();
        }

        return cachedExternalQueryString;
    }

    protected void prepareForModification() {
        if (setOperationEnded) {
            throw new IllegalStateException("Modifications to a query after connecting with a set operation is not allowed!");
        }
        needsCheck = true;
        cachedQueryString = null;
        cachedExternalQueryString = null;
        implicitJoinsApplied = false;
    }

    protected void prepareAndCheck() {
        if (checkSetBuilderEnded) {
            verifySetBuilderEnded();
        }
        if (!needsCheck) {
            return;
        }

        verifyBuilderEnded();
        joinManager.acceptVisitor(new JoinNodeVisitor() {
            @Override
            public void visit(JoinNode node) {
                Class<?> cteType = node.getType();
                // Except for VALUES clause from nodes, every cte type must be defined
                if (node.getValueQuery() == null && mainQuery.metamodel.getCte(cteType) != null) {
                    if (mainQuery.cteManager.getCte(cteType) == null) {
                        throw new IllegalStateException("Usage of CTE '" + cteType.getName() + "' without definition!");
                    }
                }
            }
        });
        // resolve unresolved aliases, object model etc.
        // we must do implicit joining at the end because we can only do
        // the aliases resolving at the end and alias resolving must happen before
        // the implicit joins
        // it makes no sense to do implicit joining before this point, since
        // the user can call the api in arbitrary orders
        // so where("b.c").join("a.b") but also
        // join("a.b", "b").where("b.c")
        // in the first case
        applyImplicitJoins(null);
        applyExpressionTransformers();

        if (keysetManager.hasKeyset()) {
            // The last order by expression must be unique, otherwise keyset scrolling wouldn't work
            List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(cbf.getMetamodel());
            if (!orderByExpressions.get(orderByExpressions.size() - 1).isUnique()) {
                throw new IllegalStateException("The last order by item must be unique!");
            }
            keysetManager.initialize(orderByExpressions);
        }

        // No need to do all that stuff again if no mutation occurs
        needsCheck = false;
    }

    protected String buildBaseQueryString(boolean externalRepresentation) {
        StringBuilder sbSelectFrom = new StringBuilder();
        buildBaseQueryString(sbSelectFrom, externalRepresentation);
        return sbSelectFrom.toString();
    }

    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        appendSelectClause(sbSelectFrom);
        List<String> whereClauseConjuncts = appendFromClause(sbSelectFrom, externalRepresentation);
        appendWhereClause(sbSelectFrom, whereClauseConjuncts);
        appendGroupByClause(sbSelectFrom);
        appendOrderByClause(sbSelectFrom);
        if (externalRepresentation && !isMainQuery) {
            // Don't render the LIMIT clause for subqueries, but let the parent render it in a LIMIT function
            if (!(this instanceof SubqueryInternalBuilder<?>)) {
                applyJpaLimit(sbSelectFrom);
            }
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
        buildBaseQueryString(sbSelectFrom, true);
    }

    protected void appendSelectClause(StringBuilder sbSelectFrom) {
        selectManager.buildSelect(sbSelectFrom, false);
    }

    protected List<String> appendFromClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        List<String> whereClauseConjuncts = new ArrayList<>();
        joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false, externalRepresentation, whereClauseConjuncts, explicitVersionEntities, nodesToFetch);
        return whereClauseConjuncts;
    }

    protected void appendWhereClause(StringBuilder sbSelectFrom) {
        appendWhereClause(sbSelectFrom, Collections.<String>emptyList());
    }

    protected void appendWhereClause(StringBuilder sbSelectFrom, List<String> whereClauseConjuncts) {
        KeysetLink keysetLink = keysetManager.getKeysetLink();
        if (keysetLink == null || keysetLink.getKeysetMode() == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom, whereClauseConjuncts);
        } else {
            sbSelectFrom.append(" WHERE ");

            if (mainQuery.getQueryConfiguration().isOptimizedKeysetPredicateRenderingEnabled()) {
                keysetManager.buildOptimizedKeysetPredicate(sbSelectFrom);
            } else {
                keysetManager.buildKeysetPredicate(sbSelectFrom);
            }

            if (whereManager.hasPredicates()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom, whereClauseConjuncts);
            }
        }
    }

    protected void appendGroupByClause(StringBuilder sbSelectFrom) {
        Set<String> clauses = new LinkedHashSet<String>();
        groupByManager.buildGroupByClauses(clauses);

        int size = transformerGroups.size();
        for (int i = 0; i < transformerGroups.size(); i++) {
            ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
            clauses.addAll(transformerGroup.getRequiredGroupByClauses());
        }

        if (hasGroupBy) {
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromSelectEnabled()) {
                selectManager.buildGroupByClauses(cbf.getMetamodel(), clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromHavingEnabled()) {
                havingManager.buildGroupByClauses(clauses);
            }
            if (mainQuery.getQueryConfiguration().isImplicitGroupByFromOrderByEnabled()) {
                orderByManager.buildGroupByClauses(clauses);
            }
        }

        if (!clauses.isEmpty()) {
            for (int i = 0; i < size; i++) {
                ExpressionTransformerGroup<?> transformerGroup = transformerGroups.get(i);
                clauses.addAll(transformerGroup.getOptionalGroupByClauses());
            }
        }

        groupByManager.buildGroupBy(sbSelectFrom, clauses);
        havingManager.buildClause(sbSelectFrom);
    }

    protected void appendOrderByClause(StringBuilder sbSelectFrom) {
        queryGenerator.setResolveSelectAliases(false);
        orderByManager.buildOrderBy(sbSelectFrom, false, false);
        queryGenerator.setResolveSelectAliases(true);
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
        if (selectManager.getSelectObjectBuilder() != null) {
            return  new ObjectBuilderTypedQuery<>(query, selectManager.getSelectObjectBuilder());
        } else {
            return (TypedQuery<QueryResultType>) query;
        }
    }
    // TODO: needs equals-hashCode implementation
}
