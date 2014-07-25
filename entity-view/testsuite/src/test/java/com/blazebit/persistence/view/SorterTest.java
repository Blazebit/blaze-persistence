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

package com.blazebit.persistence.view;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.Sortable;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Christian Beikov
 */
public class SorterTest {
    
    private final String expression = "name";
    
    @Test
    public void testAscendingNullFirst() {
        Sorter sorter = Sorters.sorter(true, true);
        verifySorter(sorter).orderBy(expression, true, true);
    }
    
    @Test
    public void testAscendingNullLast() {
        Sorter sorter = Sorters.sorter(true, false);
        verifySorter(sorter).orderBy(expression, true, false);
    }
    
    @Test
    public void testDescendingNullFirst() {
        Sorter sorter = Sorters.sorter(false, true);
        verifySorter(sorter).orderBy(expression, false, true);
    }
    
    @Test
    public void testDescendingNullLast() {
        Sorter sorter = Sorters.sorter(false, false);
        verifySorter(sorter).orderBy(expression, false, false);
    }
    
    public Sortable<?> verifySorter(Sorter sorter) {
        Sortable<?> sortable = Mockito.mock(Sortable.class);
        sorter.apply((Sortable) sortable, expression);
        return Mockito.verify(sortable);
    }
}
