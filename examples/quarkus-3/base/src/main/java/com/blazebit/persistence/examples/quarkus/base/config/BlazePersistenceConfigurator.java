/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.quarkus.base.config;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class BlazePersistenceConfigurator {

    public void observe(@Observes CriteriaBuilderConfiguration config) {
        config.registerMacro("my_macro", new JpqlMacro() {
            @Override
            public void render(FunctionRenderContext context) {
                context.addArgument(0);
            }
        });
    }

    public void observe(@Observes EntityViewConfiguration config) {
        config.setOptionalParameter("optionalParameter", "test");
    }

}
