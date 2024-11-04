/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import java.util.List;
import ${package}.model.Cat;
import ${package}.model.Person;
import ${package}.view.CatSimpleView;
import ${package}.view.CatWithOwnerView;
import ${package}.view.PersonSimpleView;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SampleTest extends AbstractSampleTest {

    @Test
    public void sampleTest() {
        transactional(em -> {
            CriteriaBuilder<Cat> catCriteriaBuilder = cbf.create(em, Cat.class);
            catCriteriaBuilder.from(Cat.class, "cat");

            EntityViewSetting<CatWithOwnerView, CriteriaBuilder<CatWithOwnerView>> setting = EntityViewSetting.create(CatWithOwnerView.class);
            CriteriaBuilder<CatWithOwnerView> cb = evm.applySetting(setting, catCriteriaBuilder);
            List<CatWithOwnerView> list = cb.getResultList();

            System.out.println(list);
            assertEquals(6, list.size());
        });
    }
}
