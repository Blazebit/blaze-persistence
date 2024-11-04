/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.blazebit.persistence.view.IdMapping;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@WebAppConfiguration
public abstract class AbstractSpringWebMvcTest extends AbstractSpringTest {

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
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
