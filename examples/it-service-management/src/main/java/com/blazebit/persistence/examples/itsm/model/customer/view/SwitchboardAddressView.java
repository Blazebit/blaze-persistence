/*
 * Copyright 2014 - 2023 Blazebit.
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
