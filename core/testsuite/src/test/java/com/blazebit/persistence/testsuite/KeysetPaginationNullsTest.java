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
import com.blazebit.persistence.testsuite.entity.KeysetEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
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
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@RunWith(Parameterized.class)
public class KeysetPaginationNullsTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { KeysetEntity.class };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                KeysetEntity k01 = new KeysetEntity(1, null, null);
                KeysetEntity k02 = new KeysetEntity(2, null, null);
                KeysetEntity k03 = new KeysetEntity(3, 0, null);
                KeysetEntity k04 = new KeysetEntity(4, 1, null);
                KeysetEntity k05 = new KeysetEntity(5, 1, null);
                KeysetEntity k06 = new KeysetEntity(6, 2, null);
                KeysetEntity k07 = new KeysetEntity(7, null, 0);
                KeysetEntity k08 = new KeysetEntity(8, null, 1);
                KeysetEntity k09 = new KeysetEntity(9, null, 1);
                KeysetEntity k10 = new KeysetEntity(10, null, 2);
                KeysetEntity k11 = new KeysetEntity(11, 0, 0);
                KeysetEntity k12 = new KeysetEntity(12, 0, 1);
                KeysetEntity k13 = new KeysetEntity(13, 1, 0);
                KeysetEntity k14 = new KeysetEntity(14, 1, 1);
                KeysetEntity k15 = new KeysetEntity(15, 1, 1);
                KeysetEntity k16 = new KeysetEntity(16, 1, 2);
                KeysetEntity k17 = new KeysetEntity(17, 2, 1);
                KeysetEntity k18 = new KeysetEntity(18, 2, 2);

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
    private final boolean idAsc;
    private final boolean idNullsFirst;
    private final PageNavigation navigation;
    private final Integer id1;
    private final Integer id2;
    private final String keysetCondition;

    public KeysetPaginationNullsTest(boolean aAsc, boolean aNullsFirst, boolean bAsc, boolean bNullsFirst, boolean idAsc, boolean idNullsFirst, PageNavigation navigation, Integer id1, Integer id2, String keysetCondition) {
        this.aAsc = aAsc;
        this.aNullsFirst = aNullsFirst;
        this.bAsc = bAsc;
        this.bNullsFirst = bNullsFirst;
        this.idAsc = idAsc;
        this.idNullsFirst = idNullsFirst;
        this.navigation = navigation;
        this.id1 = id1;
        this.id2 = id2;
        this.keysetCondition = keysetCondition;
    }
    
    // This is a little helper to generate the sorted table
    public static void main(String[] args) {
        KeysetPaginationNullsTest test = new KeysetPaginationNullsTest(true, false, true, false, true, true, null, null, null, null);
        test.init();
        List<Tuple> tuples = test.getTableCriteriaBuilder().getResultList();
        
        System.out.println("| PAGE |  A   |  B   | ID |");
        for (int i = 0; i < tuples.size(); i++) {
            Integer aValue = tuples.get(i).get(0, Integer.class);
            Integer bValue = tuples.get(i).get(1, Integer.class);
            Integer id = tuples.get(i).get(2, Integer.class);
            String a = aValue == null ? "NULL" : " " + aValue + "  ";
            String b = bValue == null ? "NULL" : " " + bValue + "  ";
            System.out.println(String.format("|  %02d  | %s | %s | %02d |", i, a, b, id));
        }
        test.em.getTransaction().commit();
        test.em.close();
        test.emf.close();
    }
    
    private CriteriaBuilder<Tuple> getTableCriteriaBuilder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeysetEntity.class, "k")
            .select("a").select("b").select("id");
        crit.orderBy("a", aAsc, aNullsFirst)
            .orderBy("b", bAsc, bNullsFirst)
            .orderBy("id", idAsc, idNullsFirst);
        return crit;
    }
    
    @Parameterized.Parameters
    public static Collection<?> orderingPossibilities() {
        Map<Object[], Object[][]> cases = new LinkedHashMap<Object[], Object[][]>();
        cases.put(new Object[] { true, true, true, true, true, true }, new Object[][] {
//            | PAGE |  A   |  B   | ID |
//            |  00  | NULL | NULL | 01 |
//            |  01  | NULL | NULL | 02 |
//            |  02  | NULL |  0   | 07 |
//            |  03  | NULL |  1   | 08 |
//            |  04  | NULL |  1   | 09 |
//            |  05  | NULL |  2   | 10 |
//            |  06  |  0   | NULL | 03 |
//            |  07  |  0   |  0   | 11 |
//            |  08  |  0   |  1   | 12 |
//            |  09  |  1   | NULL | 04 |
//            |  10  |  1   | NULL | 05 |
//            |  11  |  1   |  0   | 13 |
//            |  12  |  1   |  1   | 14 |
//            |  13  |  1   |  1   | 15 |
//            |  14  |  1   |  2   | 16 |
//            |  15  |  2   | NULL | 06 |
//            |  16  |  2   |  1   | 17 |
//            |  17  |  2   |  2   | 18 |
            { fromPage( 0).to( 0),  1,  1, ""},
            { fromPage( 0).to( 1),  1,  2, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 1))))"},
            { fromPage( 1).to( 0),  2,  1, ""},
            { fromPage( 2).to( 3),  7,  8, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b > 0 OR (k.b = 0 AND k.id > 7))))"},
            { fromPage( 8).to( 9), 12,  4, "(k.a > 0 OR (k.a = 0 AND (k.b > 1 OR (k.b = 1 AND k.id > 12))))"},
            { fromPage( 9).to(10),  4,  5, "(k.a > 1 OR (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 4))))"},
            { fromPage(10).to(11),  5, 13, "(k.a > 1 OR (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 5))))"},
            { fromPage(10).to( 9),  5,  4, "((k.a < 1 OR k.a IS NULL) OR (k.a = 1 AND (k.b IS NULL AND k.id < 5)))"}
        });
        cases.put(new Object[] { true, false, true, true, true, true }, new Object[][] {
//            | PAGE |  A   |  B   | ID |
//            |  00  |  0   | NULL | 03 |
//            |  01  |  0   |  0   | 11 |
//            |  02  |  0   |  1   | 12 |
//            |  03  |  1   | NULL | 04 |
//            |  04  |  1   | NULL | 05 |
//            |  05  |  1   |  0   | 13 |
//            |  06  |  1   |  1   | 14 |
//            |  07  |  1   |  1   | 15 |
//            |  08  |  1   |  2   | 16 |
//            |  09  |  2   | NULL | 06 |
//            |  10  |  2   |  1   | 17 |
//            |  11  |  2   |  2   | 18 |
//            |  12  | NULL | NULL | 01 |
//            |  13  | NULL | NULL | 02 |
//            |  14  | NULL |  0   | 07 |
//            |  15  | NULL |  1   | 08 |
//            |  16  | NULL |  1   | 09 |
//            |  17  | NULL |  2   | 10 |
            { fromPage( 0).to( 0),  3,  3, ""},
            { fromPage( 0).to( 1),  3, 11, "((k.a > 0 OR k.a IS NULL) OR (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 3))))"},
            { fromPage( 1).to( 0), 11,  3, ""},
            { fromPage(12).to(13),  1,  2, "(k.a IS NULL AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 1)))"},
            { fromPage(14).to(15),  7,  8, "(k.a IS NULL AND (k.b > 0 OR (k.b = 0 AND k.id > 7)))"},
        });
        cases.put(new Object[] { true, true, true, false, true, true }, new Object[][] {
//            | PAGE |  A   |  B   | ID |
//            |  00  | NULL |  0   | 07 |
//            |  01  | NULL |  1   | 08 |
//            |  02  | NULL |  1   | 09 |
//            |  03  | NULL |  2   | 10 |
//            |  04  | NULL | NULL | 01 |
//            |  05  | NULL | NULL | 02 |
//            |  06  |  0   |  0   | 11 |
//            |  07  |  0   |  1   | 12 |
//            |  08  |  0   | NULL | 03 |
//            |  09  |  1   |  0   | 13 |
//            |  10  |  1   |  1   | 14 |
//            |  11  |  1   |  1   | 15 |
//            |  12  |  1   |  2   | 16 |
//            |  13  |  1   | NULL | 04 |
//            |  14  |  1   | NULL | 05 |
//            |  15  |  2   |  1   | 17 |
//            |  16  |  2   |  2   | 18 |
//            |  17  |  2   | NULL | 06 |
            { fromPage( 0).to( 0),  7,  7, ""},
            { fromPage( 0).to( 1),  7,  8, "(k.a IS NOT NULL OR (k.a IS NULL AND ((k.b > 0 OR k.b IS NULL) OR (k.b = 0 AND k.id > 7))))"},
            { fromPage( 1).to( 0),  8,  7, ""},
            { fromPage( 4).to( 5),  1,  2, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b IS NULL AND k.id > 1)))"},
        });
        cases.put(new Object[] { true, false, true, false, true, true }, new Object[][] {
//            | PAGE |  A   |  B   | ID |
//            |  00  |  0   |  0   | 11 |
//            |  01  |  0   |  1   | 12 |
//            |  02  |  0   | NULL | 03 |
//            |  03  |  1   |  0   | 13 |
//            |  04  |  1   |  1   | 14 |
//            |  05  |  1   |  1   | 15 |
//            |  06  |  1   |  2   | 16 |
//            |  07  |  1   | NULL | 04 |
//            |  08  |  1   | NULL | 05 |
//            |  09  |  2   |  1   | 17 |
//            |  10  |  2   |  2   | 18 |
//            |  11  |  2   | NULL | 06 |
//            |  12  | NULL |  0   | 07 |
//            |  13  | NULL |  1   | 08 |
//            |  14  | NULL |  1   | 09 |
//            |  15  | NULL |  2   | 10 |
//            |  16  | NULL | NULL | 01 |
//            |  17  | NULL | NULL | 02 |
            { fromPage( 0).to( 0), 11, 11, ""},
            { fromPage( 0).to( 1), 11, 12, "((k.a > 0 OR k.a IS NULL) OR (k.a = 0 AND ((k.b > 0 OR k.b IS NULL) OR (k.b = 0 AND k.id > 11))))"},
            { fromPage( 1).to( 0), 12, 11, ""},
            { fromPage(12).to(13),  7,  8, "(k.a IS NULL AND ((k.b > 0 OR k.b IS NULL) OR (k.b = 0 AND k.id > 7)))"},
            { fromPage(16).to(17),  1,  2, "(k.a IS NULL AND (k.b IS NULL AND k.id > 1))"},
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
        config.setProperty(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "false");
        return config;
    }

    @Test
    public void matrixText() {
        boolean aAsc;
        boolean aNullsFirst;
        boolean bAsc;
        boolean bNullsFirst;
        boolean idAsc;
        boolean idNullsFirst;
        
        if (navigation.from > navigation.to && !keysetCondition.isEmpty()) {
            // We need to invert the order when we scroll back
            aAsc = !this.aAsc;
            aNullsFirst = !this.aNullsFirst;
            bAsc = !this.bAsc;
            bNullsFirst = !this.bNullsFirst;
            idAsc = !this.idAsc;
            idNullsFirst = !this.idNullsFirst;
        } else {
            aAsc = this.aAsc;
            aNullsFirst = this.aNullsFirst;
            bAsc = this.bAsc;
            bNullsFirst = this.bNullsFirst;
            idAsc = this.idAsc;
            idNullsFirst = this.idNullsFirst;
        }
        
        String expectedObjectQueryStart = "SELECT k.id, k.a, k.b FROM KeysetEntity k" + (keysetCondition.isEmpty() ? "" : " WHERE ");
        String expectedObjectQueryEnd = " ORDER BY "
            + orderByClause("k.a", aAsc, aNullsFirst) + ", "
            + orderByClause("k.b", bAsc, bNullsFirst) + ", "
            + "k.id " + (idAsc ? "ASC" : "DESC");
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeysetEntity.class, "k")
            .select("id");
        crit.orderBy("a", this.aAsc, this.aNullsFirst)
            .orderBy("b", this.bAsc, this.bNullsFirst)
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
        assertEquals(id2, result.get(0).get(0));
        
        // simple page id query test
        assertNull(pcb.getPageIdQueryString());

        // Optimized object query test
        String actualObjectQueryString = pcb.getQueryString();
        for (int i = 0; i < key.length; i++) {
            if (key[i] != null) {
                actualObjectQueryString = actualObjectQueryString.replaceAll(Pattern.quote(":_keysetParameter_" + i), key[i].toString());
            }
        }
        
        assertEquals(expectedObjectQueryStart + keysetCondition + expectedObjectQueryEnd, actualObjectQueryString);
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
