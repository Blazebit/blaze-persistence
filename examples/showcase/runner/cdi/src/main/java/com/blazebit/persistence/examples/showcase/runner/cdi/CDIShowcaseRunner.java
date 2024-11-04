/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi;

import com.blazebit.persistence.examples.showcase.spi.Showcase;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CDIShowcaseRunner {

    private CDIShowcaseRunner() {
    }

    public static void main(String[] args) {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();

        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(ApplicationScoped.class);

        ServiceLoader<Showcase> showcaseLoader = ServiceLoader.load(Showcase.class);
        Iterator<Showcase> showcaseIterator = showcaseLoader.iterator();
        if (!showcaseIterator.hasNext()) {
            throw new RuntimeException("No showcases found");
        }
        while (showcaseIterator.hasNext()) {
            BeanProvider.injectFields(showcaseIterator.next()).run();
        }

        cdiContainer.shutdown();
    }

}
