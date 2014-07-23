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
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.spi.Criteria;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class SubqueryTest extends AbstractPersistenceTest {
    @Test
    public void testWhereExsits(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class, "d");
        // query all documents that are owned by somebody
        System.out.println(crit.where("id").in().from(Person.class).select("id").where("ownedDocuments").eqExpression("d").end().getQueryString());
    }
}
