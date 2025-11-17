/*
 * Copyright (c) 2011, 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
//     Linda DeMichiel - 2.1

package jakarta.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Interface used to control stored procedure query execution.
 *
 * <p>
 * Stored procedure query execution may be controlled in accordance with 
 * the following:
 * <ul>
 * <li>The {@link #setParameter} methods are used to set the values of
 * all required {@code IN} and {@code INOUT} parameters. It is not
 * required to set the values of stored procedure parameters for which
 * default values have been defined by the stored procedure.</li>
 * <li> When {@link #getResultList} and {@link #getSingleResult} are
 * called on a {@code StoredProcedureQuery} object, the provider calls
 * {@link #execute} on an unexecuted stored procedure query before
 * processing {@code getResultList} or {@code getSingleResult}.</li>
 * <li> When {@link #executeUpdate} is called on a
 * {@code StoredProcedureQuery} object, the provider will call
 * {@link #execute} on an unexecuted stored procedure query, followed
 * by {@link #getUpdateCount}. The results of {@code executeUpdate} will
 * be those of {@code getUpdateCount}.</li>
 * <li> The {@link #execute} method supports both the simple case where
 * scalar results are passed back only via {@code INOUT} and {@code OUT}
 * parameters as well as the most general case (multiple result sets
 * and/or update counts, possibly also in combination with output
 * parameter values).</li>
 * <li> The {@code execute} method returns true if the first result is
 * a result set, and false if it is an update count or there are no
 * results other than through {@code INOUT} and {@code OUT} parameters,
 * if any.</li>
 * <li> If the {@code execute} method returns true, the pending result
 * set can be obtained by calling {@link #getResultList} or
 * {@link #getSingleResult}.</li>
 * <li> The {@link #hasMoreResults} method can then be used to test for
 * further results.</li>
 * <li> If {@code execute} or {@code hasMoreResults} returns false, the
 * {@link #getUpdateCount} method can be called to obtain the pending
 * result if it is an update count. The {@code getUpdateCount} method
 * will return either the update count (zero or greater) or -1 if there
 * is no update count (i.e., either the next result is a result set or
 * there is no next update count).</li>
 * <li> For portability, results that correspond to JDBC result sets
 * and update counts need to be processed before the values of any
 * {@code INOUT} or {@code OUT} parameters are extracted.</li>
 * <li> After results returned through {@link #getResultList} and
 * {@link #getUpdateCount} have been exhausted, results returned through
 * {@code INOUT} and {@code OUT} parameters can be retrieved.</li>
 * <li> The {@link #getOutputParameterValue} methods are used to
 * retrieve the values passed back from the procedure through
 * {@code INOUT} and {@code OUT} parameters.</li>
 * <li> When using {@code REF_CURSOR} parameters for result sets the
 * update counts should be exhausted before calling {@link #getResultList}
 * to retrieve the result set. Alternatively, the {@code REF_CURSOR}
 * result set can be retrieved through {@link #getOutputParameterValue}.
 * Result set mappings are applied to results corresponding to
 * {@code REF_CURSOR} parameters in the order the {@code REF_CURSOR}
 * parameters were registered with the query.</li>
 * <li> In the simplest case, where results are returned only via
 * {@code INOUT} and {@code OUT} parameters, {@code execute} can be
 * followed immediately by calls to {@link #getOutputParameterValue}.
 * </li>
 * </ul>
 *
 * @see Query
 * @see Parameter
 *
 * @since 2.1
 */
public interface StoredProcedureQuery extends Query {

    /**
     * Set a query property or hint. The hints elements may be used 
     * to specify query properties and hints. Properties defined by
     * this specification must be observed by the provider. 
     * Vendor-specific hints that are not recognized by a provider
     * must be silently ignored. Portable applications should not
     * rely on the standard timeout hint. Depending on the database
     * in use, this hint may or may not be observed.
     * @param hintName  name of the property or hint
     * @param value  value for the property or hint
     * @return the same query instance
     * @throws IllegalArgumentException if the second argument is not
     *         valid for the implementation
     */
    StoredProcedureQuery setHint(String hintName, Object value);

