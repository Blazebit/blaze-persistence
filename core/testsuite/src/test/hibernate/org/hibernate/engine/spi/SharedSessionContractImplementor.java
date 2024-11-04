/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package org.hibernate.engine.spi;

/**
 * See com.blazebit.persistence.testsuite.base.AbstractPersistenceTest why this is necessary.
 * The short version, we need this stub for custom user type tests for Hibernate before 5.1.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface SharedSessionContractImplementor {
}
