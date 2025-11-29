/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.6.10
 */
@Embeddable
public class SimpleEmbeddedIdEntityId implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;

    public SimpleEmbeddedIdEntityId() {
    }

    public SimpleEmbeddedIdEntityId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleEmbeddedIdEntityId that = (SimpleEmbeddedIdEntityId) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
