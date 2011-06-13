package de.javakaffee.validation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
 */
public class UniqueKeyValidatorTest {

	private static final Logger LOG = LoggerFactory.getLogger(UniqueKeyValidatorTest.class);
	private Validator _validator;
	private EntityManager _em;
	private List<Object> _objectsToRemove;

	@Before
	public void beforeMethod() {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("test");

		final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		final ValidatorContext validatorContext = validatorFactory.usingContext();
		validatorContext.constraintValidatorFactory(new ConstraintValidatorFactoryEMFImpl(entityManagerFactory));
		this._validator = validatorContext.getValidator();

		this._em = entityManagerFactory.createEntityManager();

		this._objectsToRemove = new ArrayList<Object>();
	}

	@After
	public void afterMethod() {
		for (final Object entity : this._objectsToRemove) {
			this._em.getTransaction().begin();
			this._em.remove(entity);
			this._em.getTransaction().commit();
		}
	}

	@Test
	public void testUniqueKey() {

		final Entity1 obj1 = new Entity1("val1", "someValue");

		Set<? extends ConstraintViolation<?>> violations = this._validator.validate(obj1);
		assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());

		this._em.getTransaction().begin();
		this._em.persist(obj1);
		this._em.getTransaction().commit();
		this._objectsToRemove.add(obj1);

		final Entity1 obj2 = new Entity1(obj1.getProp1(), "someValue");

		violations = this._validator.validate(obj2);
		assertEquals("Unexpected violations: " + violations + ".", 1, violations.size());
		assertPropertyPath(violations.iterator().next(), "prop1");
	}

	@Test
	public void testUniqueKeyAllowsUpdate() {

		final Entity1 obj1 = new Entity1("val1", "someValue");

		this._em.getTransaction().begin();
		this._em.persist(obj1);
		this._em.getTransaction().commit();
		this._objectsToRemove.add(obj1);

		// As we want to update the entity sometimes later it must still be seen
		// as valid
		final Set<? extends ConstraintViolation<?>> violations = this._validator.validate(obj1);
		assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());
	}

	@Test
	public void testUniqueKeyViolationShouldNotInterfereWithOtherViolations() {

		final Entity1 obj1 = new Entity1("val1", "someInvalidValue");

		Set<? extends ConstraintViolation<?>> violations = this._validator.validate(obj1);
		assertEquals("Unexpected violations: " + violations + ".", 1, violations.size());
		final ConstraintViolation<?> origViolation = violations.iterator().next();
		assertPropertyPath(origViolation, "prop2");

		this._em.getTransaction().begin();
		this._em.persist(obj1);
		this._em.getTransaction().commit();
		this._objectsToRemove.add(obj1);

		final Entity1 obj2 = new Entity1(obj1.getProp1(), obj1.getProp2());

		violations = this._validator.validate(obj2);
		assertEquals("Unexpected violations: " + violations + ".", 2, violations.size());
		assertNotNull("No violation found for propertyPath 'prop1'.", getByPropertyPath(violations, "prop1"));
		final ConstraintViolation<?> newViolation = getByPropertyPath(violations, "prop2");
		assertNotNull("No violation found for propertyPath 'prop2'.", newViolation);
		assertEqualsViolation(origViolation, newViolation);
	}

	@Test
	public void testIdPropertyIdFoundOnSuperclass() {
		// Entity2 relies on the @Id from its superclass
		final Entity2 obj1 = new Entity2("val1");

		final Set<? extends ConstraintViolation<?>> violations = this._validator.validate(obj1);
		assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());
	}

	/**
	 * Checks equality of two {@link ConstraintViolation}s, as the impl class
	 * (of hibernate validator) seems to have no suitable equals implementation.
	 */
	private void assertEqualsViolation(final ConstraintViolation<?> expectedViolation,
			final ConstraintViolation<?> actualViolation) {
		assertEquals("Another constraintViolation seems to be compromised by UniqueKeyValidator.",
				expectedViolation.getInvalidValue(), actualViolation.getInvalidValue());
		assertEquals("Another constraintViolation seems to be compromised by UniqueKeyValidator.",
				expectedViolation.getMessage(), actualViolation.getMessage());
		assertEquals("Another constraintViolation seems to be compromised by UniqueKeyValidator.",
				expectedViolation.getMessageTemplate(), actualViolation.getMessageTemplate());
		assertEquals("Another constraintViolation seems to be compromised by UniqueKeyValidator.",
				expectedViolation.getPropertyPath(), actualViolation.getPropertyPath());
	}

	private ConstraintViolation<?> getByPropertyPath(final Set<? extends ConstraintViolation<?>> violations,
			final String propertyPath) {
		for (final ConstraintViolation<?> violation : violations) {
			final Path violationPropertyPath = violation.getPropertyPath();
			if (violationPropertyPath != null && violationPropertyPath.toString().equals(propertyPath)) {
				return violation;
			}
		}
		return null;
	}

	private void assertPropertyPath(final ConstraintViolation<?> violation, final String expectedPropertyPath) {
		assertEquals("Unexpected propertyPath for violation.", expectedPropertyPath,
				violation.getPropertyPath().toString());
	}

}

/**
 * A base class holding the <code>@Id</code>, so that identification of the id
 * property can be tested.
 */
@SuppressWarnings("serial")
@MappedSuperclass
class EntityBase implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;

	public long getId() {
		return this.id;
	}
}

@SuppressWarnings("serial")
@Entity
@UniqueKey(property = "prop1")
class Entity2 extends EntityBase {

	@Column
	private String prop1;

	public Entity2() {
	}

	public Entity2(final String prop1) {
		this.prop1 = prop1;
	}

	public String getProp1() {
		return this.prop1;
	}
}
