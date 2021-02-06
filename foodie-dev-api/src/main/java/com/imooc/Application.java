package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages ="com.imooc.mapper" )
//扫描组件包以及相关组件包
@ComponentScan(basePackages ={"com.imooc","org.n3r.idworker"} )
@EnableScheduling   //开启定时任务
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
