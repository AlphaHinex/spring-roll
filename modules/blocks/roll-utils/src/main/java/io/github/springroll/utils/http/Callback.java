package io.github.springroll.utils.http;

import okhttp3.Response;

import java.io.IOException;

public interface Callback {

    /**
     * 请求成功
     * @param response response
     */
    void onSuccess(Response response);

    /**
     * 请求失败
     * @param ioe ioe
     */
    void onError(IOException ioe);

}
