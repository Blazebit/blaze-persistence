/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CTE;
import com.vladmihalcea.hibernate.type.json.internal.JsonStringSqlTypeDescriptor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
public class Issue992Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ BasicEntity.class, JsonCTE.class };
    }

    @Entity(name = "BasicEntity")
    @TypeDef(name = "json", typeClass = JsonStringSqlTypeDescriptor.class)
    public static class BasicEntity {
        @Id
        Long key1;

        @Type(type = "json")
        Map<String, String> attributeValues;

    }

    @CTE
    @Entity(name = "JsonCTE")
    @TypeDef(name = "json", typeClass = JsonStringSqlTypeDescriptor.class)
    public static class JsonCTE {
        @Id
        Long key1;

        @Type(type = "json")
        Map<String, String> attributeValues;

    }

    @Test
    public void test1() {
        cbf.create(em, JsonCTE.class)
                .with(JsonCTE.class)
                    .from(BasicEntity.class)
                    .bind("key1").select("key1")
                    .bind("attributeValues").select("NULL")
                .end()
                .getResultList();
    }

}
