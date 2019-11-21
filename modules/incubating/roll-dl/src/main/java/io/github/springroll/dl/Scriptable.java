package io.github.springroll.dl;

/**
 * 标记接口，用来筛选放入 {@code GroovyShellBindingConfiguration.groovyShellBinding} 的 bean
 * 仅实现了此接口的 bean 可以被 {@code GroovyShellExecution} 调用
 */
public interface Scriptable {

}
