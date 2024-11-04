/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import com.blazebit.persistence.examples.itsm.model.customer.entity.SwitchboardAddress;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(SwitchboardAddress.class)
@CreatableEntityView
@UpdatableEntityView
public interface SwitchboardAddressView {

    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress();

    /**
     * Sets the address.
     *
     * @param address
     *            the new address
     */
    public void setAddress(String address);

    /**
     * Gets the network.
     *
     * @return the network
     */
    public String getNetwork();

    /**
     * Sets the network.
     *
     * @param network
     *            the new network
     */
    public void setNetwork(String network);

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
