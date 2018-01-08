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

package com.blazebit.persistence.impl.eclipselink.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import org.eclipse.persistence.config.ReferenceMode;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorQueryManager;
import org.eclipse.persistence.descriptors.partitioning.PartitioningPolicy;
import org.eclipse.persistence.exceptions.ConcurrencyException;
import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.eclipse.persistence.exceptions.IntegrityChecker;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.history.AsOfClause;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.databaseaccess.BatchWritingMechanism;
import org.eclipse.persistence.internal.databaseaccess.ConnectionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.DatabaseAccessor;
import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.databaseaccess.DatasourceCall;
import org.eclipse.persistence.internal.databaseaccess.DatasourcePlatform;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.databaseaccess.Platform;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.expressions.SQLSelectStatement;
import org.eclipse.persistence.internal.helper.ConcurrencyManager;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.helper.linkedlist.ExposedNodeLinkedList;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.indirection.DatabaseValueHolder;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.sequencing.Sequencing;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.CommitManager;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.internal.sessions.cdi.EntityListenerInjectionManager;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.platform.database.converters.StructConverter;
import org.eclipse.persistence.platform.database.partitioning.DataPartitioningCallback;
import org.eclipse.persistence.platform.server.ServerPlatform;
import org.eclipse.persistence.queries.AttributeGroup;
import org.eclipse.persistence.queries.Call;
import org.eclipse.persistence.queries.DataModifyQuery;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.JPAQueryBuilder;
import org.eclipse.persistence.queries.ObjectBuildingQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.SQLCall;
import org.eclipse.persistence.queries.StoredProcedureCall;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.CopyGroup;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.ExternalTransactionController;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.Login;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEventManager;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.coordination.CommandManager;
import org.eclipse.persistence.sessions.coordination.MetadataRefreshListener;
import org.eclipse.persistence.sessions.serializers.Serializer;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EclipseLinkFunctionRenderContext implements FunctionRenderContext {
    
    private final List<String> chunks = new ArrayList<String>();
    private final int[] argumentIndices;
    private final List<Expression> arguments;
    private final AbstractSession session;

    private final DatasourceCallMock datasourceCallMock = new DatasourceCallMock();

    private int currentIndex;
    private Boolean chunkFirst;

    public EclipseLinkFunctionRenderContext(List<Expression> arguments, AbstractSession session) {
        this.argumentIndices = new int[arguments.size()];
        Arrays.fill(this.argumentIndices, -1);
        this.arguments = arguments;
        this.session = new SessionDecorator(session);
    }

    @Override
    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public String getArgument(int index) {
        StringWriter writer = new StringWriter();
        ExpressionSQLPrinter expressionSQLPrinter = new ExpressionSQLPrinter(session, null, datasourceCallMock, true, new ExpressionBuilder());
        expressionSQLPrinter.setWriter(writer);
        arguments.get(index).printSQL(expressionSQLPrinter);
        return writer.toString();
    }

    @Override
    public void addArgument(int index) {
        if (chunkFirst == null) {
            chunkFirst = false;
        }
        argumentIndices[currentIndex++] = index;
    }

    @Override
    public void addChunk(String chunk) {
        if (chunkFirst == null) {
            chunkFirst = true;
        }
        chunks.add(chunk);
    }
    
    public boolean isChunkFirst() {
        return chunkFirst;
    }
    
    public List<String> getChunks() {
        return chunks;
    }
    
    public int[] getArgumentIndices() {
        int upperBoundIdx = argumentIndices.length;
        for (int i = 0; i < argumentIndices.length; i++) {
            if (argumentIndices[i] < 0) {
                upperBoundIdx = i;
                break;
            }
        }
        return Arrays.copyOfRange(argumentIndices, 0, upperBoundIdx);
    }

    private static class DatasourceCallMock extends SQLCall {

        static final DatasourceCallMock INSTANCE;

        static {
            INSTANCE = new DatasourceCallMock();
        }

        private DatasourceCallMock() { }

        @Override
        public void appendLiteral(Writer writer, Object literal) {
            try {
                writer.write(argumentMarker());
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        }
    }

    private static class SessionDecorator extends AbstractSession {
        private final AbstractSession delegate;
        private final DatabasePlatformDecorator platformDecorator;

        private SessionDecorator(AbstractSession delegate) {
            this.delegate = delegate;
            this.platformDecorator = new DatabasePlatformDecorator(super.getPlatform());
        }

        @Override
        public DatabasePlatform getPlatform() {
            return platformDecorator;
        }

        @Override
        public ClassLoader getLoader() {
            return delegate.getLoader();
        }

        @Override
        public Platform getDatasourcePlatform() {
            return delegate.getDatasourcePlatform();
        }

        @Override
        public ServerPlatform getServerPlatform() {
            return delegate.getServerPlatform();
        }

        @Override
        public Platform getPlatform(Class domainClass) {
            return delegate.getPlatform(domainClass);
        }

        @Override
        public SessionProfiler getProfiler() {
            return delegate == null ? null : delegate.getProfiler();
        }

        @Override
        public Project getProject() {
            return delegate.getProject();
        }

        @Override
        public Map getProperties() {
            return delegate.getProperties();
        }

        @Override
        public boolean hasProperties() {
            return delegate.hasProperties();
        }

        @Override
        public boolean hasTablePerTenantDescriptors() {
            return delegate.hasTablePerTenantDescriptors();
        }

        @Override
        public boolean hasTablePerTenantQueries() {
            return delegate.hasTablePerTenantQueries();
        }

        @Override
        public Object getProperty(String name) {
            return delegate.getProperty(name);
        }

        @Override
        public Map<String, List<DatabaseQuery>> getQueries() {
            return delegate.getQueries();
        }

        @Override
        public Map<String, AttributeGroup> getAttributeGroups() {
            return delegate.getAttributeGroups();
        }

        @Override
        public List<DatabaseQuery> getAllQueries() {
            return delegate.getAllQueries();
        }

        @Override
        public DatabaseQuery getQuery(String name) {
            return delegate.getQuery(name);
        }

        @Override
        public DatabaseQuery getQuery(String name, List arguments) {
            return delegate.getQuery(name, arguments);
        }

        @Override
        public DatabaseQuery getQuery(String name, Vector arguments) {
            return delegate.getQuery(name, arguments);
        }

        @Override
        public DatabaseQuery getQuery(String name, Vector arguments, boolean shouldSearchParent) {
            return delegate.getQuery(name, arguments, shouldSearchParent);
        }

        @Override
        public Sequencing getSequencing() {
            return delegate.getSequencing();
        }

        @Override
        public AbstractSession getSessionForClass(Class domainClass) {
            return delegate.getSessionForClass(domainClass);
        }

        @Override
        public AbstractSession getSessionForName(String name) throws ValidationException {
            return delegate.getSessionForName(name);
        }

        @Override
        public SessionLog getSessionLog() {
            return delegate.getSessionLog();
        }

        @Override
        public List<ClassDescriptor> getTablePerTenantDescriptors() {
            return delegate.getTablePerTenantDescriptors();
        }

        @Override
        public List<DatabaseQuery> getTablePerTenantQueries() {
            return delegate.getTablePerTenantQueries();
        }

        @Override
        public ConcurrencyManager getTransactionMutex() {
            return delegate.getTransactionMutex();
        }

        @Override
        public Object handleException(RuntimeException exception) throws RuntimeException {
            return delegate.handleException(exception);
        }

        @Override
        public boolean hasBroker() {
            return delegate.hasBroker();
        }

        @Override
        public boolean hasDescriptor(Class theClass) {
            return delegate.hasDescriptor(theClass);
        }

        @Override
        public boolean hasExceptionHandler() {
            return delegate.hasExceptionHandler();
        }

        @Override
        public boolean hasExternalTransactionController() {
            return delegate.hasExternalTransactionController();
        }

        @Override
        public Object insertObject(Object domainObject) throws DatabaseException {
            return delegate.insertObject(domainObject);
        }

        @Override
        public Object internalExecuteQuery(DatabaseQuery query, AbstractRecord databaseRow) throws DatabaseException {
            return delegate.internalExecuteQuery(query, databaseRow);
        }

        @Override
        public boolean isBroker() {
            return delegate.isBroker();
        }

        @Override
        public boolean isInBroker() {
            return delegate.isInBroker();
        }

        @Override
        public boolean isClassReadOnly(Class theClass) {
            return delegate.isClassReadOnly(theClass);
        }

        @Override
        public boolean isClassReadOnly(Class theClass, ClassDescriptor descriptor) {
            return delegate.isClassReadOnly(theClass, descriptor);
        }

        @Override
        public boolean isClientSession() {
            return delegate.isClientSession();
        }

        @Override
        public boolean isExclusiveIsolatedClientSession() {
            return delegate.isExclusiveIsolatedClientSession();
        }

        @Override
        public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override
        public boolean isDatabaseSession() {
            return delegate.isDatabaseSession();
        }

        @Override
        public boolean isDistributedSession() {
            return delegate.isDistributedSession();
        }

        @Override
        public boolean isInProfile() {
            return delegate.isInProfile();
        }

        @Override
        public void setIsInProfile(boolean inProfile) {
            delegate.setIsInProfile(inProfile);
        }

        @Override
        public void setIsInBroker(boolean isInBroker) {
            delegate.setIsInBroker(isInBroker);
        }

        @Override
        public boolean isFinalizersEnabled() {
            return delegate.isFinalizersEnabled();
        }

        @Override
        public void registerFinalizer() {
            delegate.registerFinalizer();
        }

        @Override
        public boolean isHistoricalSession() {
            return delegate.isHistoricalSession();
        }

        @Override
        public void setIsFinalizersEnabled(boolean isFinalizersEnabled) {
            delegate.setIsFinalizersEnabled(isFinalizersEnabled);
        }

        @Override
        public boolean isInTransaction() {
            return delegate.isInTransaction();
        }

        @Override
        public boolean isJPAQueriesProcessed() {
            return delegate.isJPAQueriesProcessed();
        }

        @Override
        public boolean isProtectedSession() {
            return delegate.isProtectedSession();
        }

        @Override
        public boolean isRemoteSession() {
            return delegate.isRemoteSession();
        }

        @Override
        public boolean isRemoteUnitOfWork() {
            return delegate.isRemoteUnitOfWork();
        }

        @Override
        public boolean isServerSession() {
            return delegate.isServerSession();
        }

        @Override
        public boolean isSessionBroker() {
            return delegate.isSessionBroker();
        }

        @Override
        public boolean isSynchronized() {
            return delegate.isSynchronized();
        }

        @Override
        public Object getId(Object domainObject) throws ValidationException {
            return delegate.getId(domainObject);
        }

        @Override
        @Deprecated
        public Vector keyFromObject(Object domainObject) throws ValidationException {
            return delegate.keyFromObject(domainObject);
        }

        @Override
        public Object keyFromObject(Object domainObject, ClassDescriptor descriptor) throws ValidationException {
            return delegate.keyFromObject(domainObject, descriptor);
        }

        @Override
        public void log(SessionLogEntry entry) {
            delegate.log(entry);
        }

        @Override
        public void logMessage(String message) {
            delegate.logMessage(message);
        }

        @Override
        public DatabaseQuery prepareDatabaseQuery(DatabaseQuery query) {
            return delegate.prepareDatabaseQuery(query);
        }

        @Override
        public Vector readAllObjects(Class domainClass) throws DatabaseException {
            return delegate.readAllObjects(domainClass);
        }

        @Override
        public Vector readAllObjects(Class domainClass, String sqlString) throws DatabaseException {
            return delegate.readAllObjects(domainClass, sqlString);
        }

        @Override
        public Vector readAllObjects(Class referenceClass, Call aCall) throws DatabaseException {
            return delegate.readAllObjects(referenceClass, aCall);
        }

        @Override
        public Vector readAllObjects(Class domainClass, Expression expression) throws DatabaseException {
            return delegate.readAllObjects(domainClass, expression);
        }

        @Override
        public Object readObject(Class domainClass) throws DatabaseException {
            return delegate.readObject(domainClass);
        }

        @Override
        public Object readObject(Class domainClass, String sqlString) throws DatabaseException {
            return delegate.readObject(domainClass, sqlString);
        }

        @Override
        public Object readObject(Class domainClass, Call aCall) throws DatabaseException {
            return delegate.readObject(domainClass, aCall);
        }

        @Override
        public Object readObject(Class domainClass, Expression expression) throws DatabaseException {
            return delegate.readObject(domainClass, expression);
        }

        @Override
        public Object readObject(Object object) throws DatabaseException {
            return delegate.readObject(object);
        }

        @Override
        public Object refreshAndLockObject(Object object) throws DatabaseException {
            return delegate.refreshAndLockObject(object);
        }

        @Override
        public Object refreshAndLockObject(Object object, short lockMode) throws DatabaseException {
            return delegate.refreshAndLockObject(object, lockMode);
        }

        @Override
        public Object refreshObject(Object object) throws DatabaseException {
            return delegate.refreshObject(object);
        }

        @Override
        public void release() {
            delegate.release();
        }

        @Override
        public void releaseUnitOfWork(UnitOfWorkImpl unitOfWork) {
            delegate.releaseUnitOfWork(unitOfWork);
        }

        @Override
        public void removeProperty(String property) {
            delegate.removeProperty(property);
        }

        @Override
        public void removeQuery(String queryName) {
            delegate.removeQuery(queryName);
        }

        @Override
        public void removeQuery(String queryName, Vector argumentTypes) {
            delegate.removeQuery(queryName, argumentTypes);
        }

        @Override
        public void setAccessor(Accessor accessor) {
            delegate.setAccessor(accessor);
        }

        @Override
        public void setBroker(AbstractSession broker) {
            delegate.setBroker(broker);
        }

        @Override
        public void setCommitManager(CommitManager commitManager) {
            delegate.setCommitManager(commitManager);
        }

        @Override
        public void setEntityListenerInjectionManager(EntityListenerInjectionManager entityListenerInjectionManager) {
            delegate.setEntityListenerInjectionManager(entityListenerInjectionManager);
        }

        @Override
        public void setEventManager(SessionEventManager eventManager) {
            delegate.setEventManager(eventManager);
        }

        @Override
        public void setExceptionHandler(ExceptionHandler exceptionHandler) {
            delegate.setExceptionHandler(exceptionHandler);
        }

        @Override
        public void setExternalTransactionController(ExternalTransactionController externalTransactionController) {
            delegate.setExternalTransactionController(externalTransactionController);
        }

        @Override
        public void setIntegrityChecker(IntegrityChecker integrityChecker) {
            delegate.setIntegrityChecker(integrityChecker);
        }

        @Override
        public void setJPAQueriesProcessed(boolean jpaQueriesProcessed) {
            delegate.setJPAQueriesProcessed(jpaQueriesProcessed);
        }

        @Override
        public void setLog(Writer log) {
            delegate.setLog(log);
        }

        @Override
        public void setLogin(DatabaseLogin login) {
            delegate.setLogin(login);
        }

        @Override
        public void setLogin(Login login) {
            delegate.setLogin(login);
        }

        @Override
        public void setDatasourceLogin(Login login) {
            delegate.setDatasourceLogin(login);
        }

        @Override
        public void setName(String name) {
            delegate.setName(name);
        }

        @Override
        public void setPessimisticLockTimeoutDefault(Integer pessimisticLockTimeoutDefault) {
            delegate.setPessimisticLockTimeoutDefault(pessimisticLockTimeoutDefault);
        }

        @Override
        public void setQueryTimeoutDefault(int queryTimeoutDefault) {
            delegate.setQueryTimeoutDefault(queryTimeoutDefault);
        }

        @Override
        public void setProfiler(SessionProfiler profiler) {
            delegate.setProfiler(profiler);
        }

        @Override
        public void setProject(Project project) {
            delegate.setProject(project);
        }

        @Override
        public void setProperties(Map<Object, Object> propertiesMap) {
            delegate.setProperties(propertiesMap);
        }

        @Override
        public void setProperty(String propertyName, Object propertyValue) {
            delegate.setProperty(propertyName, propertyValue);
        }

        @Override
        public void setQueries(Map<String, List<DatabaseQuery>> queries) {
            delegate.setQueries(queries);
        }

        @Override
        public void setSessionLog(SessionLog sessionLog) {
            delegate.setSessionLog(sessionLog);
        }

        @Override
        public void setSynchronized(boolean synched) {
            delegate.setSynchronized(synched);
        }

        @Override
        public void setWasJTSTransactionInternallyStarted(boolean wasJTSTransactionInternallyStarted) {
            delegate.setWasJTSTransactionInternallyStarted(wasJTSTransactionInternallyStarted);
        }

        @Override
        public boolean shouldLogMessages() {
            return delegate.shouldLogMessages();
        }

        @Override
        public void startOperationProfile(String operationName) {
            delegate.startOperationProfile(operationName);
        }

        @Override
        public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
            delegate.startOperationProfile(operationName, query, weight);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public Object unwrapObject(Object proxy) {
            return delegate.unwrapObject(proxy);
        }

        @Override
        public Object updateObject(Object domainObject) throws DatabaseException, OptimisticLockException {
            return delegate.updateObject(domainObject);
        }

        @Override
        public void validateCache() {
            delegate.validateCache();
        }

        @Override
        public void validateQuery(DatabaseQuery query) {
            delegate.validateQuery(query);
        }

        @Override
        public boolean verifyDelete(Object domainObject) {
            return delegate.verifyDelete(domainObject);
        }

        @Override
        public boolean wasJTSTransactionInternallyStarted() {
            return delegate.wasJTSTransactionInternallyStarted();
        }

        @Override
        public Object wrapObject(Object implementation) {
            return delegate.wrapObject(implementation);
        }

        @Override
        public Object writeObject(Object domainObject) throws DatabaseException, OptimisticLockException {
            return delegate.writeObject(domainObject);
        }

        @Override
        public void writesCompleted() {
            delegate.writesCompleted();
        }

        @Override
        public void processCommand(Object command) {
            delegate.processCommand(command);
        }

        @Override
        public void processJPAQueries() {
            delegate.processJPAQueries();
        }

        @Override
        public CommandManager getCommandManager() {
            return delegate.getCommandManager();
        }

        @Override
        public void setCommandManager(CommandManager mgr) {
            delegate.setCommandManager(mgr);
        }

        @Override
        public boolean shouldPropagateChanges() {
            return delegate.shouldPropagateChanges();
        }

        @Override
        public void setShouldPropagateChanges(boolean choice) {
            delegate.setShouldPropagateChanges(choice);
        }

        @Override
        public boolean shouldLogMessages(int logLevel) {
            return delegate.shouldLogMessages(logLevel);
        }

        @Override
        public void logMessage(int logLevel, String message) {
            delegate.logMessage(logLevel, message);
        }

        @Override
        public int getLogLevel(String category) {
            return delegate.getLogLevel(category);
        }

        @Override
        public int getLogLevel() {
            return delegate.getLogLevel();
        }

        @Override
        public void setLogLevel(int level) {
            delegate.setLogLevel(level);
        }

        @Override
        public boolean shouldDisplayData() {
            return delegate.shouldDisplayData();
        }

        @Override
        public boolean shouldLog(int level, String category) {
            return delegate.shouldLog(level, category);
        }

        @Override
        public void log(int level, String category, String message) {
            delegate.log(level, category, message);
        }

        @Override
        public void log(int level, String category, String message, Object param) {
            delegate.log(level, category, message, param);
        }

        @Override
        public void log(int level, String category, String message, Object param1, Object param2) {
            delegate.log(level, category, message, param1, param2);
        }

        @Override
        public void log(int level, String category, String message, Object param1, Object param2, Object param3) {
            delegate.log(level, category, message, param1, param2, param3);
        }

        @Override
        public void log(int level, String category, String message, Object[] params) {
            delegate.log(level, category, message, params);
        }

        @Override
        public void log(int level, String category, String message, Object[] params, Accessor accessor) {
            delegate.log(level, category, message, params, accessor);
        }

        @Override
        public void log(int level, String category, String message, Object[] params, Accessor accessor, boolean shouldTranslate) {
            delegate.log(level, category, message, params, accessor, shouldTranslate);
        }

        @Override
        @Deprecated
        public void log(int level, String message, Object[] params, Accessor accessor) {
            delegate.log(level, message, params, accessor);
        }

        @Override
        @Deprecated
        public void log(int level, String message, Object[] params, Accessor accessor, boolean shouldTranslate) {
            delegate.log(level, message, params, accessor, shouldTranslate);
        }

        @Override
        public void logThrowable(int level, String category, Throwable throwable) {
            delegate.logThrowable(level, category, throwable);
        }

        @Override
        public void severe(String message, String category) {
            delegate.severe(message, category);
        }

        @Override
        public void warning(String message, String category) {
            delegate.warning(message, category);
        }

        @Override
        public void info(String message, String category) {
            delegate.info(message, category);
        }

        @Override
        public void config(String message, String category) {
            delegate.config(message, category);
        }

        @Override
        public void fine(String message, String category) {
            delegate.fine(message, category);
        }

        @Override
        public void finer(String message, String category) {
            delegate.finer(message, category);
        }

        @Override
        public void finest(String message, String category) {
            delegate.finest(message, category);
        }

        @Override
        public Object handleSevere(RuntimeException exception) throws RuntimeException {
            return delegate.handleSevere(exception);
        }

        @Override
        public void releaseReadConnection(Accessor connection) {
            delegate.releaseReadConnection(connection);
        }

        @Override
        public void copyDescriptorsFromProject() {
            delegate.copyDescriptorsFromProject();
        }

        @Override
        public void copyDescriptorNamedQueries(boolean allowSameQueryNameDiffArgsCopyToSession) {
            delegate.copyDescriptorNamedQueries(allowSameQueryNameDiffArgsCopyToSession);
        }

        @Override
        public void postAcquireConnection(Accessor accessor) {
            delegate.postAcquireConnection(accessor);
        }

        @Override
        public void preReleaseConnection(Accessor accessor) {
            delegate.preReleaseConnection(accessor);
        }

        @Override
        public int priviledgedExecuteNonSelectingCall(Call call) throws DatabaseException {
            return delegate.priviledgedExecuteNonSelectingCall(call);
        }

        @Override
        public Vector priviledgedExecuteSelectingCall(Call call) throws DatabaseException {
            return delegate.priviledgedExecuteSelectingCall(call);
        }

        @Override
        public boolean isExclusiveConnectionRequired() {
            return delegate.isExclusiveConnectionRequired();
        }

        @Override
        public ReferenceMode getDefaultReferenceMode() {
            return delegate.getDefaultReferenceMode();
        }

        @Override
        public void setDefaultReferenceMode(ReferenceMode defaultReferenceMode) {
            delegate.setDefaultReferenceMode(defaultReferenceMode);
        }

        @Override
        public void load(Object objectOrCollection, AttributeGroup group) {
            delegate.load(objectOrCollection, group);
        }

        @Override
        public void load(Object objectOrCollection, AttributeGroup group, ClassDescriptor referenceDescriptor, boolean fromFetchGroup) {
            delegate.load(objectOrCollection, group, referenceDescriptor, fromFetchGroup);
        }

        @Override
        public CacheKey retrieveCacheKey(Object primaryKey, ClassDescriptor concreteDescriptor, JoinedAttributeManager joinManager, ObjectBuildingQuery query) {
            return delegate.retrieveCacheKey(primaryKey, concreteDescriptor, joinManager, query);
        }

        @Override
        public PartitioningPolicy getPartitioningPolicy() {
            return delegate.getPartitioningPolicy();
        }

        @Override
        public void setPartitioningPolicy(PartitioningPolicy partitioningPolicy) {
            delegate.setPartitioningPolicy(partitioningPolicy);
        }

        @Override
        public MetadataRefreshListener getRefreshMetadataListener() {
            return delegate.getRefreshMetadataListener();
        }

        @Override
        public void setRefreshMetadataListener(MetadataRefreshListener metadatalistener) {
            delegate.setRefreshMetadataListener(metadatalistener);
        }

        @Override
        public boolean isConcurrent() {
            return delegate.isConcurrent();
        }

        @Override
        public void setIsConcurrent(boolean isConcurrent) {
            delegate.setIsConcurrent(isConcurrent);
        }

        @Override
        public void setShouldOptimizeResultSetAccess(boolean shouldOptimizeResultSetAccess) {
            delegate.setShouldOptimizeResultSetAccess(shouldOptimizeResultSetAccess);
        }

        @Override
        public boolean shouldOptimizeResultSetAccess() {
            return delegate.shouldOptimizeResultSetAccess();
        }

        @Override
        public void setTolerateInvalidJPQL(boolean b) {
            delegate.setTolerateInvalidJPQL(b);
        }

        @Override
        public boolean shouldTolerateInvalidJPQL() {
            return delegate.shouldTolerateInvalidJPQL();
        }

        @Override
        public void rollbackTransaction() throws DatabaseException, ConcurrencyException {
            delegate.rollbackTransaction();
        }

        @Override
        public Serializer getSerializer() {
            return delegate.getSerializer();
        }

        @Override
        public void setSerializer(Serializer serializer) {
            delegate.setSerializer(serializer);
        }

        @Override
        public JPAQueryBuilder getQueryBuilder() {
            return delegate.getQueryBuilder();
        }

        @Override
        public void setQueryBuilder(JPAQueryBuilder queryBuilder) {
            delegate.setQueryBuilder(queryBuilder);
        }

        @Override
        public void setLoggingOff(boolean loggingOff) {
            delegate.setLoggingOff(loggingOff);
        }

        @Override
        public boolean isLoggingOff() {
            return delegate.isLoggingOff();
        }

        @Override
        public long getNextQueryId() {
            return delegate.getNextQueryId();
        }

        @Override
        public UnitOfWorkImpl acquireNonSynchronizedUnitOfWork() {
            return delegate.acquireNonSynchronizedUnitOfWork();
        }

        @Override
        public UnitOfWorkImpl acquireNonSynchronizedUnitOfWork(ReferenceMode referenceMode) {
            return delegate.acquireNonSynchronizedUnitOfWork(referenceMode);
        }

        @Override
        public Session acquireHistoricalSession(AsOfClause clause) throws ValidationException {
            return delegate.acquireHistoricalSession(clause);
        }

        @Override
        public UnitOfWorkImpl acquireUnitOfWork() {
            return delegate.acquireUnitOfWork();
        }

        @Override
        public RepeatableWriteUnitOfWork acquireRepeatableWriteUnitOfWork(ReferenceMode referenceMode) {
            return delegate.acquireRepeatableWriteUnitOfWork(referenceMode);
        }

        @Override
        public UnitOfWorkImpl acquireUnitOfWork(ReferenceMode referenceMode) {
            return delegate.acquireUnitOfWork(referenceMode);
        }

        @Override
        public void addAlias(String alias, ClassDescriptor descriptor) {
            delegate.addAlias(alias, descriptor);
        }

        @Override
        public void addJPAQuery(DatabaseQuery query) {
            delegate.addJPAQuery(query);
        }

        @Override
        public void addJPATablePerTenantQuery(DatabaseQuery query) {
            delegate.addJPATablePerTenantQuery(query);
        }

        @Override
        public void addMultitenantContextProperty(String contextProperty) {
            delegate.addMultitenantContextProperty(contextProperty);
        }

        @Override
        public void addQuery(String name, DatabaseQuery query) {
            delegate.addQuery(name, query);
        }

        @Override
        public void addQuery(String name, DatabaseQuery query, boolean replace) {
            delegate.addQuery(name, query, replace);
        }

        @Override
        public void addStaticMetamodelClass(String modelClassName, String metamodelClassName) {
            delegate.addStaticMetamodelClass(modelClassName, metamodelClassName);
        }

        @Override
        public DatabaseException retryTransaction(Accessor accessor, DatabaseException databaseException, int retryCount, AbstractSession executionSession) {
            return delegate.retryTransaction(accessor, databaseException, retryCount, executionSession);
        }

        @Override
        public void releaseJTSConnection() {
            delegate.releaseJTSConnection();
        }

        @Override
        public boolean beginExternalTransaction() {
            return delegate.beginExternalTransaction();
        }

        @Override
        public void beginTransaction() throws DatabaseException, ConcurrencyException {
            delegate.beginTransaction();
        }

        @Override
        public void cleanUpEntityListenerInjectionManager() {
            delegate.cleanUpEntityListenerInjectionManager();
        }

        @Override
        public void clearIntegrityChecker() {
            delegate.clearIntegrityChecker();
        }

        @Override
        public void clearLastDescriptorAccessed() {
            delegate.clearLastDescriptorAccessed();
        }

        @Override
        public void clearDescriptors() {
            delegate.clearDescriptors();
        }

        @Override
        public void clearProfile() {
            delegate.clearProfile();
        }

        @Override
        public Object clone() {
            return delegate.clone();
        }

        @Override
        public boolean commitExternalTransaction() {
            return delegate.commitExternalTransaction();
        }

        @Override
        public void commitTransaction() throws DatabaseException, ConcurrencyException {
            delegate.commitTransaction();
        }

        @Override
        public boolean compareObjects(Object firstObject, Object secondObject) {
            return delegate.compareObjects(firstObject, secondObject);
        }

        @Override
        public boolean compareObjectsDontMatch(Object firstObject, Object secondObject) {
            return delegate.compareObjectsDontMatch(firstObject, secondObject);
        }

        @Override
        public boolean containsQuery(String queryName) {
            return delegate.containsQuery(queryName);
        }

        @Override
        public Object copy(Object originalObjectOrObjects) {
            return delegate.copy(originalObjectOrObjects);
        }

        @Override
        public Object copy(Object originalObjectOrObjects, AttributeGroup group) {
            return delegate.copy(originalObjectOrObjects, group);
        }

        @Override
        public Object copyInternal(Object originalObject, CopyGroup copyGroup) {
            return delegate.copyInternal(originalObject, copyGroup);
        }

        @Override
        public Object copyObject(Object original) {
            return delegate.copyObject(original);
        }

        @Override
        public Object copyObject(Object original, ObjectCopyingPolicy policy) {
            return delegate.copyObject(original, policy);
        }

        @Override
        public Vector copyReadOnlyClasses() {
            return delegate.copyReadOnlyClasses();
        }

        @Override
        public DatabaseValueHolder createCloneQueryValueHolder(ValueHolderInterface attributeValue, Object clone, AbstractRecord row, ForeignReferenceMapping mapping) {
            return delegate.createCloneQueryValueHolder(attributeValue, clone, row, mapping);
        }

        @Override
        public DatabaseValueHolder createCloneTransformationValueHolder(ValueHolderInterface attributeValue, Object original, Object clone, AbstractTransformationMapping mapping) {
            return delegate.createCloneTransformationValueHolder(attributeValue, original, clone, mapping);
        }

        @Override
        public EntityListenerInjectionManager createEntityListenerInjectionManager(Object beanManager) {
            return delegate.createEntityListenerInjectionManager(beanManager);
        }

        @Override
        public Object createProtectedInstanceFromCachedData(Object cached, Integer refreshCascade, ClassDescriptor descriptor) {
            return delegate.createProtectedInstanceFromCachedData(cached, refreshCascade, descriptor);
        }

        @Override
        public void checkAndRefreshInvalidObject(Object object, CacheKey cacheKey, ClassDescriptor descriptor) {
            delegate.checkAndRefreshInvalidObject(object, cacheKey, descriptor);
        }

        @Override
        public boolean isConsideredInvalid(Object object, CacheKey cacheKey, ClassDescriptor descriptor) {
            return delegate.isConsideredInvalid(object, cacheKey, descriptor);
        }

        @Override
        public void deferEvent(DescriptorEvent event) {
            delegate.deferEvent(event);
        }

        @Override
        public void deleteAllObjects(Collection domainObjects) throws DatabaseException, OptimisticLockException {
            delegate.deleteAllObjects(domainObjects);
        }

        @Override
        @Deprecated
        public void deleteAllObjects(Vector domainObjects) throws DatabaseException, OptimisticLockException {
            delegate.deleteAllObjects(domainObjects);
        }

        @Override
        public Object deleteObject(Object domainObject) throws DatabaseException, OptimisticLockException {
            return delegate.deleteObject(domainObject);
        }

        @Override
        public boolean doesObjectExist(Object object) throws DatabaseException {
            return delegate.doesObjectExist(object);
        }

        @Override
        public void dontLogMessages() {
            delegate.dontLogMessages();
        }

        @Override
        public void endOperationProfile(String operationName) {
            delegate.endOperationProfile(operationName);
        }

        @Override
        public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
            delegate.endOperationProfile(operationName, query, weight);
        }

        @Override
        public void updateProfile(String operationName, Object value) {
            delegate.updateProfile(operationName, value);
        }

        @Override
        public void updateTablePerTenantDescriptors(String property, Object value) {
            delegate.updateTablePerTenantDescriptors(property, value);
        }

        @Override
        public void incrementProfile(String operationName) {
            delegate.incrementProfile(operationName);
        }

        @Override
        public void incrementProfile(String operationName, DatabaseQuery query) {
            delegate.incrementProfile(operationName, query);
        }

        @Override
        public void executeDeferredEvents() {
            delegate.executeDeferredEvents();
        }

        @Override
        public Object executeCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
            return delegate.executeCall(call, translationRow, query);
        }

        @Override
        public void releaseConnectionAfterCall(DatabaseQuery query) {
            delegate.releaseConnectionAfterCall(query);
        }

        @Override
        public int executeNonSelectingCall(Call call) throws DatabaseException {
            return delegate.executeNonSelectingCall(call);
        }

        @Override
        public void executeNonSelectingSQL(String sqlString) throws DatabaseException {
            delegate.executeNonSelectingSQL(sqlString);
        }

        @Override
        public Object executeQuery(String queryName) throws DatabaseException {
            return delegate.executeQuery(queryName);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass, Object arg1) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass, arg1);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass, arg1, arg2);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2, Object arg3) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass, arg1, arg2, arg3);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass, List argumentValues) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass, argumentValues);
        }

        @Override
        public Object executeQuery(String queryName, Class domainClass, Vector argumentValues) throws DatabaseException {
            return delegate.executeQuery(queryName, domainClass, argumentValues);
        }

        @Override
        public Object executeQuery(String queryName, Object arg1) throws DatabaseException {
            return delegate.executeQuery(queryName, arg1);
        }

        @Override
        public Object executeQuery(String queryName, Object arg1, Object arg2) throws DatabaseException {
            return delegate.executeQuery(queryName, arg1, arg2);
        }

        @Override
        public Object executeQuery(String queryName, Object arg1, Object arg2, Object arg3) throws DatabaseException {
            return delegate.executeQuery(queryName, arg1, arg2, arg3);
        }

        @Override
        public Object executeQuery(String queryName, List argumentValues) throws DatabaseException {
            return delegate.executeQuery(queryName, argumentValues);
        }

        @Override
        public Object executeQuery(String queryName, Vector argumentValues) throws DatabaseException {
            return delegate.executeQuery(queryName, argumentValues);
        }

        @Override
        public Object executeQuery(DatabaseQuery query) throws DatabaseException {
            return delegate.executeQuery(query);
        }

        @Override
        public Object executeQuery(DatabaseQuery query, List argumentValues) throws DatabaseException {
            return delegate.executeQuery(query, argumentValues);
        }

        @Override
        public Object executeQuery(DatabaseQuery query, AbstractRecord row) throws DatabaseException {
            return delegate.executeQuery(query, row);
        }

        @Override
        public Object executeQuery(DatabaseQuery query, AbstractRecord row, int retryCount) throws DatabaseException {
            return delegate.executeQuery(query, row, retryCount);
        }

        @Override
        public Object retryQuery(DatabaseQuery query, AbstractRecord row, DatabaseException databaseException, int retryCount, AbstractSession executionSession) {
            return delegate.retryQuery(query, row, databaseException, retryCount, executionSession);
        }

        @Override
        public Vector executeSelectingCall(Call call) throws DatabaseException {
            return delegate.executeSelectingCall(call);
        }

        @Override
        public Vector executeSQL(String sqlString) throws DatabaseException {
            return delegate.executeSQL(sqlString);
        }

        @Override
        public Accessor getAccessor() {
            return delegate.getAccessor();
        }

        @Override
        public Collection<Accessor> getAccessors() {
            return delegate.getAccessors();
        }

        @Override
        public Collection<Accessor> getAccessors(Call call, AbstractRecord translationRow, DatabaseQuery query) {
            return delegate.getAccessors(call, translationRow, query);
        }

        @Override
        public Object basicExecuteCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
            return delegate.basicExecuteCall(call, translationRow, query);
        }

        @Override
        public ExposedNodeLinkedList getActiveCommandThreads() {
            return delegate.getActiveCommandThreads();
        }

        @Override
        public Session getActiveSession() {
            return delegate.getActiveSession();
        }

        @Override
        public UnitOfWork getActiveUnitOfWork() {
            return delegate.getActiveUnitOfWork();
        }

        @Override
        public Map getAliasDescriptors() {
            return delegate.getAliasDescriptors();
        }

        @Override
        public AsOfClause getAsOfClause() {
            return delegate.getAsOfClause();
        }

        @Override
        public AbstractSession getBroker() {
            return delegate.getBroker();
        }

        @Override
        public AbstractSession getRootSession(DatabaseQuery query) {
            return delegate.getRootSession(query);
        }

        @Override
        public AbstractSession getParent() {
            return delegate.getParent();
        }

        @Override
        public AbstractSession getParentIdentityMapSession(DatabaseQuery query) {
            return delegate.getParentIdentityMapSession(query);
        }

        @Override
        public AbstractSession getParentIdentityMapSession(DatabaseQuery query, boolean canReturnSelf, boolean terminalOnly) {
            return delegate.getParentIdentityMapSession(query, canReturnSelf, terminalOnly);
        }

        @Override
        public AbstractSession getParentIdentityMapSession(ClassDescriptor descriptor, boolean canReturnSelf, boolean terminalOnly) {
            return delegate.getParentIdentityMapSession(descriptor, canReturnSelf, terminalOnly);
        }

        @Override
        public Integer getPessimisticLockTimeoutDefault() {
            return delegate.getPessimisticLockTimeoutDefault();
        }

        @Override
        public int getQueryTimeoutDefault() {
            return delegate.getQueryTimeoutDefault();
        }

        @Override
        public EntityListenerInjectionManager getEntityListenerInjectionManager() {
            return delegate.getEntityListenerInjectionManager();
        }

        @Override
        public AbstractSession getExecutionSession(DatabaseQuery query) {
            return delegate.getExecutionSession(query);
        }

        @Override
        public boolean hasCommitManager() {
            return delegate.hasCommitManager();
        }

        @Override
        public CommitManager getCommitManager() {
            return delegate.getCommitManager();
        }

        @Override
        public Vector getDefaultReadOnlyClasses() {
            return delegate.getDefaultReadOnlyClasses();
        }

        @Override
        public ClassDescriptor getClassDescriptor(Class theClass) {
            return delegate.getClassDescriptor(theClass);
        }

        @Override
        public ClassDescriptor getClassDescriptor(Object domainObject) {
            return delegate.getClassDescriptor(domainObject);
        }

        @Override
        public ClassDescriptor getClassDescriptorForAlias(String alias) {
            return delegate.getClassDescriptorForAlias(alias);
        }

        @Override
        public ClassDescriptor getDescriptor(Class theClass) {
            return delegate.getDescriptor(theClass);
        }

        @Override
        public ClassDescriptor getDescriptor(Object domainObject) {
            return delegate.getDescriptor(domainObject);
        }

        @Override
        public ClassDescriptor getDescriptorForAlias(String alias) {
            return delegate.getDescriptorForAlias(alias);
        }

        @Override
        public Map<Class, ClassDescriptor> getDescriptors() {
            return delegate.getDescriptors();
        }

        @Override
        public boolean hasEventManager() {
            return delegate.hasEventManager();
        }

        @Override
        public SessionEventManager getEventManager() {
            return delegate.getEventManager();
        }

        @Override
        public String getExceptionHandlerClass() {
            return delegate.getExceptionHandlerClass();
        }

        @Override
        public ExceptionHandler getExceptionHandler() {
            return delegate.getExceptionHandler();
        }

        @Override
        public ExternalTransactionController getExternalTransactionController() {
            return delegate.getExternalTransactionController();
        }

        @Override
        public IdentityMapAccessor getIdentityMapAccessor() {
            return delegate.getIdentityMapAccessor();
        }

        @Override
        public org.eclipse.persistence.internal.sessions.IdentityMapAccessor getIdentityMapAccessorInstance() {
            return delegate.getIdentityMapAccessorInstance();
        }

        @Override
        public IntegrityChecker getIntegrityChecker() {
            return delegate.getIntegrityChecker();
        }

        @Override
        public List<DatabaseQuery> getJPAQueries() {
            return delegate.getJPAQueries();
        }

        @Override
        public List<DatabaseQuery> getJPATablePerTenantQueries() {
            return delegate.getJPATablePerTenantQueries();
        }

        @Override
        public Writer getLog() {
            return delegate.getLog();
        }

        @Override
        public String getLogSessionString() {
            return delegate.getLogSessionString();
        }

        @Override
        public String getSessionTypeString() {
            return delegate.getSessionTypeString();
        }

        @Override
        public String getStaticMetamodelClass(String modelClassName) {
            return delegate.getStaticMetamodelClass(modelClassName);
        }

        @Override
        public DatabaseLogin getLogin() {
            return delegate.getLogin();
        }

        @Override
        public Login getDatasourceLogin() {
            return delegate == null ? null : delegate.getDatasourceLogin();
        }

        @Override
        public ClassDescriptor getMappedSuperclass(String className) {
            return delegate.getMappedSuperclass(className);
        }

        @Override
        public Set<String> getMultitenantContextProperties() {
            return delegate.getMultitenantContextProperties();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Number getNextSequenceNumberValue(Class domainClass) {
            return delegate.getNextSequenceNumberValue(domainClass);
        }

        @Override
        public int getNumberOfActiveUnitsOfWork() {
            return delegate.getNumberOfActiveUnitsOfWork();
        }
    }

    private static class DatabasePlatformDecorator extends DatabasePlatform {
        private final DatabasePlatform delegate;

        private DatabasePlatformDecorator(DatabasePlatform delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean shouldBindLiterals() {
            return false;
        }

        @Override
        public void setShouldBindLiterals(boolean shouldBindLiterals) {
            delegate.setShouldBindLiterals(shouldBindLiterals);
        }

        @Override
        public boolean isDynamicSQLRequiredForFunctions() {
            return delegate.isDynamicSQLRequiredForFunctions();
        }

        @Override
        public Object getRefValue(Ref ref, Connection connection) throws SQLException {
            return delegate.getRefValue(ref, connection);
        }

        @Override
        public Object getRefValue(Ref ref, AbstractSession executionSession, Connection connection) throws SQLException {
            return delegate.getRefValue(ref, executionSession, connection);
        }

        @Override
        public void printStoredFunctionReturnKeyWord(Writer writer) throws IOException {
            delegate.printStoredFunctionReturnKeyWord(writer);
        }

        @Override
        public void printSQLSelectStatement(DatabaseCall call, ExpressionSQLPrinter printer, SQLSelectStatement statement) {
            delegate.printSQLSelectStatement(call, printer, statement);
        }

        @Override
        public boolean shouldPrintLockingClauseAfterWhereClause() {
            return delegate.shouldPrintLockingClauseAfterWhereClause();
        }

        @Override
        public boolean supportsIndividualTableLocking() {
            return delegate.supportsIndividualTableLocking();
        }

        @Override
        public boolean supportsLockingQueriesWithMultipleTables() {
            return delegate.supportsLockingQueriesWithMultipleTables();
        }

        @Override
        public boolean shouldPrintAliasForUpdate() {
            return delegate.shouldPrintAliasForUpdate();
        }

        @Override
        public String buildCreateIndex(String fullTableName, String indexName, String... columnNames) {
            return delegate.buildCreateIndex(fullTableName, indexName, columnNames);
        }

        @Override
        public String buildCreateIndex(String fullTableName, String indexName, String qualifier, boolean isUnique, String... columnNames) {
            return delegate.buildCreateIndex(fullTableName, indexName, qualifier, isUnique, columnNames);
        }

        @Override
        public String buildDropIndex(String fullTableName, String indexName) {
            return delegate.buildDropIndex(fullTableName, indexName);
        }

        @Override
        public String buildDropIndex(String fullTableName, String indexName, String qualifier) {
            return delegate.buildDropIndex(fullTableName, indexName, qualifier);
        }

        @Override
        public Writer buildSequenceObjectCreationWriter(Writer writer, String fullSeqName, int increment, int start) throws IOException {
            return delegate.buildSequenceObjectCreationWriter(writer, fullSeqName, increment, start);
        }

        @Override
        public Writer buildSequenceObjectDeletionWriter(Writer writer, String fullSeqName) throws IOException {
            return delegate.buildSequenceObjectDeletionWriter(writer, fullSeqName);
        }

        @Override
        public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String fullSeqName, int increment) throws IOException {
            return delegate.buildSequenceObjectAlterIncrementWriter(writer, fullSeqName, increment);
        }

        @Override
        public boolean isAlterSequenceObjectSupported() {
            return delegate.isAlterSequenceObjectSupported();
        }

        @Override
        public boolean supportsNestingOuterJoins() {
            return delegate.supportsNestingOuterJoins();
        }

        @Override
        public boolean supportsOuterJoinsWithBrackets() {
            return delegate.supportsOuterJoinsWithBrackets();
        }

        @Override
        public void freeTemporaryObject(Object value) throws SQLException {
            delegate.freeTemporaryObject(value);
        }

        @Override
        public void initializeConnectionData(Connection connection) throws SQLException {
            delegate.initializeConnectionData(connection);
        }

        @Override
        public void writeAddColumnClause(Writer writer, AbstractSession session, TableDefinition table, FieldDefinition field) throws IOException {
            delegate.writeAddColumnClause(writer, session, table, field);
        }

        @Override
        public boolean supportsConnectionUserName() {
            return delegate.supportsConnectionUserName();
        }

        @Override
        public String getConnectionUserName() {
            return delegate.getConnectionUserName();
        }

        @Override
        public boolean getDefaultNativeSequenceToTable() {
            return delegate.getDefaultNativeSequenceToTable();
        }

        @Override
        public void setDefaultNativeSequenceToTable(boolean defaultNativeSequenceToTable) {
            delegate.setDefaultNativeSequenceToTable(defaultNativeSequenceToTable);
        }

        @Override
        public void addOperator(ExpressionOperator operator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            return delegate.clone();
        }

        @Override
        public void sequencesAfterCloneCleanup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException {
            return delegate.convertObject(sourceObject, javaClass);
        }

        @Override
        public ConversionManager getConversionManager() {
            return delegate.getConversionManager();
        }

        @Override
        public void setConversionManager(ConversionManager conversionManager) {
            delegate.setConversionManager(conversionManager);
        }

        @Override
        public String getEndDelimiter() {
            return delegate.getEndDelimiter();
        }

        @Override
        public void setEndDelimiter(String endDelimiter) {
            delegate.setEndDelimiter(endDelimiter);
        }

        @Override
        public ExpressionOperator getOperator(int selector) {
            return delegate.getOperator(selector);
        }

        @Override
        public Map getPlatformOperators() {
            return delegate.getPlatformOperators();
        }

        @Override
        public ValueReadQuery getSelectSequenceQuery() {
            return delegate.getSelectSequenceQuery();
        }

        @Override
        public String getStartDelimiter() {
            return delegate.getStartDelimiter();
        }

        @Override
        public void setStartDelimiter(String startDelimiter) {
            delegate.setStartDelimiter(startDelimiter);
        }

        @Override
        public String getTableQualifier() {
            return delegate.getTableQualifier();
        }

        @Override
        public Timestamp getTimestampFromServer(AbstractSession session, String sessionName) {
            return delegate.getTimestampFromServer(session, sessionName);
        }

        @Override
        public ValueReadQuery getTimestampQuery() {
            return delegate.getTimestampQuery();
        }

        @Override
        public DataModifyQuery getUpdateSequenceQuery() {
            return delegate.getUpdateSequenceQuery();
        }

        @Override
        public void initializePlatformOperators() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void initializeDefaultQueries(DescriptorQueryManager queryManager, AbstractSession session) {
            delegate.initializeDefaultQueries(queryManager, session);
        }

        @Override
        public boolean isAccess() {
            return delegate.isAccess();
        }

        @Override
        public boolean isAttunity() {
            return delegate.isAttunity();
        }

        @Override
        public boolean isCloudscape() {
            return delegate.isCloudscape();
        }

        @Override
        public boolean isDerby() {
            return delegate.isDerby();
        }

        @Override
        public boolean isDB2() {
            return delegate.isDB2();
        }

        @Override
        public boolean isHANA() {
            return delegate.isHANA();
        }

        @Override
        public boolean isH2() {
            return delegate.isH2();
        }

        @Override
        public boolean isDBase() {
            return delegate.isDBase();
        }

        @Override
        public boolean isHSQL() {
            return delegate.isHSQL();
        }

        @Override
        public boolean isInformix() {
            return delegate.isInformix();
        }

        @Override
        public boolean isMySQL() {
            return delegate.isMySQL();
        }

        @Override
        public boolean isODBC() {
            return delegate.isODBC();
        }

        @Override
        public boolean isOracle() {
            return delegate.isOracle();
        }

        @Override
        public boolean isOracle9() {
            return delegate.isOracle9();
        }

        @Override
        public boolean isPervasive() {
            return delegate.isPervasive();
        }

        @Override
        public boolean isPostgreSQL() {
            return delegate.isPostgreSQL();
        }

        @Override
        public boolean isPointBase() {
            return delegate.isPointBase();
        }

        @Override
        public boolean isSQLAnywhere() {
            return delegate.isSQLAnywhere();
        }

        @Override
        public boolean isFirebird() {
            return delegate.isFirebird();
        }

        @Override
        public boolean isSQLServer() {
            return delegate.isSQLServer();
        }

        @Override
        public boolean isSybase() {
            return delegate.isSybase();
        }

        @Override
        public boolean isSymfoware() {
            return delegate.isSymfoware();
        }

        @Override
        public boolean isTimesTen() {
            return delegate.isTimesTen();
        }

        @Override
        public boolean isTimesTen7() {
            return delegate.isTimesTen7();
        }

        @Override
        public boolean isMaxDB() {
            return delegate.isMaxDB();
        }

        @Override
        public void setSelectSequenceNumberQuery(ValueReadQuery seqQuery) {
            delegate.setSelectSequenceNumberQuery(seqQuery);
        }

        @Override
        public void setSequencePreallocationSize(int size) {
            delegate.setSequencePreallocationSize(size);
        }

        @Override
        public void setTableQualifier(String qualifier) {
            delegate.setTableQualifier(qualifier);
        }

        @Override
        public void setTimestampQuery(ValueReadQuery tsQuery) {
            delegate.setTimestampQuery(tsQuery);
        }

        @Override
        public void setUpdateSequenceQuery(DataModifyQuery updateSequenceNumberQuery) {
            delegate.setUpdateSequenceQuery(updateSequenceNumberQuery);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public Vector getDataTypesConvertedFrom(Class javaClass) {
            return delegate.getDataTypesConvertedFrom(javaClass);
        }

        @Override
        public Vector getDataTypesConvertedTo(Class javaClass) {
            return delegate.getDataTypesConvertedTo(javaClass);
        }

        @Override
        public Sequence getDefaultSequence() {
            return delegate.getDefaultSequence();
        }

        @Override
        public boolean hasDefaultSequence() {
            return delegate.hasDefaultSequence();
        }

        @Override
        public void setDefaultSequence(Sequence sequence) {
            delegate.setDefaultSequence(sequence);
        }

        @Override
        public void addSequence(Sequence sequence) {
            delegate.addSequence(sequence);
        }

        @Override
        public void addSequence(Sequence sequence, boolean isSessionConnected) {
            delegate.addSequence(sequence, isSessionConnected);
        }

        @Override
        public Sequence getSequence(String seqName) {
            return delegate.getSequence(seqName);
        }

        @Override
        public Sequence removeSequence(String seqName) {
            return delegate.removeSequence(seqName);
        }

        @Override
        public void removeAllSequences() {
            delegate.removeAllSequences();
        }

        @Override
        public Map getSequences() {
            return delegate.getSequences();
        }

        @Override
        public Map getSequencesToWrite() {
            return delegate.getSequencesToWrite();
        }

        @Override
        public Sequence getDefaultSequenceToWrite() {
            return delegate.getDefaultSequenceToWrite();
        }

        @Override
        public void setSequences(Map sequences) {
            delegate.setSequences(sequences);
        }

        @Override
        public boolean usesPlatformDefaultSequence() {
            return delegate.usesPlatformDefaultSequence();
        }

        @Override
        public ConnectionCustomizer createConnectionCustomizer(Accessor accessor, AbstractSession session) {
            return delegate.createConnectionCustomizer(accessor, session);
        }

        @Override
        public boolean shouldPrepare(DatabaseQuery query) {
            return delegate.shouldPrepare(query);
        }

        @Override
        public boolean shouldSelectIncludeOrderBy() {
            return delegate.shouldSelectIncludeOrderBy();
        }

        @Override
        public boolean shouldSelectDistinctIncludeOrderBy() {
            return delegate.shouldSelectDistinctIncludeOrderBy();
        }

        @Override
        public boolean shouldNativeSequenceUseTransaction() {
            return delegate.shouldNativeSequenceUseTransaction();
        }

        @Override
        public boolean supportsIdentity() {
            return delegate.supportsIdentity();
        }

        @Override
        public boolean supportsNativeSequenceNumbers() {
            return delegate.supportsNativeSequenceNumbers();
        }

        @Override
        public boolean supportsSequenceObjects() {
            return delegate.supportsSequenceObjects();
        }

        @Override
        public ValueReadQuery buildSelectQueryForSequenceObject() {
            return delegate.buildSelectQueryForSequenceObject();
        }

        @Override
        public ValueReadQuery buildSelectQueryForSequenceObject(String qualifiedSeqName, Integer size) {
            return delegate.buildSelectQueryForSequenceObject(qualifiedSeqName, size);
        }

        @Override
        public ValueReadQuery buildSelectQueryForIdentity() {
            return delegate.buildSelectQueryForIdentity();
        }

        @Override
        public ValueReadQuery buildSelectQueryForIdentity(String seqName, Integer size) {
            return delegate.buildSelectQueryForIdentity(seqName, size);
        }

        @Override
        public DatasourceCall buildNativeCall(String queryString) {
            return delegate.buildNativeCall(queryString);
        }

        @Override
        public void initialize() {
            delegate.initialize();
        }

        @Override
        public boolean hasPartitioningCallback() {
            return delegate.hasPartitioningCallback();
        }

        @Override
        public DataPartitioningCallback getPartitioningCallback() {
            return delegate.getPartitioningCallback();
        }

        @Override
        public void setPartitioningCallback(DataPartitioningCallback partitioningCallback) {
            delegate.setPartitioningCallback(partitioningCallback);
        }

        @Override
        public boolean isCastRequired() {
            return delegate.isCastRequired();
        }

        @Override
        public void setIsCastRequired(boolean isCastRequired) {
            delegate.setIsCastRequired(isCastRequired);
        }

        @Override
        public Map<String, StructConverter> getStructConverters() {
            return delegate.getStructConverters();
        }

        @Override
        public String getTableCreationSuffix() {
            return delegate.getTableCreationSuffix();
        }

        @Override
        public Map<Class, StructConverter> getTypeConverters() {
            return delegate.getTypeConverters();
        }

        @Override
        public void addStructConverter(StructConverter converter) {
            delegate.addStructConverter(converter);
        }

        @Override
        public int addBatch(PreparedStatement statement) throws SQLException {
            return delegate.addBatch(statement);
        }

        @Override
        public boolean allowsSizeInProcedureArguments() {
            return delegate.allowsSizeInProcedureArguments();
        }

        @Override
        public void appendLiteralToCall(Call call, Writer writer, Object literal) {
            super.appendLiteralToCall(call, writer, literal);
        }

        @Override
        public void appendLiteralToCallWithBinding(Call call, Writer writer, Object literal) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void appendParameter(Call call, Writer writer, Object parameter) {
            delegate.appendParameter(call, writer, parameter);
        }

        @Override
        public int appendParameterInternal(Call call, Writer writer, Object parameter) {
            return delegate.appendParameterInternal(call, writer, parameter);
        }

        @Override
        public void autoCommit(DatabaseAccessor accessor) throws SQLException {
            delegate.autoCommit(accessor);
        }

        @Override
        public void beginTransaction(DatabaseAccessor accessor) throws SQLException {
            delegate.beginTransaction(accessor);
        }

        @Override
        public Expression buildBatchCriteria(ExpressionBuilder builder, Expression field) {
            return delegate.buildBatchCriteria(builder, field);
        }

        @Override
        public Expression buildBatchCriteriaForComplexId(ExpressionBuilder builder, List<Expression> fields) {
            return delegate.buildBatchCriteriaForComplexId(builder, fields);
        }

        @Override
        public DatabaseCall buildCallWithReturning(SQLCall sqlCall, Vector returnFields) {
            return delegate.buildCallWithReturning(sqlCall, returnFields);
        }

        @Override
        public boolean shouldUseGetSetNString() {
            return delegate.shouldUseGetSetNString();
        }

        @Override
        public boolean getDriverSupportsNVarChar() {
            return delegate.getDriverSupportsNVarChar();
        }

        @Override
        public void setDriverSupportsNVarChar(boolean b) {
            delegate.setDriverSupportsNVarChar(b);
        }

        @Override
        public boolean getUseNationalCharacterVaryingTypeForString() {
            return delegate.getUseNationalCharacterVaryingTypeForString();
        }

        @Override
        public void setUseNationalCharacterVaryingTypeForString(boolean b) {
            delegate.setUseNationalCharacterVaryingTypeForString(b);
        }

        @Override
        public String buildProcedureCallString(StoredProcedureCall call, AbstractSession session, AbstractRecord row) {
            return delegate.buildProcedureCallString(call, session, row);
        }

        @Override
        public boolean canBuildCallWithReturning() {
            return delegate.canBuildCallWithReturning();
        }

        @Override
        public boolean canBatchWriteWithOptimisticLocking(DatabaseCall call) {
            return delegate.canBatchWriteWithOptimisticLocking(call);
        }

        @Override
        public int computeMaxRowsForSQL(int firstResultIndex, int maxResults) {
            return delegate.computeMaxRowsForSQL(firstResultIndex, maxResults);
        }

        @Override
        public void commitTransaction(DatabaseAccessor accessor) throws SQLException {
            delegate.commitTransaction(accessor);
        }

        @Override
        public DatabaseQuery getVPDClearIdentifierQuery(String vpdIdentifier) {
            return delegate.getVPDClearIdentifierQuery(vpdIdentifier);
        }

        @Override
        public String getVPDCreationFunctionString(String tableName, String tenantFieldName) {
            return delegate.getVPDCreationFunctionString(tableName, tenantFieldName);
        }

        @Override
        public String getVPDCreationPolicyString(String tableName, AbstractSession session) {
            return delegate.getVPDCreationPolicyString(tableName, session);
        }

        @Override
        public String getVPDDeletionString(String tableName, AbstractSession session) {
            return delegate.getVPDDeletionString(tableName, session);
        }

        @Override
        public DatabaseQuery getVPDSetIdentifierQuery(String vpdIdentifier) {
            return delegate.getVPDSetIdentifierQuery(vpdIdentifier);
        }

        @Override
        public Object convertToDatabaseType(Object value) {
            return delegate.convertToDatabaseType(value);
        }

        @Override
        public void copyInto(Platform platform) {
            delegate.copyInto(platform);
        }

        @Override
        public String getBatchBeginString() {
            return delegate.getBatchBeginString();
        }

        @Override
        public boolean isRowCountOutputParameterRequired() {
            return delegate.isRowCountOutputParameterRequired();
        }

        @Override
        public String getBatchRowCountDeclareString() {
            return delegate.getBatchRowCountDeclareString();
        }

        @Override
        public String getBatchRowCountAssignString() {
            return delegate.getBatchRowCountAssignString();
        }

        @Override
        public String getBatchRowCountReturnString() {
            return delegate.getBatchRowCountReturnString();
        }

        @Override
        public String getBatchDelimiterString() {
            return delegate.getBatchDelimiterString();
        }

        @Override
        public String getBatchEndString() {
            return delegate.getBatchEndString();
        }

        @Override
        public Connection getConnection(AbstractSession session, Connection connection) {
            return delegate.getConnection(session, connection);
        }

        @Override
        public String getConstraintDeletionString() {
            return delegate.getConstraintDeletionString();
        }

        @Override
        public String getUniqueConstraintDeletionString() {
            return delegate.getUniqueConstraintDeletionString();
        }

        @Override
        public String getCreateViewString() {
            return delegate.getCreateViewString();
        }

        @Override
        public String getDropCascadeString() {
            return delegate.getDropCascadeString();
        }

        @Override
        public Object getCustomModifyValueForCall(Call call, Object value, DatabaseField field, boolean shouldBind) {
            return delegate.getCustomModifyValueForCall(call, value, field, shouldBind);
        }

        @Override
        public String getProcedureEndString() {
            return delegate.getProcedureEndString();
        }

        @Override
        public String getProcedureBeginString() {
            return delegate.getProcedureBeginString();
        }

        @Override
        public String getProcedureAsString() {
            return delegate.getProcedureAsString();
        }

        @Override
        public Map<String, Class> getClassTypes() {
            return delegate.getClassTypes();
        }

        @Override
        public String getAssignmentString() {
            return delegate.getAssignmentString();
        }

        @Override
        public int getCastSizeForVarcharParameter() {
            return delegate.getCastSizeForVarcharParameter();
        }

        @Override
        public String getCreationInOutputProcedureToken() {
            return delegate.getCreationInOutputProcedureToken();
        }

        @Override
        public String getCreationOutputProcedureToken() {
            return delegate.getCreationOutputProcedureToken();
        }

        @Override
        public int getCursorCode() {
            return delegate.getCursorCode();
        }

        @Override
        public String getDefaultSequenceTableName() {
            return delegate.getDefaultSequenceTableName();
        }

        @Override
        public String getCreateDatabaseSchemaString(String schema) {
            return delegate.getCreateDatabaseSchemaString(schema);
        }

        @Override
        public String getDropDatabaseSchemaString(String schema) {
            return delegate.getDropDatabaseSchemaString(schema);
        }

        @Override
        public FieldTypeDefinition getFieldTypeDefinition(Class javaClass) {
            return delegate.getFieldTypeDefinition(javaClass);
        }

        @Override
        public Map<Class, FieldTypeDefinition> getFieldTypes() {
            return delegate.getFieldTypes();
        }

        @Override
        public String getFunctionCallHeader() {
            return delegate.getFunctionCallHeader();
        }

        @Override
        public String getIdentifierQuoteCharacter() {
            return delegate.getIdentifierQuoteCharacter();
        }

        @Override
        public String getInOutputProcedureToken() {
            return delegate.getInOutputProcedureToken();
        }

        @Override
        public String getJDBCOuterJoinString() {
            return delegate.getJDBCOuterJoinString();
        }

        @Override
        public int getJDBCTypeForSetNull(DatabaseField field) {
            return delegate.getJDBCTypeForSetNull(field);
        }

        @Override
        public int getJDBCType(DatabaseField field) {
            return delegate.getJDBCType(field);
        }

        @Override
        public int getJDBCType(Class javaType) {
            return delegate.getJDBCType(javaType);
        }

        @Override
        public String getJdbcTypeName(int jdbcType) {
            return delegate.getJdbcTypeName(jdbcType);
        }

        @Override
        public long minimumTimeIncrement() {
            return delegate.minimumTimeIncrement();
        }

        @Override
        public int getMaxBatchWritingSize() {
            return delegate.getMaxBatchWritingSize();
        }

        @Override
        public int getMaxFieldNameSize() {
            return delegate.getMaxFieldNameSize();
        }

        @Override
        public int getMaxForeignKeyNameSize() {
            return delegate.getMaxForeignKeyNameSize();
        }

        @Override
        public int getMaxIndexNameSize() {
            return delegate.getMaxIndexNameSize();
        }

        @Override
        public int getMaxUniqueKeyNameSize() {
            return delegate.getMaxUniqueKeyNameSize();
        }

        @Override
        public Object getObjectFromResultSet(ResultSet resultSet, int columnNumber, int type, AbstractSession session) throws SQLException {
            return delegate.getObjectFromResultSet(resultSet, columnNumber, type, session);
        }

        @Override
        public String getInputProcedureToken() {
            return delegate.getInputProcedureToken();
        }

        @Override
        public String getIndexNamePrefix(boolean isUniqueSetOnField) {
            return delegate.getIndexNamePrefix(isUniqueSetOnField);
        }

        @Override
        public String getOutputProcedureToken() {
            return delegate.getOutputProcedureToken();
        }

        @Override
        public String getPingSQL() {
            return delegate.getPingSQL();
        }

        @Override
        public String getProcedureArgumentSetter() {
            return delegate.getProcedureArgumentSetter();
        }

        @Override
        public String getProcedureArgumentString() {
            return delegate.getProcedureArgumentString();
        }

        @Override
        public String getProcedureCallHeader() {
            return delegate.getProcedureCallHeader();
        }

        @Override
        public String getProcedureCallTail() {
            return delegate.getProcedureCallTail();
        }

        @Override
        public String getQualifiedSequenceTableName() {
            return delegate.getQualifiedSequenceTableName();
        }

        @Override
        public String getQualifiedName(String name) {
            return delegate.getQualifiedName(name);
        }

        @Override
        public String getNoWaitString() {
            return delegate.getNoWaitString();
        }

        @Override
        public String getSelectForUpdateNoWaitString() {
            return delegate.getSelectForUpdateNoWaitString();
        }

        @Override
        public String getSelectForUpdateOfString() {
            return delegate.getSelectForUpdateOfString();
        }

        @Override
        public String getSelectForUpdateString() {
            return delegate.getSelectForUpdateString();
        }

        @Override
        public String getSelectForUpdateWaitString(Integer waitTimeout) {
            return delegate.getSelectForUpdateWaitString(waitTimeout);
        }

        @Override
        public String getSequenceCounterFieldName() {
            return delegate.getSequenceCounterFieldName();
        }

        @Override
        public String getSequenceNameFieldName() {
            return delegate.getSequenceNameFieldName();
        }

        @Override
        public int getSequencePreallocationSize() {
            return delegate.getSequencePreallocationSize();
        }

        @Override
        public String getSequenceTableName() {
            return delegate.getSequenceTableName();
        }

        @Override
        public int getStatementCacheSize() {
            return delegate.getStatementCacheSize();
        }

        @Override
        public String getStoredProcedureParameterPrefix() {
            return delegate.getStoredProcedureParameterPrefix();
        }

        @Override
        public String getStoredProcedureTerminationToken() {
            return delegate.getStoredProcedureTerminationToken();
        }

        @Override
        public void setStoredProcedureTerminationToken(String storedProcedureTerminationToken) {
            delegate.setStoredProcedureTerminationToken(storedProcedureTerminationToken);
        }

        @Override
        public int getStringBindingSize() {
            return delegate.getStringBindingSize();
        }

        @Override
        public int getTransactionIsolation() {
            return delegate.getTransactionIsolation();
        }

        @Override
        public boolean isInformixOuterJoin() {
            return delegate.isInformixOuterJoin();
        }

        @Override
        public boolean isJDBCExecuteCompliant() {
            return delegate.isJDBCExecuteCompliant();
        }

        @Override
        public boolean isLockTimeoutException(DatabaseException e) {
            return delegate.isLockTimeoutException(e);
        }

        @Override
        public boolean isForUpdateCompatibleWithDistinct() {
            return delegate.isForUpdateCompatibleWithDistinct();
        }

        @Override
        public boolean isLobCompatibleWithDistinct() {
            return delegate.isLobCompatibleWithDistinct();
        }

        @Override
        public Hashtable maximumNumericValues() {
            return delegate.maximumNumericValues();
        }

        @Override
        public Hashtable minimumNumericValues() {
            return delegate.minimumNumericValues();
        }

        @Override
        public Statement prepareBatchStatement(Statement statement, int maxBatchWritingSize) throws SQLException {
            return delegate.prepareBatchStatement(statement, maxBatchWritingSize);
        }

        @Override
        public void printFieldIdentityClause(Writer writer) throws ValidationException {
            delegate.printFieldIdentityClause(writer);
        }

        @Override
        public void printFieldNotNullClause(Writer writer) throws ValidationException {
            delegate.printFieldNotNullClause(writer);
        }

        @Override
        public void printFieldNullClause(Writer writer) throws ValidationException {
            delegate.printFieldNullClause(writer);
        }

        @Override
        public int printValuelist(int[] theObjects, DatabaseCall call, Writer writer) throws IOException {
            return delegate.printValuelist(theObjects, call, writer);
        }

        @Override
        public int printValuelist(Collection theObjects, DatabaseCall call, Writer writer) throws IOException {
            return delegate.printValuelist(theObjects, call, writer);
        }

        @Override
        public void registerOutputParameter(CallableStatement statement, int index, int jdbcType) throws SQLException {
            delegate.registerOutputParameter(statement, index, jdbcType);
        }

        @Override
        public boolean requiresNamedPrimaryKeyConstraints() {
            return delegate.requiresNamedPrimaryKeyConstraints();
        }

        @Override
        public boolean requiresProcedureBrackets() {
            return delegate.requiresProcedureBrackets();
        }

        @Override
        public boolean requiresProcedureCallBrackets() {
            return delegate.requiresProcedureCallBrackets();
        }

        @Override
        public boolean requiresProcedureCallOuputToken() {
            return delegate.requiresProcedureCallOuputToken();
        }

        @Override
        public boolean requiresTypeNameToRegisterOutputParameter() {
            return delegate.requiresTypeNameToRegisterOutputParameter();
        }

        @Override
        public boolean requiresUniqueConstraintCreationOnTableCreate() {
            return delegate.requiresUniqueConstraintCreationOnTableCreate();
        }

        @Override
        public void retrieveFirstPrimaryKeyOrOne(ReportQuery subselect) {
            delegate.retrieveFirstPrimaryKeyOrOne(subselect);
        }

        @Override
        public void rollbackTransaction(DatabaseAccessor accessor) throws SQLException {
            delegate.rollbackTransaction(accessor);
        }

        @Override
        public void setCastSizeForVarcharParameter(int maxLength) {
            delegate.setCastSizeForVarcharParameter(maxLength);
        }

        @Override
        public void setClassTypes(Hashtable classTypes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCursorCode(int cursorCode) {
            delegate.setCursorCode(cursorCode);
        }

        @Override
        public void setDriverName(String driverName) {
            delegate.setDriverName(driverName);
        }

        @Override
        public void setFieldTypes(Hashtable theFieldTypes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMaxBatchWritingSize(int maxBatchWritingSize) {
            delegate.setMaxBatchWritingSize(maxBatchWritingSize);
        }

        @Override
        public void setSequenceCounterFieldName(String name) {
            delegate.setSequenceCounterFieldName(name);
        }

        @Override
        public void setSequenceNameFieldName(String name) {
            delegate.setSequenceNameFieldName(name);
        }

        @Override
        public void setSequenceTableName(String name) {
            delegate.setSequenceTableName(name);
        }

        @Override
        public void setShouldBindAllParameters(boolean shouldBindAllParameters) {
            delegate.setShouldBindAllParameters(shouldBindAllParameters);
        }

        @Override
        public void setShouldCacheAllStatements(boolean shouldCacheAllStatements) {
            delegate.setShouldCacheAllStatements(shouldCacheAllStatements);
        }

        @Override
        public void setShouldForceFieldNamesToUpperCase(boolean shouldForceFieldNamesToUpperCase) {
            delegate.setShouldForceFieldNamesToUpperCase(shouldForceFieldNamesToUpperCase);
        }

        @Override
        public void setShouldOptimizeDataConversion(boolean value) {
            delegate.setShouldOptimizeDataConversion(value);
        }

        @Override
        public void setShouldTrimStrings(boolean aBoolean) {
            delegate.setShouldTrimStrings(aBoolean);
        }

        @Override
        public void setStatementCacheSize(int statementCacheSize) {
            delegate.setStatementCacheSize(statementCacheSize);
        }

        @Override
        public void setStringBindingSize(int aSize) {
            delegate.setStringBindingSize(aSize);
        }

        @Override
        public void setSupportsAutoCommit(boolean supportsAutoCommit) {
            delegate.setSupportsAutoCommit(supportsAutoCommit);
        }

        @Override
        public void setTableCreationSuffix(String tableCreationSuffix) {
            delegate.setTableCreationSuffix(tableCreationSuffix);
        }

        @Override
        public void setTransactionIsolation(int isolationLevel) {
            delegate.setTransactionIsolation(isolationLevel);
        }

        @Override
        public void setUseJDBCStoredProcedureSyntax(Boolean useJDBCStoredProcedureSyntax) {
            delegate.setUseJDBCStoredProcedureSyntax(useJDBCStoredProcedureSyntax);
        }

        @Override
        public void setUsesBatchWriting(boolean usesBatchWriting) {
            delegate.setUsesBatchWriting(usesBatchWriting);
        }

        @Override
        public void setUsesByteArrayBinding(boolean usesByteArrayBinding) {
            delegate.setUsesByteArrayBinding(usesByteArrayBinding);
        }

        @Override
        public void setUsesJDBCBatchWriting(boolean usesJDBCBatchWriting) {
            delegate.setUsesJDBCBatchWriting(usesJDBCBatchWriting);
        }

        @Override
        public void setUsesNativeBatchWriting(boolean usesNativeBatchWriting) {
            delegate.setUsesNativeBatchWriting(usesNativeBatchWriting);
        }

        @Override
        public void setUsesNativeSQL(boolean usesNativeSQL) {
            delegate.setUsesNativeSQL(usesNativeSQL);
        }

        @Override
        public BatchWritingMechanism getBatchWritingMechanism() {
            return delegate.getBatchWritingMechanism();
        }

        @Override
        public void setBatchWritingMechanism(BatchWritingMechanism batchWritingMechanism) {
            delegate.setBatchWritingMechanism(batchWritingMechanism);
        }

        @Override
        public void setShouldUseRownumFiltering(boolean useRownumFiltering) {
            delegate.setShouldUseRownumFiltering(useRownumFiltering);
        }

        @Override
        public void setUsesStreamsForBinding(boolean usesStreamsForBinding) {
            delegate.setUsesStreamsForBinding(usesStreamsForBinding);
        }

        @Override
        public void setPrintOuterJoinInWhereClause(boolean printOuterJoinInWhereClause) {
            delegate.setPrintOuterJoinInWhereClause(printOuterJoinInWhereClause);
        }

        @Override
        public void setPrintInnerJoinInWhereClause(boolean printInnerJoinInWhereClause) {
            delegate.setPrintInnerJoinInWhereClause(printInnerJoinInWhereClause);
        }

        @Override
        public void setUsesStringBinding(boolean aBool) {
            delegate.setUsesStringBinding(aBool);
        }

        @Override
        public boolean shouldBindAllParameters() {
            return delegate.shouldBindAllParameters();
        }

        @Override
        public boolean shouldCacheAllStatements() {
            return delegate.shouldCacheAllStatements();
        }

        @Override
        public boolean shouldCreateIndicesForPrimaryKeys() {
            return delegate.shouldCreateIndicesForPrimaryKeys();
        }

        @Override
        public boolean shouldCreateIndicesOnUniqueKeys() {
            return delegate.shouldCreateIndicesOnUniqueKeys();
        }

        @Override
        public boolean shouldCreateIndicesOnForeignKeys() {
            return delegate.shouldCreateIndicesOnForeignKeys();
        }

        @Override
        public void setShouldCreateIndicesOnForeignKeys(boolean shouldCreateIndicesOnForeignKeys) {
            delegate.setShouldCreateIndicesOnForeignKeys(shouldCreateIndicesOnForeignKeys);
        }

        @Override
        public boolean shouldForceFieldNamesToUpperCase() {
            return delegate.shouldForceFieldNamesToUpperCase();
        }

        public static boolean shouldIgnoreCaseOnFieldComparisons() {
            return org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.shouldIgnoreCaseOnFieldComparisons();
        }

        @Override
        public boolean shouldIgnoreException(SQLException exception) {
            return delegate.shouldIgnoreException(exception);
        }

        @Override
        public boolean shouldOptimizeDataConversion() {
            return delegate.shouldOptimizeDataConversion();
        }

        @Override
        public boolean shouldPrintStoredProcedureVariablesAfterBeginString() {
            return delegate.shouldPrintStoredProcedureVariablesAfterBeginString();
        }

        @Override
        public boolean shouldPrintConstraintNameAfter() {
            return delegate.shouldPrintConstraintNameAfter();
        }

        @Override
        public boolean shouldPrintInOutputTokenBeforeType() {
            return delegate.shouldPrintInOutputTokenBeforeType();
        }

        @Override
        public boolean shouldPrintOuterJoinInWhereClause() {
            return delegate.shouldPrintOuterJoinInWhereClause();
        }

        @Override
        public boolean shouldPrintInnerJoinInWhereClause() {
            return delegate.shouldPrintInnerJoinInWhereClause();
        }

        @Override
        public boolean shouldPrintInputTokenAtStart() {
            return delegate.shouldPrintInputTokenAtStart();
        }

        @Override
        public boolean shouldPrintOutputTokenBeforeType() {
            return delegate.shouldPrintOutputTokenBeforeType();
        }

        @Override
        public boolean shouldPrintOutputTokenAtStart() {
            return delegate.shouldPrintOutputTokenAtStart();
        }

        @Override
        public boolean shouldPrintStoredProcedureArgumentNameInCall() {
            return delegate.shouldPrintStoredProcedureArgumentNameInCall();
        }

        @Override
        public boolean shouldPrintForUpdateClause() {
            return delegate.shouldPrintForUpdateClause();
        }

        @Override
        public boolean shouldTrimStrings() {
            return delegate.shouldTrimStrings();
        }

        @Override
        public boolean shouldUseCustomModifyForCall(DatabaseField field) {
            return delegate.shouldUseCustomModifyForCall(field);
        }

        @Override
        public boolean shouldUseJDBCOuterJoinSyntax() {
            return delegate.shouldUseJDBCOuterJoinSyntax();
        }

        @Override
        public boolean shouldUseRownumFiltering() {
            return delegate.shouldUseRownumFiltering();
        }

        @Override
        public boolean supportsANSIInnerJoinSyntax() {
            return delegate.supportsANSIInnerJoinSyntax();
        }

        @Override
        public boolean supportsAutoCommit() {
            return delegate.supportsAutoCommit();
        }

        @Override
        public boolean supportsAutoConversionToNumericForArithmeticOperations() {
            return delegate.supportsAutoConversionToNumericForArithmeticOperations();
        }

        @Override
        public boolean supportsForeignKeyConstraints() {
            return delegate.supportsForeignKeyConstraints();
        }

        @Override
        public boolean supportsUniqueKeyConstraints() {
            return delegate.supportsUniqueKeyConstraints();
        }

        @Override
        public boolean supportsVPD() {
            return delegate.supportsVPD();
        }

        @Override
        public boolean supportsPrimaryKeyConstraint() {
            return delegate.supportsPrimaryKeyConstraint();
        }

        @Override
        public boolean supportsStoredFunctions() {
            return delegate.supportsStoredFunctions();
        }

        @Override
        public boolean supportsDeleteOnCascade() {
            return delegate.supportsDeleteOnCascade();
        }

        @Override
        public int executeBatch(Statement statement, boolean isStatementPrepared) throws SQLException {
            return delegate.executeBatch(statement, isStatementPrepared);
        }

        @Override
        public Object executeStoredProcedure(DatabaseCall dbCall, PreparedStatement statement, DatabaseAccessor accessor, AbstractSession session) throws SQLException {
            return delegate.executeStoredProcedure(dbCall, statement, accessor, session);
        }

        @Override
        public void setPingSQL(String pingSQL) {
            delegate.setPingSQL(pingSQL);
        }

        @Override
        public void setParameterValueInDatabaseCall(Object parameter, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
            delegate.setParameterValueInDatabaseCall(parameter, statement, index, session);
        }

        @Override
        public boolean usesBatchWriting() {
            return delegate.usesBatchWriting();
        }

        @Override
        public boolean usesByteArrayBinding() {
            return delegate.usesByteArrayBinding();
        }

        @Override
        public boolean usesSequenceTable() {
            return delegate.usesSequenceTable();
        }

        @Override
        public boolean usesJDBCBatchWriting() {
            return delegate.usesJDBCBatchWriting();
        }

        @Override
        public boolean usesNativeBatchWriting() {
            return delegate.usesNativeBatchWriting();
        }

        @Override
        public boolean usesNativeSQL() {
            return delegate.usesNativeSQL();
        }

        @Override
        public boolean usesStreamsForBinding() {
            return delegate.usesStreamsForBinding();
        }

        @Override
        public boolean usesStringBinding() {
            return delegate.usesStringBinding();
        }

        @Override
        public void writeLOB(DatabaseField field, Object value, ResultSet resultSet, AbstractSession session) throws SQLException {
            delegate.writeLOB(field, value, resultSet, session);
        }

        @Override
        public boolean supportsCountDistinctWithMultipleFields() {
            return delegate.supportsCountDistinctWithMultipleFields();
        }

        @Override
        public boolean supportsIndexes() {
            return delegate.supportsIndexes();
        }

        @Override
        public boolean requiresTableInIndexDropDDL() {
            return delegate.requiresTableInIndexDropDDL();
        }

        @Override
        public boolean supportsTempTables() {
            return delegate.supportsTempTables();
        }

        @Override
        public boolean supportsLocalTempTables() {
            return delegate.supportsLocalTempTables();
        }

        @Override
        public boolean supportsGlobalTempTables() {
            return delegate.supportsGlobalTempTables();
        }

        @Override
        public DatabaseTable getTempTableForTable(DatabaseTable table) {
            return delegate.getTempTableForTable(table);
        }

        @Override
        public void writeCreateTempTableSql(Writer writer, DatabaseTable table, AbstractSession session, Collection pkFields, Collection usedFields, Collection allFields) throws IOException {
            delegate.writeCreateTempTableSql(writer, table, session, pkFields, usedFields, allFields);
        }

        @Override
        public void writeInsertIntoTableSql(Writer writer, DatabaseTable table, Collection usedFields) throws IOException {
            delegate.writeInsertIntoTableSql(writer, table, usedFields);
        }

        @Override
        public boolean isNullAllowedInSelectClause() {
            return delegate.isNullAllowedInSelectClause();
        }

        @Override
        public boolean isOutputAllowWithResultSet() {
            return delegate.isOutputAllowWithResultSet();
        }

        @Override
        public void writeTableCreationSuffix(Writer writer, String tableCreationSuffix) throws IOException {
            delegate.writeTableCreationSuffix(writer, tableCreationSuffix);
        }

        @Override
        public void writeUpdateOriginalFromTempTableSql(Writer writer, DatabaseTable table, Collection pkFields, Collection assignedFields) throws IOException {
            delegate.writeUpdateOriginalFromTempTableSql(writer, table, pkFields, assignedFields);
        }

        @Override
        public void writeDeleteFromTargetTableUsingTempTableSql(Writer writer, DatabaseTable table, DatabaseTable targetTable, Collection pkFields, Collection targetPkFields, DatasourcePlatform platform) throws IOException {
            delegate.writeDeleteFromTargetTableUsingTempTableSql(writer, table, targetTable, pkFields, targetPkFields, platform);
        }

        @Override
        public boolean wasFailureCommunicationBased(SQLException exception, Connection connection, AbstractSession sessionForProfile) {
            return delegate.wasFailureCommunicationBased(exception, connection, sessionForProfile);
        }

        @Override
        public void writeCleanUpTempTableSql(Writer writer, DatabaseTable table) throws IOException {
            delegate.writeCleanUpTempTableSql(writer, table);
        }

        @Override
        public boolean shouldAlwaysUseTempStorageForModifyAll() {
            return delegate.shouldAlwaysUseTempStorageForModifyAll();
        }

        @Override
        public boolean dontBindUpdateAllQueryUsingTempTables() {
            return delegate.dontBindUpdateAllQueryUsingTempTables();
        }

        public static void writeFieldsList(Writer writer, Collection fields, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeFieldsList(writer, fields, platform);
        }

        public static void writeAutoAssignmentSetClause(Writer writer, String tableName1, String tableName2, Collection fields, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeAutoAssignmentSetClause(writer, tableName1, tableName2, fields, platform);
        }

        public static void writeAutoJoinWhereClause(Writer writer, String tableName1, String tableName2, Collection pkFields, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeAutoJoinWhereClause(writer, tableName1, tableName2, pkFields, platform);
        }

        public static void writeFieldsAutoClause(Writer writer, String tableName1, String tableName2, Collection fields, String separator, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeFieldsAutoClause(writer, tableName1, tableName2, fields, separator, platform);
        }

        public static void writeJoinWhereClause(Writer writer, String tableName1, String tableName2, Collection pkFields1, Collection pkFields2, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeJoinWhereClause(writer, tableName1, tableName2, pkFields1, pkFields2, platform);
        }

        public static void writeFields(Writer writer, String tableName1, String tableName2, Collection fields1, Collection fields2, String separator, DatasourcePlatform platform) throws IOException {
            org.eclipse.persistence.internal.databaseaccess.DatabasePlatform.writeFields(writer, tableName1, tableName2, fields1, fields2, separator, platform);
        }

        @Override
        public boolean shouldPrintFieldIdentityClause(AbstractSession session, String qualifiedFieldName) {
            return delegate.shouldPrintFieldIdentityClause(session, qualifiedFieldName);
        }

        @Override
        public void printFieldTypeSize(Writer writer, FieldDefinition field, FieldTypeDefinition fieldType, boolean shouldPrintFieldIdentityClause) throws IOException {
            delegate.printFieldTypeSize(writer, field, fieldType, shouldPrintFieldIdentityClause);
        }

        @Override
        public boolean supportsUniqueColumns() {
            return delegate.supportsUniqueColumns();
        }

        @Override
        public void printFieldUnique(Writer writer, boolean shouldPrintFieldIdentityClause) throws IOException {
            delegate.printFieldUnique(writer, shouldPrintFieldIdentityClause);
        }

        @Override
        public void writeParameterMarker(Writer writer, ParameterExpression expression, AbstractRecord record, DatabaseCall call) throws IOException {
            delegate.writeParameterMarker(writer, expression, record, call);
        }

        @Override
        public Array createArray(String elementDataTypeName, Object[] elements, AbstractSession session, Connection connection) throws SQLException {
            return delegate.createArray(elementDataTypeName, elements, session, connection);
        }

        @Override
        public Struct createStruct(String structTypeName, Object[] attributes, AbstractSession session, Connection connection) throws SQLException {
            return delegate.createStruct(structTypeName, attributes, session, connection);
        }

        @Override
        public Array createArray(String elementDataTypeName, Object[] elements, Connection connection) throws SQLException {
            return delegate.createArray(elementDataTypeName, elements, connection);
        }

        @Override
        public Struct createStruct(String structTypeName, Object[] attributes, Connection connection) throws SQLException {
            return delegate.createStruct(structTypeName, attributes, connection);
        }

        @Override
        public boolean isXDBDocument(Object obj) {
            return delegate.isXDBDocument(obj);
        }
    }
    
}
