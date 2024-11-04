/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.cdi;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link Default}
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

    private static final long serialVersionUID = 3240069236025230401L;
}
