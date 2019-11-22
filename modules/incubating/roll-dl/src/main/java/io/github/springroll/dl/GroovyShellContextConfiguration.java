package io.github.springroll.dl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GroovyShellContextConfiguration implements ApplicationContextAware {

    private transient ApplicationContext applicationContext;

    @Bean
    public Map<String, Scriptable> groovyShellContext() {
        return applicationContext.getBeansOfType(Scriptable.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
