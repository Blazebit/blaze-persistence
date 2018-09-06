/*
 * Copyright 2017 the original author or authors.
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
package com.blazebit.persistence.spring.data.impl.repository;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.MethodLookup;
import org.springframework.data.repository.core.support.MethodLookup.MethodPredicate;
import org.springframework.data.repository.util.QueryExecutionConverters;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.util.Assert;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.springframework.core.GenericTypeResolver.resolveParameterType;

/**
 * Implementations of method lookup functions.
 *
 * @author Mark Paluch
 * @author Oliver Gierke
 * @since 2.0
 */
interface MethodLookups {

    /**
     * Direct method lookup filtering on exact method name, parameter count and parameter types.
     *
     * @return direct method lookup.
     */
    public static MethodLookup direct() {

        MethodPredicate direct = (invoked, candidate) -> candidate.getName().equals(invoked.getName())
                && candidate.getParameterCount() == invoked.getParameterCount()
                && Arrays.equals(candidate.getParameterTypes(), invoked.getParameterTypes());

        return () -> Collections.singletonList(direct);
    }

    /**
     * Repository type-aware method lookup composed of {@link #direct()} and {@link RepositoryAwareMethodLookup}.
     * <p/>
     * Repository-aware lookups resolve generic types from the repository declaration to verify assignability to Id/domain
     * types. This lookup also permits assignable method signatures but prefers {@link #direct()} matches.
     *
     * @param repositoryMetadata must not be {@literal null}.
     * @return the composed, repository-aware method lookup.
     * @see #direct()
     */
    public static MethodLookup forRepositoryTypes(RepositoryMetadata repositoryMetadata) {
        return direct().and(new RepositoryAwareMethodLookup(repositoryMetadata));
    }

    /**
     * Repository type-aware method lookup composed of {@link #direct()} and {@link ReactiveTypeInteropMethodLookup}.
     * <p/>
     * This method lookup considers adaptability of reactive types in method signatures. Repository methods accepting a
     * reactive type can be possibly called with a different reactive type if the reactive type can be adopted to the
     * target type. This lookup also permits assignable method signatures and resolves repository id/entity types but
     * prefers {@link #direct()} matches.
     *
     * @param repositoryMetadata must not be {@literal null}.
     * @return the composed, repository-aware method lookup.
     * @see #direct()
     * @see #forRepositoryTypes(RepositoryMetadata)
     */
    public static MethodLookup forReactiveTypes(RepositoryMetadata repositoryMetadata) {
        return direct().and(new ReactiveTypeInteropMethodLookup(repositoryMetadata));
    }

    /**
     * Default {@link MethodLookup} considering repository Id and entity types permitting calls to methods with assignable
     * arguments.
     *
     * @author Mark Paluch
     */
    static class RepositoryAwareMethodLookup implements MethodLookup {

        @SuppressWarnings("rawtypes") private static final TypeVariable<Class<Repository>>[] PARAMETERS = Repository.class
                .getTypeParameters();
        private static final String DOMAIN_TYPE_NAME = PARAMETERS[0].getName();
        private static final String ID_TYPE_NAME = PARAMETERS[1].getName();

        private final ResolvableType entityType, idType;
        private final Class<?> repositoryInterface;

