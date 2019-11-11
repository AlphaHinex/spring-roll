package io.github.springroll.web.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

@Component
public class SpElParser {

    private transient ExpressionParser parser;
    private transient StandardEvaluationContext context;
    private transient ParserContext parserContext;

    public SpElParser(@Autowired WebApplicationContext wac) {
        parser = new SpelExpressionParser();
        context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(wac));
        parserContext = new TemplateParserContext();
    }

    public String parse(String spEl) throws ExpressionException {
        return parse(spEl, null, true);
    }

    public String parse(String spEl, Map<String, Object> vars, boolean isExpTpl) throws ExpressionException {
        return parse(spEl, vars, isExpTpl, String.class);
    }

    public <T> T parse(String spEl, Map<String, Object> vars, Class<T> clz) throws ExpressionException {
        return parse(spEl, vars, false, clz);
    }

    public <T> T parse(String spEl, Map<String, Object> vars, boolean isExpTpl, Class<T> clz) throws ExpressionException {
        // 不使用 Expression template 时，过滤掉表达式中的单行注释内容
        String el = isExpTpl ? spEl : spEl.replaceAll("//.*", "");
        if (vars != null) {
            context.setVariables(vars);
        }
        Expression expression = isExpTpl ? parser.parseExpression(el, parserContext) : parser.parseExpression(el);
        return expression.getValue(context, clz);
    }

}
