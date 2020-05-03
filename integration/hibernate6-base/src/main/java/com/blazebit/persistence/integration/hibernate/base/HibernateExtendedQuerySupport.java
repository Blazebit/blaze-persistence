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

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ReflectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.spi.NonSelectQueryPlan;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.spi.SelectQueryPlan;
import org.hibernate.query.sqm.internal.AggregatedSelectQueryPlanImpl;
import org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.MultiTableDeleteQueryPlan;
import org.hibernate.query.sqm.internal.MultiTableUpdateQueryPlan;
import org.hibernate.query.sqm.internal.QuerySqmImpl;
import org.hibernate.query.sqm.internal.SimpleDeleteQueryPlan;
import org.hibernate.query.sqm.internal.SimpleUpdateQueryPlan;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.sql.SimpleSqmDeleteTranslation;
import org.hibernate.query.sqm.sql.SimpleSqmDeleteTranslator;
import org.hibernate.query.sqm.sql.SimpleSqmUpdateTranslation;
import org.hibernate.query.sqm.sql.SimpleSqmUpdateTranslator;
import org.hibernate.query.sqm.sql.SqmQuerySpecTranslation;
import org.hibernate.query.sqm.sql.SqmSelectTranslation;
import org.hibernate.query.sqm.sql.SqmSelectTranslator;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.expression.SqmFunction;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmJoin;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.insert.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.select.SqmSelectableNode;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;
import org.hibernate.sql.ast.SqlAstDeleteTranslator;
import org.hibernate.sql.ast.SqlAstSelectTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.SqlAstUpdateTranslator;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.delete.DeleteStatement;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.internal.JdbcParameterBindingsImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcDelete;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMapping;
import org.hibernate.sql.results.spi.RowTransformer;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());

    private final HibernateAccess hibernateAccess;
    
    public HibernateExtendedQuerySupport() {
        Iterator<HibernateAccess> serviceIter = ServiceLoader.load(HibernateAccess.class).iterator();
        if (!serviceIter.hasNext()) {
            throw new IllegalStateException("Hibernate integration was not found on the class path!");
        }
        this.hibernateAccess = serviceIter.next();
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
    public String getSql(EntityManager em, Query query) {
        QuerySqmImpl hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        try {
            return getJdbcOperation(factory, interpretation).getSql();
        } finally {
            interpretation.domainParameterXref.clearExpansions();
        }
    }

    @Override
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        SessionFactoryImplementor sfi = em.unwrap(SessionImplementor.class).getSessionFactory();
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        if (hqlQuery.getSqmStatement() instanceof SqmDeleteStatement<?>) {
            SqmDeleteStatement<?> deleteStatement = (SqmDeleteStatement<?>) hqlQuery.getSqmStatement();
            String mutatingEntityName = deleteStatement.getTarget().getModel().getHibernateEntityName();
            EntityMappingType entityDescriptor = sfi.getDomainModel().getEntityDescriptor(mutatingEntityName);
            SqlAstTranslatorFactory sqlAstTranslatorFactory = sfi.getJdbcServices().getJdbcEnvironment().getSqlAstTranslatorFactory();
            List<String> deleteSqls = new ArrayList<>();
            entityDescriptor.visitConstraintOrderedTables(
                (tableExpression, tableKeyColumnsVisitationSupplier) -> {

                    final TableReference targetTableReference = new TableReference(
                            tableExpression,
                            null,
                            false,
                            sfi
                    );

                    final Predicate matchingIdsPredicate = null;//new InSubQueryPredicate();
//                        matchingIdsPredicateProducer.produceRestriction(
//                                ids,
//                                entityDescriptor,
//                                targetTableReference,
//                                tableKeyColumnsVisitationSupplier,
//                                query
//                        );

                    final SqlAstDeleteTranslator sqlAstTranslator = sqlAstTranslatorFactory.buildDeleteTranslator( sfi );
                    final JdbcDelete jdbcOperation = sqlAstTranslator.translate( new DeleteStatement( targetTableReference, matchingIdsPredicate ) );
                }
            );
            return deleteSqls;
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = ((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQuerySpec();
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();
        } else {
            throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
        }

        NavigablePath navigablePath = null;
        OUTER: for (SqmRoot<?> root : querySpec.getFromClause().getRoots()) {
            if (alias.equals(root.getExplicitAlias())) {
                navigablePath = root.getNavigablePath();
                break;
            }
            for (SqmJoin<?, ?> sqmJoin : root.getSqmJoins()) {
                if (alias.equals(sqmJoin.getExplicitAlias())) {
                    navigablePath = sqmJoin.getNavigablePath();
                    break OUTER;
                }
            }
        }

        CacheableSqmQuerySpecInterpretation interpretation = buildQuerySpecPlan(query);
        return interpretation.tableGroupAccess.findTableGroup(navigablePath).getPrimaryTableReference().getIdentificationVariable();
    }

    @Override
    public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = ((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQuerySpec();
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();
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
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();
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
    public List getResultList(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride) {
        return getResultList(serviceProvider, participatingQueries, query, sqlOverride, query.unwrap(QuerySqmImpl.class));
    }

    private List getResultList(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, ExecutionContext executionContext) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SessionFactoryImplementor sessionFactory = hqlQuery.getSessionFactory();

        RowTransformer rowTransformer = getField(new ConcreteSqmSelectQueryPlan((SqmSelectStatement) hqlQuery.getSqmStatement(), hqlQuery.getDomainParameterXref(), hqlQuery.getResultType(), hqlQuery.getQueryOptions()), "rowTransformer");

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcOperation jdbcOperation = getJdbcOperation(sessionFactory, interpretation);
            parameterBinders.addAll(jdbcOperation.getParameterBinders());
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            final Map<QueryParameterImplementor<?>, Map<SqmParameter, List<JdbcParameter>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                    interpretation.domainParameterXref,
                    interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
            );

            final JdbcParameterBindings tempJdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                    participatingQuery.unwrap(QuerySqmImpl.class).getQueryParameterBindings(),
                    interpretation.domainParameterXref,
                    jdbcParamsXref,
                    session.getFactory().getDomainModel(),
                    interpretation.tableGroupAccess::findTableGroup,
                    session
            );
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        SqmTranslation sqmTranslation = interpretation.getSqmTranslation();
        final JdbcSelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator( sessionFactory ).translate((SelectStatement) sqmTranslation.getSqlAst());
        final JdbcSelect realJdbcSelect = new JdbcSelect(
                sqlOverride,
                parameterBinders,
                jdbcSelect.getJdbcValuesMappingProducer(),
                affectedTableNames
        );

        try {
            return session.getFactory().getJdbcServices().getJdbcSelectExecutor().list(
                    realJdbcSelect,
                    jdbcParameterBindings,
                    executionContext,
                    rowTransformer,
                    false
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getSingleResult(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride) {
        final List list = getResultList(serviceProvider, participatingQueries, query, sqlOverride);
        if ( list.size() == 0 ) {
            throw new NoResultException( "No entity found for query" );
        }
        return uniqueElement( list );
    }

    private static <R> R uniqueElement(List<R> list) throws NonUniqueResultException {
        int size = list.size();
        if ( size == 0 ) {
            return null;
        }
        R first = list.get( 0 );
        for ( int i = 1; i < size; i++ ) {
            if ( list.get( i ) != first ) {
                throw new NonUniqueResultException( list.size() );
            }
        }
        return first;
    }

    @Override
    public int executeUpdate(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query query, String finalSql) {
        EntityManager em = serviceProvider.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmSelectStatement<?> selectStatement = (SqmSelectStatement<?>) hqlQuery.getSqmStatement();
        if (selectStatement.getSelection() instanceof SqmFunction<?>) {
            List results = getResultList(serviceProvider, participatingQueries, query, finalSql);
            if (results.size() != 1) {
                throw new IllegalArgumentException("Expected size 1 but was: " + results.size());
            }

            Number count = (Number) results.get(0);
            return count.intValue();
        } else {
            ReturningResult<Object[]> returningResult = executeReturning(serviceProvider, participatingQueries, baseQuery, query, finalSql);
            return returningResult.getUpdateCount();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<Object[]> executeReturning(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query modificationBaseQuery, Query exampleQuery, String sqlOverride) {
        DbmsDialect dbmsDialect = serviceProvider.getService(DbmsDialect.class);
        EntityManager em = serviceProvider.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
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

        int[] returningColumnTypes = dbmsDialect.needsReturningSqlTypes() ? getReturningColumnTypes(exampleQueryJdbcOperation, sfi) : null;
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

            try {
//                for (String delete : originalDeletes) {
//                    hibernateAccess.doExecute(executor, delete, queryParameters, session, queryParametersEntry.specifications);
//                }
                results = getResultList(serviceProvider, participatingQueries, exampleQuery, finalSql, hibernateAccess.wrapExecutionContext(exampleQuery, dbmsDialect, returningColumnTypes, returningResult));
//                results = hibernateAccess.list(queryLoader, wrapSession(session, dbmsDialect, returningColumns, returningColumnTypes, returningResult), queryParameters);
                success = true;
            } catch (HibernateException e) {
                LOG.severe("Could not execute the following SQL query: " + sqlOverride);
                if (session.getFactory().getSessionFactoryOptions().isJpaBootstrap()) {
                    throw session.getExceptionConverter().convert(e);
                } else {
                    throw e;
                }
            } finally {
//                hibernateAccess.afterTransaction(session, success);
            }
            /*
             * NATIVE LIST END
             */

            returningResult.setResultList(results);
            return returningResult;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
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
            sqlTypes.add(jdbcMapping.getSqlTypeDescriptor().getSqlType());
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
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            SelectQueryPlan<?> selectQueryPlan = invokeMethod(query,"resolveSelectQueryPlan");
            if (selectQueryPlan instanceof AggregatedSelectQueryPlanImpl<?>) {
                // TODO: have to handle multiple sql strings which happens when having e.g. a polymorphic UPDATE/DELETE
                selectQueryPlan = ((SelectQueryPlan<?>[]) getField(selectQueryPlan, "aggregatedQueryPlans"))[0];
                //                throw new IllegalArgumentException("No support for multiple translators yet!");
            }
            ConcreteSqmSelectQueryPlan<?> plan = (ConcreteSqmSelectQueryPlan<?>) selectQueryPlan;
            Object cacheableSqmInterpretation = invokeMethod(plan, "resolveCacheableSqmInterpretation", query);
            JdbcSelect jdbcSelect = getField(cacheableSqmInterpretation, "jdbcSelect");
            return jdbcSelect;
        }
        return getJdbcOperation(factory, buildQueryPlan(query));
    }

    private JdbcOperation getJdbcOperation(SessionFactoryImplementor factory, CacheableSqmInterpretation interpretation) {
        if (interpretation.getSqmTranslation() instanceof SqmSelectTranslation) {
            final SqmSelectTranslation sqmInterpretation = (SqmSelectTranslation) interpretation.getSqmTranslation();
            final JdbcEnvironment jdbcEnvironment = factory.getJdbcServices().getJdbcEnvironment();
            final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();
            final SqlAstSelectTranslator sqlAstTranslator = sqlAstTranslatorFactory.buildSelectTranslator( factory );

            return sqlAstTranslator.translate( sqmInterpretation.getSqlAst() );
        } else {
            if (interpretation.getSqmTranslation() instanceof SimpleSqmDeleteTranslation) {
                final SimpleSqmDeleteTranslation sqmInterpretation = (SimpleSqmDeleteTranslation) interpretation.getSqmTranslation();
                final JdbcEnvironment jdbcEnvironment = factory.getJdbcServices().getJdbcEnvironment();
                final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();
                final SqlAstDeleteTranslator sqlAstTranslator = sqlAstTranslatorFactory.buildDeleteTranslator( factory );

                return sqlAstTranslator.translate( sqmInterpretation.getSqlAst() );
            } else if (interpretation.getSqmTranslation() instanceof SimpleSqmUpdateTranslation) {
                final SimpleSqmUpdateTranslation sqmInterpretation = (SimpleSqmUpdateTranslation) interpretation.getSqmTranslation();
                final JdbcEnvironment jdbcEnvironment = factory.getJdbcServices().getJdbcEnvironment();
                final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();
                final SqlAstUpdateTranslator sqlAstTranslator = sqlAstTranslatorFactory.buildUpdateTranslator( factory );

                return sqlAstTranslator.translate( sqmInterpretation.getSqlAst() );
            } else if (interpretation.getSqmTranslation() instanceof MultiTableDeleteQueryPlan) {

            } else if (interpretation.getSqmTranslation() instanceof MultiTableUpdateQueryPlan) {

            }
            throw new UnsupportedOperationException();
        }

    }

    private static CacheableSqmInterpretation buildQueryPlan(Query query) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            final SharedSessionContractImplementor session = hqlQuery.getSession();
            final SessionFactoryImplementor sessionFactory = session.getFactory();
            final QueryEngine queryEngine = sessionFactory.getQueryEngine();

            final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

            final SqmSelectTranslator sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory
            );

            final FromClauseAccess tableGroupAccess = sqmConverter.getFromClauseAccess();
            final SqmSelectTranslation interpretation = sqmConverter.translate((SqmSelectStatement) hqlQuery.getSqmStatement());

            return new CacheableSqmInterpretation( interpretation, tableGroupAccess, hqlQuery.getDomainParameterXref() );
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();
        } else {
            NonSelectQueryPlan nonSelectQueryPlan = invokeMethod(hqlQuery, "resolveNonSelectQueryPlan");
            if (nonSelectQueryPlan instanceof SimpleDeleteQueryPlan) {
                SqmDeleteStatement<?> sqmUpdate = getField(nonSelectQueryPlan, "sqmDelete");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
                final QueryEngine queryEngine = factory.getQueryEngine();

                final SqmTranslatorFactory translatorFactory = queryEngine.getSqmTranslatorFactory();
                final SimpleSqmDeleteTranslator translator = translatorFactory.createSimpleDeleteTranslator(
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory
                );

                final SimpleSqmDeleteTranslation sqmInterpretation = translator.translate(sqmUpdate);
                return new CacheableSqmInterpretation( sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref() );
            } else if (nonSelectQueryPlan instanceof SimpleUpdateQueryPlan) {
                SqmUpdateStatement<?> sqmUpdate = getField(nonSelectQueryPlan, "sqmUpdate");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
                final QueryEngine queryEngine = factory.getQueryEngine();

                final SqmTranslatorFactory translatorFactory = queryEngine.getSqmTranslatorFactory();
                final SimpleSqmUpdateTranslator translator = translatorFactory.createSimpleUpdateTranslator(
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory
                );

                final SimpleSqmUpdateTranslation sqmInterpretation = translator.translate(sqmUpdate);
                return new CacheableSqmInterpretation( sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref() );
            } else if (nonSelectQueryPlan instanceof MultiTableDeleteQueryPlan) {

            } else if (nonSelectQueryPlan instanceof MultiTableUpdateQueryPlan) {

            }
        }
        throw new UnsupportedOperationException("not yet implemented");
    }

    private static CacheableSqmQuerySpecInterpretation buildQuerySpecPlan(Query query) {
        QuerySqmImpl<?> hqlQuery = query.unwrap(QuerySqmImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            querySpec = ((SqmSelectStatement<?>) hqlQuery.getSqmStatement()).getQuerySpec();
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();
        } else {
            throw new IllegalArgumentException("Only supported for insert or select!");
        }
        return buildCacheableSqmInterpretation(querySpec, hqlQuery.getDomainParameterXref(), hqlQuery);
    }

    private static CacheableSqmInterpretation buildCacheableSqmInterpretation(
            SqmSelectStatement sqm,
            DomainParameterXref domainParameterXref, ExecutionContext executionContext) {
        final SharedSessionContractImplementor session = executionContext.getSession();
        final SessionFactoryImplementor sessionFactory = session.getFactory();
        final QueryEngine queryEngine = sessionFactory.getQueryEngine();

        final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

        final SqmSelectTranslator sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                executionContext.getQueryOptions(),
                domainParameterXref,
                executionContext.getQueryParameterBindings(),
                executionContext.getLoadQueryInfluencers(),
                sessionFactory
        );

        final FromClauseAccess tableGroupAccess = sqmConverter.getFromClauseAccess();

        final SqmSelectTranslation interpretation = sqmConverter.translate( sqm );

        return new CacheableSqmInterpretation( interpretation, tableGroupAccess, domainParameterXref );
    }

    private static CacheableSqmQuerySpecInterpretation buildCacheableSqmInterpretation(
            SqmQuerySpec<?> sqm,
            DomainParameterXref domainParameterXref, ExecutionContext executionContext) {
        final SharedSessionContractImplementor session = executionContext.getSession();
        final SessionFactoryImplementor sessionFactory = session.getFactory();
        final QueryEngine queryEngine = sessionFactory.getQueryEngine();

        final SqmTranslatorFactory sqmTranslatorFactory = queryEngine.getSqmTranslatorFactory();

        final SqmSelectTranslator sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                executionContext.getQueryOptions(),
                domainParameterXref,
                executionContext.getQueryParameterBindings(),
                executionContext.getLoadQueryInfluencers(),
                sessionFactory
        );

        final FromClauseAccess tableGroupAccess = sqmConverter.getFromClauseAccess();

        final SqmQuerySpecTranslation interpretation = sqmConverter.translate( sqm );

        return new CacheableSqmQuerySpecInterpretation( interpretation, tableGroupAccess, domainParameterXref );
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class CacheableSqmInterpretation {
        private final SqmTranslation sqmTranslation;
        private final FromClauseAccess tableGroupAccess;
        private final DomainParameterXref domainParameterXref;

        CacheableSqmInterpretation(
                SqmTranslation sqmTranslation,
                FromClauseAccess tableGroupAccess,
                DomainParameterXref domainParameterXref) {
            this.sqmTranslation = sqmTranslation;
            this.tableGroupAccess = tableGroupAccess;
            this.domainParameterXref = domainParameterXref;
        }

        SqmTranslation getSqmTranslation() {
            return sqmTranslation;
        }

        FromClauseAccess getTableGroupAccess() {
            return tableGroupAccess;
        }

        DomainParameterXref getDomainParameterXref() {
            return domainParameterXref;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class CacheableSqmQuerySpecInterpretation {
        private final SqmQuerySpecTranslation sqmTranslation;
        private final FromClauseAccess tableGroupAccess;
        private final DomainParameterXref domainParameterXref;

        CacheableSqmQuerySpecInterpretation(
                SqmQuerySpecTranslation sqmTranslation,
                FromClauseAccess tableGroupAccess,
                DomainParameterXref domainParameterXref) {
            this.sqmTranslation = sqmTranslation;
            this.tableGroupAccess = tableGroupAccess;
            this.domainParameterXref = domainParameterXref;
        }

        SqmQuerySpecTranslation getSqmTranslation() {
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
