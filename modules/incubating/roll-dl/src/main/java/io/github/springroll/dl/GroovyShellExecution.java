package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import io.github.springroll.base.CharacterEncoding;
import io.github.springroll.utils.StringUtil;
import io.github.springroll.utils.digest.Md5;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Groovy 脚本的运行环境
 *
 * 本类设计为线程安全，每次执行脚本时，会创建一个新的 {@code Binding} 对象作为脚本执行时的变量绑定。
 * 本类的一个主要使用场景是校验，通过动态脚本或类，使校验规则能够灵活定义并动态加载
 * 校验时应尽量使用读操作，避免写操作，以免造成预期之外的影响
 */
@Slf4j
@Component
public class GroovyShellExecution {

    /**
     * 按照"与"的方式，组合脚本执行结果
     * 单个脚本执行结果需为布尔型
     */
    public static final int AND = 1;
    /**
     * 按照"或"的方式，组合脚本执行结果
     * 单个脚本执行结果需为布尔型
     */
    public static final int OR = 0;

    private transient GroovyClassLoader loader;
    private transient Map<String, Scriptable> applicationContext;
    private transient CompilerConfiguration configuration = new CompilerConfiguration();
    private transient Map<String, Class> classCache = new ConcurrentHashMap<>();

    @Autowired
    public GroovyShellExecution(Map<String, Scriptable> groovyShellApplicationContext) {
        this.applicationContext = groovyShellApplicationContext;
        configuration.setSourceEncoding(CharacterEncoding.getCharset().name());
        loader = AccessController.doPrivileged((PrivilegedAction<GroovyClassLoader>) () ->
                new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), configuration));
    }

    public Object execute(String scriptContent) {
        return genericExecute(scriptContent, null);
    }

    public Object execute(String scriptContent, Map<String, Object> scriptContext) {
        return genericExecute(scriptContent, scriptContext);
    }

    public <T> T execute(String scriptContent, Map<String, Object> scriptContext, Class<T> clz) {
        return genericExecute(scriptContent, scriptContext, clz);
    }

    public <T> T execute(String scriptContent, Class<T> clz) {
        return genericExecute(scriptContent, null, clz);
    }

    public Object execute(File file) {
        return genericExecute(file, null);
    }

    public boolean execute(String[] scripts, Map<String, Object> scriptContext, int op) {
        Stream<String> stream = buildScriptsStream(scripts);
        return AND == op
                ? stream.allMatch(script -> execute(script, scriptContext, Boolean.class))
                : stream.anyMatch(script -> execute(script, scriptContext, Boolean.class));
    }

    private Stream<String> buildScriptsStream(String... scripts) {
        if (scripts == null) {
            throw new GroovyScriptException("Script content SHOULD NOT null!");
        }
        return Arrays.stream(scripts).parallel().filter(StringUtil::isNotBlank);
    }

    /**
     * 针对数据集合并行执行脚本集合 scripts
     * 对脚本执行结果集合取并集，并返回
     *
     * 注意，使用此方法的前提条件为脚本运算的结果是一个集合
     *
     * @param  scripts       脚本集合
     * @param  scriptContext 包含数据集合的上下文
     * @return 脚本执行结果集合的并集
     */
    public List executeParallel(String[] scripts, Map<String, Object> scriptContext) {
        return buildScriptsStream(scripts)
                .map(script -> execute(script, scriptContext, Collection.class))
                .flatMap(Collection::parallelStream)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
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
            log.error("Execute script ERROR!\r\nScript: {}", scriptContent, e);
            throw new GroovyScriptException(e);
        }
    }

    private  <T> T genericExecute(Object scriptContent, Map<String, Object> scriptContext, Class<T> clz) {
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
        String key;
        String val;
        if (obj instanceof String) {
            val = (String) obj;
            key = Md5.md5Hex(val);
        } else {
            key = ((File) obj).getAbsolutePath() + ((File) obj).lastModified();
            val = "File";
        }

        if (classCache.containsKey(key)) {
            log.trace("Found key [{}] with value [{}] from cache, use cached Script object.", key, val);
            return classCache.get(key);
        }
        log.debug("Could NOT find key [{}] with value [{}] from cache, create a new one.", key, val);

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
