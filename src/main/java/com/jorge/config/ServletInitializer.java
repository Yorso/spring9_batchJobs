package com.jorge.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.jorge.batch.BatchConfig;

public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[0];
	}

	@Override
	protected Class<?>[] getServletConfigClasses() { //This declares the Spring configuration classes.
													 //Here, we declare the AppConfig and DatabaseConfig classes that were previously defined.
		return new Class<?>[] { AppConfig.class, BatchConfig.class }; // We declared BatchConfig in the ServletInitializer class to make our Spring Batch
																	  // configuration available to the controller methods
	}

	@Override
	protected String[] getServletMappings() { //This declares the servlet root URI
		return new String[] { "/" };
	}
}
