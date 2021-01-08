/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.common.view;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.boot.internal.EnversService;

import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import org.hibernate.metamodel.spi.MetamodelImplementor;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface AuditedView extends Serializable {

    @MappingCorrelated(correlationBasis = "this",
            correlationResult = "originalId.REV",
            correlator = CteRegistrationSubqueryProvider.class,
            fetch = FetchStrategy.JOIN)
    EntityRevisionDetail getCreationMetadata();

    class CteRegistrationSubqueryProvider implements CorrelationProvider {

        private static final String REVISION_TYPE_FORMAT = "%s.REVTYPE";

        private static final String ORIGINAL_ID_FORMAT = "%s.originalId.%s";

        @Override
        public void applyCorrelation(CorrelationBuilder correlationBuilder,
                String correlationExpression) {
            FromProvider fromProvider = correlationBuilder.getCorrelationFromProvider();
            EntityType<?> entityType = this.getEntityType(correlationBuilder::getService,
                    fromProvider);
            Class<?> idType = entityType.getIdType().getJavaType();
            String idName = entityType.getId(idType).getName();
            EntityType<?> auditEntityType = this.getAuditEntityType(
                    correlationBuilder::getService, fromProvider);
            String alias = correlationBuilder.getCorrelationAlias();
            String originalId = getAuditedEntityOriginalId(
                    correlationBuilder::getService, fromProvider, alias);
            String revisionType = getRevisionType(alias);
            correlationBuilder.correlate(auditEntityType).on(originalId)
                    .eqExpression(correlationExpression + "." + idName)
                    .on(revisionType).eq(RevisionType.ADD).end();
        }

        private String getRevisionType(String auditedEntityAlias) {
            return String.format(REVISION_TYPE_FORMAT, auditedEntityAlias);
        }

        private String getAuditedEntityOriginalId(
                ServiceProvider serviceProvider, FromProvider fromProvider,
                String auditedEntityAlias) {
            EntityType<?> entityType = this.getEntityType(serviceProvider, fromProvider);
            Class<?> idType = entityType.getIdType().getJavaType();
            String idName = entityType.getId(idType).getName();
            return String.format(ORIGINAL_ID_FORMAT, auditedEntityAlias,
                    idName);
        }

        private Class<?> getEntityClass(FromProvider fromProvider) {
            return fromProvider.getRoots().iterator().next().getJavaType();
        }

        private EntityType<?> getEntityType(ServiceProvider serviceProvider,
                FromProvider fromProvider) {
            Class<?> entityClass = this.getEntityClass(fromProvider);
            MetamodelImplementor metamodel = this.getSessionFactory(serviceProvider)
                    .getMetamodel();
            return metamodel.entity(entityClass);
        }

        private EntityType<?> getAuditEntityType(
                ServiceProvider serviceProvider, FromProvider fromProvider) {
            Class<?> entityClass = this.getEntityClass(fromProvider);
            MetamodelImplementor metamodel = this.getSessionFactory(serviceProvider)
                    .getMetamodel();
            String entityName = metamodel.entityPersister(entityClass)
                    .getEntityName();
            String auditedEntityName = this.getEnvers(serviceProvider)
                    .getAuditEntitiesConfiguration()
                    .getAuditEntityName(entityName);
            return metamodel.entity(auditedEntityName);
        }

        private EnversService getEnvers(ServiceProvider serviceProvider) {
            return this.getSessionFactory(serviceProvider).getServiceRegistry()
                    .getService(EnversService.class);
        }

        private SessionFactoryImplementor getSessionFactory(
                ServiceProvider serviceProvider) {
            return serviceProvider.getService(EntityManager.class)
                    .unwrap(SessionImplementor.class).getSessionFactory();
        }

    }

}