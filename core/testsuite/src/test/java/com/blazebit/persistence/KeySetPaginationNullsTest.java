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

import com.blazebit.persistence.entity.KeySetEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@RunWith(Parameterized.class)
public class KeySetPaginationNullsTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { KeySetEntity.class };
    }

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            KeySetEntity k01 = new KeySetEntity(1, null, null);
            KeySetEntity k02 = new KeySetEntity(2, null, null);
            KeySetEntity k03 = new KeySetEntity(3, 0, null);
            KeySetEntity k04 = new KeySetEntity(4, 1, null);
            KeySetEntity k05 = new KeySetEntity(5, 1, null);
            KeySetEntity k06 = new KeySetEntity(6, 2, null);
            KeySetEntity k07 = new KeySetEntity(7, null, 0);
            KeySetEntity k08 = new KeySetEntity(8, null, 1);
            KeySetEntity k09 = new KeySetEntity(9, null, 1);
            KeySetEntity k10 = new KeySetEntity(10, null, 2);
            KeySetEntity k11 = new KeySetEntity(11, 0, 0);
            KeySetEntity k12 = new KeySetEntity(12, 0, 1);
            KeySetEntity k13 = new KeySetEntity(13, 1, 0);
            KeySetEntity k14 = new KeySetEntity(14, 1, 1);
            KeySetEntity k15 = new KeySetEntity(15, 1, 1);
            KeySetEntity k16 = new KeySetEntity(16, 1, 2);
            KeySetEntity k17 = new KeySetEntity(17, 2, 1);
            KeySetEntity k18 = new KeySetEntity(18, 2, 2);
            
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

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
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
    private final String keySetCondition;

    public KeySetPaginationNullsTest(boolean aAsc, boolean aNullsFirst, boolean bAsc, boolean bNullsFirst, boolean idAsc, boolean idNullsFirst, PageNavigation navigation, Integer id1, Integer id2, String keySetCondition) {
        this.aAsc = aAsc;
        this.aNullsFirst = aNullsFirst;
        this.bAsc = bAsc;
        this.bNullsFirst = bNullsFirst;
        this.idAsc = idAsc;
        this.idNullsFirst = idNullsFirst;
        this.navigation = navigation;
        this.id1 = id1;
        this.id2 = id2;
        this.keySetCondition = keySetCondition;
    }
    
    // This is a little helper to generate the sorted table
    public static void main(String[] args) {
        KeySetPaginationNullsTest test = new KeySetPaginationNullsTest(true, false, true, false, true, true, null, null, null, null);
        test.init();
        test.setUp();
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
    }
    
    private CriteriaBuilder<Tuple> getTableCriteriaBuilder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeySetEntity.class, "k")
            .select("a").select("b").select("id");
        crit.orderBy("a", aAsc, aNullsFirst)
            .orderBy("b", bAsc, bNullsFirst)
            .orderBy("id", idAsc, idNullsFirst);
        return crit;
    }
    
    @Parameterized.Parameters
    public static Collection orderingPossibilities() {
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
            { fromPage( 0).to( 0),  1,  1, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id >= 1))))"},
            { fromPage( 0).to( 1),  1,  2, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 1))))"},
            { fromPage( 1).to( 0),  2,  1, "(k.a IS NULL AND (k.b IS NULL AND k.id < 2))"},
            { fromPage( 2).to( 3),  7,  8, "(k.a IS NOT NULL OR (k.a IS NULL AND (k.b > 0 OR (k.b = 0 AND k.id > 7))))"},
            { fromPage( 8).to( 9), 12,  4, "(k.a > 0 OR (k.a = 0 AND (k.b > 1 OR (k.b = 1 AND k.id > 12))))"},
            { fromPage( 9).to(10),  4,  5, "(k.a > 1 OR (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 4))))"},
            { fromPage(10).to(11),  5, 13, "(k.a > 1 OR (k.a = 1 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 5))))"},
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
            { fromPage( 0).to( 0),  3,  3, "((k.a >= 0 OR k.a IS NULL) OR (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id >= 3))))"},
            { fromPage( 0).to( 1),  3, 11, "((k.a > 0 OR k.a IS NULL) OR (k.a = 0 AND (k.b IS NOT NULL OR (k.b IS NULL AND k.id > 3))))"},
            { fromPage( 1).to( 0), 11,  3, "(k.a < 0 OR (k.a = 0 AND ((k.b < 0 OR k.b IS NULL) OR (k.b = 0 AND k.id < 11))))"},
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
            { fromPage( 0).to( 0),  7,  7, "(k.a IS NOT NULL OR (k.a IS NULL AND ((k.b >= 0 OR k.b IS NULL) OR (k.b = 0 AND k.id >= 7))))"},
            { fromPage( 0).to( 1),  7,  8, "(k.a IS NOT NULL OR (k.a IS NULL AND ((k.b > 0 OR k.b IS NULL) OR (k.b = 0 AND k.id > 7))))"},
            { fromPage( 1).to( 0),  8,  7, "(k.a IS NULL AND (k.b < 1 OR (k.b = 1 AND k.id < 8)))"},
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
            { fromPage( 0).to( 0), 11, 11, "((k.a >= 0 OR k.a IS NULL) OR (k.a = 0 AND ((k.b >= 0 OR k.b IS NULL) OR (k.b = 0 AND k.id >= 11))))"},
            { fromPage( 0).to( 1), 11, 12, "((k.a > 0 OR k.a IS NULL) OR (k.a = 0 AND ((k.b > 0 OR k.b IS NULL) OR (k.b = 0 AND k.id > 11))))"},
            { fromPage( 1).to( 0), 12, 11, "(k.a < 0 OR (k.a = 0 AND (k.b < 1 OR (k.b = 1 AND k.id < 12))))"},
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

    @Test
    public void matrixText() {
        boolean aAsc;
        boolean aNullsFirst;
        boolean bAsc;
        boolean bNullsFirst;
        boolean idAsc;
        boolean idNullsFirst;
        
        if (navigation.from > navigation.to) {
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
        
        String expectedIdQueryStart = "SELECT k.id, k.a, k.b, k.id FROM KeySetEntity k WHERE ";
        String expectedIdQueryEnd = " GROUP BY k.id, k.a, k.b, k.id ORDER BY "
            + "k.a " + clause(aAsc, aNullsFirst) + ", "
            + "k.b " + clause(bAsc, bNullsFirst) + ", "
            + "k.id " + clause(idAsc, idNullsFirst);
        String expectedObjectQueryStart = "SELECT k.id, k.a, k.b, k.id FROM KeySetEntity k WHERE ";
        String expectedObjectQueryEnd = " ORDER BY "
            + "k.a " + clause(aAsc, aNullsFirst) + ", "
            + "k.b " + clause(bAsc, bNullsFirst) + ", "
            + "k.id " + clause(idAsc, idNullsFirst);
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(KeySetEntity.class, "k")
            .select("id");
        crit.orderBy("a", this.aAsc, this.aNullsFirst)
            .orderBy("b", this.bAsc, this.bNullsFirst)
            .orderBy("id", this.idAsc, this.idNullsFirst);
        
        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, navigation.from, 1);
        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(id1, result.get(0).get(0));
        
        Serializable[] key;
        
        if (navigation.from < navigation.to) {
            key = result.getKeySet().getHighest();
        } else {
            key = result.getKeySet().getLowest();
        }
        
        pcb = crit.page(result.getKeySet(), navigation.to, 1);
        result = pcb.getResultList();
        assertEquals(id2, result.get(0).get(0));
        
        // simple page id query test
        String actualQueryString = pcb.getPageIdQueryString();
        for (int i = 0; i < key.length; i++) {
            if (key[i] != null) {
                actualQueryString = actualQueryString.replaceAll(Pattern.quote(":_keySetParameter_" + i), key[i].toString());
            }
        }
        
        assertEquals(expectedIdQueryStart + keySetCondition + expectedIdQueryEnd, actualQueryString);

        // Optimized object query test
        String actualObjectQueryString = pcb.getQueryString();
        for (int i = 0; i < key.length; i++) {
            if (key[i] != null) {
                actualObjectQueryString = actualObjectQueryString.replaceAll(Pattern.quote(":_keySetParameter_" + i), key[i].toString());
            }
        }
        
        assertEquals(expectedObjectQueryStart + keySetCondition + expectedObjectQueryEnd, actualObjectQueryString);
    }
    
    private String clause(boolean asc, boolean nullsFirst) {
        if (asc) {
            if (nullsFirst) {
                return "ASC NULLS FIRST";
            } else {
                return "ASC NULLS LAST";
            }
        } else {
            if (nullsFirst) {
                return "DESC NULLS FIRST";
            } else {
                return "DESC NULLS LAST";
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
