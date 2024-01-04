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

package com.blazebit.persistence.spring.data.testsuite.webmvc;

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
