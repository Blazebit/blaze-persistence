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
        BasicCatView someCat = catDataAccess.getCatByName("Generation 2 - Cat 2/1", basicCatSetting);

        // entity-view example with CTE
        System.out.println(heading("Family tree of " + someCat));
        print(catDataAccess.getCatHierarchy(someCat.getId(), basicCatSetting));
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
