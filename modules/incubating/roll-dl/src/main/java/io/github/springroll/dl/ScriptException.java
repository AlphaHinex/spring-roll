package io.github.springroll.dl;

import io.github.springroll.base.Version;

/**
 * 运行时脚本异常
 * 包括非法脚本内容、缺失属性/方法、编译异常等
 * 通过此异常将上述异常信息包裹，并在 GroovyScriptExecution 中统一抛出此异常
 */
public class ScriptException extends RuntimeException {

    static final long serialVersionUID = Version.HASH;

    ScriptException(Throwable throwable) {
        super(throwable);
    }

}
