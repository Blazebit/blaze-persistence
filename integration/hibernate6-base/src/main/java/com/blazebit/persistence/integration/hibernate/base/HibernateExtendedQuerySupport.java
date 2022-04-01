/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ReflectionUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.CompoundSelection;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.internal.FilterJdbcParameter;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.TupleTransformer;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.spi.DomainQueryExecutionContext;
import org.hibernate.query.spi.Limit;
import org.hibernate.query.spi.NonSelectQueryPlan;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.spi.QueryInterpretationCache;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.spi.QueryPlan;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.MultiTableDeleteQueryPlan;
import org.hibernate.query.sqm.internal.MultiTableUpdateQueryPlan;
import org.hibernate.query.sqm.internal.QuerySqmImpl;
import org.hibernate.query.sqm.internal.SimpleDeleteQueryPlan;
import org.hibernate.query.sqm.internal.SimpleUpdateQueryPlan;
import org.hibernate.query.sqm.internal.SqmInterpretationsKey;
import org.hibernate.query.sqm.internal.SqmJdbcExecutionContextAdapter;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.spi.SqmParameterMappingModelResolutionAccess;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.SqmTranslator;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.tree.from.SqmJoin;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.insert.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.insert.SqmInsertStatement;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;
import org.hibernate.query.sqm.tree.select.SqmQueryGroup;
import org.hibernate.query.sqm.tree.select.SqmQueryPart;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.select.SqmSelectableNode;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;
import org.hibernate.spi.NavigablePath;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.delete.DeleteStatement;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.ast.tree.from.CollectionTableGroup;
import org.hibernate.sql.ast.tree.from.LazyTableGroup;
import org.hibernate.sql.ast.tree.from.NamedTableReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.insert.InsertStatement;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.ast.tree.select.QueryGroup;
import org.hibernate.sql.ast.tree.select.QueryPart;
import org.hibernate.sql.ast.tree.select.QuerySpec;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.ast.tree.update.UpdateStatement;
import org.hibernate.sql.exec.internal.JdbcParameterBindingsImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcDelete;
import org.hibernate.sql.exec.spi.JdbcInsert;
import org.hibernate.sql.exec.spi.JdbcMutation;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.JdbcUpdate;
import org.hibernate.sql.results.graph.entity.LoadingEntityEntry;
import org.hibernate.sql.results.internal.RowTransformerJpaTupleImpl;
import org.hibernate.sql.results.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.results.internal.RowTransformerStandardImpl;
import org.hibernate.sql.results.internal.RowTransformerTupleTransformerAdapter;
import org.hibernate.sql.results.internal.TupleMetadata;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMapping;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;
import org.hibernate.type.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());

    private final HibernateAccess hibernateAccess;
    private final BoundedConcurrentHashMap<QueryInterpretationCache.Key, QueryPlan> participatingInterpretationCache;
    private final BoundedConcurrentHashMap<QueryInterpretationCache.Key, QueryPlan> queryPlanCache;

    public HibernateExtendedQuerySupport() {
        Iterator<HibernateAccess> serviceIter = ServiceLoader.load(HibernateAccess.class).iterator();
        if (!serviceIter.hasNext()) {
            throw new IllegalStateException("Hibernate integration was not found on the class path!");
        }
        this.hibernateAccess = serviceIter.next();
        this.participatingInterpretationCache = new BoundedConcurrentHashMap<>(2048, 20, BoundedConcurrentHashMap.Eviction.LIRS);
        this.queryPlanCache = new BoundedConcurrentHashMap<>(2048, 20, BoundedConcurrentHashMap.Eviction.LIRS);
    }

    @Override
    public boolean supportsAdvancedSql() {
        return true;
    }

    @Override
    public boolean needsExampleQueryForAdvancedDml() {
        return true;
    }

    @Override
    public boolean applyFirstResultMaxResults(Query query, int firstResult, int maxResults) {
        Limit limit = query.unwrap(QuerySqmImpl.class).getQueryOptions().getLimit();
        Integer firstRow = firstResult == 0 ? null : firstResult;
        Integer maxRows = maxResults == Integer.MAX_VALUE ? null : maxResults;
        boolean changed = firstRow == null && limit.getFirstRow() != null || firstRow != null && limit.getFirstRow() == null
                || maxRows == null && limit.getMaxRows() != null || maxRows != null && limit.getMaxRows() == null;
        limit.setFirstRow(firstRow);
        limit.setMaxRows(maxRows);
        return changed;
    }

    @Override
    public String getSql(EntityManager em, Query query) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        try {
            return getJdbcOperation(factory, interpretation, hqlQuery).getSql();
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
    }

    @Override
    public boolean getSqlContainsLimit() {
        return true;
    }

    @Override
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        SessionFactoryImplementor sfi = em.unwrap(SessionImplementor.class).getSessionFactory();
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        if (hqlQuery.getSqmStatement() instanceof SqmDeleteStatement<?>) {
            SqmDeleteStatement<?> deleteStatement = (SqmDeleteStatement<?>) hqlQuery.getSqmStatement();
            String mutatingEntityName = deleteStatement.getTarget().getModel().getHibernateEntityName();
            EntityMappingType entityDescriptor = sfi.getMappingMetamodel().getEntityDescriptor(mutatingEntityName);
            SqlAstTranslatorFactory sqlAstTranslatorFactory = sfi.getJdbcServices().getJdbcEnvironment().getSqlAstTranslatorFactory();
            List<String> deleteSqls = new ArrayList<>();
            entityDescriptor.visitConstraintOrderedTables(
                (tableExpression, tableKeyColumnsVisitationSupplier) -> {

                    //                    final TableReference targetTableReference = new TableReference(
                    //                            tableExpression,
                    //                            null,
                    //                            false,
                    //                            sfi
                    //                    );

                    final Predicate matchingIdsPredicate = null;//new InSubQueryPredicate();
                    //                        matchingIdsPredicateProducer.produceRestriction(
                    //                                ids,
                    //                                entityDescriptor,
                    //                                targetTableReference,
                    //                                tableKeyColumnsVisitationSupplier,
                    //                                query
                    //                        );

                    //                    final SqlAstDeleteTranslator sqlAstTranslator = sqlAstTranslatorFactory.buildDeleteTranslator( sfi );
                    //                    final JdbcDelete jdbcOperation = sqlAstTranslator.translate( new DeleteStatement( targetTableReference, matchingIdsPredicate ) );
                }
            );
            return deleteSqls;
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias, int queryPartNumber) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = getQuerySpec(((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQueryPart(), queryPartNumber);
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = getQuerySpec(((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQueryPart(), queryPartNumber);
        } else {
            throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
        }

        NavigablePath navigablePath = findNavigablePath(alias, querySpec);

        CacheableSqmInterpretation interpretation = buildQuerySpecPlan(query);
        TableGroup tableGroup = getTableGroup(interpretation, navigablePath);
        return tableGroup.getPrimaryTableReference().getIdentificationVariable();
    }

    @Override
    public SqlFromInfo getSqlFromInfo(EntityManager em, Query query, String alias, int queryPartNumber) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = getQuerySpec(((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQueryPart(), queryPartNumber);
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = getQuerySpec(((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQueryPart(), queryPartNumber);
        } else {
            throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
        }

        NavigablePath navigablePath = findNavigablePath(alias, querySpec);

        CacheableSqmInterpretation interpretation = buildQuerySpecPlan(query);
        TableGroup tableGroup = getTableGroup(interpretation, navigablePath);
        NamedTableReference primaryTableReference = (NamedTableReference) tableGroup.getPrimaryTableReference();
        String tableAlias = primaryTableReference.getIdentificationVariable();
        SessionFactoryImplementor sfi = em.unwrap(SessionImplementor.class).getSessionFactory();
        String fromText = primaryTableReference.getTableId() + " " + tableAlias;
        String fakeFromText = primaryTableReference.getTableId() + "/**/ " + tableAlias;
        // We introduce a special marker into the table name to be able to find the correct table reference
        // Note that it is important, this interpretation does not come from a cache,
        // otherwise bad things will happen due to this mutation
        primaryTableReference.setPrunedTableExpression(primaryTableReference.getTableId() + "/**/");
        String sql;
        try {
            sql = getJdbcOperation(sfi, interpretation, hqlQuery).getSql();
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
        int startIndex = sql.indexOf(fakeFromText);
        int endIndex = startIndex + fromText.length();
        return new SqlFromInfo() {
            @Override
            public String getAlias() {
                return tableAlias;
            }

            @Override
            public int getFromStartIndex() {
                return startIndex;
            }

            @Override
            public int getFromEndIndex() {
                return endIndex;
            }
        };
    }

    private NavigablePath findNavigablePath(String alias, SqmQuerySpec<?> querySpec) {
        for (SqmRoot<?> root : querySpec.getFromClause().getRoots()) {
            NavigablePath path = findNavigablePath(alias, root);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private NavigablePath findNavigablePath(String alias, SqmFrom<?, ?> sqmFrom) {
        if (alias.equals(sqmFrom.getExplicitAlias())) {
            return sqmFrom.getNavigablePath();
        }
        for (SqmJoin<?, ?> sqmJoin : sqmFrom.getSqmJoins()) {
            NavigablePath path = findNavigablePath(alias, sqmJoin);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private SqmQuerySpec<?> getQuerySpec(SqmQueryPart<?> queryPart, int queryPartNumber) {
        Object querySpec = getQuerySpec(queryPart, 0, queryPartNumber);
        if (querySpec instanceof SqmQuerySpec<?>) {
            return (SqmQuerySpec<?>) querySpec;
        }
        throw new IllegalArgumentException("Couldn't find query part number " + queryPartNumber + " in query part: " + queryPart);
    }

    private Object getQuerySpec(SqmQueryPart<?> queryPart, int currentNumber, int queryPartNumber) {
        if (currentNumber == queryPartNumber) {
            return queryPart.getFirstQuerySpec();
        }
        if (queryPart instanceof SqmQueryGroup<?>) {
            List<? extends SqmQueryPart<?>> queryParts = ((SqmQueryGroup<?>) queryPart).getQueryParts();
            int offset = 0;
            for (int i = 0; i < queryParts.size(); i++) {
                Object result = getQuerySpec(queryParts.get(i), currentNumber + offset + i, queryPartNumber);
                if (result instanceof SqmQuerySpec<?>) {
                    return result;
                }
                offset += (int) result;
            }
            return offset;
        }
        return 1;
    }

    private TableGroup getTableGroup(CacheableSqmInterpretation interpretation, NavigablePath navigablePath) {
        TableGroup tableGroup = interpretation.tableGroupAccess.findTableGroup(navigablePath);
        if (tableGroup == null) {
            Statement sqlAst = interpretation.sqmTranslation.getSqlAst();
            if (sqlAst instanceof SelectStatement) {
                tableGroup = findTableGroup(((SelectStatement) sqlAst).getQueryPart(), navigablePath);
            } else if (sqlAst instanceof InsertStatement) {
                tableGroup = findTableGroup(((InsertStatement) sqlAst).getSourceSelectStatement(), navigablePath);
            } else {
                tableGroup = null;
            }
        }
        if (tableGroup != null) {
            if (tableGroup instanceof CollectionTableGroup) {
                TableGroup elementTableGroup = ((CollectionTableGroup) tableGroup).getElementTableGroup();
                return elementTableGroup == null || elementTableGroup instanceof LazyTableGroup && ((LazyTableGroup) elementTableGroup).getUnderlyingTableGroup() == null ? tableGroup : elementTableGroup;
            }
            return tableGroup;
        }
        throw new IllegalArgumentException("Couldn't find the table group for the navigable path: " + navigablePath);
    }

    private TableGroup findTableGroup(QueryPart queryPart, NavigablePath navigablePath) {
        if (queryPart instanceof QueryGroup) {
            for (QueryPart part : ((QueryGroup) queryPart).getQueryParts()) {
                TableGroup tableGroup = findTableGroup(part, navigablePath);
                if (tableGroup != null) {
                    return tableGroup;
                }
            }
        } else {
            QuerySpec querySpec = (QuerySpec) queryPart;
            return querySpec.getFromClause().queryTableGroups(tableGroup -> tableGroup.getNavigablePath() == navigablePath ? tableGroup : null);
        }
        return null;
    }

    @Override
    public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = ((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQuerySpec();
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQueryPart().getFirstQuerySpec();
        } else {
            throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
        }

        boolean found = false;
        int position = 1;

        for (SqmSelectableNode<?> selectionItem : querySpec.getSelectClause().getSelectionItems()) {
            if (alias.equals(selectionItem.getAlias())) {
                found = true;
                break;
            }
            position++;
        }

        return found ? position : -1;
    }

    @Override
    public int getSqlSelectAttributePosition(EntityManager em, Query query, String expression) {
        if (expression.contains(".")) {
            // TODO: implement
            throw new UnsupportedOperationException("Embeddables are not yet supported!");
        }

        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = ((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQuerySpec();
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQueryPart().getFirstQuerySpec();
        } else {
            throw new IllegalArgumentException("The expression " + expression + " could not be found in the query: " + query);
        }

        boolean found = false;
        int position = 1;

        if (querySpec.getSelectClause().getSelectionItems().size() == 1 && querySpec.getSelectClause().getSelectionItems().get(0) instanceof SqmRoot<?>) {
            SqmRoot<?> root = (SqmRoot<?>) querySpec.getSelectClause().getSelectionItems().get(0);
            EntityPersister entityPersister = hqlQuery.getSessionFactory().getMetamodel().getEntityDescriptor(root.getEntityName());
            int propertyIndex = entityPersister.getEntityMetamodel().getPropertyIndex(expression);
            Type[] propertyTypes = entityPersister.getPropertyTypes();
            for (int j = 0; j < propertyIndex; j++) {
                position += propertyTypes[j].getColumnSpan(hqlQuery.getSessionFactory());
            }
            return position;
        }

        for (SqmSelectableNode<?> selectionItem : querySpec.getSelectClause().getSelectionItems()) {
            if (expression.equals(selectionItem.asLoggableText())) {
                found = true;
                break;
            }
            position++;
        }

        return found ? position : -1;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getResultList(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled) {
        return getResultList(serviceProvider, participatingQueries, query, sqlOverride, queryPlanCacheEnabled, query.unwrap(DomainQueryExecutionContext.class));
    }

    private List getResultList(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled, DomainQueryExecutionContext executionContext) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor sessionFactory = hqlQuery.getSessionFactory();

        RowTransformer<?> rowTransformer = determineRowTransformer((SqmSelectStatement<?>) hqlQuery.getSqmStatement(), hqlQuery.getResultType(), hqlQuery.getQueryOptions());

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        Set<FilterJdbcParameter> filterJdbcParameters = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcOperation jdbcOperation = getJdbcOperation(sessionFactory, interpretation, participatingQuery.unwrap(QuerySqmImpl.class));
            if (query == participatingQuery) {
                // Don't copy over the limit and offset parameters because we need to use the LimitHandler for now
                JdbcSelect select = (JdbcSelect) jdbcOperation;
                for (JdbcParameterBinder parameterBinder : jdbcOperation.getParameterBinders()) {
                    if (parameterBinder != select.getLimitParameter() && parameterBinder != select.getOffsetParameter()) {
                        parameterBinders.add(parameterBinder);
                    }
                }
            } else {
                parameterBinders.addAll(jdbcOperation.getParameterBinders());
            }
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            filterJdbcParameters.addAll(jdbcOperation.getFilterJdbcParameters());
            final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<List<JdbcParameter>>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                    interpretation.domainParameterXref,
                    interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
            );

            final JdbcParameterBindings tempJdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                    participatingQuery.unwrap(QuerySqmImpl.class).getQueryParameterBindings(),
                    interpretation.domainParameterXref,
                    jdbcParamsXref,
                    session.getFactory().getRuntimeMetamodels().getMappingMetamodel(),
                    interpretation.tableGroupAccess::findTableGroup,
                    new SqmParameterMappingModelResolutionAccess() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                            return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                        }
                    },
                    session
            );
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        // todo: avoid double translation
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcSelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                .translate(jdbcParameterBindings, executionContext.getQueryOptions());
        final JdbcSelect realJdbcSelect = new JdbcSelect(
                sqlOverride,
                parameterBinders,
                jdbcSelect.getJdbcValuesMappingProducer(),
                affectedTableNames,
                filterJdbcParameters
//                ,jdbcSelect.getRowsToSkip(),
//                jdbcSelect.getMaxRows(),
//                jdbcSelect.getAppliedParameters(),
//                jdbcSelect.getLockStrategy(),
//                jdbcSelect.getOffsetParameter(),
//                jdbcSelect.getLimitParameter()
        );

        // todo: to get subselect fetching work, we need a slight API change in Hibernate because we need to inject our sql override somehow
        //        final SubselectFetch.RegistrationHandler subSelectFetchKeyHandler = SubselectFetch.createRegistrationHandler(
        //                session.getPersistenceContext().getBatchFetchQueue(),
        //                sqmInterpretation.selectStatement,
        //                Collections.emptyList(),
        //                jdbcParameterBindings
        //        );

        session.autoFlushIfRequired(realJdbcSelect.getAffectedTableNames());

        try {
            return session.getFactory().getJdbcServices().getJdbcSelectExecutor().list(
                    realJdbcSelect,
                    jdbcParameterBindings,
                    new SqmJdbcExecutionContextAdapter(executionContext, jdbcSelect) {
                        @Override
                        public void registerLoadingEntityEntry(EntityKey entityKey, LoadingEntityEntry entry) {
                            //                            subSelectFetchKeyHandler.addKey( entityKey );
                        }

                        @Override
                        public String getQueryIdentifier(String sql) {
                            return sql;
                        }

                        @Override
                        public boolean hasQueryExecutionToBeAddedToStatistics() {
                            return true;
                        }
                    },
                    rowTransformer,
                    ListResultsConsumer.UniqueSemantic.FILTER
            );
        } catch (HibernateException e) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
            if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
                throw session.getExceptionConverter().convert(e);
            } else {
                throw e;
            }
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
    }

    @Override
    public Object getResultStream(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled) {
        return getResultStream(serviceProvider, participatingQueries, query, sqlOverride, queryPlanCacheEnabled, query.unwrap(DomainQueryExecutionContext.class));
    }

    private Object getResultStream(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled, DomainQueryExecutionContext executionContext) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor sessionFactory = hqlQuery.getSessionFactory();

        RowTransformer<?> rowTransformer = determineRowTransformer((SqmSelectStatement<?>) hqlQuery.getSqmStatement(), hqlQuery.getResultType(), hqlQuery.getQueryOptions());

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        Set<FilterJdbcParameter> filterJdbcParameters = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcOperation jdbcOperation = getJdbcOperation(sessionFactory, interpretation, participatingQuery.unwrap(QuerySqmImpl.class));
            parameterBinders.addAll(jdbcOperation.getParameterBinders());
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            filterJdbcParameters.addAll(jdbcOperation.getFilterJdbcParameters());
            final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<List<JdbcParameter>>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                    interpretation.domainParameterXref,
                    interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
            );

            final JdbcParameterBindings tempJdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                    executionContext.getQueryParameterBindings(),
                    interpretation.domainParameterXref,
                    jdbcParamsXref,
                    session.getFactory().getRuntimeMetamodels().getMappingMetamodel(),
                    interpretation.tableGroupAccess::findTableGroup,
                    new SqmParameterMappingModelResolutionAccess() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                            return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                        }
                    },
                    session
            );
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcSelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                .translate(jdbcParameterBindings, executionContext.getQueryOptions());
        final JdbcSelect realJdbcSelect = new JdbcSelect(
                sqlOverride,
                parameterBinders,
                jdbcSelect.getJdbcValuesMappingProducer(),
                affectedTableNames,
                filterJdbcParameters
        );

        session.autoFlushIfRequired(realJdbcSelect.getAffectedTableNames());

        try {
            return session.getFactory().getJdbcServices().getJdbcSelectExecutor().stream(
                    realJdbcSelect,
                    jdbcParameterBindings,
                    new SqmJdbcExecutionContextAdapter(executionContext, realJdbcSelect),
                    rowTransformer
            );
        } catch (HibernateException e) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
            if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
                throw session.getExceptionConverter().convert(e);
            } else {
                throw e;
            }
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object getSingleResult(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled) {
        final List list = getResultList(serviceProvider, participatingQueries, query, sqlOverride, queryPlanCacheEnabled);
        if (list.size() == 0) {
            throw new NoResultException("No entity found for query");
        }
        return uniqueElement(list);
    }

    private static <R> R uniqueElement(List<R> list) throws NonUniqueResultException {
        int size = list.size();
        if (size == 0) {
            return null;
        }
        R first = list.get(0);
        for (int i = 1; i < size; i++) {
            if (list.get(i) != first) {
                throw new NonUniqueResultException(list.size());
            }
        }
        return first;
    }

    private <R> RowTransformer<R> determineRowTransformer(
            SqmSelectStatement<?> sqm,
            Class<R> resultType,
            QueryOptions queryOptions) {
        if (resultType == null || resultType.isArray()) {
            if (queryOptions.getTupleTransformer() != null) {
                return makeRowTransformerTupleTransformerAdapter(sqm, queryOptions);
            } else {
                return RowTransformerStandardImpl.instance();
            }
        }

        // NOTE : if we get here, a result-type of some kind (other than Object[].class) was specified

        final List<SqmSelection<?>> selections = sqm.getQueryPart().getFirstQuerySpec().getSelectClause().getSelections();
        if (Tuple.class.isAssignableFrom(resultType)) {
            // resultType is Tuple..
            if (queryOptions.getTupleTransformer() == null) {
                final Map<TupleElement<?>, Integer> tupleElementMap;
                if (selections.size() == 1 && selections.get(0).getSelectableNode() instanceof CompoundSelection<?>) {
                    final List<? extends JpaSelection<?>> selectionItems = selections.get(0)
                            .getSelectableNode()
                            .getSelectionItems();
                    tupleElementMap = new IdentityHashMap<>(selectionItems.size());
                    for (int i = 0; i < selectionItems.size(); i++) {
                        tupleElementMap.put(selectionItems.get(i), i);
                    }
                } else {
                    tupleElementMap = new IdentityHashMap<>(selections.size());
                    for (int i = 0; i < selections.size(); i++) {
                        final SqmSelection<?> selection = selections.get(i);
                        tupleElementMap.put(selection.getSelectableNode(), i);
                    }
                }
                return (RowTransformer<R>) new RowTransformerJpaTupleImpl(new TupleMetadata(tupleElementMap));
            }

            throw new IllegalArgumentException(
                    "Illegal combination of Tuple resultType and (non-JpaTupleBuilder) TupleTransformer : " +
                            queryOptions.getTupleTransformer()
            );
        }

        // NOTE : if we get here we have a resultType of some kind

        if (queryOptions.getTupleTransformer() != null) {
            // aside from checking the type parameters for the given TupleTransformer
            // there is not a decent way to verify that the TupleTransformer returns
            // the same type.  We rely on the API here and assume the best
            return makeRowTransformerTupleTransformerAdapter(sqm, queryOptions);
        } else if (selections.size() > 1) {
            throw new IllegalQueryOperationException("Query defined multiple selections, return cannot be typed (other that Object[] or Tuple)");
        } else {
            return RowTransformerSingularReturnImpl.instance();
        }
    }

    private <R> RowTransformer<R> makeRowTransformerTupleTransformerAdapter(
            SqmSelectStatement<?> sqm,
            QueryOptions queryOptions) {
        final List<String> aliases = new ArrayList<>();
        for (SqmSelection<?> sqmSelection : sqm.getQuerySpec().getSelectClause().getSelections()) {
            // The row a tuple transformer gets to see only contains 1 element for a dynamic instantiation
            if (sqmSelection.getSelectableNode() instanceof SqmDynamicInstantiation<?>) {
                aliases.add(sqmSelection.getAlias());
            } else {
                sqmSelection.getSelectableNode().visitSubSelectableNodes( subSelection -> aliases.add(subSelection.getAlias()) );
            }
        }


        @SuppressWarnings("unchecked")
        TupleTransformer<R> tupleTransformer = (TupleTransformer<R>) queryOptions.getTupleTransformer();
        return new RowTransformerTupleTransformerAdapter<R>(
                ArrayHelper.toStringArray(aliases),
                tupleTransformer
        );
    }

    @Override
    public int executeUpdate(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query query, String finalSql, boolean queryPlanCacheEnabled) {
        EntityManager em = serviceProvider.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        final SessionFactoryImplementor sessionFactory = session.getSessionFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        Set<FilterJdbcParameter> filterJdbcParameters = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcOperation jdbcOperation = getJdbcOperation(sessionFactory, interpretation, participatingQuery.unwrap(QuerySqmImpl.class));
            parameterBinders.addAll(jdbcOperation.getParameterBinders());
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            filterJdbcParameters.addAll(jdbcOperation.getFilterJdbcParameters());
            final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<List<JdbcParameter>>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                    interpretation.domainParameterXref,
                    interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
            );

            final JdbcParameterBindings tempJdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                    participatingQuery.unwrap(DomainQueryExecutionContext.class).getQueryParameterBindings(),
                    interpretation.domainParameterXref,
                    jdbcParamsXref,
                    session.getFactory().getRuntimeMetamodels().getMappingMetamodel(),
                    interpretation.tableGroupAccess::findTableGroup,
                    new SqmParameterMappingModelResolutionAccess() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                            return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                        }
                    },
                    session
            );
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmStatement<?> sqmStatement = hqlQuery.getSqmStatement();
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcMutation realJdbcStatement;
        if (sqmStatement instanceof SqmUpdateStatement<?>) {
//            final JdbcUpdate jdbcUpdate = sqlAstTranslatorFactory.buildUpdateTranslator(sessionFactory, (UpdateStatement) interpretation.getSqmTranslation().getSqlAst())
//                    .translate(jdbcParameterBindings, executionContext.getQueryOptions());
            realJdbcStatement = new JdbcUpdate(
                    finalSql,
                    parameterBinders,
                    affectedTableNames,
                    filterJdbcParameters,
                    Collections.emptyMap()
            );
        } else if (sqmStatement instanceof SqmDeleteStatement<?>) {
            realJdbcStatement = new JdbcDelete(
                    finalSql,
                    parameterBinders,
                    affectedTableNames,
                    filterJdbcParameters,
                    Collections.emptyMap()
            );
        } else if (sqmStatement instanceof SqmInsertSelectStatement<?>) {
            realJdbcStatement = new JdbcInsert(
                    finalSql,
                    parameterBinders,
                    affectedTableNames,
                    filterJdbcParameters,
                    Collections.emptyMap()
            );
        } else {
            throw new IllegalArgumentException("Unsupported sqm statement: " + sqmStatement);
        }

        session.autoFlushIfRequired(realJdbcStatement.getAffectedTableNames());

        Function<String, PreparedStatement> statementCreator = sql -> session.getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);
        BiConsumer<Integer, PreparedStatement> expectationCheck = (integer, preparedStatement) -> { };
        try {
            return session.getFactory().getJdbcServices().getJdbcMutationExecutor().execute(
                    realJdbcStatement,
                    jdbcParameterBindings,
                    statementCreator,
                    expectationCheck,
                    SqmJdbcExecutionContextAdapter.usingLockingAndPaging(query.unwrap(DomainQueryExecutionContext.class))
            );
        } catch (HibernateException e) {
            LOG.severe("Could not execute the following SQL query: " + finalSql);
            if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
                throw session.getExceptionConverter().convert(e);
            } else {
                throw e;
            }
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<Object[]> executeReturning(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query modificationBaseQuery, Query exampleQuery, String sqlOverride, boolean queryPlanCacheEnabled) {
        DbmsDialect dbmsDialect = serviceProvider.getService(DbmsDialect.class);
        EntityManager em = serviceProvider.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        final SessionFactoryImplementor sessionFactory = session.getSessionFactory();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        Set<FilterJdbcParameter> filterJdbcParameters = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcOperation jdbcOperation = getJdbcOperation(sessionFactory, interpretation, participatingQuery.unwrap(QuerySqmImpl.class));
            // Exclude limit/offset parameters from example query
            if (participatingQuery != exampleQuery) {
                parameterBinders.addAll(jdbcOperation.getParameterBinders());
            }
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            filterJdbcParameters.addAll(jdbcOperation.getFilterJdbcParameters());
            final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<List<JdbcParameter>>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                    interpretation.domainParameterXref,
                    interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
            );

            final JdbcParameterBindings tempJdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                    participatingQuery.unwrap(DomainQueryExecutionContext.class).getQueryParameterBindings(),
                    interpretation.domainParameterXref,
                    jdbcParamsXref,
                    session.getFactory().getRuntimeMetamodels().getMappingMetamodel(),
                    interpretation.tableGroupAccess::findTableGroup,
                    new SqmParameterMappingModelResolutionAccess() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                            return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                        }
                    },
                    session
            );
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        // Create combined query parameters
        //        List<String> queryStrings = new ArrayList<>(participatingQueries.size());
        //        Set<String> querySpaces = new HashSet<>();
        //        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries, queryStrings, querySpaces);
        //        QueryParameters queryParameters = queryParametersEntry.queryParameters;

        // Create plan for example query
        JdbcSelect exampleQueryJdbcOperation = (JdbcSelect) getJdbcOperation(exampleQuery);

        StringBuilder sqlSb = new StringBuilder(sqlOverride.length() + 100);
        sqlSb.append(sqlOverride);

        int[] returningColumnTypes = dbmsDialect.needsReturningSqlTypes() ? getReturningColumnTypes(exampleQueryJdbcOperation, sessionFactory) : null;
        boolean caseInsensitive = !Boolean.valueOf(serviceProvider.getService(ConfigurationSource.class).getProperty(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE));
        String[][] returningColumns = getReturningColumns(caseInsensitive, exampleQueryJdbcOperation.getSql());
        String finalSql = sqlSb.toString();

        try {
            HibernateReturningResult<Object[]> returningResult = new HibernateReturningResult<Object[]>();
            //            if (!queryPlanEntry.isFromCache()) {
            //                prepareQueryPlan(queryPlan, queryParametersEntry.specifications, finalSql, session, modificationBaseQuery, true, dbmsDialect);
            //                queryPlan = putQueryPlanIfAbsent(sfi, cacheKey, queryPlan);
            //            }
            //
            //            if (queryPlan.getTranslators().length > 1) {
            //                throw new IllegalArgumentException("No support for multiple translators yet!");
            //            }
            //
            //            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            //
            //            // If the DBMS doesn't support inclusion of cascading deletes in a with clause, we have to execute them manually
            //            StatementExecutor executor = getExecutor(queryTranslator, session, modificationBaseQuery);
            //            List<String> originalDeletes = Collections.emptyList();
            //
            //            if (executor != null && executor instanceof DeleteExecutor) {
            //                originalDeletes = getField(executor, "deletes");
            //            }
            //
            //            // Extract query loader for native listing
            //            QueryLoader queryLoader = getField(queryTranslator, "queryLoader");

            // Do the native list operation with custom session and combined parameters

            /*
             * NATIVE LIST START
             */
            //            hibernateAccess.checkTransactionSynchStatus(session);
            //            queryParameters.validateParameters();
            //            autoFlush(querySpaces, session);

            List<Object[]> results = Collections.EMPTY_LIST;
            boolean success = false;

            // todo: avoid double translation
            CacheableSqmInterpretation interpretation = buildQueryPlan(exampleQuery);
            DomainQueryExecutionContext domainQueryExecutionContext = exampleQuery.unwrap(DomainQueryExecutionContext.class);
            final JdbcSelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                    .translate(jdbcParameterBindings, domainQueryExecutionContext.getQueryOptions());
            final JdbcSelect realJdbcSelect = new JdbcSelect(
                    sqlOverride,
                    parameterBinders,
                    jdbcSelect.getJdbcValuesMappingProducer(),
                    affectedTableNames,
                    filterJdbcParameters
                                    ,jdbcSelect.getRowsToSkip(),
                                    jdbcSelect.getMaxRows(),
                                    jdbcSelect.getAppliedParameters(),
                                    jdbcSelect.getLockStrategy(),
                                    jdbcSelect.getOffsetParameter(),
                                    jdbcSelect.getLimitParameter()
            );
            ExecutionContext executionContext = new SqmJdbcExecutionContextAdapter(domainQueryExecutionContext, realJdbcSelect) {
                @Override
                public void registerLoadingEntityEntry(EntityKey entityKey, LoadingEntityEntry entry) {
//                                subSelectFetchKeyHandler.addKey( entityKey, entry );
                }

                @Override
                public String getQueryIdentifier(String sql) {
                    return sql;
                }

                @Override
                public boolean hasQueryExecutionToBeAddedToStatistics() {
                    return true;
                }
            };
//            ExecutionContext executionContext = new ExecutionContext() {
//                @Override
//                public void registerLoadingEntityEntry(EntityKey entityKey, LoadingEntityEntry entry) {
////                                subSelectFetchKeyHandler.addKey( entityKey, entry );
//                }
//
//                @Override
//                public SharedSessionContractImplementor getSession() {
//                    return domainQueryExecutionContext.getSession();
//                }
//
//                @Override
//                public QueryOptions getQueryOptions() {
//                    return domainQueryExecutionContext.getQueryOptions();
//                }
//
//                @Override
//                public QueryParameterBindings getQueryParameterBindings() {
//                    return domainQueryExecutionContext.getQueryParameterBindings();
//                }
//
//                @Override
//                public Callback getCallback() {
//                    return domainQueryExecutionContext.getCallback();
//                }
//
//                @Override
//                public String getQueryIdentifier(String sql) {
//                    return sql;
//                }
//
//                @Override
//                public boolean hasQueryExecutionToBeAddedToStatistics() {
//                    return true;
//                }
//            };

            // todo: to get subselect fetching work, we need a slight API change in Hibernate because we need to inject our sql override somehow
            //        final SubselectFetch.RegistrationHandler subSelectFetchKeyHandler = SubselectFetch.createRegistrationHandler(
            //                session.getPersistenceContext().getBatchFetchQueue(),
            //                sqmInterpretation.selectStatement,
            //                Collections.emptyList(),
            //                jdbcParameterBindings
            //        );

            session.autoFlushIfRequired(realJdbcSelect.getAffectedTableNames());

            try {
                final SubselectFetch.RegistrationHandler subSelectFetchKeyHandler = SubselectFetch.createRegistrationHandler(
                        session.getPersistenceContext().getBatchFetchQueue(),
                        (SelectStatement) interpretation.sqmTranslation.getSqlAst(),
                        Collections.emptyList(),
                        jdbcParameterBindings
                );

                results = session.getFactory().getJdbcServices().getJdbcSelectExecutor().list(
                        realJdbcSelect,
                        jdbcParameterBindings,
                        hibernateAccess.wrapExecutionContext(executionContext, dbmsDialect, returningColumns, returningColumnTypes, returningResult),
                        RowTransformerStandardImpl.instance(),
                        ListResultsConsumer.UniqueSemantic.FILTER
                );
            } catch (HibernateException e) {
                LOG.severe("Could not execute the following SQL query: " + sqlOverride);
                if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
                    throw session.getExceptionConverter().convert(e);
                } else {
                    throw e;
                }
            } finally {
                interpretation.domainParameterXref.clearExpansions();
            }
