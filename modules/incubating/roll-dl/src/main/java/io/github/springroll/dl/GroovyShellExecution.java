package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
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

    private transient Map<String, Scriptable> shellContext;
    private transient Map<String, Class> classCache = new ConcurrentHashMap<>();

    @Autowired
    public GroovyShellExecution(Map<String, Scriptable> groovyShellContext) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setSourceEncoding(CharacterEncoding.getCharset().name());

        shell = new GroovyShell(null, configuration);
    }

    public Object execute(String scriptContent) {
        return genericExecute(scriptContent);
    }

    public <T> T execute(String scriptContent, Class<T> clz) {
        return genericExecute(scriptContent, clz);
    }

    public Object execute(File file) {
        return genericExecute(file);
    }

    private Object genericExecute(Object scriptContent) {
        if (scriptContent == null) {
            throw new GroovyScriptException("Script content SHOULD NOT null!");
        }
        try {
            Binding binding = new Binding();

            Class scriptClass = getScriptClass(scriptContent);
            InvokerHelper.createScript(scriptClass, );
            Script script = getScriptObject(scriptContent);
            return script.run();
        } catch (Exception e) {
            LOGGER.error("Execute script ERROR!\r\nScript: {}", scriptContent, e);
            throw new GroovyScriptException(e);
        }
    }

    private  <T> T genericExecute(Object scriptContent, Class<T> clz) {
        Object result = genericExecute(scriptContent);
        return clz.cast(result);
    }

    /**
     * 避免每次都进行脚本文件的编译及加载，
     * 根据脚本内容做 md5，并以此为 key，缓存 Script 对象
     *
     * @param  obj 脚本内容
     * @return 解析后的脚本对象
     */
    private Script getScriptObject(Object obj) throws IOException {
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
        Script script;
        if (obj instanceof String) {
            script = shell.parse((String) obj);
        } else {
            script = shell.parse((File) obj);
        }
        classCache.put(key, script);
        return script;
    }

}
