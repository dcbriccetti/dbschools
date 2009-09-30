package com.dbschools.music;

import net.jcip.annotations.Immutable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Immutable
public final class SpringAccess {

    public static ApplicationContext getContext() {
        return context;
    }
    
    private static final ApplicationContext context = new ClassPathXmlApplicationContext("spring-test.xml");
}
