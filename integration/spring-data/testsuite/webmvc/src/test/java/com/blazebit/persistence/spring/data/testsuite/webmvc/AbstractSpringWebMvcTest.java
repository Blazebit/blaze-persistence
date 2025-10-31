/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc;

import java.lang.reflect.Method;

import com.blazebit.persistence.view.IdMapping;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@WebAppConfiguration
public abstract class AbstractSpringWebMvcTest extends AbstractSpringTest {

    private static final Method CONTENT;
    private static final Method CONTENT_TYPE;
    private static final Method ACCEPT;

    static {
        Method content = null;
        Method contentType = null;
        Method accept = null;
        try {
            Class<?> clazz = Class.forName("org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder");
            content = clazz.getMethod("content", byte[].class);
			contentType = clazz.getMethod("contentType", MediaType.class);
			accept = clazz.getMethod("accept", String[].class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {

        }
        CONTENT = content;
        CONTENT_TYPE = contentType;
        ACCEPT = accept;
    }

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

    protected RequestBuilder putJson(String path, Object idParam, Object updateView, boolean withId, String accept)
            throws JsonProcessingException {
        MockHttpServletRequestBuilder builder = idParam == null ? put(path) : put(path, idParam);
        if (CONTENT != null) {
            try {
                CONTENT.invoke(builder, withId ? toJsonWithId(updateView) : toJsonWithoutId(updateView));
                CONTENT_TYPE.invoke(builder, MediaType.APPLICATION_JSON);
                if (accept != null) {
                    ACCEPT.invoke(builder, (Object) new String[]{ accept });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return builder;
        } else {
            builder.content(withId ? toJsonWithId(updateView) : toJsonWithoutId(updateView))
                    .contentType(MediaType.APPLICATION_JSON);
            return accept == null ? builder : builder.accept(accept);
        }
    }

    protected RequestBuilder postJson(String path, Object createView) throws JsonProcessingException {
        MockHttpServletRequestBuilder builder = post(path);
        if (CONTENT != null) {
            try {
                CONTENT.invoke(builder, toJsonWithId(createView));
                CONTENT_TYPE.invoke(builder, MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return builder;
        } else {
            return builder.content(toJsonWithId(createView))
                    .contentType(MediaType.APPLICATION_JSON);
        }
    }

}
