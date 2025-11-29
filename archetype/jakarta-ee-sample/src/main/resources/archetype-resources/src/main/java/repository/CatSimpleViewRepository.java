/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.repository;

import com.blazebit.persistence.*;
import com.blazebit.persistence.view.*;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import jakarta.inject.Inject;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

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
