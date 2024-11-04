/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public class SwitchboardInterface implements Serializable {

    private String number;

    private String slot;

    private String description;

    SwitchboardInterface() {
    }

    /**
     * Instantiates a new switchboard interface.
     *
     * @param number
     *            the number
     * @param slot
     *            the slot
     */
    public SwitchboardInterface(String number, String slot) {
        this.number = number;
        this.slot = slot;
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public String getNumber() {
        return this.number;
    }

    /**
     * Sets the number.
     *
     * @param number
     *            the new number
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Gets the slot.
     *
     * @return the slot
     */
    public String getSlot() {
        return this.slot;
    }

    /**
     * Sets the slot.
     *
     * @param slot
     *            the new slot
     */
    public void setSlot(String slot) {
        this.slot = slot;
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
        return Objects.hash(this.number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SwitchboardInterface) {
            SwitchboardInterface other = (SwitchboardInterface) obj;
            return Objects.equals(this.number, other.number);
        } else {
            return false;
        }
    }

}
