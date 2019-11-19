package io.github.springroll.dl;

import groovy.lang.Binding;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroovyShellConfiguration implements ApplicationContextAware {

    private transient ApplicationContext applicationContext;

    @Bean
    Binding groovyShellBinding() {
        return new Binding(applicationContext.getBeansOfType(Scriptable.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
