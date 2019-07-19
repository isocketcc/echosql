package com.echo.echosql;

import com.echo.echosql.common.backend.factory.BackendConnectFactory;
import com.echo.echosql.common.fronted.factory.FrontendConnectFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
//自动加载配置信息
@Configuration
//包的路径下带有@Value的注解自动注入
@SpringBootApplication
public class EchosqlApplication{
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = SpringApplication.run(EchosqlApplication.class,args);
        FrontendConnectFactory frontendConnectFactory = context.getBean(FrontendConnectFactory.class);
        frontendConnectFactory.run();


//        BackendConnectFactory backendConnection  =context.getBean(BackendConnectFactory.class);
//        backendConnection.start();
    }
}