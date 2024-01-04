/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Embeddable
public class SwitchboardAddress implements Serializable {

    private String address;

    private String network;

    private String description;

    SwitchboardAddress() {
    }

    /**
     * Instantiates a new switchboard address.
     *
     * @param address
     *            the address
     * @param network
     *            the network
     */
    public SwitchboardAddress(String address, String network) {
        this.address = address;
        this.network = network;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Sets the address.
     *
     * @param address
     *            the new address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the network.
     *
     * @return the network
     */
    public String getNetwork() {
        return this.network;
    }

    /**
     * Sets the network.
     *
     * @param network
     *            the new network
     */
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.address);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SwitchboardAddress) {
            SwitchboardAddress other = (SwitchboardAddress) obj;
            return Objects.equals(this.address, other.address);
        } else {
            return false;
        }
    }

}
