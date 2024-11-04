/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.entity;

import org.springframework.data.history.RevisionMetadata;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface AuditMetadata extends RevisionMetadata<Long> {

}