package io.github.springroll.test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.lang.reflect.Modifier
import java.util.concurrent.BlockingQueue

@RunWith(SpringRunner.class)
@SpringBootTest
abstract class AbstractSpringTest {

    @Autowired
    protected WebApplicationContext wac

    @Autowired
    protected MockHttpServletRequest mockRequest

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor

    protected MockMvc mockMvc

    private def mockUser

    @Before
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build()
        mockUser = null
    }

    /**
     * 在模拟请求中，模拟出一个用户
     * @param id        用户 id
     * @param username  用户名
     * @param password  密码
     * @param isSuper   是否超级用户
     */
    protected void mockUser(String id='id', String username='uname', String password='pwd', boolean isSuper=false) {
        mockUser = [id: id, username: username, password: password, isSuper: isSuper]
        mockRequest.setAttribute('mockUser', mockUser)
    }

    protected MvcResult post(String url, String data, HttpStatus statusCode) {
        return post(url, MediaType.APPLICATION_JSON_UTF8, null, data, statusCode)
    }

    protected MvcResult post(String url, MediaType produces, String data, HttpStatus statusCode) {
        return post(url, MediaType.APPLICATION_JSON_UTF8, produces, data, statusCode)
    }

    protected MvcResult post(String url, MediaType consumes, MediaType produces, String data, HttpStatus statusCode) {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(url)
        if (consumes != null) {
            req = req.contentType(consumes)
        }
        if (produces != null) {
            req = req.accept(produces)
        }
        req.content(data)
        return perform(req, statusCode)
    }

    protected MvcResult get(String url, HttpStatus statusCode) {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(url)
        return perform(req, statusCode)
    }

    protected MvcResult put(String url, String data, HttpStatus statusCode) {
        return put(url, MediaType.APPLICATION_JSON_UTF8, null, data, statusCode)
    }

    protected MvcResult put(String url, MediaType produces, String data, HttpStatus statusCode) {
        return put(url, MediaType.APPLICATION_JSON_UTF8, produces, data, statusCode)
    }

    protected MvcResult put(String url, MediaType consumes, MediaType produces, String data, HttpStatus statusCode) {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.put(url)
        if (consumes != null) {
            req = req.contentType(consumes)
        }
        if (produces != null) {
            req = req.accept(produces)
        }
        req.content(data)
        return perform(req, statusCode)
    }

    protected MvcResult delete(String url, HttpStatus statusCode) {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.delete(url)
        return perform(req, statusCode)
    }

    protected MvcResult options(String url, HttpStatus statusCode) {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.options(url)
        return perform(req, statusCode)
    }

    /**
     * 模拟请求，并响应状态码
     * 可用于模拟 RESTFul 请求
     *
     * @param req           模拟请求构造器
     * @param statusCode    期望的响应状态
     * @return
     */
    private MvcResult perform(MockHttpServletRequestBuilder req, HttpStatus statusCode) {
        if (mockUser != null) {
            req.requestAttr('mockUser', mockUser)
        }
        for (String header : mockRequest.getHeaderNames()) {
            req.header(header, mockRequest.getHeader(header))
        }
        return mockMvc
            .perform(req)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is(statusCode.value()))
            .andReturn()
    }

    protected static void coverBean(Object bean) {
        bean.getClass().getDeclaredMethods().each { method ->
            println method
            if (shouldCover(method.modifiers)) {
                def params = []
                method.getParameterTypes().each { type ->
                    params << (type.isPrimitive() ? 0 : (type.isInterface() ? [] : type.newInstance()))
                }
                params.size() == 0 ? method.invoke(bean) :
                    (params.size() == 1 ? method.invoke(bean, params[0]) : method.invoke(bean, params))
            }
        }
    }

    private static boolean shouldCover(int modifiers) {
        !Modifier.isPrivate(modifiers) && !Modifier.isAbstract(modifiers)
    }

    protected def resOfGet(uri, status) {
        def str = get(uri, status).response.contentAsString
        try {
            str = new JsonSlurper().parseText(str)
        } catch (ignored) { }
        str
    }

    protected def resOfPost(uri, entity) {
        resOfPost(uri, JsonOutput.toJson(entity), HttpStatus.CREATED)
    }

    protected def resOfPost(uri, data, status) {
        def str = post(uri, data, status).response.contentAsString
        try {
            str = new JsonSlurper().parseText(str)
        } catch (ignored) { }
        str
    }

    protected def resOfPut(uri, data, status) {
        def str = put(uri, data, status).response.contentAsString
        try {
            str = new JsonSlurper().parseText(str)
        } catch (ignored) { }
        str
    }

    protected def waitExecutorDone() {
        BlockingQueue<Runnable> queue = threadPoolTaskExecutor.getThreadPoolExecutor().getQueue()
        while (queue != null && !queue.isEmpty()) {
            println "sleep 5 milliseconds to wait, current blocking queue is ${queue.size()}"
            sleep(5)
        }
        while (threadPoolTaskExecutor.activeCount > 0) {
            println("sleep 5 milliseconds to wait, current active count is ${threadPoolTaskExecutor.activeCount}")
            sleep(5)
        }
    }

    /**
     * 按照名称覆盖单例 bean
     *
     * @param beanName          bean 名称
     * @param singletonObject   单例对象
     */
    protected void overrideSingleton(String beanName, Object singletonObject) {
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) wac.getAutowireCapableBeanFactory()
        bf.destroySingleton(beanName)
        bf.registerSingleton(beanName, singletonObject)
    }

}
