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

package com.blazebit.persistence.integration.openjpa;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class OpenJPAEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {
    
    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryIntegrator.class.getName());
    
    @Override
    public String getDbms(EntityManagerFactory entityManagerFactory) {
        return null;
    }

    @Override
    public JpaProviderFactory getJpaProviderFactory(final EntityManagerFactory entityManagerFactory) {
        return new JpaProviderFactory() {
            @Override
            public JpaProvider createJpaProvider(EntityManager em) {
                PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory == null ? null : entityManagerFactory.getPersistenceUnitUtil();
                if (persistenceUnitUtil == null && em != null) {
                    persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
                }
                return new OpenJPAJpaProvider(persistenceUnitUtil);
            }
        };
    }

    @Override
    public Map<String, JpqlFunction> getRegisteredFunctions(EntityManagerFactory entityManagerFactory) {
        // TODO: implement
        Map<String, JpqlFunction> functions = new HashMap<>();
        return functions;
    }

    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        // TODO: implement
        return entityManagerFactory;
    }
}
