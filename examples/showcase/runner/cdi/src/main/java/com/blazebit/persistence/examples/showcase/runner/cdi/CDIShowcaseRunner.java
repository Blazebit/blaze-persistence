/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
