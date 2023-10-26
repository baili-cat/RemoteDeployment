/*
 * Created by baili on 2020/12/05.
 */
package com.baili.sharingPlatform.web.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.baili.sharingPlatform.common.GenericEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baili
 * @date 2020/12/05.
 */
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class GenericEnumParameterPlugin implements ParameterBuilderPlugin, ModelPropertyBuilderPlugin {

	private TypeResolver resolver;

	@Autowired
	public GenericEnumParameterPlugin(TypeResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean supports(DocumentationType documentationType) {
		return true;
	}

	@Override
	public void apply(ParameterContext context) {
		Class<?> type = context.resolvedMethodParameter().getParameterType().getErasedType();
		if (GenericEnum.class.isAssignableFrom(type)) {
			context.parameterBuilder().type(resolver.resolve(getValueType(type)));
			context.parameterBuilder().allowableValues(getAllowableListValues(type));
		}
	}

	@Override
	public void apply(ModelPropertyContext context) {
		if (context.getBeanPropertyDefinition().isPresent()) {
			BeanPropertyDefinition definition = context.getBeanPropertyDefinition().get();
			Class<?> type = definition.getRawPrimaryType();
			if (GenericEnum.class.isAssignableFrom(type)) {
				context.getBuilder().type(context.getResolver().resolve(getValueType(type)));
				context.getBuilder().allowableValues(getAllowableListValues(type));
			}
		}
	}

	private Class<?> getValueType(Class<?> type) {
		ResolvableType resolvableType = ResolvableType.forClass(type).as(GenericEnum.class);
		ResolvableType[] generics = resolvableType.getGenerics();
		return generics[0].resolve();
	}

	private AllowableListValues getAllowableListValues(Class<?> type) {
		//noinspection unchecked
		Class<GenericEnum<?>> genericEnumClass = (Class<GenericEnum<?>>)type;
		List<String> values = new ArrayList<>();
		for (GenericEnum<?> genericEnum : genericEnumClass.getEnumConstants()) {
			values.add(String.valueOf(genericEnum.getValue()));
		}
		return new AllowableListValues(values, "LIST");
	}
}

