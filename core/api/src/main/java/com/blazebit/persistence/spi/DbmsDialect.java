/*
 * Copyright 2015 Blazebit.
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

package com.blazebit.persistence.spi;



/**
 * Interface for implementing some dbms specifics.
 *
 * @author Christian Beikov
 * @since 1.1.0
 * @see CriteriaBuilderConfiguration#registerDialect(java.lang.String, com.blazebit.persistence.spi.DbmsDialect)
 */
public interface DbmsDialect {
	
	/* With clause handling */
	
	/**
	 * Returns true if the dbms supports the with clause, false otherwise.
	 * 
	 * @return Whether the with clause is supported by the dbms
	 */
	public boolean supportsWithClause();
	
	/**
	 * Returns true if the dbms supports the non-recursive with clause, false otherwise.
	 * 
	 * @return Whether the non-recursive with clause is supported by the dbms
	 */
	public boolean supportsNonRecursiveWithClause();

    /**
     * Returns the SQL representation for the normal or recursive with clause. 
     * 
     * @param context The context into which the function should be rendered
     * @return The with clause name
     */
    public String getWithClause(boolean recursive);

    /**
     * Returns true if the dbms requires the with clause after the insert clause, false if it should be before it.
     * 
     * @return Whether the with clause should come after the insert clause or before it
     */
    public boolean usesWithClauseAfterInsert();

    /**
     * Returns true if the dbms supports the with clause in modification queries, false otherwise.
     * 
     * @return Whether the with clause is supported in modification queries by the dbms
     */
    public boolean supportsWithClauseInModificationQuery();

    /**
     * Returns true if the dbms supports modification queries in the with clause, false otherwise.
     * 
     * @return Whether modification queries are supported in the with clause by the dbms
     */
    public boolean supportsModificationQueryInWithClause();
    
    /* Returning clause handling */

    /**
     * Returns true if the dbms supports returning generated keys, false otherwise.
     * 
     * @return Whether returning generated keys is supported by the dbms
     */
	public boolean supportsReturningGeneratedKeys();
    
    /**
     * Returns true if the dbms supports returning all generated keys, false otherwise.
     * 
     * @return Whether returning all generated keys is supported by the dbms
     */
    public boolean supportsReturningAllGeneratedKeys();

    /**
     * Returns true if the dbms supports returning columns from a modified row, false otherwise.
     * 
     * @return Whether returning columns from a modified row is supported by the dbms
     */
	public boolean supportsReturningColumns();

	// TODO: documentation
    public boolean supportsLimit();

    // TODO: documentation
    public boolean supportsLimitOffset();

    // TODO: documentation
    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset);
}
