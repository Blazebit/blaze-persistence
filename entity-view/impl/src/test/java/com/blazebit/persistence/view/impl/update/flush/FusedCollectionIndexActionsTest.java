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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.ListAction;
import com.blazebit.persistence.view.impl.collection.ListAddAction;
import com.blazebit.persistence.view.impl.collection.ListAddAllAction;
import com.blazebit.persistence.view.impl.collection.ListRemoveAction;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class FusedCollectionIndexActionsTest {

    @Test
    public void testRemoveAndAppend() {
        List<String> objects = Arrays.asList("o1", "o2", "o3", "o4");
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListRemoveAction<>(1, false, objects),
                new ListAddAction<>(3, true, "o5")
        ));
        Assert.assertEquals(1, indexActions.getRemoveCount());
        Assert.assertEquals(1, indexActions.getAddCount());
        Assert.assertEquals(1, indexActions.getUpdateCount());
    }

    @Test
    public void testFuseRemoveAndInsert() {
        List<String> objects = Arrays.asList("o1", "o2", "o3", "o4");
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListRemoveAction<>(1, false, objects),
                new ListAddAction<>(1, false, "o2New")
        ));
        Assert.assertEquals(0, indexActions.getRemoveCount());
        Assert.assertEquals(0, indexActions.getAddCount());
        Assert.assertEquals(1, indexActions.getUpdateCount());
    }

    @Test
    public void testFuseRemovesAndTranslates() {
        List<String> objects = Arrays.asList("o1", "o2", "o3", "o4");
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(1, false, objects)
        ));
        Assert.assertEquals(1, indexActions.getRemoveCount());
        Assert.assertEquals(0, indexActions.getAddCount());
        Assert.assertEquals(1, indexActions.getUpdateCount());
    }

    @Test
    public void testMultipleRemoveRangesWithLast() {
        List<String> objects = Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6");
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(2, false, objects),
                new ListRemoveAction<>(2, true, objects)
        ));
        Assert.assertEquals(2, indexActions.getRemoveCount());
        Assert.assertEquals(0, indexActions.getAddCount());
        Assert.assertEquals(1, indexActions.getUpdateCount());
    }

    @Test
    public void testMultipleRemoveRanges() {
        List<String> objects = Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6", "o7");
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(2, false, objects),
                new ListRemoveAction<>(2, false, objects)
        ));
        Assert.assertEquals(2, indexActions.getRemoveCount());
        Assert.assertEquals(0, indexActions.getAddCount());
        Assert.assertEquals(2, indexActions.getUpdateCount());
    }

    @Test
    public void testAddClearAndReadd() {
        List<String> objects = new ArrayList<>(Arrays.asList("o1", "o2"));
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListAddAction<>(1, true, objects.get(1)),
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(0, false, objects),
                new ListAddAllAction<>(0, true, objects)
        ));
        Assert.assertEquals(0, indexActions.getRemoveCount());
        Assert.assertEquals(1, indexActions.getAddCount());
        Assert.assertEquals(0, indexActions.getUpdateCount());
    }

    @Test
    public void testAddClearAndReadd2() {
        List<String> objects = new ArrayList<>(Arrays.asList("o1", "o2", "o3", "o4"));
        FusedCollectionIndexActions indexActions = new FusedCollectionIndexActions(Arrays.<ListAction<?>>asList(
                new ListAddAction<>(3, true, objects.get(3)),
                new ListRemoveAction<>(3, false, objects),
                new ListRemoveAction<>(2, false, objects),
                new ListRemoveAction<>(1, false, objects),
                new ListRemoveAction<>(0, false, objects),
                new ListAddAllAction<>(0, true, objects)
        ));
        Assert.assertEquals(0, indexActions.getRemoveCount());
        Assert.assertEquals(1, indexActions.getAddCount());
        Assert.assertEquals(0, indexActions.getUpdateCount());
    }
}
