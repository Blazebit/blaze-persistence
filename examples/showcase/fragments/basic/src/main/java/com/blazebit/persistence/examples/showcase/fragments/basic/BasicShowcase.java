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

package com.blazebit.persistence.examples.showcase.fragments.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.examples.showcase.fragments.basic.data.CatDataAccess;
import com.blazebit.persistence.examples.showcase.fragments.basic.data.TestDataGenerator;
import com.blazebit.persistence.examples.showcase.fragments.basic.view.BasicCatView;
import com.blazebit.persistence.examples.showcase.spi.AbstractShowcase;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;

import javax.inject.Inject;

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
