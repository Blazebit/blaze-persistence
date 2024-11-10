/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import java.util.*;
import ${package}.model.Cat;
import ${package}.model.Person;
import ${package}.view.CatSimpleView;
import ${package}.repository.CatSimpleViewRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.*;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SampleTest.TestConfig.class)
public class SampleTest extends AbstractSampleTest {

    @Autowired
    private CatSimpleViewRepository catSimpleViewRepository;

    @Test
    public void sampleTest() {
        transactional(em -> {
            final Iterable<CatSimpleView> listIterable = catSimpleViewRepository.findAll();
            final List<CatSimpleView> list = new ArrayList<>();
            listIterable.forEach(view -> list.add(view));
            assertEquals(6, list.size());
        });
    }

    @Configuration
    @ComponentScan("${package}")
    @ImportResource("/META-INF/application-config.xml")
    @EnableEntityViews(basePackages = { "${package}.view"})
    @EnableBlazeRepositories(basePackages = "${package}.repository")
    static class TestConfig {
    }
}
