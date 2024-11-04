/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jackson;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
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
