/*
 * Copyright 2011 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.javakaffee.validation;

import static de.javakaffee.validation.ReflectionUtils.getIdField;
import static de.javakaffee.validation.ReflectionUtils.getPropertyValue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * The validator for beans annotated with {@link UniqueKey}. The validator
 * {@link UniqueKeyValidator} allows annotated bean to use read-only properties
 * (immutable). Also an update of the entity is allowed, without having to use
 * different validation groups for create/edit.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public class UniqueKeyValidator implements ConstraintValidator<UniqueKey, Serializable>, EntityManagerAwareValidator {

    private EntityManager entityManager;
    private UniqueKey constraintAnnotation;

    public UniqueKeyValidator() {
    }

    public UniqueKeyValidator(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void initialize(final UniqueKey constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;

    }

    @Override
    public boolean isValid(final Serializable target, final ConstraintValidatorContext context) {

        if (entityManager == null) {
            // eclipselink may be configured with a BeanValidationListener that
            // validates an entity on prePersist
            // In this case we don't want to and we cannot check anything (the
            // entityManager is not set)
            //
            // Alternatively, you can disalbe bean validation during jpa
            // operations
            // by adding the property "javax.persistence.validation.mode" with
            // value "NONE" to persistence.xml
            return true;
        }

        final Class<?> entityClass = target.getClass();

        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        final CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();

        final Root<?> root = criteriaQuery.from(entityClass);

        try {
            final Object propertyValue = getPropertyValue(target, constraintAnnotation.property());
            final Predicate uniquePropertyPredicate = criteriaBuilder.equal(root.get(constraintAnnotation.property()),
                    propertyValue);

            final Field idField = getIdField(entityClass);
            final String idProperty = idField.getName();
            final Object idValue = getPropertyValue(target, idProperty);

            if (idValue != null) {
                final Predicate idNotEqualsPredicate = criteriaBuilder.notEqual(root.get(idProperty), idValue);
                criteriaQuery.where(uniquePropertyPredicate, idNotEqualsPredicate);
            } else {
                criteriaQuery.where(uniquePropertyPredicate);
            }

        } catch (final Exception e) {
            throw new RuntimeException("An error occurred when trying to create the jpa predicate for the @UniqueKey '"
                    + constraintAnnotation.property() + "' on bean " + entityClass + ".", e);
        }

        final List<Object> resultSet = entityManager.createQuery(criteriaQuery).getResultList();

        if (!resultSet.isEmpty()) {
            context.buildConstraintViolationWithTemplate(constraintAnnotation.message())
                    .addNode(constraintAnnotation.property()).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        return true;
    }

}
