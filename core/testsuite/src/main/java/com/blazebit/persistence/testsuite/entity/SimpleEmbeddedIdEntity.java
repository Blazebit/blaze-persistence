/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.6.10
 */
@Entity
@Table(name = "SIMP_EMB_ID_ENT")
public class SimpleEmbeddedIdEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private SimpleEmbeddedIdEntityId id;
    private String name;

    @EmbeddedId
    public SimpleEmbeddedIdEntityId getId() {
        return id;
    }

    public void setId(SimpleEmbeddedIdEntityId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
