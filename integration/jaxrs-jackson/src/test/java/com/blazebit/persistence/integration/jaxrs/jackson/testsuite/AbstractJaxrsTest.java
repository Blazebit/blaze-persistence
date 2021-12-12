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

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite;

import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.DocumentView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config.EntityManagerFactoryHolder;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.resource.Application;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.IdMapping;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.BindException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class AbstractJaxrsTest {

    private static final Logger LOG = Logger.getLogger(AbstractJaxrsTest.class.getName());
    private static final int SERVER_START_PORT = 18080;
    private static final String SERVER_HOST = "localhost";
    private static Server SERVER;

    @Inject
    private EntityManagerFactoryHolder emfHolder;
    private EntityManager em;
    @Inject
    protected EntityViewManager evm;
    protected WebTarget webTarget;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(AbstractJaxrsTest.class.getResourceAsStream(
                    "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @BeforeClass
    public static void bootContainer() throws Exception {
        int port = SERVER_START_PORT;
        boolean created = false;
        while (!created) {
            URI baseUri = UriBuilder.fromUri("http://" + SERVER_HOST).port(port++).build();
            try {
                SERVER = JettyHttpContainerFactory.createServer(baseUri, new Application());
                created = true;
            } catch (ProcessingException e) {
                if (!(getRootCause(e) instanceof BindException)) {
                    throw e;
                }
                LOG.log(Level.SEVERE, "Can't create http endpoint. Retrying with different port...", e);
            }
        }
        SERVER.start();
        while (!SERVER.isStarted()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    private static Throwable getRootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    @AfterClass
    public static void shutdownContainer() throws Exception {
        SERVER.stop();
    }

    @Before
    public void initInstance() {
        ((JettyHttpContainer) SERVER.getHandler()).getApplicationHandler().getInjectionManager().inject(this);
        dropAndCreateSchema();
        this.webTarget = ClientBuilder.newClient()
                .register(JacksonJsonProvider.class)
                .target(SERVER.getURI());
        this.em = emfHolder.getEntityManagerFactory().createEntityManager();
    }

    private void dropAndCreateSchema() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.schema-generation.database.action", "drop-and-create");
        Persistence.createEntityManagerFactory("TestsuiteBase", properties);
    }

    @After
    public void closeEntityManager() {
        em.close();
    }

    protected void transactional(Consumer<EntityManager> consumer) {
        transactional(em -> {
            consumer.accept(em);
            return null;
        });
    }

    protected <T> T transactional(Function<EntityManager, T> producer) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;

        T result;
        try {
            tx.begin();
            result = producer.apply(em);
            success = true;
        } finally {
            if (success) {
                tx.commit();
            } else {
                tx.rollback();
            }
        }
        return result;
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

    protected static final class DocumentViewImpl implements DocumentView {

        private Long id;
        private String name;
        private PersonViewImpl owner;
        private long ownerDocumentCount;
        private String optionalParameter;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public PersonViewImpl getOwner() {
            return owner;
        }

        @Override
        public long getOwnerDocumentCount() {
            return ownerDocumentCount;
        }

        @Override
        public String getOptionalParameter() {
            return optionalParameter;
        }
    }

    protected static final class PersonViewImpl implements PersonView {
        private UUID id;
        private String name;

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
