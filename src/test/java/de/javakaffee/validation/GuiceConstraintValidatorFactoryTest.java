package de.javakaffee.validation;

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

/**
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
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
			return Validation.byDefaultProvider().configure().constraintValidatorFactory(
					new GuiceConstraintValidatorFactory(injector)).buildValidatorFactory();
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
	private Validator _validator;
	private Injector injector;

	@Before
	public void beforeMethod() {
		this.injector = Guice.createInjector(new TestModule());
		this._validator = this.injector.getInstance(Validator.class);
	}

	@Test
	public void testUniqueKeyValidatorHasEntityManagerSet() {
		final UniqueKeyValidator uniqueKeyValidator = this.injector.getInstance(UniqueKeyValidator.class);
		assertNotNull(uniqueKeyValidator.getEntityManager());
	}

	@Test
	public void testStandardValidatorIsFound() {
		final NotNullValidator validator = this.injector.getInstance(NotNullValidator.class);
		assertNotNull(validator);
	}

	@Test
	public void testUniqueKey() {
		final Entity1 obj1 = new Entity1("val1", "someValue");

		// The validation already invokes the UniqueKeyValidator, so if this
		// does not throw an
		// error the setup is fine
		final Set<? extends ConstraintViolation<?>> violations = this._validator.validate(obj1);
		assertEquals("Unexpected violations: " + violations + ".", 0, violations.size());
	}

}
