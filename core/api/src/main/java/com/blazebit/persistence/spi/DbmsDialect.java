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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


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
    
    /* Returning clause handling */
	
	/**
	 * Returns true if the dbms supports the with clause, false otherwise.
	 * 
	 * @return Whether the with clause is supported by the dbms
	 */
	public boolean supportsJdbcReturning();

	public boolean supportsQueryReturning();

	public void applyQueryReturning(StringBuilder sqlSb, String[] returningColumns);

	public void applyQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException;

	public List<Object[]> getQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException;
	
//	public String getLastInsertedId();
}
