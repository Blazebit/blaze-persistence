/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Ignore
public class CaseWhenTest extends AbstractCoreTest {
    
    @Test
    public void testCaseWhen() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        criteria.selectSimpleCase("document.type")
            .when("'vertrag'", "2")
            .when("'info'", "1")
            .thenElse("0");
        
        criteria.selectCase()
                .when("document.type").eq("vertrag").then("2")
                
                .whenAnd()
                    .and("document.type").eq("vertrag")
                    .and("document.type").eq("info")
                .then("1")
                .whenAnd()
                    .and("document.type").eq("vertrag")
                    .and("document.type").eq("info")
                .then("1")
                .whenOr()
                    .or("document.type").eq("vertrag")
                    .or("document.type").eq("info")
                .then("1")
                .whenOr()
                    .and()
                        .and("document.type").eq("vertrag")
                        .and("document.type").eq("info")
                    .endAnd()
                    .and()
                        .and("document.type").eq("vertrag")
                        .and("document.type").eq("info")
                    .endAnd()
                .then("2")
                .thenElse("0");
    }
}
