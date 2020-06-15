package com.spring.boot.batch.configuration.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MybatisConfiguration {
	
	private final ApplicationContext ctx;

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setMapperLocations(ctx.getResources("classpath:/mybatis/**/*.xml"));
		return factoryBean.getObject();
	}
	
	@Bean 
	public SqlSessionTemplate sessionTemplate(SqlSessionFactory factory) {
		return new SqlSessionTemplate(factory);
	}
	
}
