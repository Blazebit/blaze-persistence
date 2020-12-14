/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.examples.microprofile.graphql;

import com.blazebit.reflection.ReflectionUtils;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@ApplicationScoped
public class GraphQLProducer {

//    @Inject
//    EntityViewManager evm;

    GraphQLEntityViewSupport graphQLEntityViewSupport;

    void alterOperation(@Observes Operation operation) {
        if ("com.blazebit.persistence.integration.graphql.GraphQLRelayConnection".equals(operation.getWrapper().getWrapperClassName())) {
            try {
                List<Argument> arguments = operation.getArguments();
                Class<?>[] parameterTypes = new Class[arguments.size()];
                for (int i = 0; i < arguments.size(); i++) {
                    Argument argument = arguments.get(i);
                    if (argument.getWrapper() == null) {
                        parameterTypes[i] = Class.forName(argument.getReference().getClassName());
                    } else {
                        parameterTypes[i] = Class.forName(argument.getWrapper().getWrapperClassName());
                    }
                }

                Class<?> clazz = Class.forName(operation.getClassName());
                Method method = clazz.getDeclaredMethod(operation.getMethodName(), parameterTypes);
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(clazz, method);
                Class<?> elementType = typeArguments[0];
                operation.setWrapper(null);
                operation.getReference().setClassName("com.blazebit.persistence.integration.graphql.GraphQLRelayConnection");
                operation.getReference().setGraphQlClassName("com.blazebit.persistence.integration.graphql.GraphQLRelayConnection");
                operation.getReference().setName(elementType.getSimpleName() + "Connection");
                operation.getReference().setType(ReferenceType.TYPE);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    void configure(@Observes GraphQLSchema.Builder schemaBuilder) {

        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(true, true);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(schemaBuilder, null);
    }

    @Produces
    @ApplicationScoped
    GraphQLEntityViewSupport graphQLEntityViewSupport() {
        return graphQLEntityViewSupport;
    }
}
