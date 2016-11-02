/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SubqueryInitiator;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryInitiatorFactory {

    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;

    public SubqueryInitiatorFactory(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
    }

    public <T> SubqueryInitiator<T> createSubqueryInitiator(T result, SubqueryBuilderListener<T> listener) {
        return new SubqueryInitiatorImpl<T>(mainQuery, aliasManager, parentJoinManager, result, listener);
    }
}
