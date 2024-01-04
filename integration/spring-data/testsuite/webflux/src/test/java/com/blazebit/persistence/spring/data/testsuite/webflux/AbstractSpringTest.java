/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.spring.data.testsuite.webflux;

import com.blazebit.persistence.spring.data.testsuite.webflux.config.CustomLocalContainerEntityManagerFactoryBean;
import com.blazebit.persistence.spring.data.testsuite.webflux.config.SystemPropertyBasedActiveProfilesResolver;
import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Person;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewAwareJackson2JsonDecoder;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewIdValueAccessorImpl;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.MutablePersistenceUnitInfo;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.IdMapping;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ActiveProfiles(resolver = SystemPropertyBasedActiveProfilesResolver.class)
public abstract class AbstractSpringTest extends AbstractPersistenceTest {

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private EntityViewManager evm;
    private TestContextManager testContextManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                Document.class,
                Person.class
        };
    }
    @Override
    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        // No-op
    }

    @Before
    public void setUpContext() throws Exception {
        // We have to close the EM and EMF constructed by the abstraction
        cleanDatabase();
        this.em.getTransaction().rollback();
        this.em.close();
        this.emf.close();
        this.emf = null;
        this.em = null;
        this.cbf = null;
        this.jpaProvider = null;
        this.dbmsDialect = null;

        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        CustomLocalContainerEntityManagerFactoryBean.properties = createProperties("none");
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        testContextManager.registerTestExecutionListeners(new DirtiesContextTestExecutionListener());
    }

    @Before
    public void configureWebTestClient() {
        webTestClient = webTestClient.mutateWith(new WebTestClientConfigurer() {
            @Override
            public void afterConfigurerAdded(WebTestClient.Builder builder, WebHttpHandlerBuilder webHttpHandlerBuilder, ClientHttpConnector clientHttpConnector) {
                ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new EntityViewAwareJackson2JsonDecoder(evm, new EntityViewIdValueAccessorImpl(conversionService)));

                }).build();
                builder.exchangeStrategies(strategies).build();
            }
        });
    }

    @After
    public void tearDownContext() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
        testContextManager.getTestContext().setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
    }

    protected byte[] toJsonWithId(Object entityView) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(entityView);
    }

    protected byte[] toJsonWithoutId(Object entityView) throws JsonProcessingException {
        VisibilityChecker<?> old = objectMapper.getVisibilityChecker();
        objectMapper.setVisibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT) {
            @Override
            public boolean isGetterVisible(AnnotatedMethod m) {
                return !m.hasAnnotation(IdMapping.class) && super.isGetterVisible(m);
            }
        });
        byte[] result = objectMapper.writeValueAsBytes(entityView);
        objectMapper.setVisibility(old);
        return result;
    }
}
