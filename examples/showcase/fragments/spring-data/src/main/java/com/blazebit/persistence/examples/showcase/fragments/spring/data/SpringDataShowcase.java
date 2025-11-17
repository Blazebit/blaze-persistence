/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.spring.data;

import com.blazebit.persistence.examples.showcase.fragments.spring.data.data.TestDataGenerator;
import com.blazebit.persistence.examples.showcase.fragments.spring.data.repository.CatRepository;
import com.blazebit.persistence.examples.showcase.spi.AbstractShowcase;

import jakarta.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SpringDataShowcase extends AbstractShowcase {

    @Inject
    private TestDataGenerator testDataGenerator;

    @Inject
    private CatRepository catRepository;

    @Override
    public void run() {
        testDataGenerator.generateTestData();
        print(catRepository.findAll());
    }
}
