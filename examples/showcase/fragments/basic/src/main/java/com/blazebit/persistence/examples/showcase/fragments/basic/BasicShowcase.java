/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.examples.showcase.fragments.basic.data.CatDataAccess;
import com.blazebit.persistence.examples.showcase.fragments.basic.data.TestDataGenerator;
import com.blazebit.persistence.examples.showcase.fragments.basic.view.BasicCatView;
import com.blazebit.persistence.examples.showcase.spi.AbstractShowcase;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;

import jakarta.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class BasicShowcase extends AbstractShowcase {

    @Inject
    private TestDataGenerator testDataGenerator;

    @Inject
    private CatDataAccess catDataAccess;

    @Override
    public void run() {
        testDataGenerator.generateTestData();

        EntityViewSetting<BasicCatView, CriteriaBuilder<BasicCatView>> sortedBasicCatSetting = EntityViewSetting.create(BasicCatView.class);

        // we can sort our results by name like this
        sortedBasicCatSetting.addAttributeSorter("name", Sorters.ascending());

        System.out.println(heading("Basic Cat Views - By Name Ascending:"));
        print(catDataAccess.getCats(sortedBasicCatSetting));

        // or vice versa
        sortedBasicCatSetting.addAttributeSorter("name", Sorters.descending());

        System.out.println(heading("Basic Cat Views - By Name Descending:"));
        print(catDataAccess.getCats(sortedBasicCatSetting));
    }

}
