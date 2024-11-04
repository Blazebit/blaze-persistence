/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
