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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.KeysetEntity2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
// DataNucleus has a bug with null precedence rendering
// see https://github.com/datanucleus/datanucleus-rdbms/issues/224
@Category({ NoDatanucleus.class })
@RunWith(Parameterized.class)
public class OptimizedKeysetPaginationNullsTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { KeysetEntity2.class };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                KeysetEntity2 k01 = new KeysetEntity2(1, 1, null, null);
                KeysetEntity2 k02 = new KeysetEntity2(2, 0, null, null);
                KeysetEntity2 k03 = new KeysetEntity2(3, 2, 0, null);
                KeysetEntity2 k04 = new KeysetEntity2(4, 1, 1, null);
                KeysetEntity2 k05 = new KeysetEntity2(5, 2, 1, null);
                KeysetEntity2 k06 = new KeysetEntity2(6, 1, 2, null);
                KeysetEntity2 k07 = new KeysetEntity2(7, 2, null, 0);
                KeysetEntity2 k08 = new KeysetEntity2(8, 1, null, 1);
                KeysetEntity2 k09 = new KeysetEntity2(9, 0, null, 1);
                KeysetEntity2 k10 = new KeysetEntity2(10, 2, null, 2);
                KeysetEntity2 k11 = new KeysetEntity2(11, 1, 0, 0);
                KeysetEntity2 k12 = new KeysetEntity2(12, 1, 0, 1);
                KeysetEntity2 k13 = new KeysetEntity2(13, 0, 1, 0);
                KeysetEntity2 k14 = new KeysetEntity2(14, 0, 1, 1);
                KeysetEntity2 k15 = new KeysetEntity2(15, 0, 1, 1);
                KeysetEntity2 k16 = new KeysetEntity2(16, 0, 1, 2);
                KeysetEntity2 k17 = new KeysetEntity2(17, 0, 2, 1);
                KeysetEntity2 k18 = new KeysetEntity2(18, 0, 2, 2);

                em.persist(k01);
                em.persist(k02);
                em.persist(k03);
                em.persist(k04);
                em.persist(k05);
                em.persist(k06);
                em.persist(k07);
                em.persist(k08);
                em.persist(k09);
                em.persist(k10);
                em.persist(k11);
                em.persist(k12);
                em.persist(k13);
                em.persist(k14);
                em.persist(k15);
                em.persist(k16);
                em.persist(k17);
                em.persist(k18);
            }
        });
    }

    private final boolean aAsc;
    private final boolean aNullsFirst;
    private final boolean bAsc;
    private final boolean bNullsFirst;
    private final boolean cAsc;
    private final boolean cNullsFirst;
    private final boolean idAsc;
    private final boolean idNullsFirst;
    private final PageNavigation navigation;
    private final Integer id1;
    private final Integer id2;
    private final String keysetCondition;

    public OptimizedKeysetPaginationNullsTest(boolean aAsc, boolean aNullsFirst, boolean bAsc, boolean bNullsFirst, boolean cAsc, boolean cNullsFirst, boolean idAsc, boolean idNullsFirst, PageNavigation navigation, Integer id1, Integer id2, String keysetCondition) {
        this.aAsc = aAsc;
        this.aNullsFirst = aNullsFirst;
        this.bAsc = bAsc;
        this.bNullsFirst = bNullsFirst;
        this.cAsc = cAsc;
        this.cNullsFirst = cNullsFirst;
        this.idAsc = idAsc;
        this.idNullsFirst = idNullsFirst;
        this.navigation = navigation;
        this.id1 = id1;
        this.id2 = id2;
        this.keysetCondition = keysetCondition;
    }
    
    // This is a little helper to generate the sorted table
    public static void main(String[] args) {
        OptimizedKeysetPaginationNullsTest test = new OptimizedKeysetPaginationNullsTest(true, false, true, false, false, false, true, true, null, null, null, null);
        test.init();
        List<Tuple> tuples = test.getTableCriteriaBuilder().getResultList();
        
        System.out.println("| PAGE |  A   |  B   |  C   | ID |");
        for (int i = 0; i < tuples.size(); i++) {
            Integer aValue = tuples.get(i).get(0, Integer.class);
            Integer bValue = tuples.get(i).get(1, Integer.class);
            Integer cValue = tuples.get(i).get(2, Integer.class);
            Integer id = tuples.get(i).get(3, Integer.class);
            String a = aValue == null ? "NULL" : " " + aValue + "  ";
            String b = bValue == null ? "NULL" : " " + bValue + "  ";
            String c = cValue == null ? "NULL" : " " + cValue + "  ";
            System.out.println(String.format("|  %02d  | %s | %s | %s | %02d |", i, a, b, c, id));
        }
        test.em.getTransaction().commit();
        test.em.close();
        test.emf.close();
    }
    
    private CriteriaBuilder<Tuple> getTableCriteriaBuilder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeysetEntity2.class, "k")
            .select("a").select("b").select("c").select("id");
        crit.orderBy("a", aAsc, aNullsFirst)
            .orderBy("b", bAsc, bNullsFirst)
            .orderBy("c", cAsc, cNullsFirst)
            .orderBy("id", idAsc, idNullsFirst);
        return crit;
    }
    
    @Parameterized.Parameters
    public static Collection<?> orderingPossibilities() {
        Map<Object[], Object[][]> cases = new LinkedHashMap<Object[], Object[][]>();
        cases.put(new Object[] { true, true, true, true, true, true, true, true }, new Object[][] {
//            | PAGE |  A   |  B   |  C   | ID |
//            |  00  |  0   | NULL | NULL | 02 |
//            |  01  |  0   | NULL |  1   | 09 |
//            |  02  |  0   |  1   |  0   | 13 |
//            |  03  |  0   |  1   |  1   | 14 |
//            |  04  |  0   |  1   |  1   | 15 |
//            |  05  |  0   |  1   |  2   | 16 |
//            |  06  |  0   |  2   |  1   | 17 |
//            |  07  |  0   |  2   |  2   | 18 |
//            |  08  |  1   | NULL | NULL | 01 |
//            |  09  |  1   | NULL |  1   | 08 |
//            |  10  |  1   |  0   |  0   | 11 |
//            |  11  |  1   |  0   |  1   | 12 |
//            |  12  |  1   |  1   | NULL | 04 |
//            |  13  |  1   |  2   | NULL | 06 |
//            |  14  |  2   | NULL |  0   | 07 |
//            |  15  |  2   | NULL |  2   | 10 |
//            |  16  |  2   |  0   | NULL | 03 |
//            |  17  |  2   |  1   | NULL | 05 |
            { fromPage( 0).to( 0),  2,  2, ""},
            { fromPage( 0).to( 1),  2,  9, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c IS NULL AND k.id <= 2)))"},
            { fromPage( 1).to( 0),  9,  2, ""},
            { fromPage( 2).to( 3), 13, 14, "k.a >= 0 AND NOT (k.a = 0 AND (k.b < 1 OR (k.b = 1 AND (k.c < 0 OR (k.c = 0 AND k.id <= 13)))))"},
            { fromPage( 3).to( 2), 14, 13, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b > 1 OR (k.b IS NOT NULL AND k.b = 1 AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id >= 14)))))"},
            { fromPage( 7).to( 8), 18,  1, "k.a >= 0 AND NOT (k.a = 0 AND (k.b < 2 OR (k.b = 2 AND (k.c < 2 OR (k.c = 2 AND k.id <= 18)))))"},
            { fromPage( 8).to( 7),  1, 18, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id >= 1)))))"},
            { fromPage( 9).to(10),  8, 11, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c < 1 OR (k.c = 1 AND k.id <= 8))))"},
            { fromPage(10).to( 9), 11,  8, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL AND k.b > 0 OR (k.b IS NOT NULL AND k.b = 0 AND (k.c IS NOT NULL AND k.c > 0 OR (k.c IS NOT NULL AND k.c = 0 AND k.id >= 11)))))"},
            { fromPage(13).to(14),  6,  7, "k.a >= 1 AND NOT (k.a = 1 AND (k.b < 2 OR (k.b = 2 AND (k.c IS NULL AND k.id <= 6))))"},
            { fromPage(14).to(13),  7,  6, "k.a <= 2 AND NOT (k.a = 2 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL AND k.c > 0 OR (k.c IS NOT NULL AND k.c = 0 AND k.id >= 7)))))"},
            { fromPage(15).to(16), 10,  3, "k.a >= 2 AND NOT (k.a = 2 AND (k.b IS NULL AND (k.c < 2 OR (k.c = 2 AND k.id <= 10))))"},
            { fromPage(16).to(15),  3, 10, "k.a <= 2 AND NOT (k.a = 2 AND (k.b IS NOT NULL AND k.b > 0 OR (k.b IS NOT NULL AND k.b = 0 AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id >= 3)))))"}
        });
        cases.put(new Object[] { true, false, true, false, true, true, true, true }, new Object[][] {
//            | PAGE |  A   |  B   |  C   | ID |
//            |  00  |  0   |  1   |  0   | 13 |
//            |  01  |  0   |  1   |  1   | 14 |
//            |  02  |  0   |  1   |  1   | 15 |
//            |  03  |  0   |  1   |  2   | 16 |
//            |  04  |  0   |  2   |  1   | 17 |
//            |  05  |  0   |  2   |  2   | 18 |
//            |  06  |  0   | NULL | NULL | 02 |
//            |  07  |  0   | NULL |  1   | 09 |
//            |  08  |  1   |  0   |  0   | 11 |
//            |  09  |  1   |  0   |  1   | 12 |
//            |  10  |  1   |  1   | NULL | 04 |
//            |  11  |  1   |  2   | NULL | 06 |
//            |  12  |  1   | NULL | NULL | 01 |
//            |  13  |  1   | NULL |  1   | 08 |
//            |  14  |  2   |  0   | NULL | 03 |
//            |  15  |  2   |  1   | NULL | 05 |
//            |  16  |  2   | NULL |  0   | 07 |
//            |  17  |  2   | NULL |  2   | 10 |
            { fromPage( 0).to( 0), 13, 13, ""},
            { fromPage( 3).to( 4), 16, 17, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 1 OR (k.b IS NOT NULL AND k.b = 1 AND (k.c < 2 OR (k.c = 2 AND k.id <= 16)))))"},
            { fromPage( 4).to( 3), 17, 16, "k.a <= 0 AND NOT (k.a = 0 AND (k.b > 2 OR (k.b = 2 AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id >= 17)))))"},
            { fromPage( 5).to( 6), 18,  2, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c < 2 OR (k.c = 2 AND k.id <= 18)))))"},
            { fromPage( 6).to( 5),  2, 18, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id >= 2))))"},
            { fromPage( 6).to( 7),  2,  9, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NULL AND k.id <= 2))))"},
            { fromPage( 7).to( 6),  9,  2, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id >= 9))))"},
            { fromPage( 7).to( 8),  9, 11, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c < 1 OR (k.c = 1 AND k.id <= 9)))))"},
            { fromPage( 8).to( 7), 11,  9, "k.a <= 1 AND NOT (k.a = 1 AND (k.b > 0 OR (k.b = 0 AND (k.c IS NOT NULL AND k.c > 0 OR (k.c IS NOT NULL AND k.c = 0 AND k.id >= 11)))))"},
            { fromPage(11).to(12),  6,  1, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c IS NULL AND k.id <= 6))))"},
            { fromPage(12).to(11),  1,  6, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id >= 1))))"},
            { fromPage(13).to(14),  8,  3, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c < 1 OR (k.c = 1 AND k.id <= 8)))))"},
            { fromPage(14).to(13),  3,  8, "k.a <= 2 AND NOT (k.a = 2 AND (k.b > 0 OR (k.b = 0 AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id >= 3)))))"}
        });
        cases.put(new Object[] { true, true, true, true, true, false, true, true }, new Object[][] {
//            | PAGE |  A   |  B   |  C   | ID |
//            |  00  |  0   | NULL |  1   | 09 |
//            |  01  |  0   | NULL | NULL | 02 |
//            |  02  |  0   |  1   |  0   | 13 |
//            |  03  |  0   |  1   |  1   | 14 |
//            |  04  |  0   |  1   |  1   | 15 |
//            |  05  |  0   |  1   |  2   | 16 |
//            |  06  |  0   |  2   |  1   | 17 |
//            |  07  |  0   |  2   |  2   | 18 |
//            |  08  |  1   | NULL |  1   | 08 |
//            |  09  |  1   | NULL | NULL | 01 |
//            |  10  |  1   |  0   |  0   | 11 |
//            |  11  |  1   |  0   |  1   | 12 |
//            |  12  |  1   |  1   | NULL | 04 |
//            |  13  |  1   |  2   | NULL | 06 |
//            |  14  |  2   | NULL |  0   | 07 |
//            |  15  |  2   | NULL |  2   | 10 |
//            |  16  |  2   |  0   | NULL | 03 |
//            |  17  |  2   |  1   | NULL | 05 |
            { fromPage( 0).to( 0),  9,  9, ""},
            { fromPage( 4).to( 5), 15, 16, "k.a >= 0 AND NOT (k.a = 0 AND (k.b < 1 OR (k.b = 1 AND (k.c IS NOT NULL AND k.c < 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 15)))))"},
            { fromPage( 5).to( 4), 16, 15, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b > 1 OR (k.b IS NOT NULL AND k.b = 1 AND (k.c > 2 OR (k.c = 2 AND k.id >= 16)))))"},
            { fromPage( 1).to( 0),  2,  9, ""},
            { fromPage( 7).to( 8), 18,  8, "k.a >= 0 AND NOT (k.a = 0 AND (k.b < 2 OR (k.b = 2 AND (k.c IS NOT NULL AND k.c < 2 OR (k.c IS NOT NULL AND k.c = 2 AND k.id <= 18)))))"},
            { fromPage( 8).to( 7),  8, 18, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c > 1 OR (k.c = 1 AND k.id >= 8)))))"},
            { fromPage( 8).to( 9),  8,  1, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c IS NOT NULL AND k.c < 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 8))))"},
            { fromPage( 9).to( 8),  1,  8, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NULL AND k.id >= 1))))"},
            { fromPage( 9).to(10),  1, 11, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 1))))"},
            { fromPage(10).to( 9), 11,  1, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL AND k.b > 0 OR (k.b IS NOT NULL AND k.b = 0 AND (k.c > 0 OR (k.c = 0 AND k.id >= 11)))))"},
            { fromPage(13).to(14),  6,  7, "k.a >= 1 AND NOT (k.a = 1 AND (k.b < 2 OR (k.b = 2 AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 6)))))"},
            { fromPage(14).to(13),  7,  6, "k.a <= 2 AND NOT (k.a = 2 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c > 0 OR (k.c = 0 AND k.id >= 7)))))"},
        });
        cases.put(new Object[] { true, false, true, false, true, false, true, true }, new Object[][] {
//            | PAGE |  A   |  B   |  C   | ID |
//            |  00  |  0   |  1   |  0   | 13 |
//            |  01  |  0   |  1   |  1   | 14 |
//            |  02  |  0   |  1   |  1   | 15 |
//            |  03  |  0   |  1   |  2   | 16 |
//            |  04  |  0   |  2   |  1   | 17 |
//            |  05  |  0   |  2   |  2   | 18 |
//            |  06  |  0   | NULL |  1   | 09 |
//            |  07  |  0   | NULL | NULL | 02 |
//            |  08  |  1   |  0   |  0   | 11 |
//            |  09  |  1   |  0   |  1   | 12 |
//            |  10  |  1   |  1   | NULL | 04 |
//            |  11  |  1   |  2   | NULL | 06 |
//            |  12  |  1   | NULL |  1   | 08 |
//            |  13  |  1   | NULL | NULL | 01 |
//            |  14  |  2   |  0   | NULL | 03 |
//            |  15  |  2   |  1   | NULL | 05 |
//            |  16  |  2   | NULL |  0   | 07 |
//            |  17  |  2   | NULL |  2   | 10 |
            { fromPage( 0).to( 0), 13, 13, ""},
            { fromPage( 0).to( 1), 13, 14, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 1 OR (k.b IS NOT NULL AND k.b = 1 AND (k.c IS NOT NULL AND k.c < 0 OR (k.c IS NOT NULL AND k.c = 0 AND k.id <= 13)))))"},
            { fromPage( 1).to( 0), 14, 13, ""},
            { fromPage( 5).to( 6), 18,  9, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c IS NOT NULL AND k.c < 2 OR (k.c IS NOT NULL AND k.c = 2 AND k.id <= 18)))))"},
            { fromPage( 6).to( 5),  9, 18, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c > 1 OR (k.c = 1 AND k.id >= 9))))"},
            { fromPage( 6).to( 7),  9,  2, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL AND k.c < 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 9)))))"},
            { fromPage( 7).to( 6),  2,  9, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c IS NULL AND k.id >= 2)))"},
            { fromPage( 7).to( 8),  2, 11, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 2)))))"},
            { fromPage( 8).to( 7), 11,  2, "k.a <= 1 AND NOT (k.a = 1 AND (k.b > 0 OR (k.b = 0 AND (k.c > 0 OR (k.c = 0 AND k.id >= 11)))))"},
            { fromPage(11).to(12),  6,  8, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 6)))))"},
            { fromPage(12).to(11),  8,  6, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c > 1 OR (k.c = 1 AND k.id >= 8))))"},
        });
        cases.put(new Object[] { true, false, true, false, false, false, true, true }, new Object[][] {
//            | PAGE |  A   |  B   |  C   | ID |
//            |  00  |  0   |  1   |  2   | 16 |
//            |  01  |  0   |  1   |  1   | 14 |
//            |  02  |  0   |  1   |  1   | 15 |
//            |  03  |  0   |  1   |  0   | 13 |
//            |  04  |  0   |  2   |  2   | 18 |
//            |  05  |  0   |  2   |  1   | 17 |
//            |  06  |  0   | NULL |  1   | 09 |
//            |  07  |  0   | NULL | NULL | 02 |
//            |  08  |  1   |  0   |  1   | 12 |
//            |  09  |  1   |  0   |  0   | 11 |
//            |  10  |  1   |  1   | NULL | 04 |
//            |  11  |  1   |  2   | NULL | 06 |
//            |  12  |  1   | NULL |  1   | 08 |
//            |  13  |  1   | NULL | NULL | 01 |
//            |  14  |  2   |  0   | NULL | 03 |
//            |  15  |  2   |  1   | NULL | 05 |
//            |  16  |  2   | NULL |  2   | 10 |
//            |  17  |  2   | NULL |  0   | 07 |
            { fromPage( 2).to( 3), 15, 13, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 1 OR (k.b IS NOT NULL AND k.b = 1 AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 15)))))"},
            { fromPage( 3).to( 2), 13, 15, "k.a <= 0 AND NOT (k.a = 0 AND (k.b > 1 OR (k.b = 1 AND (k.c < 0 OR (k.c = 0 AND k.id >= 13)))))"},
            { fromPage( 5).to( 6), 17,  9, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 17)))))"},
            { fromPage( 6).to( 5),  9, 17, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c < 1 OR (k.c = 1 AND k.id >= 9))))"},
            { fromPage( 6).to( 7),  9,  2, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL AND k.c > 1 OR (k.c IS NOT NULL AND k.c = 1 AND k.id <= 9)))))"},
            { fromPage( 7).to( 6),  2,  9, "k.a <= 0 AND NOT (k.a = 0 AND (k.b IS NULL AND (k.c IS NULL AND k.id >= 2)))"},
            { fromPage( 7).to( 8),  2, 12, "k.a >= 0 AND NOT (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 2)))))"},
            { fromPage( 8).to( 7), 12,  2, "k.a <= 1 AND NOT (k.a = 1 AND (k.b > 0 OR (k.b = 0 AND (k.c < 1 OR (k.c = 1 AND k.id >= 12)))))"},
            { fromPage(11).to(12),  6,  8, "k.a >= 1 AND NOT (k.a = 1 AND (k.b IS NOT NULL AND k.b < 2 OR (k.b IS NOT NULL AND k.b = 2 AND (k.c IS NOT NULL OR (k.c IS NULL AND k.id <= 6)))))"},
            { fromPage(12).to(11),  8,  6, "k.a <= 1 AND NOT (k.a = 1 AND (k.b IS NULL AND (k.c < 1 OR (k.c = 1 AND k.id >= 8))))"},
        });
        
        // TODO: need tests for other orders
        
        // The following converts the map into an object array so that JUnit can use it
        List<Object[]> possibilities = new ArrayList<Object[]>();
        List<Object[]> possibilities2 = new ArrayList<Object[]>();
        for (Map.Entry<Object[], Object[][]> entry : cases.entrySet()) {
            Object[] key = entry.getKey();
            Object[][] value = entry.getValue();
            int valueLength = value[0].length;
            
            for (int i = 0; i < value.length; i++) {
                Object[] possibility1 = new Object[key.length + valueLength];
                System.arraycopy(key, 0, possibility1, 0, key.length);
                System.arraycopy(value[i], 0, possibility1, key.length, valueLength);
                possibilities.add(possibility1);
                
                // Analog possibility with NULLS LAST for id which should have the same effect because id is not nullable
                Object[] possibility2 = new Object[key.length + valueLength];
                System.arraycopy(possibility1, 0, possibility2, 0, possibility1.length);
                possibility2[key.length - 1] = false;
                possibilities2.add(possibility2);
            }
        }
        
        possibilities.addAll(possibilities2);
        return possibilities;
    }

    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.setProperty(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "true");
        return config;
    }

    @Test
    public void matrixText() {
        boolean aAsc;
        boolean aNullsFirst;
        boolean bAsc;
        boolean bNullsFirst;
        boolean cAsc;
        boolean cNullsFirst;
        boolean idAsc;
        boolean idNullsFirst;
        
        if (navigation.from > navigation.to && !keysetCondition.isEmpty()) {
            // We need to invert the order when we scroll back
            aAsc = !this.aAsc;
            aNullsFirst = !this.aNullsFirst;
            bAsc = !this.bAsc;
            bNullsFirst = !this.bNullsFirst;
            cAsc = !this.cAsc;
            cNullsFirst = !this.cNullsFirst;
            idAsc = !this.idAsc;
            idNullsFirst = !this.idNullsFirst;
        } else {
            aAsc = this.aAsc;
            aNullsFirst = this.aNullsFirst;
            bAsc = this.bAsc;
            bNullsFirst = this.bNullsFirst;
            cAsc = this.cAsc;
            cNullsFirst = this.cNullsFirst;
            idAsc = this.idAsc;
            idNullsFirst = this.idNullsFirst;
        }
        
        String expectedObjectQueryStart = "SELECT k.id, k.a, k.b, k.c FROM KeysetEntity2 k" + (keysetCondition.isEmpty() ? "" : " WHERE ");
        String expectedObjectQueryEnd = " ORDER BY "
            + "k.a " + (aAsc ? "ASC" : "DESC") + ", "
            + orderByClause("k.b", bAsc, bNullsFirst) + ", "
            + orderByClause("k.c", cAsc, cNullsFirst) + ", "
            + "k.id " + (idAsc ? "ASC" : "DESC");
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeysetEntity2.class, "k")
            .select("id");
        crit.orderBy("a", this.aAsc, this.aNullsFirst)
            .orderBy("b", this.bAsc, this.bNullsFirst)
            .orderBy("c", this.cAsc, this.cNullsFirst)
            .orderBy("id", this.idAsc, this.idNullsFirst);
        
        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, navigation.from, 1);
        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(id1, result.get(0).get(0));
        
        Serializable[] key;
        
        if (navigation.from < navigation.to) {
            key = result.getKeysetPage().getHighest().getTuple();
        } else {
            key = result.getKeysetPage().getLowest().getTuple();
        }
        
        pcb = crit.page(result.getKeysetPage(), navigation.to, 1);
        result = pcb.getResultList();
        
        assertNull(pcb.getPageIdQueryString());

        // Optimized object query test
        String actualObjectQueryString = pcb.getQueryString();
        for (int i = 0; i < key.length; i++) {
            if (key[i] != null) {
                actualObjectQueryString = actualObjectQueryString.replaceAll(Pattern.quote(":_keysetParameter_" + i), key[i].toString());
            }
        }
        
        assertEquals(expectedObjectQueryStart + keysetCondition + expectedObjectQueryEnd, actualObjectQueryString);
        assertEquals(id2, result.get(0).get(0));
    }
    
    private String orderByClause(String expression, boolean asc, boolean nullsFirst) {
        if (asc) {
            if (nullsFirst) {
                return renderNullPrecedence(expression, "ASC", "FIRST");
            } else {
                return renderNullPrecedence(expression, "ASC", "LAST");
            }
        } else {
            if (nullsFirst) {
                return renderNullPrecedence(expression, "DESC", "FIRST");
            } else {
                return renderNullPrecedence(expression, "DESC", "LAST");
            }
        }
    }

    private String groupByClause(String expression, boolean asc, boolean nullsFirst) {
        if (asc) {
            if (nullsFirst) {
                return renderNullPrecedenceGroupBy(expression);
            } else {
                return renderNullPrecedenceGroupBy(expression);
            }
        } else {
            if (nullsFirst) {
                return renderNullPrecedenceGroupBy(expression);
            } else {
                return renderNullPrecedenceGroupBy(expression);
            }
        }
    }
        
    static PageNavigation.PageNavigation1 fromPage(int from) {
        PageNavigation.PageNavigation1 result = new PageNavigation.PageNavigation1();
        result.from = from;
        return result;
    }
    
    static class PageNavigation {
        
        int from;
        int to;

        public PageNavigation(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        static class PageNavigation1 {
            int from;
            
            PageNavigation to(int to) {
                return new PageNavigation(from, to);
            }
        }
    }
}
