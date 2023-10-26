/*
 * Created by baili on 2020/12/05.
 */
package com.baili.sharingPlatform.web.swagger;

import com.fasterxml.classmate.ResolvedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.readers.parameter.ParameterTypeReader;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Set;

import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;

/**
 * @author baili
 * @date 2020/12/05.
 */
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class ParameterTypePlugin implements ParameterBuilderPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTypeReader.class);

	public static String findParameterType(ParameterContext parameterContext) {
		ResolvedMethodParameter parameter = parameterContext.resolvedMethodParameter();
		ResolvedType parameterType = parameter.getParameterType();
		parameterType = parameterContext.alternateFor(parameterType);

		//Multi-part file trumps any other annotations
		if (isFileType(parameterType) || isListOfFiles(parameterType)) {
			return "form";
		}
		if (parameter.hasParameterAnnotation(PathVariable.class)) {
			return "path";
		} else if (parameter.hasParameterAnnotation(RequestBody.class)) {
			return "body";
		} else if (parameter.hasParameterAnnotation(RequestPart.class)) {
			return "formData";
		} else if (parameter.hasParameterAnnotation(RequestParam.class)) {
			return determineScalarParameterType(parameterContext.getOperationContext().consumes(),
					parameterContext.getOperationContext().httpMethod());
		} else if (parameter.hasParameterAnnotation(RequestHeader.class)) {
			return "header";
		} else if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
			LOGGER.warn("@ModelAttribute annotated parameters should have already been expanded via " + "the ExpandedParameterBuilderPlugin");
		}

		return determineScalarParameterType(parameterContext.getOperationContext().consumes(), parameterContext.getOperationContext().httpMethod());
	}

	private static boolean isListOfFiles(ResolvedType parameterType) {
		return isContainerType(parameterType) && isFileType(collectionElementType(parameterType));
	}

	private static boolean isFileType(ResolvedType parameterType) {
		return MultipartFile.class.isAssignableFrom(parameterType.getErasedType());
	}

	public static String determineScalarParameterType(Set<? extends MediaType> consumes, HttpMethod method) {
		String parameterType = "body";

		if (consumes.contains(MediaType.MULTIPART_FORM_DATA) && method == HttpMethod.POST) {
			parameterType = "formData";
		} else if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
			parameterType = "form";
		} else if (method == HttpMethod.GET || method == HttpMethod.DELETE || method == HttpMethod.HEAD) {
			parameterType = "query";
		}
		return parameterType;
	}

	@Override
	public void apply(ParameterContext context) {
		context.parameterBuilder().parameterType(findParameterType(context));
	}

	@Override
	public boolean supports(DocumentationType delimiter) {
		return true;
	}
}
