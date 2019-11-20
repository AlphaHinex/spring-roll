package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.github.springroll.base.CharacterEncoding;
import io.github.springroll.utils.digest.Md5;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private transient GroovyShell shell;

    private transient Map<String, Script> scriptMap = new ConcurrentHashMap<>();

    @Autowired
    public GroovyShellExecution(Binding groovyShellBinding) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setSourceEncoding(CharacterEncoding.getCharset().name());
        shell = new GroovyShell(null, groovyShellBinding, configuration);
    }

    public Object execute(String scriptContent) {
        if (scriptContent == null) {
            throw new GroovyScriptException("Script content should not null!");
        }
        try {
            Script script = getScriptObject(scriptContent);
            return script.run();
        } catch (RuntimeException e) {
            LOGGER.error("Execute script ERROR!\r\nScript: {}", scriptContent, e);
            throw new GroovyScriptException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String scriptContent, Class<T> clz) {
        Object result = execute(scriptContent);
        return (T) result;
    }

    private Script getScriptObject(String scriptContent) {
        String md5 = Md5.md5Hex(scriptContent);
        if (scriptMap.containsKey(md5)) {
            return scriptMap.get(md5);
        }
        Script script = shell.parse(scriptContent);
        scriptMap.put(md5, script);
        return script;
    }

}
