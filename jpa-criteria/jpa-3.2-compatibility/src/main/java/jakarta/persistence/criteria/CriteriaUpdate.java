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

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.EntityType;

/**
 * The {@code CriteriaUpdate} interface defines functionality for
 * performing bulk update operations using the Criteria API.
 *
 * <p>Criteria API bulk update operations map directly to database
 * update operations, bypassing any optimistic locking checks.
 * Portable applications using bulk update operations must manually
 * update the value of the version column, if desired, and/or manually
 * validate the value of the version column. The persistence context
 * is not automatically synchronized with the result of the bulk update.
 *
 * <p> A {@code CriteriaUpdate} object must have a single root.
 *
 * @param <T>  the entity type that is the target of the update
 *
 * @since 2.1
 */
public interface CriteriaUpdate<T> extends CommonAbstractCriteria {

   /**
    * Create and add a query root corresponding to the entity
    * that is the target of the update.
    * A {@code CriteriaUpdate} object has a single root, the
    * entity that is being updated.
    * @param entityClass  the entity class
    * @return query root corresponding to the given entity
    */
   Root<T> from(Class<T> entityClass);

   /**
    * Create and add a query root corresponding to the entity
    * that is the target of the update.
    * A {@code CriteriaUpdate} object has a single root, the
    * entity that is being updated.
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
    * Update the value of the specified attribute.
    * @param attribute  attribute to be updated
    * @param value  new value
    * @return  the modified update query
    */
   <Y, X extends Y> CriteriaUpdate<T> set( SingularAttribute<? super T, Y> attribute, X value);

   /**
    * Update the value of the specified attribute.
    * @param attribute  attribute to be updated
    * @param value  new value
    * @return  the modified update query
    */
   <Y> CriteriaUpdate<T> set( SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

   /**
    * Update the value of the specified attribute.
    * @param attribute  attribute to be updated
    * @param value  new value
    * @return  the modified update query
    */
   <Y, X extends Y> CriteriaUpdate<T> set(Path<Y> attribute, X value);

   /**
    * Update the value of the specified attribute.
    * @param attribute  attribute to be updated
    * @param value  new value
    * @return  the modified update query
    */
   <Y> CriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value);

   /**
    * Update the value of the specified attribute.
    * @param attributeName  name of the attribute to be updated
    * @param value  new value
    * @return  the modified update query
    */
   CriteriaUpdate<T> set(String attributeName, Object value);

    /**
     * Modify the update query to restrict the target of the
     * update according to the specified boolean expression.
     * Replaces the previously added restriction(s), if any.
     * @param restriction  a simple or compound boolean expression
     * @return the modified update query
     */    
   CriteriaUpdate<T> where(Expression<Boolean> restriction);

    /**
     * Modify the update query to restrict the target of the
     * update according to the conjunction of the specified
     * restriction predicates.
     * Replaces the previously added restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * @param restrictions  zero or more restriction predicates
     * @return the modified update query
     */
   CriteriaUpdate<T> where(Predicate... restrictions);
}
