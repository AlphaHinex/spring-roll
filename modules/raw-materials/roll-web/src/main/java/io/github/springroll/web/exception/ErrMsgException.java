package io.github.springroll.web.exception;

import io.github.springroll.Version;

/**
 * 用于只关心错误消息，不关心异常堆栈等信息的场景
 * 例如，controller 中需要返回一个异常响应，
 * 可以抛出此异常，然后由 BaseController 的 handleException 来进行异常响应的封装
 */
public class ErrMsgException extends RuntimeException {

    public static final long serialVersionUID = Version.HASH;

    public ErrMsgException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

}
