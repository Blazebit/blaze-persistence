/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.spring.data.repository;

import com.blazebit.persistence.examples.showcase.fragments.spring.data.view.CatView;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface CatRepository extends EntityViewRepository<CatView, Integer> {

    List<CatView> findByName(String lastname);

}
