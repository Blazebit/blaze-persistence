/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.cte.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.blazebit.persistence.CTE;

@CTE
@Entity
public class DocumentOwnersCTE {

    @Id
    Long id;

    Long documentCount;
}
