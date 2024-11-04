/*
 * Copyright (c) 2011, 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Linda DeMichiel - 2.1

package jakarta.persistence.criteria;

import jakarta.persistence.metamodel.EntityType;

/**
 * The {@code CriteriaDelete} interface defines functionality for 
 * performing bulk delete operations using the Criteria API
 *
 * <p>Criteria API bulk delete operations map directly to database 
 * delete operations. The persistence context is not synchronized 
 * with the result of the bulk delete.
 *
 * <p> A {@code CriteriaDelete} object must have a single root.
 *
 * @param <T>  the entity type that is the target of the DELETE
 *
 * @since 2.1
 */
public interface CriteriaDelete<T> extends CommonAbstractCriteria {


    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the DELETE.
     * A {@code CriteriaDelete} object has a single root, the entity that
     * is being deleted.
     * @param entityClass  the entity class
     * @return query root corresponding to the given entity
     */
    Root<T> from(Class<T> entityClass);

    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the DELETE.
     * A {@code CriteriaDelete} object has a single root, the entity that
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
     * Modify the DELETE query to restrict the target of the deletion 
     * according to the specified boolean expression.
     * Replaces the previously added restriction(s), if any.
     * @param restriction  a simple or compound boolean expression
     * @return the modified delete query
     */    
   CriteriaDelete<T> where(Expression<Boolean> restriction);

    /**
     * Modify the DELETE query to restrict the target of the deletion
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
