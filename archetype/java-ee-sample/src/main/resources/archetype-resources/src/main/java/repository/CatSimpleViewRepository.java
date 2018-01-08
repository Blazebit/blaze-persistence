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

package ${package}.repository;

import com.blazebit.persistence.*;
import com.blazebit.persistence.view.*;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.inject.Inject;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import ${package}.model.*;
import ${package}.view.*;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CatSimpleViewRepository {

    @PersistenceContext
    EntityManager entityManager;
    @Inject
    CriteriaBuilderFactory criteriaBuilderFactory;
    @Inject
    EntityViewManager entityViewManager;

    public List<CatSimpleView> findAll() {
        return entityViewManager.applySetting(
                EntityViewSetting.create(CatSimpleView.class),
                criteriaBuilderFactory.create(entityManager, Cat.class)
        ).getResultList();
    }
}
