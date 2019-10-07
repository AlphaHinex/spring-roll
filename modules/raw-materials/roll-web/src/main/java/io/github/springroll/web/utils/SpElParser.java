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

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class SpElParser {

    @Autowired
    WebApplicationContext wac;

    private ExpressionParser parser;
    private StandardEvaluationContext context;
    private ParserContext parserContext;

    @PostConstruct
    public void init() {
        parser = new SpelExpressionParser();
        context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(wac));
        parserContext = new TemplateParserContext();
    }

    public String parse(String spEL) throws ExpressionException {
        return parse(spEL, null, true);
    }

    public String parse(String spEL, Map<String, Object> vars, boolean isExpTpl) throws ExpressionException {
        return parse(spEL, vars, isExpTpl, String.class);
    }

    public <T> T parse(String spEL, Map<String, Object> vars, Class<T> clz) throws ExpressionException {
        return parse(spEL, vars, false, clz);
    }

    public <T> T parse(String spEL, Map<String, Object> vars, boolean isExpTpl, Class<T> clz) throws ExpressionException {
        // 不使用 Expression template 时，过滤掉表达式中的单行注释内容
        spEL = isExpTpl ? spEL : spEL.replaceAll("//.*", "");
        if (vars != null) {
            context.setVariables(vars);
        }
        Expression expression = isExpTpl ? parser.parseExpression(spEL, parserContext) : parser.parseExpression(spEL);
        return expression.getValue(context, clz);
    }

}
