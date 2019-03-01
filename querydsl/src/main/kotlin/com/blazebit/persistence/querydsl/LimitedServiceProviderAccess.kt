/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.querydsl

/**
 * During conversion of QueryDSL expressions to Blaze Persist expressions, we need access
 * to the active EntityManager and the ConstantRegistry to bind found parameters in
 * the ParameterManager of the criteria builder. Both these dependencies are obtained
 * through a ServiceProvider.
 *
 * Criteria builders are service providers, and this mechanism is used to obtain services.
 * However, sub builders such as WhereOrBuilder, BaseHavingBuilder, etc. are not service
 * providers. As a result, our extension methods only work on root builders.
 *
 * The sub builders do not keep a back reference to the owning criteria builder, but
 * only to their direct parent result, and this parent is not type bound to any type of CriteriaBuilder
 * or ServiceProvider, even on occasions where it is effectively the case).
 *
 * Unfortunately, to make ServiceProvider available from the WhereOrBuilder for example,
 * we'd have to pass ServiceProviders down the entire chain of builders which is a rather intensive change.
 *
 * To make our extension methods available to some of the sub-builders however, we have
 * experimental wrappers that add some additional methods as well as provide the required
 * access to the ServiceBuilder from the parent builder.
 *
 * As soon as an alternative mechanism arises to obtain access to the required services,
 * these wrappers will go away. Although it is the intent that the use of extension methods
 * will remain binary compatible after the rework, it can happen that we will have to
 * decide that we cannot properly support that use case after all and as such it may get removed
 * altogether.
 */
@Experimental
annotation class LimitedServiceProviderAccess