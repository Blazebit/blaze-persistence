/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Component
public class CatViewRepository {

    @Autowired
    EntityManager em;
    @Autowired
    CriteriaBuilderFactory cbf;
    @Autowired
    EntityViewManager evm;

    public <T> T findById(EntityViewSetting<T, CriteriaBuilder<T>> setting, Long id) {
        return evm.find(em, setting, id);
    }

    public <T> List<T> findAll(EntityViewSetting<T, ?> setting) {
        return evm.applySetting(setting, cbf.create(em, evm.getMetamodel().managedView(setting.getEntityViewClass()).getEntityClass())).getResultList();
    }
}
