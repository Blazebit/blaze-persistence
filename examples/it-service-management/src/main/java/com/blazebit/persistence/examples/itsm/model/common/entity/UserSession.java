/*
 * Copyright 2014 - 2021 Blazebit.
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
