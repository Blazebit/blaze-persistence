/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest;

import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.examples.deltaspike.data.rest.repository.CatViewRepository;
import com.blazebit.persistence.examples.deltaspike.data.rest.view.CatWithOwnerView;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SampleTest extends AbstractSampleTest {

    @Inject
    private CatViewRepository catRepository;

    @Test
    public void sampleTest() {
        transactional(em -> {
            Page<CatWithOwnerView> list = catRepository.findAll(null, new PageRequest(0, 10, Sort.Direction.ASC, "id"));
            
            System.out.println(list);
            Assert.assertEquals(6, list.getContent().size());
        });
    }
}
