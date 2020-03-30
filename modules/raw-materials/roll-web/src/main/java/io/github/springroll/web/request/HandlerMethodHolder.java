package io.github.springroll.web.request;

import io.github.springroll.web.ApplicationContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;

@Service
public class HandlerMethodHolder {

    public Optional<HandlerMethod> getHandlerMethod(HttpServletRequest request) throws Exception {
        Optional<HandlerExecutionChain> handlerExecutionChain = getHandler(request);
        return handlerExecutionChain.map(executionChain -> (HandlerMethod) executionChain.getHandler());
    }

    /**
     * Borrow from DispatcherServlet's getHandler method
     * Return the HandlerExecutionChain for this request.
     * <p>Tries all handler mappings in order.
     *
     * @param  request current HTTP request
     * @return the HandlerExecutionChain, or {@code null} if no handler could be found
     * @throws Exception if there is an internal error
     */
    private Optional<HandlerExecutionChain> getHandler(HttpServletRequest request) throws Exception {
        Collection<RequestMappingHandlerMapping> handlerMappings = ApplicationContextHolder.getApplicationContext().getBeansOfType(RequestMappingHandlerMapping.class).values();
        if (CollectionUtils.isNotEmpty(handlerMappings)) {
            for (HandlerMapping mapping : handlerMappings) {
                HandlerExecutionChain handler = mapping.getHandler(request);
                if (handler != null) {
                    return Optional.of(handler);
                }
            }
        }
        return Optional.empty();
    }

}
