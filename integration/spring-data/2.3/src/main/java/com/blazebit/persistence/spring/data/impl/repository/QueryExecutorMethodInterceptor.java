/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.spring.data.impl.repository;

import kotlin.coroutines.Continuation;
import kotlinx.coroutines.reactive.AwaitKt;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.reactivestreams.Publisher;
import org.springframework.core.KotlinDetector;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.util.QueryExecutionConverters;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.data.repository.util.ReactiveWrappers;
import org.springframework.data.util.KotlinReflectionUtils;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This {@link MethodInterceptor} intercepts calls to methods of the custom implementation and delegates the to it if
 * configured. Furthermore it resolves method calls to finders and triggers execution of them. You can rely on having a
 * custom repository implementation instance set if this returns true.
 *
 * @author Oliver Gierke
 * @author Mark Paluch
 */
class QueryExecutorMethodInterceptor implements MethodInterceptor {

	private static final GenericConversionService CONVERSION_SERVICE;

	static {
		try {
			Field field = RepositoryFactorySupport.class.getDeclaredField("CONVERSION_SERVICE");
			field.setAccessible(true);
			CONVERSION_SERVICE = (GenericConversionService) field.get(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private final Map<Method, RepositoryQuery> queries;
	private final Map<Method, QueryMethodInvoker> invocationMetadataCache = new ConcurrentReferenceHashMap<>();
	private final QueryExecutionResultHandler resultHandler;
	private final NamedQueries namedQueries;
	private final List<QueryCreationListener<?>> queryPostProcessors;

	/**
	 * Creates a new {@link QueryExecutorMethodInterceptor}. Builds a model of {@link QueryMethod}s to be invoked on
	 * execution of repository interface methods.
	 */
	public QueryExecutorMethodInterceptor(RepositoryInformation repositoryInformation,
                                          ProjectionFactory projectionFactory, Optional<QueryLookupStrategy> queryLookupStrategy, NamedQueries namedQueries,
                                          List<QueryCreationListener<?>> queryPostProcessors) {

		this.namedQueries = namedQueries;
		this.queryPostProcessors = queryPostProcessors;

		this.resultHandler = new QueryExecutionResultHandler(CONVERSION_SERVICE);

		if (!queryLookupStrategy.isPresent() && repositoryInformation.hasQueryMethods()) {

			throw new IllegalStateException("You have defined query method in the repository but "
					+ "you don't have any query lookup strategy defined. The "
					+ "infrastructure apparently does not support query methods!");
		}

		this.queries = queryLookupStrategy //
				.map(it -> mapMethodsToQuery(repositoryInformation, it, projectionFactory)) //
				.orElse(Collections.emptyMap());
	}

	private Map<Method, RepositoryQuery> mapMethodsToQuery(RepositoryInformation repositoryInformation,
			QueryLookupStrategy lookupStrategy, ProjectionFactory projectionFactory) {

		return repositoryInformation.getQueryMethods().stream() //
				.map(method -> lookupQuery(method, repositoryInformation, lookupStrategy, projectionFactory)) //
				.peek(pair -> invokeListeners(pair.getSecond())) //
				.collect(Pair.toMap());
	}

	private Pair<Method, RepositoryQuery> lookupQuery(Method method, RepositoryInformation information,
			QueryLookupStrategy strategy, ProjectionFactory projectionFactory) {
		return Pair.of(method, strategy.resolveQuery(method, information, projectionFactory, namedQueries));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void invokeListeners(RepositoryQuery query) {

		for (QueryCreationListener listener : queryPostProcessors) {

			ResolvableType typeArgument = ResolvableType.forClass(QueryCreationListener.class, listener.getClass())
					.getGeneric(0);

			if (typeArgument != null && typeArgument.isAssignableFrom(ResolvableType.forClass(query.getClass()))) {
				listener.onCreation(query);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	@Nullable
	public Object invoke(@SuppressWarnings("null") MethodInvocation invocation) throws Throwable {

		Method method = invocation.getMethod();

		QueryExecutionConverters.ExecutionAdapter executionAdapter = QueryExecutionConverters //
				.getExecutionAdapter(method.getReturnType());

		if (executionAdapter == null) {
			return resultHandler.postProcessInvocationResult(doInvoke(invocation), method);
		}

		return executionAdapter //
				.apply(() -> resultHandler.postProcessInvocationResult(doInvoke(invocation), method));
	}

	@Nullable
	private Object doInvoke(MethodInvocation invocation) throws Throwable {

		Method method = invocation.getMethod();

		if (hasQueryFor(method)) {

			QueryMethodInvoker invocationMetadata = invocationMetadataCache.get(method);

			if (invocationMetadata == null) {
				invocationMetadata = new QueryMethodInvoker(method);
				invocationMetadataCache.put(method, invocationMetadata);
			}

			RepositoryQuery repositoryQuery = queries.get(method);
			return invocationMetadata.invoke(repositoryQuery, invocation.getArguments());
		}

		return invocation.proceed();
	}

	/**
	 * Returns whether we know of a query to execute for the given {@link Method};
	 *
	 * @param method
	 * @return
	 */
	private boolean hasQueryFor(Method method) {
		return queries.containsKey(method);
	}

	/**
	 * Invoker for Query Methods. Considers
	 */
	static class QueryMethodInvoker {

		private final boolean suspendedDeclaredMethod;
		private final Class<?> returnedType;
		private final boolean returnsReactiveType;

		QueryMethodInvoker(Method invokedMethod) {

			if (KotlinDetector.isKotlinReflectPresent()) {

				this.suspendedDeclaredMethod = KotlinReflectionUtils.isSuspend(invokedMethod);
				this.returnedType = this.suspendedDeclaredMethod ? KotlinReflectionUtils.getReturnType(invokedMethod)
						: invokedMethod.getReturnType();
			} else {

				this.suspendedDeclaredMethod = false;
				this.returnedType = invokedMethod.getReturnType();
			}

			this.returnsReactiveType = ReactiveWrappers.supports(returnedType);
		}

		@Nullable
		public Object invoke(RepositoryQuery query, Object[] args) {
			return suspendedDeclaredMethod ? invokeReactiveToSuspend(query, args) : query.execute(args);
		}

		@Nullable
		@SuppressWarnings({ "unchecked", "ConstantConditions" })
		private Object invokeReactiveToSuspend(RepositoryQuery query, Object[] args) {

			/*
			* Kotlin suspended functions are invoked with a synthetic Continuation parameter that keeps track of the Coroutine context.
			* We're invoking a method without Continuation as we expect the method to return any sort of reactive type,
			* therefore we need to strip the Continuation parameter.
			*/
			Continuation<Object> continuation = (Continuation) args[args.length - 1];
			args[args.length - 1] = null;
			Object result = query.execute(args);

			if (returnsReactiveType) {
				return ReactiveWrapperConverters.toWrapper(result, returnedType);
			}

			Publisher<?> publisher = result instanceof Publisher ? (Publisher<?>) result
					: ReactiveWrapperConverters.toWrapper(result, Publisher.class);

			return AwaitKt.awaitFirstOrNull(publisher, continuation);
		}
	}
}
