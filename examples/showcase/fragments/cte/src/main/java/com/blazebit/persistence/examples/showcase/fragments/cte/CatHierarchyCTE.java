/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.cte;

import com.blazebit.persistence.CTE;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@CTE
@Entity
public class CatHierarchyCTE {

    private Integer id;
    private Integer motherId;
    private Integer fatherId;
    private Integer generation;

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMotherId() {
        return motherId;
    }

    public void setMotherId(Integer motherId) {
        this.motherId = motherId;
    }

    public Integer getFatherId() {
        return fatherId;
    }

    public void setFatherId(Integer fatherId) {
        this.fatherId = fatherId;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }
}
