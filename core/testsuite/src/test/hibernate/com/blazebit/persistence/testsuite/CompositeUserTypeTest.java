/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.testsuite.entity.Product;
import com.blazebit.persistence.testsuite.treat.entity.MonetaryAmount;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public class CompositeUserTypeTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{Product.class};
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager entityManager) {
                Product productA = new Product();
                productA.setPrice(new MonetaryAmount(BigDecimal.TEN, Currency.getInstance("EUR")));
                em.persist(productA);

                Product productB = new Product();
                productB.setPrice(new MonetaryAmount(BigDecimal.TEN, Currency.getInstance("EUR")));
                em.persist(productB);

                em.flush();
                em.clear();
            }
        });
    }

    @Test
    public void testSelectCompositeField() {
        List<Tuple> resultList = cbf.create(em, Tuple.class)
                .from(Product.class)
                .select("product.price.amount")
                .select("product.price.currency")
                .getResultList();

        Assert.assertNotNull(resultList.get(0).get(0));
        Assert.assertNotNull(resultList.get(0).get(1));
    }
}


