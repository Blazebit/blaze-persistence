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

package com.blazebit.persistence.examples.showcase.fragments.cte.data;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import com.blazebit.persistence.examples.showcase.base.model.Cat;
import com.blazebit.persistence.examples.showcase.fragments.cte.CatHierarchyCTE;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import javax.inject.Inject;
import javax.persistence.Tuple;
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

    public <T> T getCatByName(String name, EntityViewSetting<T, CriteriaBuilder<T>> setting) {
        CriteriaBuilder<Cat> cb = cbf.create(emHolder.getEntityManager(), Cat.class)
                .where("name").eq(name);

        return evm.applySetting(setting, cb).getSingleResult();
    }

    public <T> List<T> getCatHierarchy(Integer catId, EntityViewSetting<T, CriteriaBuilder<T>> setting) {
        CriteriaBuilder<Tuple> cb = cbf.create(emHolder.getEntityManager(), Tuple.class)
                .withRecursive(CatHierarchyCTE.class)
                    .from(Cat.class)
                    .bind("id").select("id")
                    .bind("motherId").select("mother.id")
                    .bind("fatherId").select("father.id")
                    .bind("generation").select("0")
                    .where("id").eqExpression(catId.toString())
                .unionAll()
                    .from(Cat.class, "cat")
                    .from(CatHierarchyCTE.class, "cte")
                    .bind("id").select("cat.id")
                    .bind("motherId").select("cat.mother.id")
                    .bind("fatherId").select("cat.father.id")
                    .bind("generation").select("cte.generation + 1")
                    .whereOr()
                        .where("cat.id").eqExpression("cte.motherId")
                        .where("cat.id").eqExpression("cte.fatherId")
                    .endOr()
                .end()
                .from(Cat.class, "cat")
                .innerJoinOn(CatHierarchyCTE.class, "cte").on("cte.id").eqExpression("cat.id").end()
                .orderByAsc("cte.generation");

        return evm.applySetting(setting, cb).getResultList();
    }

    public <T> PagedList<T> getPaginatedCats(EntityViewSetting<T, PaginatedCriteriaBuilder<T>> setting) {
        CriteriaBuilder<Cat> cb = cbf.create(emHolder.getEntityManager(), Cat.class)
                .orderByAsc("name")
                .orderByAsc("id");
        return evm.applySetting(setting, cb).withKeysetExtraction(true).getResultList();
    }
}
