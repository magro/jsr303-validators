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
package de.hashcode.validation;

import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConstraintValidatorFactory} that sets an
 * {@link javax.persistence.EntityManager} (retrieved from the provided
 * {@link EntityManagerFactory}) on {@link ConstraintValidator}s that implement
 * {@link EntityManagerAwareValidator}.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public class ConstraintValidatorFactoryEMFImpl implements ConstraintValidatorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintValidatorFactoryEMFImpl.class);

    private final EntityManagerFactory entityManagerFactory;

    public ConstraintValidatorFactoryEMFImpl(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
        T instance = null;

        try {
            instance = key.newInstance();
        } catch (final Exception e) {
            LOG.error("Could not instantiate " + key.getName(), e);
            return null;
        }

        if (EntityManagerAwareValidator.class.isAssignableFrom(key)) {
            final EntityManagerAwareValidator validator = (EntityManagerAwareValidator) instance;
            validator.setEntityManager(entityManagerFactory.createEntityManager());
        }

        return instance;
    }

}
