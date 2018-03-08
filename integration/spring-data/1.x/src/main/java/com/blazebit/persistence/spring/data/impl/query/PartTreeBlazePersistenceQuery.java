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

package com.blazebit.persistence.spring.data.impl.query;


import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spring.data.base.query.AbstractPartTreeBlazePersistenceQuery;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareJpaQueryMethod;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.EntityManager;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PartTreeBlazePersistenceQuery extends AbstractPartTreeBlazePersistenceQuery {

    public PartTreeBlazePersistenceQuery(EntityViewAwareJpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        super(method, em, persistenceProvider, cbf, evm);
    }

    @Override
    protected boolean isCountProjection(PartTree tree) {
        return tree.isCountProjection();
    }

    @Override
    protected boolean isDelete(PartTree tree) {
        return tree.isDelete();
    }
}
