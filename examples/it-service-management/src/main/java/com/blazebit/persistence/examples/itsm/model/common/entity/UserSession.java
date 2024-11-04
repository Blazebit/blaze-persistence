/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.entity;

import java.time.Instant;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class UserSession {

    @Id
    @GeneratedValue
    private String id;

    @ManyToOne
    private User user;

    private Instant initInstant;

    private Instant heartbeatInstant;

    private Instant destroyInstant;

    private boolean destroyed;

    public String getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getInitInstant() {
        return this.initInstant;
    }

    public void setInitInstant(Instant initInstant) {
        this.initInstant = initInstant;
    }

    public Instant getHeartbeatInstant() {
        return this.heartbeatInstant;
    }

    public void setHeartbeatInstant(Instant heartbeatInstant) {
        this.heartbeatInstant = heartbeatInstant;
    }

    public Instant getDestroyInstant() {
        return this.destroyInstant;
    }

    public void setDestroyInstant(Instant destroyInstant) {
        this.destroyInstant = destroyInstant;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserSession)) {
            return false;
        }
        UserSession other = (UserSession) obj;
        return Objects.equals(this.id, other.id);
    }

}
