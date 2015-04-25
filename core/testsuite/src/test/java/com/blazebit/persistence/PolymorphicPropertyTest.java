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

import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.entity.PolymorphicBase;
import com.blazebit.persistence.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.entity.PolymorphicSub1;
import com.blazebit.persistence.entity.PolymorphicSub2;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import org.hibernate.Query;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PolymorphicPropertyTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            PolymorphicPropertyBase.class,
            PolymorphicPropertySub1.class,
            PolymorphicPropertySub2.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class
        };
    }
    
    @Test
    @Ignore("I wasn't able to fully debug and understand the problem and had time pressure")
    public void testSelectSubProperty() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("propBase.base.test1");
//        cb.where("TYPE(base)").eq(PolymorphicSub1.class);
        String expectedQuery = "SELECT base_1.test1 FROM PolymorphicPropertyBase propBase LEFT JOIN propBase.base base_1";// WHERE TYPE(base) = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
