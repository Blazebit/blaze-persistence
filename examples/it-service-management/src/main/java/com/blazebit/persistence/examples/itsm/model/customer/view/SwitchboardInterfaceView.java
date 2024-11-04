/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import com.blazebit.persistence.examples.itsm.model.customer.entity.SwitchboardInterface;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(SwitchboardInterface.class)
@CreatableEntityView
@UpdatableEntityView
public interface SwitchboardInterfaceView {

    public String getNumber();

    /**
     * Sets the number.
     *
     * @param number
     *            the new number
     */
    public void setNumber(String number);

    /**
     * Gets the slot.
     *
     * @return the slot
     */
    public String getSlot();

    /**
     * Sets the slot.
     *
     * @param slot
     *            the new slot
     */
    public void setSlot(String slot);

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription();

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description);

}
