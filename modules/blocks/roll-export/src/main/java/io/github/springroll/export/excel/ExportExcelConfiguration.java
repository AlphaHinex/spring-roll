package io.github.springroll.export.excel;

import io.github.springroll.export.excel.handler.DecodeHandler;
import io.github.springroll.export.excel.handler.PaginationHandler;
import io.github.springroll.web.ApplicationContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class ExportExcelConfiguration {

    @Bean
    public Collection<PaginationHandler> paginationHandlers() {
        return ApplicationContextHolder.getApplicationContext().getBeansOfType(PaginationHandler.class).values();
    }

    @Bean
    public Collection<DecodeHandler> decodeHandlers() {
        return ApplicationContextHolder.getApplicationContext().getBeansOfType(DecodeHandler.class).values();
    }

}
