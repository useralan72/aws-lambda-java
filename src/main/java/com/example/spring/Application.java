package com.example.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
	
	private static ApplicationContext springContext = null;
    private static ApplicationContext getSpringContext() {
        if (springContext == null) {
            synchronized (ApplicationContext.class) {
                if (springContext == null) {
                    springContext = new ClassPathXmlApplicationContext("/application-context.xml");
                }
            }
        }
        return springContext;
    }
    public static <T> T getBean(Class<T> clazz) {
        return getSpringContext().getBean(clazz);
    }

}
