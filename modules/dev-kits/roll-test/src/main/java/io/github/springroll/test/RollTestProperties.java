package io.github.springroll.test;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roll.test")
@Data
public class RollTestProperties {

    private Datasource datasource;

    @Data
    public static class Datasource {

        /**
         * 默认为空，当前可使用值为 mysql2h2，设置后会自动将扫描到的 MySQL 脚本转化为 H2 脚本，并执行到 H2 库中
         */
        private String type;

        /**
         * 扫描 MySQL 脚本文件的默认路径
         */
        private String mysqlScripts = "classpath*:sql/**/*.sql";

        /**
         * 是否忽略通过 MySQL 脚本转化过来的 H2 脚本执行时的错误
         */
        private boolean ignoreErrors = true;

    }

}
