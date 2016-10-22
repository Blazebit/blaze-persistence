/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.examples.cdi;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.examples.cdi.data.CatDataAccess;
import com.blazebit.persistence.examples.cdi.data.TestDataGenerator;
import com.blazebit.persistence.examples.cdi.view.BasicCatView;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
@ApplicationScoped
public class Application {

    @Inject
    private TestDataGenerator testDataGenerator;

    @Inject
    private CatDataAccess catDataAccess;

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

    // helpers

    private String heading(String heading) {
        return "\n" + frame(evenLeftRightPad(heading, 80, '-'), 80, '-') + "\n";
    }

    private String frame(String s, int width, char frameChar) {
        final String frameLine = evenLeftRightPad("", width, frameChar);
        StringBuilder sb = new StringBuilder(frameLine).append('\n');
        if (s.endsWith("\n")) {
            sb.append(s);
        } else {
            sb.append(s).append('\n');
        }
        return sb.append(frameLine).toString();
    }

    private String evenLeftRightPad(String s, int length, char padChar) {
        length = length - s.length();
        if (length > 0) {
            s = StringUtils.leftPad(s, s.length() + length / 2 + length % 2, padChar);
            return StringUtils.rightPad(s, s.length() + length / 2, padChar);
        } else {
            return s;
        }
    }

    private void print(Iterable<?> objects) {
        for (Object o : objects) {
            System.out.println(o);
        }
    }
}
