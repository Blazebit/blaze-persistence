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

package javax.persistence.criteria;

import javax.persistence.metamodel.EntityType;

/**
 * The <code>CriteriaDelete</code> interface defines functionality for performing
 * bulk delete operations using the Criteria API
 *
 * <p>Criteria API bulk delete operations map directly to database 
 * delete operations.  The persistence context is not synchronized 
 * with the result of the bulk delete.
 *
 * <p> A <code>CriteriaDelete</code> object must have a single root.
 *
 * @param <T>  the entity type that is the target of the delete
 *
 * @since Java Persistence 2.1
 */
public interface CriteriaDelete<T> extends CommonAbstractCriteria {


    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the delete.
     * A <code>CriteriaDelete</code> object has a single root, the entity that 
     * is being deleted.
     * @param entityClass  the entity class
     * @return query root corresponding to the given entity
     */
    Root<T> from(Class<T> entityClass);

    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the delete.
     * A <code>CriteriaDelete</code> object has a single root, the entity that 
     * is being deleted.
     * @param entity  metamodel entity representing the entity
     *                of type X
     * @return query root corresponding to the given entity
     */
    Root<T> from(EntityType<T> entity);

   /**
    * Return the query root.
    * @return the query root
    */
    Root<T> getRoot();

    /**
     * Modify the delete query to restrict the target of the deletion 
     * according to the specified boolean expression.
     * Replaces the previously added restriction(s), if any.
     * @param restriction  a simple or compound boolean expression
     * @return the modified delete query
     */    
    CriteriaDelete<T> where(Expression<Boolean> restriction);

    /**
     * Modify the delete query to restrict the target of the deletion
     * according to the conjunction of the specified restriction 
     * predicates.
     * Replaces the previously added restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * @param restrictions  zero or more restriction predicates
     * @return the modified delete query
     */
    CriteriaDelete<T> where(Predicate... restrictions);

}
