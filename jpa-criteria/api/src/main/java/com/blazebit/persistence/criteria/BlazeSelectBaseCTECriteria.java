/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @param <T>
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public interface BlazeSelectBaseCTECriteria<T> extends BlazeBaseCTECriteria<T> {

    /**
     * Bind the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> BlazeSelectBaseCTECriteria<T> bind(SingularAttribute<? super T, Y> attribute, X value);

    /**
     * Bind the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> BlazeSelectBaseCTECriteria<T> bind(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    /**
     * Bind the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> BlazeSelectBaseCTECriteria<T> bind(Path<Y> attribute, X value);

    /**
     * Bind the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> BlazeSelectBaseCTECriteria<T> bind(Path<Y> attribute, Expression<? extends Y> value);

    /**
     * Bind the value of the specified attribute.
     *
     * @param attributeName name of the attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    BlazeSelectBaseCTECriteria<T> bind(String attributeName, Object value);

}
