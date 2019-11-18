package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.github.springroll.base.CharacterEncoding;
import org.codehaus.groovy.control.CompilerConfiguration;

public class GroovyScriptExecution {

    private GroovyScriptExecution() { }

    public static Object execute(String scriptContent) {
        try {
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setSourceEncoding(CharacterEncoding.getCharset().name());
            GroovyShell shell = new GroovyShell(null, new Binding(), configuration);
            Script script = shell.parse(scriptContent);
            return script.run();
        } catch (RuntimeException e) {
            throw new ScriptException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T execute(String scriptContent, Class<T> clz) {
        Object result = execute(scriptContent);
        return (T) result;
    }

}
