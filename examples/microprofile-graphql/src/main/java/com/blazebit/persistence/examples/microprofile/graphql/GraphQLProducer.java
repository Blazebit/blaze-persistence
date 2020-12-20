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

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@ApplicationScoped
public class GraphQLProducer {

//    @Inject
//    EntityViewManager evm;

    Set<Reference> interfaceReferences = new HashSet<>();
    GraphQLEntityViewSupport graphQLEntityViewSupport;

    void alterOperation(@Observes Operation operation) {
//        List<Reference> references = new ArrayList<>();
//        references.add(operation.getReference());
//        while (!references.isEmpty()) {
//            Reference reference = references.remove(references.size() - 1);
//            if (reference.getType() == ReferenceType.INTERFACE) {
//                interfaceReferences.add(reference);
//            }
//            Map<String, Reference> parametrizedTypeArguments = reference.getParametrizedTypeArguments();
//            if (parametrizedTypeArguments != null) {
//                references.addAll(parametrizedTypeArguments.values());
//            }
//        }
    }

    void configure(@Observes GraphQLSchema.Builder schemaBuilder) {
//        for (Reference interfaceReference : interfaceReferences) {
//            schemaBuilder.additionalType(
//                GraphQLObjectType.newObject()
//                    .name(interfaceReference.getName() + "Impl")
//                    .withInterface(GraphQLTypeReference.typeRef(interfaceReference.getName()))
//                    .build()
//            );
//        }

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