        /**
         * Creates a new {@link RepositoryAwareMethodLookup} for the given {@link RepositoryMetadata}.
         *
         * @param repositoryMetadata must not be {@literal null}.
         */
        public RepositoryAwareMethodLookup(RepositoryMetadata repositoryMetadata) {

            Assert.notNull(repositoryMetadata, "Repository metadata must not be null!");

            this.entityType = ResolvableType.forClass(repositoryMetadata.getDomainType());
            this.idType = ResolvableType.forClass(repositoryMetadata.getIdType());
            this.repositoryInterface = repositoryMetadata.getRepositoryInterface();
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.core.support.MethodLookup#getLookups()
         */
        @Override
        public List<MethodPredicate> getLookups() {

            MethodPredicate detailedComparison = (invoked, candidate) -> Optional.of(candidate)
                    .filter(baseClassMethod -> baseClassMethod.getName().equals(invoked.getName()))// Right name
                    .filter(baseClassMethod -> baseClassMethod.getParameterCount() == invoked.getParameterCount())
                    .filter(baseClassMethod -> parametersMatch(invoked.getMethod(), baseClassMethod))// All parameters match
                    .isPresent();

            return Collections.singletonList(detailedComparison);
        }

        /**
         * Checks whether the given parameter type matches the generic type of the given parameter. Thus when {@literal PK}
         * is declared, the method ensures that given method parameter is the primary key type declared in the given
         * repository interface e.g.
         *
         * @param variable must not be {@literal null}.
         * @param parameterType must not be {@literal null}.
         * @return
         */
        protected boolean matchesGenericType(TypeVariable<?> variable, ResolvableType parameterType) {

            GenericDeclaration declaration = variable.getGenericDeclaration();

            if (declaration instanceof Class) {

                if (ID_TYPE_NAME.equals(variable.getName()) && parameterType.isAssignableFrom(idType)) {
                    return true;
                }

                Type boundType = variable.getBounds()[0];
                String referenceName = boundType instanceof TypeVariable ? boundType.toString() : variable.toString();

                return DOMAIN_TYPE_NAME.equals(referenceName) && parameterType.isAssignableFrom(entityType);
            }

            for (Type type : variable.getBounds()) {
                if (ResolvableType.forType(type).isAssignableFrom(parameterType)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Checks the given method's parameters to match the ones of the given base class method. Matches generic arguments
         * against the ones bound in the given repository interface.
         *
         * @param invokedMethod
         * @param candidate
         * @return
         */
        private boolean parametersMatch(Method invokedMethod, Method candidate) {

            Class<?>[] methodParameterTypes = invokedMethod.getParameterTypes();
            Type[] genericTypes = candidate.getGenericParameterTypes();
            Class<?>[] types = candidate.getParameterTypes();

            for (int i = 0; i < genericTypes.length; i++) {

                Type genericType = genericTypes[i];
                Class<?> type = types[i];
                MethodParameter parameter = new MethodParameter(invokedMethod, i);
                Class<?> parameterType = resolveParameterType(parameter, repositoryInterface);

                if (genericType instanceof TypeVariable<?>) {

                    if (!matchesGenericType((TypeVariable<?>) genericType, ResolvableType.forMethodParameter(parameter))) {
                        return false;
                    }

                    continue;
                }

                if (types[i].equals(parameterType)) {
                    continue;
                }

                if (!type.isAssignableFrom(parameterType) || !type.equals(methodParameterTypes[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Extension to {@link RepositoryAwareMethodLookup} considering reactive type adoption and entity types permitting
     * calls to methods with assignable arguments.
     *
     * @author Mark Paluch
     */
    static class ReactiveTypeInteropMethodLookup extends RepositoryAwareMethodLookup {

        private final RepositoryMetadata repositoryMetadata;

        public ReactiveTypeInteropMethodLookup(RepositoryMetadata repositoryMetadata) {

            super(repositoryMetadata);
            this.repositoryMetadata = repositoryMetadata;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.core.support.MethodLookups.RepositoryAwareMethodLookup#getLookups()
         */
        @Override
        public List<MethodPredicate> getLookups() {

            MethodPredicate convertibleComparison = (invokedMethod, candidate) -> {

                List<Supplier<Optional<Method>>> suppliers = new ArrayList<>();

                if (usesParametersWithReactiveWrappers(invokedMethod.getMethod())) {
                    suppliers.add(() -> getMethodCandidate(invokedMethod, candidate, assignableWrapperMatch())); //
                    suppliers.add(() -> getMethodCandidate(invokedMethod, candidate, wrapperConversionMatch()));
                }

                return suppliers.stream().anyMatch(supplier -> supplier.get().isPresent());
            };

            MethodPredicate detailedComparison = (invokedMethod, candidate) -> getMethodCandidate(invokedMethod, candidate,
                    matchParameterOrComponentType(repositoryMetadata.getRepositoryInterface())).isPresent();

            return Arrays.asList(convertibleComparison, detailedComparison);
        }

        /**
         * {@link Predicate} to check parameter assignability between a parameters in which the declared parameter may be
         * wrapped but supports unwrapping. Usually types like {@link Optional} or {@link Stream}.
         *
         * @param repositoryInterface
         * @return
         * @see QueryExecutionConverters
         * @see #matchesGenericType
         */
        private Predicate<ParameterOverrideCriteria> matchParameterOrComponentType(Class<?> repositoryInterface) {

            return (parameterCriteria) -> {

                Class<?> parameterType = resolveParameterType(parameterCriteria.getDeclared(), repositoryInterface);
                Type genericType = parameterCriteria.getGenericBaseType();

                if (genericType instanceof TypeVariable<?>) {

                    if (!matchesGenericType((TypeVariable<?>) genericType,
                            ResolvableType.forMethodParameter(parameterCriteria.getDeclared()))) {
                        return false;
                    }
                }

                return parameterCriteria.getBaseType().isAssignableFrom(parameterType)
                        && parameterCriteria.isAssignableFromDeclared();
            };
        }

        /**
         * Checks whether the type is a wrapper without unwrapping support. Reactive wrappers don't like to be unwrapped.
         *
         * @param parameterType must not be {@literal null}.
         * @return
         */
        private static boolean isNonUnwrappingWrapper(Class<?> parameterType) {

            Assert.notNull(parameterType, "Parameter type must not be null!");

            return QueryExecutionConverters.supports(parameterType)
                    && !QueryExecutionConverters.supportsUnwrapping(parameterType);
        }

        /**
         * Returns whether the given {@link Method} uses a reactive wrapper type as parameter.
         *
         * @param method must not be {@literal null}.
         * @return
         */
        private static boolean usesParametersWithReactiveWrappers(Method method) {

            Assert.notNull(method, "Method must not be null!");

            return Arrays.stream(method.getParameterTypes())//
                    .anyMatch(ReactiveTypeInteropMethodLookup::isNonUnwrappingWrapper);
        }

        /**
         * Returns a candidate method from the base class for the given one or the method given in the first place if none
         * one the base class matches.
         *
         * @param method must not be {@literal null}.
         * @param baseClass must not be {@literal null}.
         * @param predicate must not be {@literal null}.
         * @return
         */
        private static Optional<Method> getMethodCandidate(InvokedMethod invokedMethod, Method candidate,
                Predicate<ParameterOverrideCriteria> predicate) {

            return Optional.of(candidate)//
                    .filter(it -> invokedMethod.getName().equals(it.getName()))//
                    .filter(it -> invokedMethod.getParameterCount() == it.getParameterCount())//
                    .filter(it -> parametersMatch(it, invokedMethod.getMethod(), predicate));
        }

        /**
         * Checks the given method's parameters to match the ones of the given base class method. Matches generic arguments
         * against the ones bound in the given repository interface.
         *
         * @param baseClassMethod must not be {@literal null}.
         * @param declaredMethod must not be {@literal null}.
         * @param predicate must not be {@literal null}.
         * @return
         */
        private static boolean parametersMatch(Method baseClassMethod, Method declaredMethod,
                Predicate<ParameterOverrideCriteria> predicate) {

            return methodParameters(baseClassMethod, declaredMethod).allMatch(predicate);
        }

        /**
         * {@link Predicate} to check whether a method parameter is a {@link #isNonUnwrappingWrapper(Class)} and can be
         * converted into a different wrapper. Usually {@link rx.Observable} to {@link org.reactivestreams.Publisher}
         * conversion.
         *
         * @return
         */
        private static Predicate<ParameterOverrideCriteria> wrapperConversionMatch() {

            return (parameterCriteria) -> isNonUnwrappingWrapper(parameterCriteria.getBaseType()) //
                    && isNonUnwrappingWrapper(parameterCriteria.getDeclaredType()) //
                    && ReactiveWrapperConverters.canConvert(parameterCriteria.getDeclaredType(), parameterCriteria.getBaseType());
        }

        /**
         * {@link Predicate} to check parameter assignability between a {@link #isNonUnwrappingWrapper(Class)} parameter and
         * a declared parameter. Usually {@link reactor.core.publisher.Flux} vs. {@link org.reactivestreams.Publisher}
         * conversion.
         *
         * @return
         */
        private static Predicate<ParameterOverrideCriteria> assignableWrapperMatch() {

            return (parameterCriteria) -> isNonUnwrappingWrapper(parameterCriteria.getBaseType()) //
                    && isNonUnwrappingWrapper(parameterCriteria.getDeclaredType()) //
                    && parameterCriteria.getBaseType().isAssignableFrom(parameterCriteria.getDeclaredType());
        }

        private static Stream<ParameterOverrideCriteria> methodParameters(Method first, Method second) {

            Assert.isTrue(first.getParameterCount() == second.getParameterCount(), "Method parameter count must be equal!");

            return IntStream.range(0, first.getParameterCount()) //
                    .mapToObj(index -> ParameterOverrideCriteria.of(new MethodParameter(first, index),
                            new MethodParameter(second, index)));
        }

        /**
         * Criterion to represent {@link MethodParameter}s from a base method and its declared (overridden) method. Method
         * parameters indexes are correlated so {@link ParameterOverrideCriteria} applies only to methods with same
         * parameter count.
         */
        static class ParameterOverrideCriteria {

            private final MethodParameter base;
            private final MethodParameter declared;

            private ParameterOverrideCriteria(MethodParameter base, MethodParameter declared) {
                this.base = base;
                this.declared = declared;
            }

            public static ParameterOverrideCriteria of(MethodParameter base, MethodParameter declared) {
                return new ParameterOverrideCriteria(base, declared);
            }

            public MethodParameter getBase() {
                return base;
            }

            public MethodParameter getDeclared() {
                return declared;
            }

            /**
             * @return base method parameter type.
             */
            public Class<?> getBaseType() {
                return base.getParameterType();
            }

            /**
             * @return generic base method parameter type.
             */
            public Type getGenericBaseType() {
                return base.getGenericParameterType();
            }

            /**
             * @return declared method parameter type.
             */
            public Class<?> getDeclaredType() {
                return declared.getParameterType();
            }

            public boolean isAssignableFromDeclared() {
                return getBaseType().isAssignableFrom(getDeclaredType());
            }
        }
    }
}
