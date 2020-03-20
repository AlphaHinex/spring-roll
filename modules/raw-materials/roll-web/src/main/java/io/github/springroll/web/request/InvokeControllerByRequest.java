package io.github.springroll.web.request;

import io.github.springroll.web.ApplicationContextHolder;
import io.github.springroll.web.HandlerHolder;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Component
public class InvokeControllerByRequest {

    private transient HandlerHolder handlerHolder;

    @Autowired
    public InvokeControllerByRequest(HandlerHolder handlerHolder) {
        this.handlerHolder = handlerHolder;
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
        HandlerMethod handlerMethod = handlerHolder.getHandler(request);
        InvocableHandlerMethod invocableHandlerMethod = new InvocableHandlerMethod(handlerMethod);

        // Set resolvers
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
        RequestMappingHandlerAdapter handlerAdapter = ApplicationContextHolder.getBean(RequestMappingHandlerAdapter.class);
        composite.addResolvers(handlerAdapter.getArgumentResolvers());
        invocableHandlerMethod.setHandlerMethodArgumentResolvers(composite);

        // Set data binder factory
        invocableHandlerMethod.setDataBinderFactory(
                new ServletRequestDataBinderFactory(new ArrayList<>(), new ConfigurableWebBindingInitializer()));

        NativeWebRequest nativeWebRequest = new DispatcherServletWebRequest(request);
        return invocableHandlerMethod.invokeForRequest(nativeWebRequest, new ModelAndViewContainer());
    }

}
