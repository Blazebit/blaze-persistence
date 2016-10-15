package com.blazebit.persistence.view.impl.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.10.2016.
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value={java.lang.annotation.ElementType.TYPE})
@Documented
@Import({EntityViewRegistrar.class})
public @interface EnableEntityViews {
    /**
     * Alias for {@link #basePackages}.
     * <p>Allows for more concise annotation declarations if no other attributes
     * are needed &mdash; for example, {@code @ComponentScan("org.my.pkg")}
     * instead of {@code @ComponentScan(basePackages = "org.my.pkg")}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated components.
     * <p>{@link #value} is an alias for (and mutually exclusive with) this
     * attribute.
     * <p>Use {@link #basePackageClasses} for a type-safe alternative to
     * String-based package names.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};
}
