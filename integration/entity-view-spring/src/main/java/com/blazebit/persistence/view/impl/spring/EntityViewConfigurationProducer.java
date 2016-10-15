package com.blazebit.persistence.view.impl.spring;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.10.2016.
 */
@Configuration
public class EntityViewConfigurationProducer {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();

    public EntityViewConfigurationProducer(Set<Class<?>> entityViewClasses) {
        for (Class<?> entityViewClass : entityViewClasses) {
            configuration.addEntityView(entityViewClass);
        }
    }

    @Bean
    public EntityViewConfiguration getEntityViewConfiguration() {
        return configuration;
    }


}
