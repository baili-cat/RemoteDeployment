package com.baili.sharingPlatform.common.utils;///*
// * Created by baili on 2020/11/17.
// */
//package com.baili.agentcase.core.utils;
//
//import javax.validation.*;
//import java.util.Set;
//
///**
// * @author baili
// * @date 2020/11/17.
// */
//public class ValidatorUtils {
//
//	private static final Validator VALIDATOR;
//
//	static {
//		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//		VALIDATOR = factory.getValidator();
//	}
//
//	public static void validate(Object object, Class<?>... groups) {
//		Set<ConstraintViolation<Object>> violationSet = VALIDATOR.validate(object, groups);
//		if (!violationSet.isEmpty()) {
//			throw new ConstraintViolationException(violationSet);
//		}
//	}
//
//	/*public static void validateProperty(Object object, String propertyName, Class<?>... groups) {
//		Set<ConstraintViolation<Object>> violationSet = VALIDATOR.validateProperty(object, propertyName, groups);
//		if (!violationSet.isEmpty()) {
//			throw new ConstraintViolationException(violationSet);
//		}
//	}*/
//
//	/*public static void validateValue(Class<?> beanType, String propertyName, Object value, Class<?>... groups) {
//		// Set<ConstraintViolation<?>> violationSet = VALIDATOR.validateValue(beanType, propertyName, value, groups);
//		Set<? extends ConstraintViolation<?>> violationSet = VALIDATOR.validateValue(beanType, propertyName, value, groups);
//		if (!violationSet.isEmpty()) {
//			throw new ConstraintViolationException(violationSet);
//		}
//	}*/
//
//}
