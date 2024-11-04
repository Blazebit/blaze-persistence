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
import ${package}.repository.CatSimpleViewRepository;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;

public class SampleTest extends AbstractSampleTest {

    @Inject
    private CatSimpleViewRepository catRepository;

    @Test
    public void sampleTest() {
        transactional(em -> {
            List<CatWithOwnerView> list = catRepository.getWithOwnerView();
            
            System.out.println(list);
            Assert.assertEquals(6, list.size());
        });
    }
}
