/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.host.entity;

import java.time.Instant;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@DiscriminatorValue("long")
public class HostDeviceItemLong extends HostDeviceItem {

    @ElementCollection
    Map<Instant, Long> values;

}
