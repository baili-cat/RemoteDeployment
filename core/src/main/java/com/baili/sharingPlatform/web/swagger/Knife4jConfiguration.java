/*
 * Created by baili on 2020/12/05.
 */
package com.baili.sharingPlatform.web.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author baili
 * @date 2020/12/05.
 */
@Configuration
@EnableSwagger2WebMvc
@Import({BeanValidatorPluginsConfiguration.class})
public class Knife4jConfiguration {

	@Bean(value = "defaultApi")
	public Docket defaultApi2() {
		return build("TestOps平台-测试工具-API接口文档", "com.baili.sharingPlatform.api.web.controller");
	}

	public Docket build(String title, String basePackage) {
		ApiInfo apiInfo = new ApiInfoBuilder().title(title).version("1.0").build();
		return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false)//
		                                              .apiInfo(apiInfo).select()//
		                                              .apis(RequestHandlerSelectors.basePackage(basePackage))//
		                                              .paths(PathSelectors.any())//
		                                              //.paths(PathSelectors.regex("/healthz").negate())//
		                                              .build()//
				// .securitySchemes(securitySchemes())//
				// .securityContexts(securityContexts())
				;
	}

	/*private List<ApiKey> securitySchemes() {
		return Lists.newArrayList(new ApiKey("Authorization", "Authorization", "header"));
	}

	private List<SecurityContext> securityContexts() {
		return Lists.newArrayList(SecurityContext.builder()//
		                                         .securityReferences(defaultAuth())//
		                                         // .paths(PathSelectors.regex("^(?!auth).*$"))
		                                         .forPaths(PathSelectors.any())//
		                                         .build());
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{//
				new AuthorizationScope("global", "accessEverything")//
		};
		return Lists.newArrayList(new SecurityReference("Authorization", authorizationScopes));
	}*/
}
