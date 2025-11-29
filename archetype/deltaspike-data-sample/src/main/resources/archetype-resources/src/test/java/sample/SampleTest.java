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
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SampleTest extends AbstractSampleTest {

    @Inject
    private CatSimpleViewRepository catRepository;

    @Test
    public void sampleTest() {
        transactional(em -> {
            List<CatWithOwnerView> list = catRepository.getWithOwnerView();
            
            System.out.println(list);
            assertEquals(6, list.size());
        });
    }
}
