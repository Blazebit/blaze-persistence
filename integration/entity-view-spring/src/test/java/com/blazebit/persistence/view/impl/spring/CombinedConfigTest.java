package com.blazebit.persistence.view.impl.spring;

import com.blazebit.persistence.view.impl.spring.views.sub1.TestView1;
import com.blazebit.persistence.view.impl.spring.views.sub2.TestView2;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.10.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CombinedConfigTest.TestConfig.class)
public class CombinedConfigTest {

    @Inject
    private EntityViewConfiguration entityViewConfiguration;

    @BeforeClass
    public static void test() {
        CombinedConfigTest.class.getClassLoader().getResource("entity-views-config.xml");
    }

    @Test
    public void testInjection() {
        Set<Class<?>> entityViews = entityViewConfiguration.getEntityViews();
        Assert.assertEquals(2, entityViews.size());
        Assert.assertTrue(entityViewConfiguration.getEntityViews().contains(TestView1.class));
        Assert.assertTrue(entityViewConfiguration.getEntityViews().contains(TestView2.class));
    }

    @Configuration
    @EnableEntityViews("com.blazebit.persistence.view.impl.spring.views.sub2")
    @ImportResource("/com/blazebit/persistence/view/impl/spring/entity-views-config.xml")
    static class TestConfig {
    }

}
