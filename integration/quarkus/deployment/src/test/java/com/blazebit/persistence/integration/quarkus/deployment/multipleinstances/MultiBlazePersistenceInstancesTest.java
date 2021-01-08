/*
 * Copyright 2014 - 2021 Blazebit.
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
package com.blazebit.persistence.integration.quarkus.deployment.multipleinstances;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.Car;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.Desk;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.SharedEntity;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config.car.CarView;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config.desk.DeskView;
import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config.shared.SharedEntityView;
import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstance;
import com.blazebit.persistence.view.EntityViewManager;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
public class MultiBlazePersistenceInstancesTest {

    @RegisterExtension
    final static QuarkusUnitTest RUNNER = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Desk.class, Car.class, SharedEntity.class)
                    .addPackages(true, "com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config")
                    .addAsResource("application-multiple-instances.properties", "application.properties")
                    .addAsResource("META-INF/persistence-multiple-persistence-units.xml", "META-INF/persistence.xml")
            );

    @Inject
    @PersistenceUnit("CarPU")
    EntityManager carEntityManager;
    @Inject
    @PersistenceUnit("DeskPU")
    EntityManager deskEntityManager;
    @Inject
    @BlazePersistenceInstance("CarBPInstance")
    CriteriaBuilderFactory carCbf;
    @Inject
    @BlazePersistenceInstance("DeskBPInstance")
    CriteriaBuilderFactory deskCbf;
    @Inject
    @BlazePersistenceInstance("CarBPInstance")
    EntityViewManager carEvm;
    @Inject
    @BlazePersistenceInstance("DeskBPInstance")
    EntityViewManager deskEvm;

    @Test
    @Transactional
    public void testCar() {
        SharedEntity shared = new SharedEntity("Shared");
        Car car = new Car("BMW");
        Desk desk = new Desk("Wooden desk");

        // test EntityManager
        assertThatThrownBy(() -> carEntityManager.persist(desk)).hasMessageContaining("Unknown entity");
        carEntityManager.persist(car);
        carEntityManager.persist(shared);

        // test CriteriaBuilderFactory
        assertNotNull(carCbf.create(carEntityManager, Car.class).where("id").eq(car.getId()).getSingleResult());
        assertNotNull(carCbf.create(carEntityManager, SharedEntity.class).where("id").eq(shared.getId()).getSingleResult());
        assertThatThrownBy(() -> carCbf.create(carEntityManager, Desk.class).where("id").eq(desk.getId()).getSingleResult());

        // test EntityViewManager
        assertEquals("BMW", carEvm.find(carEntityManager, CarView.class, car.getId()).getName());
        assertEquals("Shared", carEvm.find(carEntityManager, SharedEntityView.class, shared.getId()).getName());
        assertThatThrownBy(() -> carEvm.find(carEntityManager, Desk.class, desk.getId()));
    }

    @Test
    @Transactional
    public void testDesk() {
        SharedEntity shared = new SharedEntity("Shared");
        Car car = new Car("BMW");
        Desk desk = new Desk("Wooden desk");

        // test EntityManager
        assertThatThrownBy(() -> deskEntityManager.persist(car)).hasMessageContaining("Unknown entity");
        deskEntityManager.persist(desk);
        deskEntityManager.persist(shared);

        // test CriteriaBuilderFactory
        assertNotNull(deskCbf.create(deskEntityManager, Desk.class).where("id").eq(desk.getId()).getSingleResult());
        assertNotNull(deskCbf.create(deskEntityManager, SharedEntity.class).where("id").eq(shared.getId()).getSingleResult());
        assertThatThrownBy(() -> deskCbf.create(deskEntityManager, Car.class).where("id").eq(car.getId()).getSingleResult());

        // test EntityViewManager
        assertEquals("Wooden desk", deskEvm.find(deskEntityManager, DeskView.class, desk.getId()).getName());
        assertEquals("Shared", deskEvm.find(deskEntityManager, SharedEntityView.class, shared.getId()).getName());
        assertThatThrownBy(() -> deskEvm.find(deskEntityManager, Car.class, car.getId()));
    }
}
