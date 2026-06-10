package com.example.demo.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.util.Set;

/**
 * Utility class for entity validation using Jakarta Validation API
 */
public class ValidationUtil {
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Validate entity and throw exception if invalid
     *
     * @param entity Entity to validate
     * @throws IllegalArgumentException if entity validation fails
     */
    public static <T> void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                  .append(": ")
                  .append(violation.getMessage())
                  .append(" | ");
            }
            throw new IllegalArgumentException("Validation failed: " + sb.toString());
        }
    }

}

