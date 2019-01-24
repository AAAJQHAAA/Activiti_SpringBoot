package com.example.demo;

import java.util.HashSet;
import java.util.Set;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.demo.config.MyProcessEngineConfiguration;
import com.example.demo.dao.MycustomDao3;



@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DemoExcel1Application {
	public static void main(String[] args) {
		SpringApplication.run(DemoExcel1Application.class, args);
	}
//	@Bean
//	public MyProcessEngineConfiguration addMyProcessEngineConfiguration() {
//		MyProcessEngineConfiguration pecfg=new MyProcessEngineConfiguration();
//		pecfg.setJdbcDriver("com.mysql.cj.jdbc.Driver");
//		pecfg.setJdbcUrl("jdbc:mysql://localhost:3306/at3?useSSL=true&serverTimezone=GMT%2B8");
//		pecfg.setJdbcUsername("root");
//		pecfg.setJdbcPassword("root");
//		
//		Set<Class<?>> mappers=new HashSet<>();
//		mappers.add(MycustomDao3.class);
//		pecfg.setCustomMybatisMappers(mappers);
//		
//		Set<String> xmlMappers=new HashSet<>();
//		xmlMappers.add("mybatis/MyMapper2.xml");
//		pecfg.setCustomMybatisXMLMappers(xmlMappers);
//		return pecfg;
//	}
}

