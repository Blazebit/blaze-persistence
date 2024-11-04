/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import com.blazebit.persistence.CriteriaBuilder;
import java.util.List;
import ${package}.model.Cat;
import ${package}.model.Person;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SampleTest extends AbstractSampleTest {
    
    @Test
    public void sampleTest() {
        transactional(em -> {
            CriteriaBuilder<Person> cb = cbf.create(em, Person.class);
            cb.from(Cat.class, "cat");
            cb.where("cat.owner").isNotNull();
            cb.select("cat.owner");
            cb.distinct();
            List<Person> list = cb.getResultList();
            
            System.out.println(list);
            assertEquals(2, list.size());
        });
    }
}