//            try {
//                //                for (String delete : originalDeletes) {
//                //                    hibernateAccess.doExecute(executor, delete, queryParameters, session, queryParametersEntry.specifications);
//                //                }
//                results = getResultList(serviceProvider, participatingQueries, exampleQuery, finalSql, queryPlanCacheEnabled, hibernateAccess.wrapExecutionContext(exampleQuery, dbmsDialect, returningColumnTypes, returningResult));
//                //                results = hibernateAccess.list(queryLoader, wrapSession(session, dbmsDialect, returningColumns, returningColumnTypes, returningResult), queryParameters);
//                success = true;
//            } catch (HibernateException e) {
//                LOG.severe("Could not execute the following SQL query: " + sqlOverride);
//                if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
//                    throw session.getExceptionConverter().convert(e);
//                } else {
//                    throw e;
//                }
//            } finally {
//                //                hibernateAccess.afterTransaction(session, success);
//            }
            /*
             * NATIVE LIST END
             */

            returningResult.setResultList(results);
            return returningResult;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private static String[][] getReturningColumns(boolean caseInsensitive, String exampleQuerySql) {
        int fromIndex = exampleQuerySql.indexOf("from");
        int selectIndex = exampleQuerySql.indexOf("select");
        String[] selectItems = splitSelectItems(exampleQuerySql.subSequence(selectIndex + "select".length() + 1, fromIndex));
        String[][] returningColumns = new String[selectItems.length][2];

        for (int i = 0; i < selectItems.length; i++) {
            String columnName = selectItems[i].substring(selectItems[i].lastIndexOf('.') + 1);
            if (caseInsensitive) {
                returningColumns[i][0] = columnName.toLowerCase();
                returningColumns[i][1] = columnName.toLowerCase();
            } else {
                returningColumns[i][0] = columnName;
                returningColumns[i][1] = columnName;
            }
        }

        return returningColumns;
    }

    private static String[] splitSelectItems(CharSequence itemsString) {
        List<String> selectItems = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        boolean text = false;

        int i = 0;
        int length = itemsString.length();
        while (i < length) {
            char c = itemsString.charAt(i);

            if (text) {
                if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                } else if (parenthesis == 0 && c == ',') {
                    selectItems.add(trim(sb));
                    sb.setLength(0);
                    text = false;

                    i++;
                    continue;
                }

                sb.append(c);
            } else {
                if (Character.isWhitespace(c)) {
                    // skip whitespace
                } else {
                    sb.append(c);
                    text = true;
                }
            }

            i++;
        }

        if (text) {
            selectItems.add(trim(sb));
        }

        return selectItems.toArray(new String[selectItems.size()]);
    }

    private static String trim(StringBuilder sb) {
        int i = sb.length() - 1;
        while (i >= 0) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                break;
            } else {
                i--;
            }
        }

        return sb.substring(0, i + 1);
    }

    //    public void autoFlush(Set<String> querySpaces, SessionImplementor sessionImplementor) {
    //        AutoFlushEvent event = new AutoFlushEvent(querySpaces, (EventSource) sessionImplementor);
    //        for (AutoFlushEventListener listener : sessionImplementor.getFactory().getServiceRegistry().getService(EventListenerRegistry.class).getEventListenerGroup(EventType.AUTO_FLUSH).listeners()) {
    //            listener.onAutoFlush(event);
    //        }
    //    }
    //

    private static int[] getReturningColumnTypes(JdbcSelect queryPlan, SessionFactoryImplementor sfi) {
        JdbcValuesMapping jdbcValuesMapping = queryPlan.getJdbcValuesMappingProducer().resolve(null, null);
        List<SqlSelection> sqlSelections = jdbcValuesMapping.getSqlSelections();
        List<Integer> sqlTypes = new ArrayList<>(sqlSelections.size());

        for (int i = 0; i < sqlSelections.size(); i++) {
            JdbcMapping jdbcMapping = getField(sqlSelections.get(i), "jdbcMapping");
            sqlTypes.add(jdbcMapping.getJdbcType().getDefaultSqlTypeCode());
        }

        int[] returningColumnTypes = new int[sqlTypes.size()];
        for (int i = 0; i < sqlTypes.size(); i++) {
            returningColumnTypes[i] = sqlTypes.get(i);
        }

        return returningColumnTypes;
    }

    private JdbcOperation getJdbcOperation(Query query) {
        QuerySqmImpl hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
//        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
//            SelectQueryPlan<?> selectQueryPlan = invokeMethod(query, "resolveSelectQueryPlan");
//            if (selectQueryPlan instanceof AggregatedSelectQueryPlanImpl<?>) {
//                // TODO: have to handle multiple sql strings which happens when having e.g. a polymorphic UPDATE/DELETE
//                selectQueryPlan = ((SelectQueryPlan<?>[]) getField(selectQueryPlan, "aggregatedQueryPlans"))[0];
//                //                throw new IllegalArgumentException("No support for multiple translators yet!");
//            }
//            ConcreteSqmSelectQueryPlan<?> plan = (ConcreteSqmSelectQueryPlan<?>) selectQueryPlan;
//            Object cacheableSqmInterpretation = invokeMethod(plan, "resolveCacheableSqmInterpretation", query);
//            JdbcSelect jdbcSelect = getField(cacheableSqmInterpretation, "jdbcSelect");
//            return jdbcSelect;
//        }
        return getJdbcOperation(factory, buildQueryPlan(query), hqlQuery);
    }

    private JdbcOperation getJdbcOperation(SessionFactoryImplementor factory, CacheableSqmInterpretation interpretation, QuerySqmImpl<?> query) {
        JdbcEnvironment jdbcEnvironment = factory.getJdbcServices().getJdbcEnvironment();
        SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();
        SqmTranslation<?> sqmTranslation = interpretation.getSqmTranslation();
        Statement sqlAst = sqmTranslation.getSqlAst();
        final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<List<JdbcParameter>>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                interpretation.domainParameterXref,
                interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
        );

        final JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                query.getQueryParameterBindings(),
                interpretation.domainParameterXref,
                jdbcParamsXref,
                factory.getRuntimeMetamodels().getMappingMetamodel(),
                interpretation.tableGroupAccess::findTableGroup,
                new SqmParameterMappingModelResolutionAccess() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                        return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                    }
                },
                query.getSession()
        );

        if (sqlAst instanceof SelectStatement) {
            SqlAstTranslator<JdbcSelect> translator = sqlAstTranslatorFactory.buildSelectTranslator(factory, (SelectStatement) sqlAst);
            return translator.translate(jdbcParameterBindings, query.getQueryOptions());
        } else if (sqlAst instanceof DeleteStatement) {
            SqlAstTranslator<JdbcDelete> translator = sqlAstTranslatorFactory.buildDeleteTranslator(factory, (DeleteStatement) sqlAst);
            return translator.translate(jdbcParameterBindings, query.getQueryOptions());
        } else if (sqlAst instanceof UpdateStatement) {
            SqlAstTranslator<JdbcUpdate> translator = sqlAstTranslatorFactory.buildUpdateTranslator(factory, (UpdateStatement) sqlAst);
            return translator.translate(jdbcParameterBindings, query.getQueryOptions());
        } else if (sqlAst instanceof InsertStatement) {
            SqlAstTranslator<JdbcInsert> translator = sqlAstTranslatorFactory.buildInsertTranslator(factory, (InsertStatement) sqlAst);
            return translator.translate(jdbcParameterBindings, query.getQueryOptions());
        }
        throw new UnsupportedOperationException();
    }

    private static CacheableSqmInterpretation buildQueryPlan(Query query) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            final SharedSessionContractImplementor session = hqlQuery.getSession();
            final SessionFactoryImplementor sessionFactory = session.getFactory();
            final QueryEngine queryEngine = sessionFactory.getQueryEngine();
            final QueryInterpretationCache.Key cacheKey = SqmInterpretationsKey.createInterpretationsKey(hqlQuery);

            final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

            final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                    (SqmSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory,
                    false
            );

            final SqmTranslation<SelectStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            //            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();

            final SharedSessionContractImplementor session = hqlQuery.getSession();
            final SessionFactoryImplementor sessionFactory = session.getFactory();
            final QueryEngine queryEngine = sessionFactory.getQueryEngine();
            final QueryInterpretationCache.Key cacheKey = SqmInterpretationsKey.createInterpretationsKey(hqlQuery);

            final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

            final SqmTranslator<InsertStatement> sqmConverter = sqmTranslatorFactory.createInsertTranslator(
                    (SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory
            );

            final SqmTranslation<InsertStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else {
            NonSelectQueryPlan nonSelectQueryPlan = invokeMethod(hqlQuery, "resolveNonSelectQueryPlan");
            if (nonSelectQueryPlan instanceof SimpleDeleteQueryPlan) {
                SqmDeleteStatement<?> sqmDelete = getField(nonSelectQueryPlan, "sqmDelete");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
                final QueryEngine queryEngine = factory.getQueryEngine();

                final SqmTranslatorFactory translatorFactory = queryEngine.getSqmTranslatorFactory();
                final SqmTranslator<DeleteStatement> translator = translatorFactory.createSimpleDeleteTranslator(
                        sqmDelete,
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory
                );

                final SqmTranslation<DeleteStatement> sqmInterpretation = translator.translate();
                return new CacheableSqmInterpretation(sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
            } else if (nonSelectQueryPlan instanceof SimpleUpdateQueryPlan) {
                SqmUpdateStatement<?> sqmUpdate = getField(nonSelectQueryPlan, "sqmUpdate");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
                final QueryEngine queryEngine = factory.getQueryEngine();

                final SqmTranslatorFactory translatorFactory = queryEngine.getSqmTranslatorFactory();
                final SqmTranslator<UpdateStatement> translator = translatorFactory.createSimpleUpdateTranslator(
                        sqmUpdate,
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory
                );

                final SqmTranslation<UpdateStatement> sqmInterpretation = translator.translate();
                return new CacheableSqmInterpretation(sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
            } else if (nonSelectQueryPlan instanceof MultiTableDeleteQueryPlan) {

            } else if (nonSelectQueryPlan instanceof MultiTableUpdateQueryPlan) {

            }
        }
        throw new UnsupportedOperationException("not yet implemented");
    }

    private static CacheableSqmInterpretation buildQuerySpecPlan(Query query) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final SessionFactoryImplementor sessionFactory = session.getFactory();
        final QueryEngine queryEngine = sessionFactory.getQueryEngine();

        final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                    (SqmSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory,
                    false
            );

            final SqmTranslation<SelectStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            final SqmTranslator<InsertStatement> sqmConverter = sqmTranslatorFactory.createInsertTranslator(
                    (SqmInsertStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory
            );

            final SqmTranslation<InsertStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else {
            throw new IllegalArgumentException("Only supported for insert or select!");
        }
    }

    private static CacheableSqmInterpretation buildCacheableSqmInterpretation(
            SqmSelectStatement sqm,
            DomainParameterXref domainParameterXref, ExecutionContext executionContext) {
        final SharedSessionContractImplementor session = executionContext.getSession();
        final SessionFactoryImplementor sessionFactory = session.getFactory();
        final QueryEngine queryEngine = sessionFactory.getQueryEngine();

        final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

        final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                sqm,
                executionContext.getQueryOptions(),
                domainParameterXref,
                executionContext.getQueryParameterBindings(),
                executionContext.getLoadQueryInfluencers(),
                sessionFactory,
                false
        );

        final FromClauseAccess tableGroupAccess = sqmConverter.getFromClauseAccess();

        final SqmTranslation<SelectStatement> interpretation = sqmConverter.translate();

        return new CacheableSqmInterpretation(interpretation, tableGroupAccess, domainParameterXref);
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class CacheableSqmInterpretation {
        private final SqmTranslation<?> sqmTranslation;
        private final FromClauseAccess tableGroupAccess;
        private final DomainParameterXref domainParameterXref;

        CacheableSqmInterpretation(
                SqmTranslation<?> sqmTranslation,
                FromClauseAccess tableGroupAccess,
                DomainParameterXref domainParameterXref) {
            this.sqmTranslation = sqmTranslation;
            this.tableGroupAccess = tableGroupAccess;
            this.domainParameterXref = domainParameterXref;
        }

        SqmTranslation<?> getSqmTranslation() {
            return sqmTranslation;
        }

        FromClauseAccess getTableGroupAccess() {
            return tableGroupAccess;
        }

        DomainParameterXref getDomainParameterXref() {
            return domainParameterXref;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeMethod(Object object, String method, Object... args) {
        try {
            if (args.length == 0) {
                Method m = object.getClass().getDeclaredMethod(method);
                m.setAccessible(true);
                return (T) m.invoke(object);
            } else {
                for (Method m : object.getClass().getDeclaredMethods()) {
                    if (method.equals(m.getName())) {
                        m.setAccessible(true);
                        return (T) m.invoke(object, args);
                    }
                }
                // Let getDeclaredMethod throw the exception
                object.getClass().getDeclaredMethod(method);
                return null;
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object object, String field) {
        try {
            Field f = ReflectionUtils.getField(object.getClass(), field);
            f.setAccessible(true);
            return (T) f.get(object);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private static void setField(Object object, String field, Object value) {
        setField(object, object.getClass(), field, value);
    }

    private static void setField(Object object, Class<?> clazz, String field, Object value) {
        try {
            Field f = ReflectionUtils.getField(clazz, field);
            f.setAccessible(true);
            f.set(object, value);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

}
