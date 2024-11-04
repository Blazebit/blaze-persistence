/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class CatViewRepository {

    @Inject
    EntityManager em;
    @Inject
    CriteriaBuilderFactory cbf;
    @Inject
    EntityViewManager evm;

    public <T> T findById(EntityViewSetting<T, CriteriaBuilder<T>> setting, Long id) {
        return evm.find(em, setting, id);
    }

    public <T> List<T> findAll(EntityViewSetting<T, ?> setting) {
        return evm.applySetting(setting, cbf.create(em, evm.getMetamodel().managedView(setting.getEntityViewClass()).getEntityClass())).getResultList();
    }
}
