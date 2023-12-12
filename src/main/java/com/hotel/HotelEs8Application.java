package com.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Administrator
 */
@MapperScan("com.hotel.mapper")
@SpringBootApplication
public class HotelEs8Application {

    public static void main(String[] args) {
        SpringApplication.run(HotelEs8Application.class, args);
    }

}
