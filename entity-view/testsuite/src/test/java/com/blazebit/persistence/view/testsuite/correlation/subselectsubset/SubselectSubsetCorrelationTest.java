/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.PersonSubselectView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.DocumentSubselectView;
import com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model.SimplePersonSubView;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SubselectSubsetCorrelationTest extends AbstractCorrelationTest {

    @Test
    public void testSubselectSubsetCorrelation() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PersonSubselectView.class);
        cfg.addEntityView(DocumentSubselectView.class);
        cfg.addEntityView(SimplePersonSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
                .orderByDesc("name")
                .setMaxResults(2);
        EntityViewSetting<PersonSubselectView, CriteriaBuilder<PersonSubselectView>> setting =
                EntityViewSetting.create(PersonSubselectView.class);
        CriteriaBuilder<PersonSubselectView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

}
