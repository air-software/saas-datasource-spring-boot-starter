/*
 * Copyright 2018-2022 AIR Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airsoftware.saas.datasource.core;

import com.airsoftware.saas.datasource.context.SaaSDataSource;
import com.airsoftware.saas.datasource.provider.SaaSDataSourceProvider;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDatasourceAopProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 自动配置
 *
 * @author bit
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureAfter(DynamicDataSourceAutoConfiguration.class)
@Import(DruidDynamicDataSourceConfiguration.class)
public class SaaSDataSourceAutoConfiguration {
	
	@Resource
    private DataSource dynamicRoutingDataSource;
	
	@Lazy
	@Resource
	private SaaSDataSourceProvider saasDataSourceProvider;
	
	@Bean
    @ConditionalOnMissingBean
    public SaaSDataSourceAnnotationAdvisor saasDataSourceAnnotationAdvisor(DynamicDataSourceProperties properties) {
	    // 设置拦截器
        SaaSDataSourceAnnotationInterceptor interceptor = new SaaSDataSourceAnnotationInterceptor();
        SaaSDataSourceAnnotationAdvisor advisor = new SaaSDataSourceAnnotationAdvisor(interceptor);
        // 设置数据源管理器
        SaaSDataSourceManager manager = new SaaSDataSourceManager();
        manager.setSaasDataSourceProvider(saasDataSourceProvider);
        manager.setDynamicRoutingDataSource((DynamicRoutingDataSource)dynamicRoutingDataSource);
        // 为拦截器设置数据源管理器
        interceptor.setManager(manager);
        // 为手动切换工具设置数据源管理器
        SaaSDataSource.setManager(manager);
        
        DynamicDatasourceAopProperties aopProperties = properties.getAop();
        advisor.setOrder(aopProperties.getOrder());
        return advisor;
    }
	
	@Bean
	@ConditionalOnMissingBean
	public SaaSDataSourceCreator saasDataSourceCreator(DynamicDataSourceProperties properties){
		return new SaaSDataSourceCreator(properties);
	}
}
