package com.spingcould.es8;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Administrator
 */

@EnableFeignClients(basePackages = "com.springcolud.feign.api")
@MapperScan("com.spingcould.es8.mapper")
@SpringBootApplication
public class Es8ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Es8ServiceApplication.class, args);
    }

}
