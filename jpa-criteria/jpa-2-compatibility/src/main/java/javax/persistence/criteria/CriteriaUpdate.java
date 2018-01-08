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
import javax.persistence.metamodel.SingularAttribute;

/**
 * The CriteriaUpdate interface defines functionality for performing
 * bulk update operations using the Criteria API.
 * <p/>
 * Criteria API bulk update operations map directly to database update
 * operations, bypassing any optimistic locking checks. Portable
 * applications using bulk update operations must manually update the
 * value of the version column, if desired, and/or manually validate
 * the value of the version column.
 * The persistence context is not synchronized with the result of the
 * bulk update.
 * <p/>
 * A CriteriaUpdate object must have a single root.
 *
 * @param <T> the entity type that is the target of the update
 *
 * @since Java Persistence 2.1
 */
public interface CriteriaUpdate<T> extends CommonAbstractCriteria {
    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the update.
     * A CriteriaUpdate object has a single root, the object that
     * is being updated.
     *
     * @param entityClass the entity class
     *
     * @return query root corresponding to the given entity
     */
    Root<T> from(Class<T> entityClass);

    /**
     * Create and add a query root corresponding to the entity
     * that is the target of the update.
     * A CriteriaUpdate object has a single root, the object that
     * is being updated.
     *
     * @param entity metamodel entity representing the entity of type X
     *
     * @return query root corresponding to the given entity
     */
    Root<T> from(EntityType<T> entity);

    /**
     * Return the query root.
     *
     * @return the query root
     */
    Root<T> getRoot();

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> CriteriaUpdate<T> set(Path<Y> attribute, X value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> CriteriaUpdate<T> set(
            Path<Y> attribute,
            Expression<? extends Y> value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attributeName name of the attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    CriteriaUpdate<T> set(String attributeName, Object value);

    /**
     * Modify the query to restrict the target of the update
     * according to the specified boolean expression.
     * Replaces the previously added restriction(s), if any.
     *
     * @param restriction a simple or compound boolean expression
     *
     * @return the modified query
     */
    CriteriaUpdate<T> where(Expression<Boolean> restriction);

    /**
     * Modify the query to restrict the target of the update
     * according to the conjunction of the specified restriction
     * predicates.
     * Replaces the previously added restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     *
     * @param restrictions zero or more restriction predicates
     *
     * @return the modified query
     */
    CriteriaUpdate<T> where(Predicate... restrictions);
}
