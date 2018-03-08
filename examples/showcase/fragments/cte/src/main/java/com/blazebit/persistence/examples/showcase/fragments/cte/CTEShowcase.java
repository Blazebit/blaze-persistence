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

package com.blazebit.persistence.examples.showcase.fragments.cte;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.examples.showcase.fragments.cte.data.CatDataAccess;
import com.blazebit.persistence.examples.showcase.fragments.cte.data.TestDataGenerator;
import com.blazebit.persistence.examples.showcase.fragments.cte.view.BasicCatView;
import com.blazebit.persistence.examples.showcase.spi.AbstractShowcase;
import com.blazebit.persistence.view.EntityViewSetting;

import javax.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CTEShowcase extends AbstractShowcase {

    @Inject
    private TestDataGenerator testDataGenerator;

    @Inject
    private CatDataAccess catDataAccess;

    @Override
    public void run() {
        testDataGenerator.generateTestData();

        EntityViewSetting<BasicCatView, CriteriaBuilder<BasicCatView>> basicCatSetting = EntityViewSetting.create(BasicCatView.class);
        BasicCatView someCat = catDataAccess.getCatByName("G - Generation 2 - Cat 2/1", basicCatSetting);

        // entity-view example with CTE
        System.out.println(heading("Family tree of " + someCat));
        print(catDataAccess.getCatHierarchy(someCat.getId(), basicCatSetting));

        // pagination & entity views
        EntityViewSetting<BasicCatView, PaginatedCriteriaBuilder<BasicCatView>> paginationSetting = EntityViewSetting.create(BasicCatView.class, 0, 3);
        PagedList<BasicCatView> pagedResults = catDataAccess.getPaginatedCats(paginationSetting);

        System.out.println(heading("Page 1"));
        print(pagedResults);

        // insert new cat
        // in the sort order defined by CatDataAccess#getPaginatedCats, this cat is inserted at index 1 (0-based)
        testDataGenerator.addCat("B - New cat");

        // since we use keyset pagination, the inserted cat does not influence the next page
        // depending on the firstRow parameter and the passed keyset page, the criteria builder will automatically decide
        // if it can use keyset pagination or if it must perform offset pagination
        paginationSetting = EntityViewSetting.create(BasicCatView.class, 3, 3);
        paginationSetting.withKeysetPage(pagedResults.getKeysetPage());
        pagedResults = catDataAccess.getPaginatedCats(paginationSetting);

        System.out.println(heading("Page 2"));
        print(pagedResults);

        // scroll back to page 1
        // page 1 will now contain the new cat
        paginationSetting = EntityViewSetting.create(BasicCatView.class, 0, 3);
        paginationSetting.withKeysetPage(pagedResults.getKeysetPage());
        pagedResults = catDataAccess.getPaginatedCats(paginationSetting);

        System.out.println(heading("Back at Page 1"));
        print(pagedResults);
    }

}
