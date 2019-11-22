package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import io.github.springroll.base.CharacterEncoding;
import io.github.springroll.utils.digest.Md5;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 脚本的运行环境
 * 因 {@code Binding} 对象的非线程安全性，其中的数据在所有脚本中共享
 * 在多线程环境下使用时务必小心
 * 本运行环境的一个主要使用场景是校验，通过动态脚本或类，使校验规则能够灵活定义并动态加载
 * 校验时应尽量使用读操作，避免写操作，以免对共享数据对象造成改变及预期之外的影响
 */
@Component
public class GroovyShellExecution {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyShellExecution.class);

    private transient GroovyClassLoader loader;
    private transient Map<String, Scriptable> applicationContext;
    private transient CompilerConfiguration configuration = new CompilerConfiguration();
    private transient Map<String, Class> classCache = new ConcurrentHashMap<>();

    @Autowired
    public GroovyShellExecution(Map<String, Scriptable> groovyShellApplicationContext) {
        this.applicationContext = groovyShellApplicationContext;
        configuration.setSourceEncoding(CharacterEncoding.getCharset().name());
        loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
            @Override
            public GroovyClassLoader run() {
                return new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), configuration);
            }
        });
    }

    public Object execute(String scriptContent) {
        return genericExecute(scriptContent, null);
    }

    public <T> T execute(String scriptContent, Class<T> clz) {
        return genericExecute(scriptContent, clz, null);
    }

    public Object execute(File file) {
        return genericExecute(file, null);
    }

    private Object genericExecute(Object scriptContent, Map<String, Object> scriptContext) {
        if (scriptContent == null) {
            throw new GroovyScriptException("Script content SHOULD NOT null!");
        }
        try {
            Binding binding = new Binding();
            binding.setVariable("applicationContext", applicationContext);
            binding.setVariable("scriptContext", scriptContext);
            Class scriptClass = getScriptClass(scriptContent);
            return InvokerHelper.createScript(scriptClass, binding).run();
        } catch (Exception e) {
            LOGGER.error("Execute script ERROR!\r\nScript: {}", scriptContent, e);
            throw new GroovyScriptException(e);
        }
    }

    private  <T> T genericExecute(Object scriptContent, Class<T> clz, Map<String, Object> scriptContext) {
        Object result = genericExecute(scriptContent, scriptContext);
        return clz.cast(result);
    }

    /**
     * 将脚本编译成类并缓存，相同脚本不重新编译
     * 脚本类型目前支持字符串和文件
     * 字符串脚本以内容 md5 为 key 进行缓存
     * 文件脚本以文件路径及最后修改时间为 key
     *
     * @param  obj 脚本内容
     * @return 解析后的脚本类
     */
    private Class getScriptClass(Object obj) throws IOException {
        if (!(obj instanceof String) && !(obj instanceof File)) {
            throw new GroovyScriptException("Not supported content type: " + obj.getClass());
        }

        String key;
        if (obj instanceof String) {
            key = Md5.md5Hex((String) obj);
        } else {
            key = ((File) obj).getAbsolutePath() + ((File) obj).lastModified();
        }

        if (classCache.containsKey(key)) {
            LOGGER.debug("Found key [{}] from cache, use cached Script object.", key);
            return classCache.get(key);
        }

        GroovyCodeSource gcs;
        if (obj instanceof String) {
            gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () ->
                    new GroovyCodeSource((String) obj, key, GroovyShell.DEFAULT_CODE_BASE));
        } else {
            gcs = new GroovyCodeSource((File) obj, configuration.getSourceEncoding());
        }
        Class clz = loader.parseClass(gcs, false);
        classCache.put(key, clz);
        return clz;
    }

}
