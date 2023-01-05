/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

import javax.persistence.metamodel.Type;
import java.util.List;

/**
 * An entity view root.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface ViewRoot {

    /**
     * The name of the entity view root through which it can be accessed in the entity view mappings.
     *
     * @return The entity view root name
     */
    String getName();

    /**
     * Returns the type of the view root. May be <code>null</code> if not resolvable.
     *
     * @return The type of the view root
     */
    public Type<?> getType();

    /**
     * Returns the correlation provider factory of the view root.
     *
     * @return The correlation provider factory of the view root
     */
    public CorrelationProviderFactory getCorrelationProviderFactory();

    /**
     * Returns the correlation provider of the view root.
     *
     * @return The correlation provider of the view root
     */
    public Class<? extends CorrelationProvider> getCorrelationProvider();

    /**
     * The join type to use for the entity view root.
     *
     * @return The join type
     */
    JoinType getJoinType();

    /**
     * The associations that should be fetched along with the entity mapped by this attribute.
     *
     * @return The association that should be fetched
     */
    public String[] getFetches();

    /**
     * Returns the order by items for the limit expression.
     *
     * @return The order by items for the limit expression
     */
    public List<OrderByItem> getOrderByItems();

    /**
     * Returns the limit expression.
     *
     * @return The limit expression
     */
    public String getLimitExpression();

    /**
     * Returns the offset expression.
     *
     * @return The offset expression
     */
    public String getOffsetExpression();
}
