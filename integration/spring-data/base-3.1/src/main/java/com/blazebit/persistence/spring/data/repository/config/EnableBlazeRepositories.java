/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.spring.data.repository.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable Blaze repositories. Will scan the package of the annotated configuration class for Spring Data
 * repositories by default.
 *
 * Like {@link org.springframework.data.jpa.repository.config.EnableJpaRepositories}, but with a custom registrar.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(BlazeRepositoriesRegistrar.class)
public @interface EnableBlazeRepositories {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableBlazeRepositories("org.my.pkg")} instead of {@code @EnableBlazeRepositories(basePackages="org.my.pkg")}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
     * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
     * each package that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
     * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
     */
    ComponentScan.Filter[] includeFilters() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     */
    ComponentScan.Filter[] excludeFilters() default {};

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
     * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
     * for {@code PersonRepositoryImpl}.
     *
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties file. Will default to
     * {@code META-INF/jpa-named-queries.properties}.
     *
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
     * {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND}.
     *
     */
    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
     * {@code com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean}.
     *
     */
    Class<?> repositoryFactoryBeanClass() default void.class;

    /**
     * Configure the repository base class to be used to create repository proxies for this particular configuration.
     *
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    // JPA specific configuration

    /**
     * Configures the name of the {@link EntityManagerFactory} bean definition to be used to create repositories
     * discovered through this annotation. Defaults to {@code entityManagerFactory}.
     *
     */
    String entityManagerFactoryRef() default "entityManagerFactory";

    /**
     * Configures the name of the {@link PlatformTransactionManager} bean definition to be used to create repositories
     * discovered through this annotation. Defaults to {@code transactionManager}.
     *
     */
    String transactionManagerRef() default "transactionManager";

    /**
     * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
     * repositories infrastructure.
     */
    boolean considerNestedRepositories() default false;

    /**
     * Configures whether to enable default transactions for Spring Data JPA repositories. Defaults to {@literal true}. If
     * disabled, repositories must be used behind a facade that's configuring transactions (e.g. using Spring's annotation
     * driven transaction facilities) or repository methods have to be used to demarcate transactions.
     *
     * @return whether to enable default transactions, defaults to {@literal true}.
     */
    boolean enableDefaultTransactions() default true;

    /**
     * Configures when the repositories are initialized in the bootstrap lifecycle. {@link BootstrapMode#DEFAULT}
     * (default) means eager initialization except all repository interfaces annotated with {@link Lazy},
     * {@link BootstrapMode#LAZY} means lazy by default including injection of lazy-initialization proxies into client
     * beans so that those can be instantiated but will only trigger the initialization upon first repository usage (i.e a
     * method invocation on it). This means repositories can still be uninitialized when the application context has
     * completed its bootstrap. {@link BootstrapMode#DEFERRED} is fundamentally the same as {@link BootstrapMode#LAZY},
     * but triggers repository initialization when the application context finishes its bootstrap.
     *
     */
    BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

    /**
     * Configures what character is used to escape the wildcards {@literal _} and {@literal %} in derived queries with
     * {@literal contains}, {@literal startsWith} or {@literal endsWith} clauses.
     *
     * @return a single character used for escaping.
     */
    char escapeCharacter() default '\\';
}
