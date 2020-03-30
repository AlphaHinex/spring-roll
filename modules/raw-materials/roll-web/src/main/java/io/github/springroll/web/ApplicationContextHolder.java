package io.github.springroll.web;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Lazy(false)
public class ApplicationContextHolder implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final ApplicationContextHolder HOLDER = new ApplicationContextHolder();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        HOLDER.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return HOLDER.applicationContext;
    }

    public static Object getBean(String name) throws BeansException {
        return HOLDER.applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return HOLDER.applicationContext.getBean(requiredType);
    }

    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return HOLDER.applicationContext.getBean(name, requiredType);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return HOLDER.applicationContext.getBeansOfType(type);
    }

}
