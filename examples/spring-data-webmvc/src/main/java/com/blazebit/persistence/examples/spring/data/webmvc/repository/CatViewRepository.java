/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webmvc.repository;

import com.blazebit.persistence.examples.spring.data.webmvc.model.Cat;
import com.blazebit.persistence.examples.spring.data.webmvc.view.CatUpdateView;
import com.blazebit.persistence.examples.spring.data.webmvc.view.CatWithOwnerView;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CatViewRepository extends Repository<Cat, Long> {

    public KeysetAwarePage<CatWithOwnerView> findAll(Specification<Cat> specification, Pageable pageable);

    public CatUpdateView save(CatUpdateView catCreateView);
}
