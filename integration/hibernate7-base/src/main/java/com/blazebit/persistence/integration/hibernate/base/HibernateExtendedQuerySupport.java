/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsStatementType;
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
import org.hibernate.ScrollMode;
import org.hibernate.action.internal.BulkOperationCleanupAction;
import org.hibernate.dialect.DmlTargetColumnQualifierSupport;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.MutableObject;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.ForeignKeyDescriptor;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.internal.EmbeddedAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.TupleTransformer;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.internal.ScrollableResultsIterator;
import org.hibernate.query.spi.DomainQueryExecutionContext;
import org.hibernate.query.spi.Limit;
import org.hibernate.query.spi.NonSelectQueryPlan;
import org.hibernate.query.spi.QueryInterpretationCache;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.spi.QueryPlan;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.MultiTableDeleteQueryPlan;
import org.hibernate.query.sqm.internal.MultiTableUpdateQueryPlan;
import org.hibernate.query.sqm.internal.SqmQueryImpl;
import org.hibernate.query.sqm.internal.SimpleDeleteQueryPlan;
import org.hibernate.query.sqm.internal.SimpleNonSelectQueryPlan;
import org.hibernate.query.sqm.internal.SqmInterpretationsKey;
import org.hibernate.query.sqm.internal.SqmJdbcExecutionContextAdapter;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.spi.SqmParameterMappingModelResolutionAccess;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.SqmTranslator;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;
import org.hibernate.query.sqm.tree.SqmDmlStatement;
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
import org.hibernate.sql.ast.tree.MutationStatement;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.delete.DeleteStatement;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.CollectionTableGroup;
import org.hibernate.sql.ast.tree.from.LazyTableGroup;
import org.hibernate.sql.ast.tree.from.MutatingTableReferenceGroupWrapper;
import org.hibernate.sql.ast.tree.from.NamedTableReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.insert.InsertSelectStatement;
import org.hibernate.sql.ast.tree.insert.InsertStatement;
import org.hibernate.sql.ast.tree.predicate.InSubQueryPredicate;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.ast.tree.select.QueryGroup;
import org.hibernate.sql.ast.tree.select.QueryPart;
import org.hibernate.sql.ast.tree.select.QuerySpec;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.ast.tree.update.UpdateStatement;
import org.hibernate.sql.exec.internal.JdbcParameterBindingsImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcOperationQuery;
import org.hibernate.sql.exec.spi.JdbcOperationQueryMutation;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcParametersList;
import org.hibernate.sql.results.internal.RowTransformerJpaTupleImpl;
import org.hibernate.sql.results.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.results.internal.RowTransformerStandardImpl;
import org.hibernate.sql.results.internal.RowTransformerTupleTransformerAdapter;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.internal.TupleMetadata;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;
import org.hibernate.type.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());
    private static final String[] KNOWN_STATEMENTS = { "select ", "insert ", "update ", "delete " };

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
        Limit limit = query.unwrap(SqmQueryImpl.class).getQueryOptions().getLimit();
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
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        SessionFactoryImplementor factory = hqlQuery.getSessionFactory();
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        return getJdbcOperation(factory, interpretation, hqlQuery).getSqlString();
    }

    @Override
    public boolean getSqlContainsLimit() {
        return true;
    }

    @Override
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        if (hqlQuery.getSqmStatement() instanceof SqmDeleteStatement<?>) {
            List<JdbcOperationQueryMutation> deletes = getCollectionTableDeletes( hqlQuery ).deletes;

            List<String> deleteSqls = new ArrayList<>( deletes.size() );
            for ( JdbcOperationQueryMutation delete : deletes ) {
                deleteSqls.add( delete.getSqlString() );
            }
            return deleteSqls;
        }

        return Collections.EMPTY_LIST;
    }

    private static class CollectionTableDeleteInfo {
        private final List<JdbcOperationQueryMutation> deletes;
        private final JdbcParameterBindings parameterBindings;

        public CollectionTableDeleteInfo(
                List<JdbcOperationQueryMutation> deletes,
                JdbcParameterBindings parameterBindings) {
            this.deletes = deletes;
            this.parameterBindings = parameterBindings;
        }
    }

    private static CollectionTableDeleteInfo getCollectionTableDeletes(SqmQueryImpl<?> hqlQuery) {
        SessionFactoryImplementor sfi = hqlQuery.getSessionFactory();
        SqmDeleteStatement<?> deleteStatement = (SqmDeleteStatement<?>) hqlQuery.getSqmStatement();
        String mutatingEntityName = deleteStatement.getTarget().getModel().getHibernateEntityName();
        EntityMappingType entityDescriptor = sfi.getMappingMetamodel().getEntityDescriptor(mutatingEntityName);

        CacheableSqmInterpretation sqmInterpretation = buildQueryPlan( hqlQuery );

        Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                hqlQuery.getDomainParameterXref(),
                sqmInterpretation.getSqmTranslation()::getJdbcParamsBySqmParam
        );

        final JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                hqlQuery.getQueryParameterBindings(),
                hqlQuery.getDomainParameterXref(),
                jdbcParamsXref,
                new SqmParameterMappingModelResolutionAccess() {
                    @Override @SuppressWarnings("unchecked")
                    public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                        return (MappingModelExpressible<T>) sqmInterpretation.getSqmTranslation().getSqmParameterMappingModelTypeResolutions().get(parameter);
                    }
                },
                hqlQuery.getSession()
        );
        hqlQuery.getDomainParameterXref().clearExpansions();
        DeleteStatement sqlAst = (DeleteStatement) sqmInterpretation.getSqmTranslation().getSqlAst();
        final boolean missingRestriction = sqlAst.getRestriction() == null;
        return new CollectionTableDeleteInfo( getCollectionTableDeletes(
                entityDescriptor,
                (tableReference, attributeMapping) -> {
                    final TableGroup collectionTableGroup = new MutatingTableReferenceGroupWrapper(
                            new NavigablePath( attributeMapping.getRootPathName() ),
                            attributeMapping,
                            (NamedTableReference) tableReference
                    );

                    final MutableObject<Predicate> additionalPredicate = new MutableObject<>();
                    attributeMapping.getCollectionDescriptor().applyBaseRestrictions(
                            p -> additionalPredicate.set( Predicate.combinePredicates( additionalPredicate.get(), p ) ),
                            collectionTableGroup,
                            sfi.getJdbcServices().getDialect().getDmlTargetColumnQualifierSupport() == DmlTargetColumnQualifierSupport.TABLE_ALIAS,
                            hqlQuery.getSession().getLoadQueryInfluencers().getEnabledFilters(),
                            null,
                            null
                    );

                    if ( missingRestriction ) {
                        return additionalPredicate.get();
                    }

                    final ForeignKeyDescriptor fkDescriptor = attributeMapping.getKeyDescriptor();
                    final Expression fkColumnExpression = MappingModelCreationHelper.buildColumnReferenceExpression(
                            collectionTableGroup,
                            fkDescriptor.getKeyPart(),
                            null,
                            sfi
                    );

                    final QuerySpec matchingIdSubQuery = new QuerySpec( false );

                    final MutatingTableReferenceGroupWrapper tableGroup = new MutatingTableReferenceGroupWrapper(
                            new NavigablePath( attributeMapping.getRootPathName() ),
                            attributeMapping,
                            sqlAst.getTargetTable()
                    );
                    final Expression fkTargetColumnExpression = MappingModelCreationHelper.buildColumnReferenceExpression(
                            tableGroup,
                            fkDescriptor.getTargetPart(),
                            sqmInterpretation.getSqmTranslation().getSqlExpressionResolver(),
                            sfi
                    );
                    matchingIdSubQuery.getSelectClause().addSqlSelection( new SqlSelectionImpl( 0, fkTargetColumnExpression ) );

                    matchingIdSubQuery.getFromClause().addRoot(
                            tableGroup
                    );

                    matchingIdSubQuery.applyPredicate( sqlAst.getRestriction() );

                    return Predicate.combinePredicates(
                            additionalPredicate.get(),
                            new InSubQueryPredicate( fkColumnExpression, matchingIdSubQuery, false )
                    );
                },
                jdbcParameterBindings,
                SqmJdbcExecutionContextAdapter.usingLockingAndPaging( hqlQuery )
        ), jdbcParameterBindings);
    }

    private static List<JdbcOperationQueryMutation> getCollectionTableDeletes(
            EntityMappingType entityDescriptor,
            BiFunction<TableReference, PluralAttributeMapping, Predicate> restrictionProducer,
            JdbcParameterBindings jdbcParameterBindings,
            ExecutionContext executionContext) {
        List<PluralAttributeMapping> pluralAttributeMappings = collectCollectionTables( entityDescriptor );
        List<JdbcOperationQueryMutation> deletes = new ArrayList<>( pluralAttributeMappings.size() );
        for ( PluralAttributeMapping attributeMapping : pluralAttributeMappings ) {
            final String separateCollectionTable = attributeMapping.getSeparateCollectionTable();

            final SessionFactoryImplementor sessionFactory = executionContext.getSession().getFactory();
            final JdbcServices jdbcServices = sessionFactory.getJdbcServices();

            final NamedTableReference tableReference = new NamedTableReference(
                    separateCollectionTable,
                    DeleteStatement.DEFAULT_ALIAS,
                    true
            );

            final DeleteStatement sqlAstDelete = new DeleteStatement(
                    tableReference,
                    restrictionProducer.apply( tableReference, attributeMapping )
            );

            deletes.add( jdbcServices.getJdbcEnvironment()
                                 .getSqlAstTranslatorFactory()
                                 .buildMutationTranslator( sessionFactory, sqlAstDelete )
                                 .translate( jdbcParameterBindings, executionContext.getQueryOptions() )
            );
        }
        return deletes;
    }

    private static List<PluralAttributeMapping> collectCollectionTables(EntityMappingType entityDescriptor) {
        List<PluralAttributeMapping> pluralAttributeMappings = new ArrayList<>();
        collectCollectionTables( entityDescriptor, pluralAttributeMappings );
        return pluralAttributeMappings;
    }

    private static void collectCollectionTables(EntityMappingType entityDescriptor, List<PluralAttributeMapping> pluralAttributeMappings) {
        if ( ! entityDescriptor.getEntityPersister().hasCollections() ) {
            // none to clean-up
            return;
        }

        entityDescriptor.visitSubTypeAttributeMappings(
                attributeMapping -> {
                    if ( attributeMapping instanceof PluralAttributeMapping ) {
                        PluralAttributeMapping pluralAttributeMapping = (PluralAttributeMapping) attributeMapping;
                        if (pluralAttributeMapping.getSeparateCollectionTable() != null) {
                            pluralAttributeMappings.add( pluralAttributeMapping );
                        }
                    } else if ( attributeMapping instanceof EmbeddedAttributeMapping ) {
                        collectCollectionTables(
                                (EmbeddedAttributeMapping) attributeMapping,
                                pluralAttributeMappings
                        );
                    }
                }
        );
    }

    private static void collectCollectionTables(EmbeddedAttributeMapping attributeMapping, List<PluralAttributeMapping> pluralAttributeMappings) {
        attributeMapping.visitSubParts(
                modelPart -> {
                    if ( modelPart instanceof PluralAttributeMapping ) {
                        PluralAttributeMapping pluralAttributeMapping = (PluralAttributeMapping) attributeMapping;
                        if (pluralAttributeMapping.getSeparateCollectionTable() != null) {
                            pluralAttributeMappings.add( pluralAttributeMapping );
                        }
                    } else if ( modelPart instanceof EmbeddedAttributeMapping ) {
                        collectCollectionTables(
                                (EmbeddedAttributeMapping) modelPart,
                                pluralAttributeMappings
                        );
                    }
                },
                null
        );
    }

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias, int queryPartNumber) {
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
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
        interpretation.domainParameterXref.clearExpansions();
        TableGroup tableGroup = getTableGroup(interpretation, navigablePath);
        return tableGroup.getPrimaryTableReference().getIdentificationVariable();
    }

    @Override
    public SqlFromInfo getSqlFromInfo(EntityManager em, Query query, String alias, int queryPartNumber) {
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
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
        String sql = getJdbcOperation(sfi, interpretation, hqlQuery).getSqlString();
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
                Object result = getQuerySpec(queryParts.get(i), currentNumber + offset, queryPartNumber);
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
            } else if (sqlAst instanceof InsertSelectStatement) {
                tableGroup = findTableGroup(((InsertSelectStatement) sqlAst).getSourceSelectStatement(), navigablePath);
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
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
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

        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
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
            EntityPersister entityPersister = hqlQuery.getSessionFactory().getMappingMetamodel().getEntityDescriptor(root.getEntityName());
            int propertyIndex = entityPersister.getPropertyIndex(expression);
            Type[] propertyTypes = entityPersister.getPropertyTypes();
            for (int j = 0; j < propertyIndex; j++) {
                position += propertyTypes[j].getColumnSpan(hqlQuery.getSessionFactory().getRuntimeMetamodels());
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
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        SessionFactoryImplementor sessionFactory = hqlQuery.getSessionFactory();

        RowTransformer<?> rowTransformer = determineRowTransformer((SqmSelectStatement<?>) hqlQuery.getSqmStatement(), hqlQuery.getResultType(), hqlQuery.getQueryOptions());

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcTranslation translation = getJdbcTranslation(sessionFactory, interpretation, participatingQuery.unwrap(SqmQueryImpl.class));
            JdbcOperationQuery jdbcOperation = translation.query;
            if (query == participatingQuery) {
                // Don't copy over the limit and offset parameters because we need to use the LimitHandler for now
                for (JdbcParameterBinder parameterBinder : jdbcOperation.getParameterBinders()) {
                    if (parameterBinder != hibernateAccess.getLimitParameter(jdbcOperation) && parameterBinder != hibernateAccess.getOffsetParameter(jdbcOperation)) {
                        parameterBinders.add(parameterBinder);
                    }
                }
            } else {
                parameterBinders.addAll(jdbcOperation.getParameterBinders());
            }
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            final JdbcParameterBindings tempJdbcParameterBindings = translation.parameterBindings;
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        // todo: avoid double translation
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcOperationQuery jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                .translate(jdbcParameterBindings, executionContext.getQueryOptions());
        final JdbcOperationQuery realJdbcSelect = hibernateAccess.createJdbcSelect(
                sqlOverride,
                parameterBinders,
                jdbcSelect,
                affectedTableNames
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

        session.autoFlushIfRequired(affectedTableNames);

        try {
            return hibernateAccess.list(
                    realJdbcSelect,
                    jdbcParameterBindings,
                    hibernateAccess.createExecutionContextAdapter(executionContext, jdbcSelect),
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
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        SessionFactoryImplementor sessionFactory = hqlQuery.getSessionFactory();

        RowTransformer<?> rowTransformer = determineRowTransformer((SqmSelectStatement<?>) hqlQuery.getSqmStatement(), hqlQuery.getResultType(), hqlQuery.getQueryOptions());

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
        final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
        final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

        List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
        Set<String> affectedTableNames = new HashSet<>();
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcTranslation translation = getJdbcTranslation(sessionFactory, interpretation, participatingQuery.unwrap(SqmQueryImpl.class));
            JdbcOperationQuery jdbcOperation = translation.query;
            parameterBinders.addAll(jdbcOperation.getParameterBinders());
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            final JdbcParameterBindings tempJdbcParameterBindings = translation.parameterBindings;
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcOperationQuery jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                .translate(jdbcParameterBindings, executionContext.getQueryOptions());
        final JdbcOperationQuery realJdbcSelect = hibernateAccess.createJdbcSelect(
                sqlOverride,
                parameterBinders,
                jdbcSelect,
                affectedTableNames
        );

        session.autoFlushIfRequired(realJdbcSelect.getAffectedTableNames());

        try {
            ScrollableResultsImplementor<?> scrollableResults = hibernateAccess.scroll(
                    realJdbcSelect,
                    ScrollMode.FORWARD_ONLY,
                    jdbcParameterBindings,
                    hibernateAccess.createExecutionContextAdapter(executionContext, realJdbcSelect),
                    rowTransformer
            );
            ScrollableResultsIterator iterator = new ScrollableResultsIterator<>(scrollableResults);
            Spliterator spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL);
            Stream stream = StreamSupport.stream(spliterator, false);
            return stream.onClose(scrollableResults::close);
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
                return (RowTransformer<R>) new RowTransformerJpaTupleImpl(buildTupleMetadata(selections));
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

    private TupleMetadata buildTupleMetadata(List<SqmSelection<?>> selections) {
        return new TupleMetadata( buildTupleElementArray(selections), buildTupleAliasArray(selections) );
    }

    private static TupleElement<?>[] buildTupleElementArray(List<SqmSelection<?>> selections) {
        if (selections.size() == 1) {
            final SqmSelectableNode<?> selectableNode = selections.get(0).getSelectableNode();
            if (selectableNode instanceof CompoundSelection<?>) {
                final List<? extends JpaSelection<?>> selectionItems = selectableNode.getSelectionItems();
                final TupleElement<?>[] elements = new TupleElement<?>[selectionItems.size()];
                for (int i = 0; i < selectionItems.size(); i++) {
                    elements[i] = selectionItems.get(i);
                }
                return elements;
            } else {
                return new TupleElement<?>[]{selectableNode};
            }
        } else {
            final TupleElement<?>[] elements = new TupleElement<?>[selections.size()];
            for (int i = 0; i < selections.size(); i++) {
                elements[i] = selections.get(i).getSelectableNode();
            }
            return elements;
        }
    }

    private static String[] buildTupleAliasArray(List<SqmSelection<?>> selections) {
        if (selections.size() == 1) {
            final SqmSelectableNode<?> selectableNode = selections.get(0).getSelectableNode();
            if (selectableNode instanceof CompoundSelection<?>) {
                final List<? extends JpaSelection<?>> selectionItems = selectableNode.getSelectionItems();
                final String[] elements = new String[selectionItems.size()];
                for (int i = 0; i < selectionItems.size(); i++) {
                    elements[i] = selectionItems.get(i).getAlias();
                }
                return elements;
            } else {
                return new String[]{selectableNode.getAlias()};
            }
        } else {
            final String[] elements = new String[selections.size()];
            for (int i = 0; i < selections.size(); i++) {
                elements[i] = selections.get(i).getAlias();
            }
            return elements;
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
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcTranslation translation = getJdbcTranslation(sessionFactory, interpretation, participatingQuery.unwrap(SqmQueryImpl.class));
            JdbcOperationQuery jdbcOperation = translation.query;
            parameterBinders.addAll(jdbcOperation.getParameterBinders());
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());
            final JdbcParameterBindings tempJdbcParameterBindings = translation.parameterBindings;
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        SqmStatement<?> sqmStatement = hqlQuery.getSqmStatement();
        CacheableSqmInterpretation interpretation = buildQueryPlan(query);
        final JdbcOperationQueryMutation realJdbcStatement;
        if (sqmStatement instanceof SqmUpdateStatement<?>) {
//            final JdbcUpdate jdbcUpdate = sqlAstTranslatorFactory.buildUpdateTranslator(sessionFactory, (UpdateStatement) interpretation.getSqmTranslation().getSqlAst())
//                    .translate(jdbcParameterBindings, executionContext.getQueryOptions());
            realJdbcStatement = hibernateAccess.createJdbcUpdate(
                    finalSql,
                    parameterBinders,
                    affectedTableNames
            );
        } else if (sqmStatement instanceof SqmDeleteStatement<?>) {
            realJdbcStatement = hibernateAccess.createJdbcUpdate(
                    finalSql,
                    parameterBinders,
                    affectedTableNames
            );
        } else if (sqmStatement instanceof SqmInsertSelectStatement<?>) {
            realJdbcStatement = hibernateAccess.createJdbcUpdate(
                    finalSql,
                    parameterBinders,
                    affectedTableNames
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
        final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl(0);
        for (Query participatingQuery : participatingQueries) {
            CacheableSqmInterpretation interpretation = buildQueryPlan(participatingQuery);
            JdbcTranslation translation = getJdbcTranslation(sessionFactory, interpretation, participatingQuery.unwrap(SqmQueryImpl.class));
            JdbcOperationQuery jdbcOperation = translation.query;
            // Exclude limit/offset parameters from example query
            if (participatingQuery != exampleQuery) {
                parameterBinders.addAll(jdbcOperation.getParameterBinders());
            }
            affectedTableNames.addAll(jdbcOperation.getAffectedTableNames());

            final JdbcParameterBindings tempJdbcParameterBindings = translation.parameterBindings;
            if (!tempJdbcParameterBindings.getBindings().isEmpty()) {
                tempJdbcParameterBindings.visitBindings(jdbcParameterBindings::addBinding);
            }
        }

        // Create plan for example query
        JdbcOperationQuery exampleQueryJdbcOperation = getJdbcOperation(exampleQuery);

        StringBuilder sqlSb = new StringBuilder(sqlOverride.length() + 100);
        sqlSb.append(sqlOverride);

        int[] returningColumnTypes = dbmsDialect.needsReturningSqlTypes() ? hibernateAccess.getReturningColumnTypes(exampleQueryJdbcOperation, sessionFactory) : null;
        boolean caseInsensitive = !Boolean.valueOf(serviceProvider.getService(ConfigurationSource.class).getProperty(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE));
        String[][] returningColumns = getReturningColumns(caseInsensitive, exampleQueryJdbcOperation.getSqlString());
        String finalSql = sqlSb.toString();

        try {
            HibernateReturningResult<Object[]> returningResult = new HibernateReturningResult<Object[]>();

            // Do the native list operation with custom session and combined parameters

            /*
             * NATIVE LIST START
             */
            List<Object[]> results = Collections.EMPTY_LIST;
            boolean success = false;

            // todo: avoid double translation
            CacheableSqmInterpretation interpretation = buildQueryPlan(exampleQuery);
            DomainQueryExecutionContext domainQueryExecutionContext = exampleQuery.unwrap(DomainQueryExecutionContext.class);
            final JdbcOperationQuery jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator(sessionFactory, (SelectStatement) interpretation.getSqmTranslation().getSqlAst())
                    .translate(jdbcParameterBindings, domainQueryExecutionContext.getQueryOptions());
            final JdbcOperationQuery realJdbcSelect = hibernateAccess.createFullJdbcSelect(
                    finalSql,
                    parameterBinders,
                    jdbcSelect,
                    affectedTableNames
            );
            ExecutionContext executionContext = hibernateAccess.createExecutionContextAdapter(domainQueryExecutionContext, realJdbcSelect);

            session.autoFlushIfRequired(realJdbcSelect.getAffectedTableNames());

            try {
                if (modificationBaseQuery != null) {
                    SqmQueryImpl<?> querySqm = modificationBaseQuery.unwrap(SqmQueryImpl.class);
                    SqmStatement<?> modificationSqmStatement = querySqm.getSqmStatement();
                    if (modificationSqmStatement instanceof SqmDmlStatement<?>) {
                        SqmDmlStatement<?> sqmDmlStatement = (SqmDmlStatement<?>) modificationSqmStatement;
                        BulkOperationCleanupAction.schedule(executionContext.getSession(), sqmDmlStatement);
                        if (sqmDmlStatement instanceof SqmDeleteStatement<?> && !dbmsDialect.supportsModificationQueryInWithClause()) {
                            CollectionTableDeleteInfo collectionTableDeletes = getCollectionTableDeletes(querySqm);
                            List<JdbcOperationQueryMutation> deletes;
                            int withIndex;
                            if ((withIndex = finalSql.indexOf("with ")) != -1) {
                                int end = getCTEEnd(finalSql, withIndex);

                                int maxLength = 0;

                                for (JdbcOperationQueryMutation delete : collectionTableDeletes.deletes) {
                                    maxLength = Math.max(maxLength, delete.getSqlString().length());
                                }

                                deletes = new ArrayList<>(collectionTableDeletes.deletes.size());
                                StringBuilder newSb = new StringBuilder(end + maxLength);
                                // Prefix properly with cte
                                StringBuilder withClauseSb = new StringBuilder(end - withIndex);
                                withClauseSb.append(finalSql, withIndex, end);

                                for (JdbcOperationQueryMutation delete : collectionTableDeletes.deletes) {
                                    // TODO: The strings should also receive the simple CTE name instead of the complex one
                                    newSb.append(delete.getSqlString());
                                    dbmsDialect.appendExtendedSql(newSb, DbmsStatementType.DELETE, false, false, withClauseSb, null, null, null, null, null);
                                    deletes.add(hibernateAccess.createJdbcDelete(delete, newSb));
                                    newSb.setLength(0);
                                }
                            } else {
                                deletes = collectionTableDeletes.deletes;
                            }

                            Function<String, PreparedStatement> statementCreator = sql -> session.getJdbcCoordinator()
                                    .getStatementPreparer()
                                    .prepareStatement(sql);
                            BiConsumer<Integer, PreparedStatement> expectationCheck = (integer, preparedStatement) -> { };
                            SqmJdbcExecutionContextAdapter executionContextAdapter = SqmJdbcExecutionContextAdapter.usingLockingAndPaging(
                                    modificationBaseQuery.unwrap(DomainQueryExecutionContext.class));
                            for (JdbcOperationQueryMutation delete : deletes) {
                                session.getFactory().getJdbcServices().getJdbcMutationExecutor().execute(
                                        delete,
                                        collectionTableDeletes.parameterBindings,
                                        statementCreator,
                                        expectationCheck,
                                        executionContextAdapter
                                );
                            }
                        }
                    }
                }

                results = hibernateAccess.list(
                        realJdbcSelect,
                        jdbcParameterBindings,
                        hibernateAccess.wrapExecutionContext(executionContext, dbmsDialect, returningColumns, returningColumnTypes, returningResult),
                        RowTransformerStandardImpl.instance(),
                        ListResultsConsumer.UniqueSemantic.FILTER
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
            /*
             * NATIVE LIST END
             */

            returningResult.setResultList(results);
            return returningResult;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private int getCTEEnd(String sql, int start) {
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;
        boolean started = false;

        int i = start;
        int end = sql.length();
        OUTER: while (i < end) {
            final char c = sql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    started = true;
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                } else if (started && parenthesis == 0 && c != ',' && !Character.isWhitespace(c)) {
                    for (String statementType : KNOWN_STATEMENTS) {
                        if (sql.regionMatches(true, i, statementType, 0, statementType.length())) {
                            break OUTER;
                        }
                    }
                }
            }

            i++;
        }

        return i;
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

    private JdbcOperationQuery getJdbcOperation(Query query) {
        SqmQueryImpl hqlQuery = query.unwrap(SqmQueryImpl.class);
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
        CacheableSqmInterpretation interpretation = buildQueryPlan( query );
        return getJdbcOperation( factory, interpretation, hqlQuery );
    }

    private JdbcOperationQuery getJdbcOperation(SessionFactoryImplementor factory, CacheableSqmInterpretation interpretation, SqmQueryImpl<?> query) {
        return getJdbcTranslation( factory, interpretation, query ).query;
    }

    private static class JdbcTranslation {
        private final JdbcOperationQuery query;
        private final JdbcParameterBindings parameterBindings;

        public JdbcTranslation(JdbcOperationQuery query, JdbcParameterBindings parameterBindings) {
            this.query = query;
            this.parameterBindings = parameterBindings;
        }
    }

    private JdbcTranslation getJdbcTranslation(SessionFactoryImplementor factory, CacheableSqmInterpretation interpretation, SqmQueryImpl<?> query) {
        JdbcEnvironment jdbcEnvironment = factory.getJdbcServices().getJdbcEnvironment();
        SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();
        SqmTranslation<?> sqmTranslation = interpretation.getSqmTranslation();
        Statement sqlAst = sqmTranslation.getSqlAst();
        final Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil.generateJdbcParamsXref(
                interpretation.domainParameterXref,
                interpretation.getSqmTranslation()::getJdbcParamsBySqmParam
        );

        final JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                query.getQueryParameterBindings(),
                interpretation.domainParameterXref,
                jdbcParamsXref,
                new SqmParameterMappingModelResolutionAccess() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                        return (MappingModelExpressible<T>) interpretation.sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                    }
                },
                query.getSession()
        );
        interpretation.domainParameterXref.clearExpansions();

        if (sqlAst instanceof SelectStatement) {
            SqlAstTranslator<? extends JdbcOperationQuery> translator = sqlAstTranslatorFactory.buildSelectTranslator(factory, (SelectStatement) sqlAst);
            return new JdbcTranslation(translator.translate(jdbcParameterBindings, query.getQueryOptions()), jdbcParameterBindings);
        } else if (sqlAst instanceof DeleteStatement) {
            SqlAstTranslator<? extends JdbcOperationQueryMutation> translator = sqlAstTranslatorFactory.buildMutationTranslator(factory, (DeleteStatement) sqlAst);
            return new JdbcTranslation(translator.translate(jdbcParameterBindings, query.getQueryOptions()), jdbcParameterBindings);
        } else if (sqlAst instanceof UpdateStatement) {
            SqlAstTranslator<? extends JdbcOperationQueryMutation> translator = sqlAstTranslatorFactory.buildMutationTranslator(factory, (UpdateStatement) sqlAst);
            return new JdbcTranslation(translator.translate(jdbcParameterBindings, query.getQueryOptions()), jdbcParameterBindings);
        } else if (sqlAst instanceof InsertStatement) {
            SqlAstTranslator<? extends JdbcOperationQueryMutation> translator = sqlAstTranslatorFactory.buildMutationTranslator(factory, (InsertStatement) sqlAst);
            return new JdbcTranslation(translator.translate(jdbcParameterBindings, query.getQueryOptions()), jdbcParameterBindings);
        }
        throw new UnsupportedOperationException();
    }

    private static CacheableSqmInterpretation buildQueryPlan(Query query) {
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);
        SqmQuerySpec<?> querySpec;
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            final SharedSessionContractImplementor session = hqlQuery.getSession();
            final SessionFactoryImplementor sessionFactory = session.getFactory();
            final QueryInterpretationCache.Key cacheKey = SqmInterpretationsKey.createInterpretationsKey(hqlQuery);

            final SqmTranslatorFactory sqmTranslatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(sessionFactory);

            final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                    (SqmSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory.getSqlTranslationEngine(),
                    false
            );

            final SqmTranslation<SelectStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            //            querySpec = ((SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement()).getSelectQuerySpec();

            final SharedSessionContractImplementor session = hqlQuery.getSession();
            final SessionFactoryImplementor sessionFactory = session.getFactory();
            final QueryInterpretationCache.Key cacheKey = SqmInterpretationsKey.createInterpretationsKey(hqlQuery);

            final SqmTranslatorFactory sqmTranslatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(sessionFactory);

            final SqmTranslator<? extends MutationStatement> sqmConverter = sqmTranslatorFactory.createMutationTranslator(
                    (SqmInsertSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory.getSqlTranslationEngine()
            );

            final SqmTranslation<? extends MutationStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else {
            NonSelectQueryPlan nonSelectQueryPlan = invokeMethod(hqlQuery, "resolveNonSelectQueryPlan");
            if (nonSelectQueryPlan instanceof SimpleDeleteQueryPlan) {
                SqmDeleteStatement<?> sqmDelete = getField(nonSelectQueryPlan, "statement");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();

                final SqmTranslatorFactory translatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(factory);
                final SqmTranslator<? extends MutationStatement> translator = translatorFactory.createMutationTranslator(
                        sqmDelete,
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory.getSqlTranslationEngine()
                );

                final SqmTranslation<? extends MutationStatement> sqmInterpretation = translator.translate();
                return new CacheableSqmInterpretation(sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
            } else if (nonSelectQueryPlan instanceof SimpleNonSelectQueryPlan) {
                SqmUpdateStatement<?> sqmUpdate = getField(nonSelectQueryPlan, "statement");
                SessionFactoryImplementor factory = hqlQuery.getSessionFactory();

                final SqmTranslatorFactory translatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(factory);
                final SqmTranslator<? extends MutationStatement> translator = translatorFactory.createMutationTranslator(
                        sqmUpdate,
                        hqlQuery.getQueryOptions(),
                        hqlQuery.getDomainParameterXref(),
                        hqlQuery.getQueryParameterBindings(),
                        hqlQuery.getLoadQueryInfluencers(),
                        factory.getSqlTranslationEngine()
                );

                final SqmTranslation<? extends MutationStatement> sqmInterpretation = translator.translate();
                return new CacheableSqmInterpretation(sqmInterpretation, translator.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
            } else if (nonSelectQueryPlan instanceof MultiTableDeleteQueryPlan) {

            } else if (nonSelectQueryPlan instanceof MultiTableUpdateQueryPlan) {

            }
        }
        throw new UnsupportedOperationException("not yet implemented");
    }

    private static CacheableSqmInterpretation buildQuerySpecPlan(Query query) {
        SqmQueryImpl<?> hqlQuery = query.unwrap(SqmQueryImpl.class);

        final SharedSessionContractImplementor session = hqlQuery.getSession();
        final SessionFactoryImplementor sessionFactory = session.getFactory();

        final SqmTranslatorFactory sqmTranslatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(sessionFactory);
        if (hqlQuery.getSqmStatement() instanceof SqmSelectStatement<?>) {
            final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                    (SqmSelectStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory.getSqlTranslationEngine(),
                    false
            );

            final SqmTranslation<SelectStatement> interpretation = sqmConverter.translate();

            return new CacheableSqmInterpretation(interpretation, sqmConverter.getFromClauseAccess(), hqlQuery.getDomainParameterXref());
        } else if (hqlQuery.getSqmStatement() instanceof SqmInsertSelectStatement<?>) {
            final SqmTranslator<? extends MutationStatement> sqmConverter = sqmTranslatorFactory.createMutationTranslator(
                    (SqmInsertStatement<?>) hqlQuery.getSqmStatement(),
                    hqlQuery.getQueryOptions(),
                    hqlQuery.getDomainParameterXref(),
                    hqlQuery.getQueryParameterBindings(),
                    hqlQuery.getLoadQueryInfluencers(),
                    sessionFactory.getSqlTranslationEngine()
            );

            final SqmTranslation<? extends MutationStatement> interpretation = sqmConverter.translate();

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

        final SqmTranslatorFactory sqmTranslatorFactory = HibernateAccessUtils.getSqmTranslatorFactory(sessionFactory);

        final SqmTranslator<SelectStatement> sqmConverter = sqmTranslatorFactory.createSelectTranslator(
                sqm,
                executionContext.getQueryOptions(),
                domainParameterXref,
                executionContext.getQueryParameterBindings(),
                executionContext.getLoadQueryInfluencers(),
                sessionFactory.getSqlTranslationEngine(),
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

}