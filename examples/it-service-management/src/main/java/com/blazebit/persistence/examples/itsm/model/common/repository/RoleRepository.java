/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.examples.itsm.model.common.entity.Role;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

}
