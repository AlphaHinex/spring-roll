package io.github.springroll.web.factory;

import io.github.springroll.utils.StringUtil;
import io.github.springroll.web.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来将多个 list bean 合并成为一个 bean 的工厂 bean
 * 支持按 bean name 的样式进行合并
 */
public class ComposeListFactoryBean extends AbstractFactoryBean<List> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposeListFactoryBean.class);

    /**
     * 要合并的 list 类型 bean 的名称样式
     */
    private transient String listBeanNamePattern;

    public ComposeListFactoryBean(String listBeanNamePattern) {
        this.listBeanNamePattern = listBeanNamePattern;
    }

    @Override
    public Class<List> getObjectType() {
        return List.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List createInstance() {
        List instance = new ArrayList();
        if (StringUtil.isBlank(listBeanNamePattern)) {
            LOGGER.debug("Not define list bean name pattern, return empty list!");
            return instance;
        }

        LOGGER.debug("Composing list with '{}' pattern...", listBeanNamePattern);
        try {
            String[] beanNames = ApplicationContextHolder.getApplicationContext().getBeanNamesForType(List.class);
            for (String beanName : beanNames) {
                if (beanName.matches(listBeanNamePattern)) {
                    LOGGER.debug("Add '{}' with '{}' pattern.", beanNames, listBeanNamePattern);
                    instance.addAll(ApplicationContextHolder.getBean(beanName, List.class));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Find beans with '{}' pattern error!", listBeanNamePattern, e);
        }
        return instance;
    }

}
