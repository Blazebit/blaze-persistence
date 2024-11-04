/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest.repository;


import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.examples.deltaspike.data.rest.model.Cat;
import com.blazebit.persistence.examples.deltaspike.data.rest.view.CatUpdateView;
import com.blazebit.persistence.examples.deltaspike.data.rest.view.CatWithOwnerView;
import org.apache.deltaspike.data.api.Repository;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Repository(forEntity = Cat.class)
public interface CatViewRepository {

    public KeysetAwarePage<CatWithOwnerView> findAll(Specification<Cat> specification, Pageable pageable);

    public CatUpdateView save(CatUpdateView catUpdateView);
}
