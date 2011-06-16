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

import static de.javakaffee.validation.TestUtils.getByPropertyPath;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link FieldsMatchValidator}.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public class FieldsMatchValidatorTest {

    private Validator validator;

    @Before
    public void beforeMethod() {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final ValidatorContext validatorContext = validatorFactory.usingContext();
        validator = validatorContext.getValidator();
    }

    @Test
    public void testMatchingFieldsMatchingShouldBeValid() {
        final Entity obj1 = new Entity("foo", "foo");
        assertTrue(validator.validate(obj1).isEmpty());
    }

    @Test
    public void testNotMatchingFieldShouldNotBeValid() {
        final Entity obj1 = new Entity("foo", "bar");
        final Set<ConstraintViolation<Entity>> violations = validator.validate(obj1);
        assertEquals("Unexpected violations: " + violations + ".", 1, violations.size());
        assertNotNull("No violation found for propertyPath 'prop1Confirmation'.",
                getByPropertyPath(violations, "prop1Confirmation"));
    }

    @Test
    public void testFieldsMatchListWithNoViolation() {
        final EntityMulti obj1 = new EntityMulti("foo", "foo", "bar", "bar");
        final Set<ConstraintViolation<EntityMulti>> violations = validator.validate(obj1);
        assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());
    }

    @Test
    public void testFieldsMatchListWithSingleViolation() {
        final EntityMulti obj1 = new EntityMulti("foo", "foo", "bar", "barWrong");
        final Set<ConstraintViolation<EntityMulti>> violations = validator.validate(obj1);
        assertEquals("Unexpected violations: " + violations + ".", 1, violations.size());
        assertNotNull("No violation found for propertyPath 'prop2Confirmation'.",
                getByPropertyPath(violations, "prop2Confirmation"));
    }

    @Test
    public void testFieldsMatchListWithAllViolations() {
        final EntityMulti obj1 = new EntityMulti("foo", "fooWrong", "bar", "barWrong");
        final Set<ConstraintViolation<EntityMulti>> violations = validator.validate(obj1);
        assertEquals("Unexpected violations: " + violations + ".", 2, violations.size());
        assertNotNull("No violation found for propertyPath 'prop1Confirmation'.",
                getByPropertyPath(violations, "prop1Confirmation"));
        assertNotNull("No violation found for propertyPath 'prop2Confirmation'.",
                getByPropertyPath(violations, "prop2Confirmation"));
    }

    @FieldsMatch(first = "prop1", second = "prop1Confirmation")
    static class Entity {
        private final String prop1;
        private final String prop1Confirmation;

        public Entity(final String prop1, final String prop1Confirmation) {
            this.prop1 = prop1;
            this.prop1Confirmation = prop1Confirmation;
        }

        public String getProp1() {
            return prop1;
        }

        public String getProp1Confirmation() {
            return prop1Confirmation;
        }
    }

    @FieldsMatch.List({ @FieldsMatch(first = "prop1", second = "prop1Confirmation"),
            @FieldsMatch(first = "prop2", second = "prop2Confirmation") })
    static class EntityMulti {
        private final String prop1;
        private final String prop1Confirmation;
        private final String prop2;
        private final String prop2Confirmation;

        public EntityMulti(final String prop1, final String prop1Confirmation, final String prop2,
                final String prop2Confirmation) {
            this.prop1 = prop1;
            this.prop1Confirmation = prop1Confirmation;
            this.prop2 = prop2;
            this.prop2Confirmation = prop2Confirmation;
        }
        public String getProp1() {
            return prop1;
        }

        public String getProp1Confirmation() {
            return prop1Confirmation;
        }
        public String getProp2() {
            return prop2;
        }

        public String getProp2Confirmation() {
            return prop2Confirmation;
        }
    }

}
