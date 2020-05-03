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

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.NullPrecedence;
import org.hibernate.ScrollMode;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.dialect.ColumnAliasExtractor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.LobMergeStrategy;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.sequence.SequenceSupport;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelperBuilder;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.engine.jdbc.env.spi.SchemaNameResolver;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.loader.BatchLoadSizingStrategy;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.SqlExpressable;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.query.CastType;
import org.hibernate.query.TemporalUnit;
import org.hibernate.query.TrimSpec;
import org.hibernate.query.hql.HqlTranslator;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.mutation.spi.SqmMultiTableMutationStrategy;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.CaseExpressionWalker;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import javax.persistence.TemporalType;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Hibernate60DelegatingDialect extends Dialect {

    private final Dialect delegate;

    public Hibernate60DelegatingDialect(Dialect delegate) {
        this.delegate = delegate;
    }

    @Override
    public void initializeFunctionRegistry(QueryEngine queryEngine) {
        delegate.initializeFunctionRegistry(queryEngine);
    }

    @Override
    public String currentDate() {
        return delegate.currentDate();
    }

    @Override
    public String currentTime() {
        return delegate.currentTime();
    }

    @Override
    public String currentTimestamp() {
        return delegate.currentTimestamp();
    }

    @Override
    public String currentLocalTime() {
        return delegate.currentLocalTime();
    }

    @Override
    public String currentLocalTimestamp() {
        return delegate.currentLocalTimestamp();
    }

    @Override
    public String currentTimestampWithTimeZone() {
        return delegate.currentTimestampWithTimeZone();
    }

    @Override
    public String extractPattern(TemporalUnit unit) {
        return delegate.extractPattern(unit);
    }

    @Override
    public String castPattern(CastType from, CastType to) {
        return delegate.castPattern(from, to);
    }

    @Override
    public String trimPattern(TrimSpec specification, char character) {
        return delegate.trimPattern(specification, character);
    }

    @Override
    public String timestampdiffPattern(TemporalUnit unit, boolean fromTimestamp, boolean toTimestamp) {
        return delegate.timestampdiffPattern(unit, fromTimestamp, toTimestamp);
    }

    @Override
    public String timestampaddPattern(TemporalUnit unit, boolean timestamp) {
        return delegate.timestampaddPattern(unit, timestamp);
    }

    @Deprecated
    public static Dialect getDialect() throws HibernateException {
        return Dialect.getDialect();
    }

    @Deprecated
    public static Dialect getDialect(Properties props) throws HibernateException {
        return Dialect.getDialect(props);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        delegate.contributeTypes(typeContributions, serviceRegistry);
    }

    @Override
    public String getRawTypeName(int code) throws HibernateException {
        return delegate.getRawTypeName(code);
    }

    @Override
    public String getRawTypeName(SqlTypeDescriptor sqlTypeDescriptor) throws HibernateException {
        return delegate.getRawTypeName(sqlTypeDescriptor);
    }

    @Override
    public String getTypeName(SqlTypeDescriptor sqlTypeDescriptor) throws HibernateException {
        return delegate.getTypeName(sqlTypeDescriptor);
    }

    @Override
    public String getTypeName(int code) throws HibernateException {
        return delegate.getTypeName(code);
    }

    @Override
    public String getTypeName(int code, Size size) throws HibernateException {
        return delegate.getTypeName(code, size);
    }

    @Override
    public String getTypeName(SqlTypeDescriptor sqlTypeDescriptor, Size size) {
        return delegate.getTypeName(sqlTypeDescriptor, size);
    }

    @Override
    public String getCastTypeName(SqlExpressable type, Long length, Integer precision, Integer scale) {
        return delegate.getCastTypeName(type, length, precision, scale);
    }

    @Override
    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
        return delegate.remapSqlTypeDescriptor(sqlTypeDescriptor);
    }

    @Override
    public LobMergeStrategy getLobMergeStrategy() {
        return delegate.getLobMergeStrategy();
    }

    @Override
    public String getHibernateTypeName(int code) throws HibernateException {
        return delegate.getHibernateTypeName(code);
    }

    @Override
    public boolean isTypeNameRegistered(String typeName) {
        return delegate.isTypeNameRegistered(typeName);
    }

    @Override
    public String getHibernateTypeName(int code, Integer length, Integer precision, Integer scale) throws HibernateException {
        return delegate.getHibernateTypeName(code, length, precision, scale);
    }

    @Override
    @Deprecated
    public Class getNativeIdentifierGeneratorClass() {
        return delegate.getNativeIdentifierGeneratorClass();
    }

    @Override
    public String getNativeIdentifierGeneratorStrategy() {
        return delegate.getNativeIdentifierGeneratorStrategy();
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return delegate.getIdentityColumnSupport();
    }

    @Override
    public SequenceSupport getSequenceSupport() {
        return delegate.getSequenceSupport();
    }

    @Override
    public String getQuerySequencesString() {
        return delegate.getQuerySequencesString();
    }

    @Override
    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return delegate.getSequenceInformationExtractor();
    }

    @Override
    public String getSelectGUIDString() {
        return delegate.getSelectGUIDString();
    }

    @Override
    public String getFromDual() {
        return delegate.getFromDual();
    }

    @Override
    public LimitHandler getLimitHandler() {
        return delegate.getLimitHandler();
    }

    @Override
    public boolean supportsLockTimeouts() {
        return delegate.supportsLockTimeouts();
    }

    @Override
    public boolean isLockTimeoutParameterized() {
        return delegate.isLockTimeoutParameterized();
    }

    @Override
    public LockingStrategy getLockingStrategy(Lockable lockable, LockMode lockMode) {
        return delegate.getLockingStrategy(lockable, lockMode);
    }

    @Override
    public String getForUpdateString(LockOptions lockOptions) {
        return delegate.getForUpdateString(lockOptions);
    }

    @Override
    public String getForUpdateString(LockMode lockMode) {
        return delegate.getForUpdateString(lockMode);
    }

    @Override
    public String getForUpdateString() {
        return delegate.getForUpdateString();
    }

    @Override
    public String getWriteLockString(int timeout) {
        return delegate.getWriteLockString(timeout);
    }

    @Override
    public String getWriteLockString(String aliases, int timeout) {
        return delegate.getWriteLockString(aliases, timeout);
    }

    @Override
    public String getReadLockString(int timeout) {
        return delegate.getReadLockString(timeout);
    }

    @Override
    public String getReadLockString(String aliases, int timeout) {
        return delegate.getReadLockString(aliases, timeout);
    }

    @Override
    public boolean forUpdateOfColumns() {
        return delegate.forUpdateOfColumns();
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return delegate.supportsOuterJoinForUpdate();
    }

    @Override
    public String getForUpdateString(String aliases) {
        return delegate.getForUpdateString(aliases);
    }

    @Override
    public String getForUpdateString(String aliases, LockOptions lockOptions) {
        return delegate.getForUpdateString(aliases, lockOptions);
    }

    @Override
    public String getForUpdateNowaitString() {
        return delegate.getForUpdateNowaitString();
    }

    @Override
    public String getForUpdateSkipLockedString() {
        return delegate.getForUpdateSkipLockedString();
    }

    @Override
    public String getForUpdateNowaitString(String aliases) {
        return delegate.getForUpdateNowaitString(aliases);
    }

    @Override
    public String getForUpdateSkipLockedString(String aliases) {
        return delegate.getForUpdateSkipLockedString(aliases);
    }

    @Override
    @Deprecated
    public String appendLockHint(LockMode mode, String tableName) {
        return delegate.appendLockHint(mode, tableName);
    }

    @Override
    public String appendLockHint(LockOptions lockOptions, String tableName) {
        return delegate.appendLockHint(lockOptions, tableName);
    }

    @Override
    public String applyLocksToSql(String sql, LockOptions aliasedLockOptions, Map<String, String[]> keyColumnNames) {
        return delegate.applyLocksToSql(sql, aliasedLockOptions, keyColumnNames);
    }

    @Override
    public String getCreateTableString() {
        return delegate.getCreateTableString();
    }

    @Override
    public String getAlterTableString(String tableName) {
        return delegate.getAlterTableString(tableName);
    }

    @Override
    public String getCreateMultisetTableString() {
        return delegate.getCreateMultisetTableString();
    }

    @Override
    public SqmMultiTableMutationStrategy getFallbackSqmMutationStrategy(EntityMappingType entityDescriptor, RuntimeModelCreationContext runtimeModelCreationContext) {
        return delegate.getFallbackSqmMutationStrategy(entityDescriptor, runtimeModelCreationContext);
    }

    @Override
    public int registerResultSetOutParameter(CallableStatement statement, int position) throws SQLException {
        return delegate.registerResultSetOutParameter(statement, position);
    }

    @Override
    public int registerResultSetOutParameter(CallableStatement statement, String name) throws SQLException {
        return delegate.registerResultSetOutParameter(statement, name);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement) throws SQLException {
        return delegate.getResultSet(statement);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement, int position) throws SQLException {
        return delegate.getResultSet(statement, position);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement, String name) throws SQLException {
        return delegate.getResultSet(statement, name);
    }

    @Override
    @Deprecated
    public boolean supportsCurrentTimestampSelection() {
        return delegate.supportsCurrentTimestampSelection();
    }

    @Override
    @Deprecated
    public boolean isCurrentTimestampSelectStringCallable() {
        return delegate.isCurrentTimestampSelectStringCallable();
    }

    @Override
    @Deprecated
    public String getCurrentTimestampSelectString() {
        return delegate.getCurrentTimestampSelectString();
    }

    @Override
    @Deprecated
    public String getCurrentTimestampSQLFunctionName() {
        return delegate.getCurrentTimestampSQLFunctionName();
    }

    @Override
    @Deprecated
    public SQLExceptionConverter buildSQLExceptionConverter() {
        return delegate.buildSQLExceptionConverter();
    }

    @Override
    public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
        return delegate.buildSQLExceptionConversionDelegate();
    }

    @Override
    public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
        return delegate.getViolatedConstraintNameExtracter();
    }

    @Override
    public String getSelectClauseNullString(int sqlType) {
        return delegate.getSelectClauseNullString(sqlType);
    }

    @Override
    public boolean supportsUnionAll() {
        return delegate.supportsUnionAll();
    }

    @Override
    @Deprecated
    public JoinFragment createOuterJoinFragment() {
        return delegate.createOuterJoinFragment();
    }

    @Override
    @Deprecated
    public CaseFragment createCaseFragment() {
        return delegate.createCaseFragment();
    }

    @Override
    public CaseExpressionWalker getCaseExpressionWalker() {
        return delegate.getCaseExpressionWalker();
    }

    @Override
    public String getNoColumnsInsertString() {
        return delegate.getNoColumnsInsertString();
    }

    @Override
    public boolean supportsNoColumnsInsert() {
        return delegate.supportsNoColumnsInsert();
    }

    @Override
    public String getLowercaseFunction() {
        return delegate.getLowercaseFunction();
    }

    @Override
    public String getCaseInsensitiveLike() {
        return delegate.getCaseInsensitiveLike();
    }

    @Override
    public boolean supportsCaseInsensitiveLike() {
        return delegate.supportsCaseInsensitiveLike();
    }

    @Override
    public String transformSelectString(String select) {
        return delegate.transformSelectString(select);
    }

    @Override
    public int getMaxAliasLength() {
        return delegate.getMaxAliasLength();
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return delegate.toBooleanValueString(bool);
    }

    @Override
    @Deprecated
    public Set<String> getKeywords() {
        return delegate.getKeywords();
    }

    @Override
    public IdentifierHelper buildIdentifierHelper(IdentifierHelperBuilder builder, DatabaseMetaData dbMetaData) throws SQLException {
        return delegate.buildIdentifierHelper(builder, dbMetaData);
    }

    @Override
    public char openQuote() {
        return delegate.openQuote();
    }

    @Override
    public char closeQuote() {
        return delegate.closeQuote();
    }

    @Override
    public Exporter<Table> getTableExporter() {
        return delegate.getTableExporter();
    }

    @Override
    public Exporter<Sequence> getSequenceExporter() {
        return delegate.getSequenceExporter();
    }

    @Override
    public Exporter<Index> getIndexExporter() {
        return delegate.getIndexExporter();
    }

    @Override
    public Exporter<ForeignKey> getForeignKeyExporter() {
        return delegate.getForeignKeyExporter();
    }

    @Override
    public Exporter<Constraint> getUniqueKeyExporter() {
        return delegate.getUniqueKeyExporter();
    }

    @Override
    public Exporter<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectExporter() {
        return delegate.getAuxiliaryDatabaseObjectExporter();
    }

    @Override
    public boolean canCreateCatalog() {
        return delegate.canCreateCatalog();
    }

    @Override
    public String[] getCreateCatalogCommand(String catalogName) {
        return delegate.getCreateCatalogCommand(catalogName);
    }

    @Override
    public String[] getDropCatalogCommand(String catalogName) {
        return delegate.getDropCatalogCommand(catalogName);
    }

    @Override
    public boolean canCreateSchema() {
        return delegate.canCreateSchema();
    }

    @Override
    public String[] getCreateSchemaCommand(String schemaName) {
        return delegate.getCreateSchemaCommand(schemaName);
    }

    @Override
    public String[] getDropSchemaCommand(String schemaName) {
        return delegate.getDropSchemaCommand(schemaName);
    }

    @Override
    public String getCurrentSchemaCommand() {
        return delegate.getCurrentSchemaCommand();
    }

    @Override
    public SchemaNameResolver getSchemaNameResolver() {
        return delegate.getSchemaNameResolver();
    }

    @Override
    public boolean hasAlterTable() {
        return delegate.hasAlterTable();
    }

    @Override
    public boolean dropConstraints() {
        return delegate.dropConstraints();
    }

    @Override
    public boolean qualifyIndexName() {
        return delegate.qualifyIndexName();
    }

    @Override
    public String getAddColumnString() {
        return delegate.getAddColumnString();
    }

    @Override
    public String getAddColumnSuffixString() {
        return delegate.getAddColumnSuffixString();
    }

    @Override
    public String getDropForeignKeyString() {
        return delegate.getDropForeignKeyString();
    }

    @Override
    public String getTableTypeString() {
        return delegate.getTableTypeString();
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return delegate.getAddForeignKeyConstraintString(constraintName, foreignKey, referencedTable, primaryKey, referencesPrimaryKey);
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
        return delegate.getAddForeignKeyConstraintString(constraintName, foreignKeyDefinition);
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return delegate.getAddPrimaryKeyConstraintString(constraintName);
    }

    @Override
    public boolean hasSelfReferentialForeignKeyBug() {
        return delegate.hasSelfReferentialForeignKeyBug();
    }

    @Override
    public String getNullColumnString() {
        return delegate.getNullColumnString();
    }

    @Override
    public boolean supportsCommentOn() {
        return delegate.supportsCommentOn();
    }

    @Override
    public String getTableComment(String comment) {
        return delegate.getTableComment(comment);
    }

    @Override
    public String getColumnComment(String comment) {
        return delegate.getColumnComment(comment);
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return delegate.supportsIfExistsBeforeTableName();
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return delegate.supportsIfExistsAfterTableName();
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return delegate.supportsIfExistsBeforeConstraintName();
    }

    @Override
    public boolean supportsIfExistsAfterConstraintName() {
        return delegate.supportsIfExistsAfterConstraintName();
    }

    @Override
    public boolean supportsIfExistsAfterAlterTable() {
        return delegate.supportsIfExistsAfterAlterTable();
    }

    @Override
    public String getDropTableString(String tableName) {
        return delegate.getDropTableString(tableName);
    }

    @Override
    public boolean supportsColumnCheck() {
        return delegate.supportsColumnCheck();
    }

    @Override
    public boolean supportsTableCheck() {
        return delegate.supportsTableCheck();
    }

    @Override
    public boolean supportsCascadeDelete() {
        return delegate.supportsCascadeDelete();
    }

    @Override
    public String getCascadeConstraintsString() {
        return delegate.getCascadeConstraintsString();
    }

    @Override
    public String getCrossJoinSeparator() {
        return delegate.getCrossJoinSeparator();
    }

    @Override
    public String getTableAliasSeparator() {
        return delegate.getTableAliasSeparator();
    }

    @Override
    public ColumnAliasExtractor getColumnAliasExtractor() {
        return delegate.getColumnAliasExtractor();
    }

    @Override
    public boolean supportsEmptyInList() {
        return delegate.supportsEmptyInList();
    }

    @Override
    public boolean areStringComparisonsCaseInsensitive() {
        return delegate.areStringComparisonsCaseInsensitive();
    }

    @Override
    public boolean supportsRowValueConstructorSyntax() {
        return delegate.supportsRowValueConstructorSyntax();
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInInList() {
        return delegate.supportsRowValueConstructorSyntaxInInList();
    }

    @Override
    public boolean useInputStreamToInsertBlob() {
        return delegate.useInputStreamToInsertBlob();
    }

    @Override
    public boolean supportsParametersInInsertSelect() {
        return delegate.supportsParametersInInsertSelect();
    }

    @Override
    public boolean replaceResultVariableInOrderByClauseWithPosition() {
        return delegate.replaceResultVariableInOrderByClauseWithPosition();
    }

    @Override
    public String renderOrderByElement(String expression, String collation, String order, NullPrecedence nulls) {
        return delegate.renderOrderByElement(expression, collation, order, nulls);
    }

    @Override
    public boolean requiresCastingOfParametersInSelectClause() {
        return delegate.requiresCastingOfParametersInSelectClause();
    }

    @Override
    public boolean supportsResultSetPositionQueryMethodsOnForwardOnlyCursor() {
        return delegate.supportsResultSetPositionQueryMethodsOnForwardOnlyCursor();
    }

    @Override
    public boolean supportsCircularCascadeDeleteConstraints() {
        return delegate.supportsCircularCascadeDeleteConstraints();
    }

    @Override
    public boolean supportsSubselectAsInPredicateLHS() {
        return delegate.supportsSubselectAsInPredicateLHS();
    }

    @Override
    public boolean supportsExpectedLobUsagePattern() {
        return delegate.supportsExpectedLobUsagePattern();
    }

    @Override
    public boolean supportsLobValueChangePropogation() {
        return delegate.supportsLobValueChangePropogation();
    }

    @Override
    public boolean supportsUnboundedLobLocatorMaterialization() {
        return delegate.supportsUnboundedLobLocatorMaterialization();
    }

    @Override
    public boolean supportsSubqueryOnMutatingTable() {
        return delegate.supportsSubqueryOnMutatingTable();
    }

    @Override
    public boolean supportsExistsInSelect() {
        return delegate.supportsExistsInSelect();
    }

    @Override
    public boolean doesReadCommittedCauseWritersToBlockReaders() {
        return delegate.doesReadCommittedCauseWritersToBlockReaders();
    }

    @Override
    public boolean doesRepeatableReadCauseReadersToBlockWriters() {
        return delegate.doesRepeatableReadCauseReadersToBlockWriters();
    }

    @Override
    public boolean supportsBindAsCallableArgument() {
        return delegate.supportsBindAsCallableArgument();
    }

    @Override
    public boolean supportsTupleCounts() {
        return delegate.supportsTupleCounts();
    }

    @Override
    public boolean supportsTupleDistinctCounts() {
        return delegate.supportsTupleDistinctCounts();
    }

    @Override
    public boolean requiresParensForTupleDistinctCounts() {
        return delegate.requiresParensForTupleDistinctCounts();
    }

    @Override
    public int getInExpressionCountLimit() {
        return delegate.getInExpressionCountLimit();
    }

    @Override
    public boolean forceLobAsLastValue() {
        return delegate.forceLobAsLastValue();
    }

    @Override
    public boolean isEmptyStringTreatedAsNull() {
        return delegate.isEmptyStringTreatedAsNull();
    }

    @Override
    public boolean useFollowOnLocking(String sql, QueryOptions queryOptions) {
        return delegate.useFollowOnLocking(sql, queryOptions);
    }

    @Override
    public String getNotExpression(String expression) {
        return delegate.getNotExpression(expression);
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
        return delegate.getUniqueDelegate();
    }

    @Override
    @Deprecated
    public boolean supportsUnique() {
        return delegate.supportsUnique();
    }

    @Override
    @Deprecated
    public boolean supportsUniqueConstraintInCreateAlterTable() {
        return delegate.supportsUniqueConstraintInCreateAlterTable();
    }

    @Override
    @Deprecated
    public String getAddUniqueConstraintString(String constraintName) {
        return delegate.getAddUniqueConstraintString(constraintName);
    }

    @Override
    @Deprecated
    public boolean supportsNotNullUnique() {
        return delegate.supportsNotNullUnique();
    }

    @Override
    public String getQueryHintString(String query, List<String> hintList) {
        return delegate.getQueryHintString(query, hintList);
    }

    @Override
    public String getQueryHintString(String query, String hints) {
        return delegate.getQueryHintString(query, hints);
    }

    @Override
    public ScrollMode defaultScrollMode() {
        return delegate.defaultScrollMode();
    }

    @Override
    public boolean supportsTuplesInSubqueries() {
        return delegate.supportsTuplesInSubqueries();
    }

    @Override
    public CallableStatementSupport getCallableStatementSupport() {
        return delegate.getCallableStatementSupport();
    }

    @Override
    public NameQualifierSupport getNameQualifierSupport() {
        return delegate.getNameQualifierSupport();
    }

    @Override
    public BatchLoadSizingStrategy getDefaultBatchLoadSizingStrategy() {
        return delegate.getDefaultBatchLoadSizingStrategy();
    }

    @Override
    public boolean isJdbcLogWarningsEnabledByDefault() {
        return delegate.isJdbcLogWarningsEnabledByDefault();
    }

    @Override
    public void augmentRecognizedTableTypes(List<String> tableTypesList) {
        delegate.augmentRecognizedTableTypes(tableTypesList);
    }

    @Override
    public boolean supportsPartitionBy() {
        return delegate.supportsPartitionBy();
    }

    @Override
    public boolean supportsNamedParameters(DatabaseMetaData databaseMetaData) throws SQLException {
        return delegate.supportsNamedParameters(databaseMetaData);
    }

    @Override
    public boolean supportsNationalizedTypes() {
        return delegate.supportsNationalizedTypes();
    }

    @Override
    public int getPreferredSqlTypeCodeForBoolean() {
        return delegate.getPreferredSqlTypeCodeForBoolean();
    }

    @Override
    public boolean supportsNonQueryWithCTE() {
        return delegate.supportsNonQueryWithCTE();
    }

    @Override
    public boolean supportsValuesList() {
        return delegate.supportsValuesList();
    }

    @Override
    public boolean supportsSkipLocked() {
        return delegate.supportsSkipLocked();
    }

    @Override
    public boolean supportsNoWait() {
        return delegate.supportsNoWait();
    }

    @Override
    public String inlineLiteral(String literal) {
        return delegate.inlineLiteral(literal);
    }

    @Override
    public boolean supportsJdbcConnectionLobCreation(DatabaseMetaData databaseMetaData) {
        return delegate.supportsJdbcConnectionLobCreation(databaseMetaData);
    }

    @Override
    public String addSqlHintOrComment(String sql, boolean commentsEnabled) {
        return delegate.addSqlHintOrComment(sql, commentsEnabled);
    }

    @Override
    public HqlTranslator getHqlTranslator() {
        return delegate.getHqlTranslator();
    }

    @Override
    public SqmTranslatorFactory getSqmTranslatorFactory() {
        return delegate.getSqmTranslatorFactory();
    }

    @Override
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return delegate.getSqlAstTranslatorFactory();
    }

    @Override
    public boolean supportsSelectAliasInGroupByClause() {
        return delegate.supportsSelectAliasInGroupByClause();
    }

    @Override
    public DefaultSizeStrategy getDefaultSizeStrategy() {
        return delegate.getDefaultSizeStrategy();
    }

    @Override
    public void setDefaultSizeStrategy(DefaultSizeStrategy defaultSizeStrategy) {
        delegate.setDefaultSizeStrategy(defaultSizeStrategy);
    }

    @Override
    public long getDefaultLobLength() {
        return delegate.getDefaultLobLength();
    }

    @Override
    public int getDefaultDecimalPrecision() {
        return delegate.getDefaultDecimalPrecision();
    }

    @Override
    public int getDefaultTimestampPrecision() {
        return delegate.getDefaultTimestampPrecision();
    }

    @Override
    public int getFloatPrecision() {
        return delegate.getFloatPrecision();
    }

    @Override
    public int getDoublePrecision() {
        return delegate.getDoublePrecision();
    }

    @Override
    public long getFractionalSecondPrecisionInNanos() {
        return delegate.getFractionalSecondPrecisionInNanos();
    }

    @Override
    public boolean supportsBitType() {
        return delegate.supportsBitType();
    }

    @Override
    public String translateDatetimeFormat(String format) {
        return delegate.translateDatetimeFormat(format);
    }

    @Override
    public String translateExtractField(TemporalUnit unit) {
        return delegate.translateExtractField(unit);
    }

    @Override
    public String translateDurationField(TemporalUnit unit) {
        return delegate.translateDurationField(unit);
    }

    @Override
    public String formatDateTimeLiteral(TemporalAccessor temporalAccessor, TemporalType precision) {
        return delegate.formatDateTimeLiteral(temporalAccessor, precision);
    }

    @Override
    public String formatDateTimeLiteral(Date date, TemporalType precision) {
        return delegate.formatDateTimeLiteral(date, precision);
    }

    @Override
    public String formatDateTimeLiteral(Calendar calendar, TemporalType precision) {
        return delegate.formatDateTimeLiteral(calendar, precision);
    }

    @Override
    @Deprecated
    public boolean supportsLimit() {
        return delegate.supportsLimit();
    }

    @Override
    @Deprecated
    public boolean supportsLimitOffset() {
        return delegate.supportsLimitOffset();
    }

    @Override
    @Deprecated
    public boolean supportsVariableLimit() {
        return delegate.supportsVariableLimit();
    }

    @Override
    @Deprecated
    public boolean bindLimitParametersInReverseOrder() {
        return delegate.bindLimitParametersInReverseOrder();
    }

    @Override
    @Deprecated
    public boolean bindLimitParametersFirst() {
        return delegate.bindLimitParametersFirst();
    }

    @Override
    @Deprecated
    public boolean useMaxForLimit() {
        return delegate.useMaxForLimit();
    }

    @Override
    @Deprecated
    public boolean forceLimitUsage() {
        return delegate.forceLimitUsage();
    }

    @Override
    @Deprecated
    public String getLimitString(String query, int offset, int limit) {
        return delegate.getLimitString(query, offset, limit);
    }

    @Override
    @Deprecated
    public int convertToFirstRowValue(int zeroBasedFirstResult) {
        return delegate.convertToFirstRowValue(zeroBasedFirstResult);
    }

    @Override
    @Deprecated
    public boolean supportsSequences() {
        return delegate.supportsSequences();
    }

    @Override
    @Deprecated
    public boolean supportsPooledSequences() {
        return delegate.supportsPooledSequences();
    }

    @Override
    @Deprecated
    public String getSequenceNextValString(String sequenceName) throws MappingException {
        return delegate.getSequenceNextValString(sequenceName);
    }

    @Override
    @Deprecated
    public String getSelectSequenceNextValString(String sequenceName) throws MappingException {
        return delegate.getSelectSequenceNextValString(sequenceName);
    }

    @Override
    @Deprecated
    public String getSequenceNextValString(String sequenceName, int increment) throws MappingException {
        return delegate.getSequenceNextValString(sequenceName, increment);
    }

    @Override
    @Deprecated
    public String[] getCreateSequenceStrings(String sequenceName, int initialValue, int incrementSize) throws MappingException {
        return delegate.getCreateSequenceStrings(sequenceName, initialValue, incrementSize);
    }


    @Override
    @Deprecated
    public String[] getDropSequenceStrings(String sequenceName) throws MappingException {
        return delegate.getDropSequenceStrings(sequenceName);
    }

}
