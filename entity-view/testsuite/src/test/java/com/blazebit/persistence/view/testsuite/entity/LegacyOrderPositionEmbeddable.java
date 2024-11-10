/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LegacyOrderPositionEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public LegacyOrderPositionEmbeddable() {
    }

    public LegacyOrderPositionEmbeddable(String name) {
        this.name = name;
    }

    @Column(name = "emb_simp_emb_name")
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LegacyOrderPositionEmbeddable other = (LegacyOrderPositionEmbeddable) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
