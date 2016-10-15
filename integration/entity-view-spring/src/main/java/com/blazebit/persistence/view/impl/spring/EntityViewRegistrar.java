package com.blazebit.persistence.view.impl.spring;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.10.2016.
 */
public class EntityViewRegistrar implements ImportBeanDefinitionRegistrar, BeanDefinitionParser {

    private static final String BASE_PACKAGE = "base-package";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        try {
            BeanDefinitionRegistry registry = parserContext.getRegistry();

            List<String> basePackages = getBasePackages(element);
            if (basePackages.isEmpty()) {
                parserContext.getReaderContext().error("You have to specify at least one base package for entity views!", element);
            }

            scanAndRegisterEntityViews(basePackages, registry);
        } catch (RuntimeException e) {
            handleError(e, element, parserContext.getReaderContext());
        }

        return null;
    }

    public List<String> getBasePackages(Element element) {
        String attribute = element.getAttribute(BASE_PACKAGE);
        return Arrays.asList(StringUtils.delimitedListToStringArray(attribute, ",", " "));
    }

    private void handleError(Exception e, Element source, ReaderContext reader) {
        reader.error(e.getMessage(), reader.extractSource(source), e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        MultiValueMap<String, Object> annotationAttributes = importingClassMetadata.getAllAnnotationAttributes(EnableEntityViews.class.getName());

        List<String> basePackages = new ArrayList<String>();
        Collections.addAll(basePackages, (String[]) annotationAttributes.getFirst("value"));
        Class<?>[] basePackageClasses = (Class<?>[]) annotationAttributes.getFirst("basePackageClasses");
        for (Class<?> basePackageClass : basePackageClasses) {
            basePackages.add(basePackageClass.getPackage().getName());
        }
        if (basePackages.isEmpty()) {
            try {
                basePackages.add(Class.forName(importingClassMetadata.getClassName()).getPackage().getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        scanAndRegisterEntityViews(basePackages, registry);
    }

    @SuppressWarnings("unchecked")
    private void scanAndRegisterEntityViews(List<String> basePackages, BeanDefinitionRegistry registry) {
        Set<Class<?>> entityViewClasses = new HashSet<Class<?>>();
        ClassPathScanningCandidateComponentProvider provider = createComponentScanner();
        for (String basePackage : basePackages) {
            for (BeanDefinition beanDef : provider.findCandidateComponents(basePackage)) {
                try {
                    entityViewClasses.add(Class.forName(beanDef.getBeanClassName()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final String entityViewClassHolderBeanName = "entityViewClassesHolder";
        if (registry.containsBeanDefinition(entityViewClassHolderBeanName)) {
            BeanDefinition existingClassHolder = registry.getBeanDefinition(entityViewClassHolderBeanName);
            Set<Class<?>> existingEntityViewClasses = (Set<Class<?>>) ((GenericBeanDefinition) existingClassHolder).getConstructorArgumentValues().getGenericArgumentValue(Set.class).getValue();
            existingEntityViewClasses.addAll(entityViewClasses);
        } else {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(EntityViewClassesHolder.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(entityViewClasses);
            registry.registerBeanDefinition(entityViewClassHolderBeanName, beanDefinition);

            // register configuration class
            beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(EntityViewConfigurationProducer.class);
            registry.registerBeanDefinition("entityViewConfigurationProducer", beanDefinition);
        }
    }

    private ClassPathScanningCandidateComponentProvider createComponentScanner() {
        ClassPathScanningCandidateComponentProvider provider
                = new ClassPathScanningCandidateComponentProvider(false) {
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent();
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(EntityView.class, false, true));
        return provider;
    }
}