    /**
     * Bind the value of a {@code Parameter} object.
     * @param param  parameter object
     * @param value  parameter value
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter does not
     *         correspond to a parameter of the query
     */
    <T> StoredProcedureQuery setParameter(Parameter<T> param, 
                                          T value);

    /**
     * Bind an instance of {@link java.util.Calendar} to a {@link Parameter} object.
     * @param param parameter object
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter does not
     *         correspond to a parameter of the query
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(Parameter<Calendar> param,
                                      Calendar value, 
                                      TemporalType temporalType);

    /**
     * Bind an instance of {@link java.util.Date} to a {@link Parameter} object.
     * @param param parameter object
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter does not
     *         correspond to a parameter of the query
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(Parameter<Date> param,
                                      Date value,
                                      TemporalType temporalType);

    /**
     * Bind an argument value to a named parameter.
     * @param name  parameter name
     * @param value  parameter value
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter name does 
     *         not correspond to a parameter of the query or if the
     *         argument is of incorrect type
     */
    StoredProcedureQuery setParameter(String name, Object value);

    /**
     * Bind an instance of {@code java.util.Calendar} to a named parameter.
     * @param name  parameter name
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter name does 
     *         not correspond to a parameter of the query or if the
     *         value argument is of incorrect type
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(String name, 
                                      Calendar value, 
                                      TemporalType temporalType);

    /**
     * Bind an instance of {@code java.util.Date} to a named parameter.
     * @param name  parameter name
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if the parameter name does 
     *         not correspond to a parameter of the query or if the
     *         value argument is of incorrect type
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(String name, 
                                      Date value, 
                                      TemporalType temporalType);

    /**
     * Bind an argument value to a positional parameter.
     * @param position  position
     * @param value  parameter value
     * @return the same query instance
     * @throws IllegalArgumentException if position does not
     *         correspond to a positional parameter of the query
     *         or if the argument is of incorrect type
     */
    StoredProcedureQuery setParameter(int position, Object value);

    /**
     * Bind an instance of {@code java.util.Calendar} to a positional
     * parameter.
     * @param position  position
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if position does not
     *         correspond to a positional parameter of the query or
     *         if the value argument is of incorrect type
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(int position, 
                                      Calendar value,  
                                      TemporalType temporalType);

    /**
     * Bind an instance of {@code java.util.Date} to a positional parameter.
     * @param position  position
     * @param value  parameter value
     * @param temporalType  temporal type
     * @return the same query instance
     * @throws IllegalArgumentException if position does not
     *         correspond to a positional parameter of the query or
     *         if the value argument is of incorrect type
     * @deprecated Newly-written code should use the date/time types
     *             defined in {@link java.time}.
     */
    @Deprecated(since = "3.2")
    StoredProcedureQuery setParameter(int position, 
                                      Date value,  
                                      TemporalType temporalType);

    /**
     * Set the flush mode type to be used for the query execution.
     * The flush mode type applies to the query regardless of the
     * flush mode type in use for the entity manager.
     * @param flushMode  flush mode
     * @return the same query instance
     */
    StoredProcedureQuery setFlushMode(FlushModeType flushMode);

