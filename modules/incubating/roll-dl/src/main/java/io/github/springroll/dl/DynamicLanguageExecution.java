package io.github.springroll.dl;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class DynamicLanguageExecution {

    public static Object execute(String scriptContent) {
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(scriptContent);
        return script.run();
    }

}
