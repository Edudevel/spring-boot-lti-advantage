package edu.uoc.elearn.spring.security.lti.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author Xavi Aracil <xaracil@uoc.edu>
 */
@Configuration
public class LTIMvcConfig implements WebMvcConfigurer {
	@Autowired
	private CurrentContextHandlerMethodArgumentResolver currentContextHandlerMethodArgumentResolver;

	@Autowired
	private CurrentUserHandlerMethodArgumentResolver currentUserHandlerMethodArgumentResolver;

	@Autowired
	private CurrentToolHandlerMethodArgumentResolver currentToolHandlerMethodArgumentResolver;

	public LTIMvcConfig() {
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(this.currentContextHandlerMethodArgumentResolver);
		argumentResolvers.add(this.currentUserHandlerMethodArgumentResolver);
		argumentResolvers.add(this.currentToolHandlerMethodArgumentResolver);
	}

}