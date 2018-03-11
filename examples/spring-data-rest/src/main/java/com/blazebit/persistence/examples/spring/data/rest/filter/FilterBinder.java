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

package com.blazebit.persistence.examples.spring.data.rest.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ControllerAdvice
public class FilterBinder {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final ObjectReader filterReader = mapper.readerFor(Filter.class);
        final ObjectWriter filterWriter = mapper.writerFor(Filter.class);
        binder.registerCustomEditor(Filter.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                try {
                    if (text.charAt(0) == '[') {
                        List<Object> objects = filterReader.readValues(text).readAll();
                        setValue(objects.toArray(new Filter[objects.size()]));
                    } else {
                        setValue(filterReader.readValue(text));
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public String getAsText() {
                try {
                    return filterWriter.writeValueAsString(getValue());
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }
            
}
