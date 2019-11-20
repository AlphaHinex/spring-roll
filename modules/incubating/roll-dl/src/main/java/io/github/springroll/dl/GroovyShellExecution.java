package io.github.springroll.dl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.github.springroll.base.CharacterEncoding;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroovyShellExecution {

    private transient GroovyShell shell;

    @Autowired
    public GroovyShellExecution(Binding groovyShellBinding) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setSourceEncoding(CharacterEncoding.getCharset().name());
        shell = new GroovyShell(null, groovyShellBinding, configuration);
    }

    public Object execute(String scriptContent) {
        try {
            return shell.evaluate(scriptContent);
        } catch (RuntimeException e) {
            throw new GroovyScriptException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String scriptContent, Class<T> clz) {
        Object result = execute(scriptContent);
        return (T) result;
    }

}
