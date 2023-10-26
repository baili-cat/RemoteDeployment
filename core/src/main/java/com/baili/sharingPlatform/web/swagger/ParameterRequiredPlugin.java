/*
 * Created by baili on 2020/12/05.
 */
package com.baili.sharingPlatform.web.swagger;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Optional;

import static springfox.bean.validators.plugins.Validators.*;

/**
 * @author baili
 * @date 2020/12/05.
 */
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class ParameterRequiredPlugin implements ParameterBuilderPlugin, ModelPropertyBuilderPlugin {

	@Override
	public boolean supports(DocumentationType documentationType) {
		return true;
	}

	@Override
	public void apply(ParameterContext context) {
		Optional<NotEmpty> notEmpty = annotationFromParameter(context, NotEmpty.class);
		Optional<NotBlank> notBlank = annotationFromParameter(context, NotBlank.class);
		Optional<NotNull> notNull = annotationFromParameter(context, NotNull.class);

		if (notEmpty.isPresent() || notBlank.isPresent() || notNull.isPresent()) {
			context.parameterBuilder().required(true);
		}
	}

	@Override
	public void apply(ModelPropertyContext context) {
		Optional<NotEmpty> notEmpty = extractAnnotation(context, NotEmpty.class);
		Optional<NotBlank> notBlank = extractAnnotation(context, NotBlank.class);
		Optional<NotNull> notNull = extractAnnotation(context, NotNull.class);

		if (notEmpty.isPresent() || notBlank.isPresent() || notNull.isPresent()) {
			context.getBuilder().required(true);
		}
	}

	private <T extends Annotation> Optional<T> extractAnnotation(ModelPropertyContext context, Class<T> annotationType) {
		return annotationFromBean(context, annotationType).map(Optional::of).orElse(annotationFromField(context, annotationType));
	}

}
