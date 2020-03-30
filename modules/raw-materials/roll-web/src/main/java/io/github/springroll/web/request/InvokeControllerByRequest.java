package io.github.springroll.web.request;

import io.github.springroll.web.ApplicationContextHolder;
import io.github.springroll.web.exception.ErrMsgException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
public class InvokeControllerByRequest {

    private transient HandlerMethodHolder handlerMethodHolder;

    @Autowired
    public InvokeControllerByRequest(HandlerMethodHolder handlerMethodHolder) {
        this.handlerMethodHolder = handlerMethodHolder;
    }

    /**
     * Invoke controller method according to input request.
     * Could build a request with ArtificialHttpServletRequest
     *
     * @param  request http servlet request
     * @return output of controller method or null when could not mapping a controller
     * @throws Exception exception will be thrown when getHandler or invokeForRequest
     */
    public Object invoke(HttpServletRequest request) throws Exception {
        // Find the handler method by request
        Optional<HandlerMethod> handlerMethod = handlerMethodHolder.getHandlerMethod(request);
        if (!handlerMethod.isPresent()) {
            throw new ErrMsgException("Could NOT find handler method for request " + request.getRequestURI());
        }
        InvocableHandlerMethod invocableHandlerMethod = new InvocableHandlerMethod(handlerMethod.get());

        // Set resolvers
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
        RequestMappingHandlerAdapter handlerAdapter = getHandlerAdapter(invocableHandlerMethod);
        composite.addResolvers(handlerAdapter.getArgumentResolvers());
        invocableHandlerMethod.setHandlerMethodArgumentResolvers(composite);

        // Set data binder factory
        invocableHandlerMethod.setDataBinderFactory(
                new ServletRequestDataBinderFactory(new ArrayList<>(), new ConfigurableWebBindingInitializer()));

        NativeWebRequest nativeWebRequest = new DispatcherServletWebRequest(request);
        return invocableHandlerMethod.invokeForRequest(nativeWebRequest, new ModelAndViewContainer());
    }

    private RequestMappingHandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        Collection<RequestMappingHandlerAdapter> handlerAdapters =
                ApplicationContextHolder.getBeansOfType(RequestMappingHandlerAdapter.class).values();
        if (CollectionUtils.isNotEmpty(handlerAdapters)) {
            for (RequestMappingHandlerAdapter adapter : handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

}
