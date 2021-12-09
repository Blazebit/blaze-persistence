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

package com.blazebit.persistence.examples.microprofile.graphql.view;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

import javax.json.bind.annotation.JsonbTypeDeserializer;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@CreatableEntityView
@EntityView(Cat.class)
@JsonbTypeDeserializer(CatCreateView.MyDeserializer.class)
public interface CatCreateView extends CatSimpleCreateView {

    Set<CatSimpleCreateView> getKittens();
    void setKittens(Set<CatSimpleCreateView> kittens);

    class MyDeserializer implements JsonbDeserializer<CatCreateView> {
        @Override
        public CatCreateView deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
            return null;
        }
    }
}
