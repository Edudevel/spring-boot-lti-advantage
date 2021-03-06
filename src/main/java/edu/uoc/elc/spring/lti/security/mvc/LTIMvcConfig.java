package edu.uoc.elc.spring.lti.security.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author xaracil@uoc.edu
 */
@Configuration
public class LTIMvcConfig implements WebMvcConfigurer {
	private final CurrentContextHandlerMethodArgumentResolver currentContextHandlerMethodArgumentResolver;

	private final CurrentUserHandlerMethodArgumentResolver currentUserHandlerMethodArgumentResolver;

	private final CurrentToolHandlerMethodArgumentResolver currentToolHandlerMethodArgumentResolver;

	public LTIMvcConfig(CurrentContextHandlerMethodArgumentResolver currentContextHandlerMethodArgumentResolver, CurrentToolHandlerMethodArgumentResolver currentToolHandlerMethodArgumentResolver, CurrentUserHandlerMethodArgumentResolver currentUserHandlerMethodArgumentResolver) {
		this.currentContextHandlerMethodArgumentResolver = currentContextHandlerMethodArgumentResolver;
		this.currentToolHandlerMethodArgumentResolver = currentToolHandlerMethodArgumentResolver;
		this.currentUserHandlerMethodArgumentResolver = currentUserHandlerMethodArgumentResolver;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(this.currentContextHandlerMethodArgumentResolver);
		argumentResolvers.add(this.currentUserHandlerMethodArgumentResolver);
		argumentResolvers.add(this.currentToolHandlerMethodArgumentResolver);
	}

}
