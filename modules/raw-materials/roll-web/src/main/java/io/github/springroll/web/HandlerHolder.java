package io.github.springroll.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Service
public class HandlerHolder {

    private transient RequestMappingHandlerMapping mapping;

    @Autowired
    public HandlerHolder(RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    public HandlerMethod getHandler(HttpServletRequest request) throws Exception {
        HandlerExecutionChain handlerExecutionChain = mapping.getHandler(request);
        return handlerExecutionChain == null ? null : (HandlerMethod) handlerExecutionChain.getHandler();
    }

}
