/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Embeddable
public class LocalizedEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = -8539302606114365372L;

    private String name;
    private String description;

    /**
     * Empty Constructor
     */
    public LocalizedEntity() {
        super();
    }

    /**
     * @param name
     * @param description
     */
    public LocalizedEntity(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    @Column(
        name = "NAME",
        nullable = false,
        length = 255)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(
        name = "DESCRIPTION",
        nullable = false,
        length = 2500)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public LocalizedEntity clone() {
        return new LocalizedEntity(this.getName(), this.getDescription());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocalizedEntity other = (LocalizedEntity) obj;
        if (getDescription() == null) {
            if (other.getDescription() != null) {
                return false;
            }
        } else if (!getDescription().equals(other.getDescription())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }
}
