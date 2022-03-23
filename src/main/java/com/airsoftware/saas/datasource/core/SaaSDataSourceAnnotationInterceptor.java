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

import com.airsoftware.saas.datasource.annotation.SaaS;
import com.airsoftware.saas.datasource.context.SaaSDataSource;
import com.airsoftware.saas.datasource.provider.SaaSDataSourceProvider;
import com.airsoftware.saas.datasource.util.StringUtil;
import com.baomidou.dynamic.datasource.DynamicDataSourceClassResolver;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spel.DynamicDataSourceSpelParser;
import com.baomidou.dynamic.datasource.spel.DynamicDataSourceSpelResolver;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * SaaS数据源AOP核心拦截器
 *
 * @author bit
 */
@Slf4j
public class SaaSDataSourceAnnotationInterceptor implements MethodInterceptor {
    
    @Setter
    private SaaSDataSourceProvider dynamicDataSourceProvider;
    
    @Setter
    private DynamicRoutingDataSource dynamicRoutingDataSource;
    
    @Setter
    private DynamicDataSourceSpelResolver dynamicDataSourceSpelResolver;
    
    @Setter
    private DynamicDataSourceSpelParser dynamicDataSourceSpelParser;
    
    private DynamicDataSourceClassResolver dynamicDataSourceClassResolver = new DynamicDataSourceClassResolver();
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            String dsKeyField = getDsKeyField(invocation);
            String dsKey = "";
            
            // Request优先级：Session > Header
            if (RequestContextHolder.getRequestAttributes() != null) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String sessionValue = (String) request.getSession().getAttribute(dsKeyField);
                dsKey = StringUtil.isNotBlank(sessionValue) ? sessionValue : request.getHeader(dsKeyField);
            }
            
            // 手动设置数据源优先级最高
            String currentContext = SaaSDataSource.current();
            if (StringUtil.isNotBlank(currentContext)) {
                dsKey = currentContext;
            }
            
            if (StringUtil.isNotBlank(dsKey)) {
                // 初始化该key对应的数据源
                initDataSource(dsKey);
                // 切换上下文
                DynamicDataSourceContextHolder.setDataSourceLookupKey(dsKey);
            }
            return invocation.proceed();
        } catch (Exception e) {
            log.error("An exception occurred during the invocation of @SaaS, data source will switch to default.", e);
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceLookupKey();
        }
    }
    
    /**
     * 获取数据源key的字段名
     *
     * @param invocation
     * @return 数据源key的字段名
     * @throws Throwable
     */
    private String getDsKeyField(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> declaringClass = dynamicDataSourceClassResolver.targetClass(invocation);
        SaaS saas = method.isAnnotationPresent(SaaS.class) ? method.getAnnotation(SaaS.class)
                : AnnotationUtils.findAnnotation(declaringClass, SaaS.class);
        Assert.notNull(saas, "Can not find @SaaS annotation, please ensure that you put the @SaaS annotation in right place.");
        return saas.value();
    }
    
    /**
     * 根据key初始化数据源
     *
     * @param dsKey
     */
    private void initDataSource(String dsKey) {
        Map<String, DataSource> dsMap = dynamicRoutingDataSource.getCurrentDataSources();
        // 如果已被缓存则直接返回
        if (dsMap != null && dsMap.containsKey(dsKey)) {
            return;
        }
        
        // 由开发者自行实现此接口来提供数据源
        DataSource ds = dynamicDataSourceProvider.createDataSource(dsKey);
        dynamicRoutingDataSource.addDataSource(dsKey, ds);
    }
}
