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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.constraints.impl.NotNullValidator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import de.hashcode.validation.GuiceConstraintValidatorFactory;
import de.hashcode.validation.UniqueKeyValidator;

/**
 * Test for {@link GuiceConstraintValidatorFactory}.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public class GuiceConstraintValidatorFactoryTest {

    @SuppressWarnings("unused")
    private final class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }

        @Provides
        @Singleton
        public ValidatorFactory getValidatorFactory(final Injector injector) {
            return Validation.byDefaultProvider().configure()
                    .constraintValidatorFactory(new GuiceConstraintValidatorFactory(injector)).buildValidatorFactory();
        }

        @Provides
        @Singleton
        public Validator getValidator(final ValidatorFactory validatorFactory) {
            return validatorFactory.getValidator();
        }

        @Provides
        @Singleton
        public UniqueKeyValidator getUniqueKeyValidator(final EntityManagerFactory entityManagerFactory) {
            return new UniqueKeyValidator(entityManagerFactory.createEntityManager());
        }

        @Provides
        @Singleton
        public EntityManagerFactory getEntityManagerFactory() {
            return Persistence.createEntityManagerFactory("test");
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(UniqueKeyValidatorTest.class);
    private Validator validator;
    private Injector injector;

    @Before
    public void beforeMethod() {
        injector = Guice.createInjector(new TestModule());
        validator = injector.getInstance(Validator.class);
    }

    @Test
    public void testUniqueKeyValidatorHasEntityManagerSet() {
        final UniqueKeyValidator uniqueKeyValidator = injector.getInstance(UniqueKeyValidator.class);
        assertNotNull(uniqueKeyValidator.getEntityManager());
    }

    @Test
    public void testStandardValidatorIsFound() {
        final NotNullValidator validator = injector.getInstance(NotNullValidator.class);
        assertNotNull(validator);
    }

    @Test
    public void testUniqueKey() {
        final Entity1 obj1 = new Entity1("val1", "someValue");

        // The validation already invokes the UniqueKeyValidator, so if this
        // does not throw an
        // error the setup is fine
        final Set<? extends ConstraintViolation<?>> violations = validator.validate(obj1);
        assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());
    }

}