    /**
     * Set the cache retrieval mode that is in effect during
     * query execution. This cache retrieval mode overrides the
     * cache retrieve mode in use by the entity manager.
     * @param cacheRetrieveMode cache retrieval mode
     * @return the same query instance
     * @since 3.2
     */
    StoredProcedureQuery setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode);

    /**
     * Set the cache storage mode that is in effect during
     * query execution. This cache storage mode overrides the
     * cache storage mode in use by the entity manager.
     * @param cacheStoreMode cache storage mode
     * @return the same query instance
     * @since 3.2
     */
    StoredProcedureQuery setCacheStoreMode(CacheStoreMode cacheStoreMode);

    /**
     * Set the query timeout, in milliseconds. This is a hint,
     * and is an alternative to {@linkplain #setHint setting
     * the hint} {@code jakarta.persistence.query.timeout}.
     * @param timeout the timeout, in milliseconds, or null to
     *                indicate no timeout
     * @return the same query instance
     * @since 3.2
     */
    StoredProcedureQuery setTimeout(Integer timeout);

    /**
     * Register a positional parameter.
     * All parameters must be registered.
     * @param position  parameter position
     * @param type  type of the parameter
     * @param mode  parameter mode 
     * @return the same query instance
     */
    StoredProcedureQuery registerStoredProcedureParameter(
	  int position,
	  Class<?> type,
	  ParameterMode mode);

    /**
     * Register a named parameter.
     * @param parameterName  name of the parameter as registered or
     *            specified in metadata
     * @param type  type of the parameter
     * @param mode  parameter mode 
     * @return the same query instance
     */
    StoredProcedureQuery registerStoredProcedureParameter(
	  String parameterName,
	  Class<?> type,
	  ParameterMode mode);

    /**
     * Retrieve a value passed back from the procedure
     * through an INOUT or OUT parameter.
     * For portability, all results corresponding to result sets
     * and update counts must be retrieved before the values of 
     * output parameters.
     * @param position  parameter position
     * @return the result that is passed back through the parameter
     * @throws IllegalArgumentException if the position does
     *         not correspond to a parameter of the query or is
     *         not an INOUT or OUT parameter
     */
    Object getOutputParameterValue(int position);

    /**
     * Retrieve a value passed back from the procedure
     * through an INOUT or OUT parameter.
     * For portability, all results corresponding to result sets
     * and update counts must be retrieved before the values of 
     * output parameters.
     * @param parameterName  name of the parameter as registered or
     *        specified in metadata
     * @return the result that is passed back through the parameter
     * @throws IllegalArgumentException if the parameter name does
     *         not correspond to a parameter of the query or is
     *         not an INOUT or OUT parameter
     */
    Object getOutputParameterValue(String parameterName);

    /**
     * Return true if the first result corresponds to a result set,
     * and false if it is an update count or if there are no results
     * other than through INOUT and OUT parameters, if any.
     * @return  true if first result corresponds to result set
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    boolean execute();

    /**
     * Return the update count of -1 if there is no pending result or
     * if the first result is not an update count.  The provider will
     * call {@code execute} on the query if needed.
     * @return the update count or -1 if there is no pending result
     * or if the next result is not an update count.
     * @throws TransactionRequiredException if there is 
     *         no transaction or the persistence context has not
     *         been joined to the transaction
     * @throws QueryTimeoutException if the statement execution 
     *         exceeds the query timeout value set and only
     *         the statement is rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    int executeUpdate();

    /**
     * Retrieve the list of results from the next result set.
     * The provider will call {@code execute} on the query
     * if needed.
     * A {@code REF_CURSOR} result set, if any, is retrieved
     * in the order the {@code REF_CURSOR} parameter was 
     * registered with the query.
     * @return a list of the results or null is the next item is not 
     * a result set
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    List getResultList();

    /**
     * Retrieve a single result from the next result set.
     * The provider will call {@code execute} on the query
     * if needed.
     * A {@code REF_CURSOR} result set, if any, is retrieved
     * in the order the {@code REF_CURSOR} parameter was 
     * registered with the query.
     * @return the result or null if the next item is not a result set
     * @throws NoResultException if there is no result in the next
     *         result set
     * @throws NonUniqueResultException if more than one result
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    Object getSingleResult();

    /**
     * Retrieve a single result from the next result set.
     * The provider will call {@code execute} on the query
     * if needed.
     * A {@code REF_CURSOR} result set, if any, is retrieved
     * in the order the {@code REF_CURSOR} parameter was
     * registered with the query.
     * @return the result or null if the next item is not a result set
     *         or if there is no result in the next result set
     * @throws NonUniqueResultException if more than one result
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    Object getSingleResultOrNull();

    /**
     * Return true if the next result corresponds to a result set,
     * and false if it is an update count or if there are no results
     * other than through INOUT and OUT parameters, if any.
     * @return true if next result corresponds to result set
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    boolean hasMoreResults();

    /**
     * Return the update count or  -1 if there is no pending result
     * or if the next result is not an update count.
     * @return update count or -1 if there is no pending result or if
     *         the next result is not an update count
     * @throws QueryTimeoutException if the query execution exceeds
     *         the query timeout value set and only the statement is
     *         rolled back
     * @throws PersistenceException if the query execution exceeds 
     *         the query timeout value set and the transaction
     *         is rolled back
     */
    int getUpdateCount();

}
