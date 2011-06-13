package de.javakaffee.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * A {@link ConstraintValidatorFactory} that relies on guice for creating
 * validators.
 * 
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
 */
public class GuiceConstraintValidatorFactory implements ConstraintValidatorFactory {

	private final Injector injector;

	@Inject
	public GuiceConstraintValidatorFactory(final Injector injector) {
		this.injector = injector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
		return this.injector.getInstance(key);
	}

}
