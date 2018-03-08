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

package com.blazebit.persistence.examples.showcase.fragments.basic.data;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import com.blazebit.persistence.examples.showcase.base.model.Cat;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Transactional
public class CatDataAccess {

    @Inject
    private EntityManagerHolder emHolder;

    @Inject
    private EntityViewManager evm;

    @Inject
    private CriteriaBuilderFactory cbf;

    public <T> List<T> getCats(EntityViewSetting<T, CriteriaBuilder<T>> setting) {
        // formulate a query via a CriteriaBuilder
        CriteriaBuilder<Cat> cb = cbf.create(emHolder.getEntityManager(), Cat.class);
        // you could extend the query by restrictions

        // at this point we combine the entity view setting with the CriteriaBuilder
        // this results in a CriteriaBuilder returning instances of the desired entity view
        CriteriaBuilder<T> basicCb = evm.applySetting(setting, cb);

        // now we can issue the query and get the results
        return basicCb.getResultList();
    }

    public <T> T getCatByName(String name, EntityViewSetting<T, CriteriaBuilder<T>> setting) {
        CriteriaBuilder<Cat> cb = cbf.create(emHolder.getEntityManager(), Cat.class)
                .where("name").eq(name);

        return evm.applySetting(setting, cb).getSingleResult();
    }

    public <T> PagedList<T> getPaginatedCats(EntityViewSetting<T, PaginatedCriteriaBuilder<T>> setting) {
        CriteriaBuilder<Cat> cb = cbf.create(emHolder.getEntityManager(), Cat.class)
                .orderByAsc("name")
                .orderByAsc("id");
        return evm.applySetting(setting, cb).withKeysetExtraction(true).getResultList();
    }
}
