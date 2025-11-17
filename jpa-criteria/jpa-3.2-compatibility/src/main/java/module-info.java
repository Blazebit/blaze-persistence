/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

module jakarta.persistence {

    requires java.logging;
    requires transitive java.sql;

    exports jakarta.persistence;
    exports jakarta.persistence.criteria;
    exports jakarta.persistence.metamodel;
    exports jakarta.persistence.spi;

    uses jakarta.persistence.spi.PersistenceProvider;
}
