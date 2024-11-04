/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jsonb;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Entity
public class SomeEntity {
    @Id
    Long id;
    String name;
    @ManyToOne(fetch = FetchType.LAZY)
    SomeEntity parent;
    @Basic
    @Convert(converter = StringListConverter.class)
    List<String> tags;
    @OneToMany(mappedBy = "parent")
    Set<SomeEntity> children;
}
