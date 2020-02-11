package com.proper.enterprise.platform.swagger;

import org.apache.commons.collections.ListUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.*;

@Configuration
@EnableSwagger2WebMvc
public class ApplicationSwaggerConfiguration implements WebMvcConfigurer {

    @Bean
    public List<String> ignorePatternsListSwagger() {
        List<String> list = new ArrayList<>(4);
        list.add("*:/swagger-ui.html");
        list.add("*:/webjars/**");
        list.add("*:/swagger-resources/**");
        list.add("*:/v2/api-docs/**");
        // Ignore all for directly invoke from swagger ui
        list.add("*:/**");
        return list;
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Docket docket() {
        final List<ResponseMessage> globalResponse = Arrays.asList(
            new ResponseMessageBuilder().code(400).message("Bad Request").build(),
            new ResponseMessageBuilder().code(401).message("Unauthorized").build(),
            new ResponseMessageBuilder().code(403).message("Forbidden").build(),
            new ResponseMessageBuilder().code(500).message("Internal Error").build()
        );

        final List<ResponseMessage> postResponse = Arrays.asList(
            new ResponseMessageBuilder().code(201).message("Created").build()
        );

        final List<ResponseMessage> deleteResponse = Arrays.asList(
            new ResponseMessageBuilder().code(204).message("No content").build(),
            new ResponseMessageBuilder().code(404).message("Not Found").build()
        );

        final List<ResponseMessage> normalResponse = Arrays.asList(
            new ResponseMessageBuilder().code(200).message("OK").build()
        );

        final Set<String> cap = new HashSet<>(1);
        cap.add(MediaType.APPLICATION_JSON_UTF8_VALUE);

        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .globalResponseMessage(RequestMethod.POST, ListUtils.union(postResponse, globalResponse))
            .globalResponseMessage(RequestMethod.GET, ListUtils.union(normalResponse, globalResponse))
            .globalResponseMessage(RequestMethod.DELETE, ListUtils.union(deleteResponse, globalResponse))
            .globalResponseMessage(RequestMethod.PUT, ListUtils.union(normalResponse, globalResponse))
            .consumes(cap).produces(cap);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
