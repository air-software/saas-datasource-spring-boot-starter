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
import com.airsoftware.saas.datasource.util.StringUtil;
import com.baomidou.dynamic.datasource.support.DataSourceClassResolver;
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
import java.lang.reflect.Method;

/**
 * SaaS数据源AOP核心拦截器
 *
 * @author bit
 */
@Slf4j
public class SaaSDataSourceAnnotationInterceptor implements MethodInterceptor {
    
    @Setter
    private SaaSDataSourceManager manager;
    
    private DataSourceClassResolver dataSourceClassResolver = new DataSourceClassResolver();
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String dsKey = "";
        boolean requestAddDataSource = false;
        try {
            String dsKeyField = getDsKeyField(invocation);
            
            // Request优先级：Session > Header
            if (RequestContextHolder.getRequestAttributes() != null) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String sessionValue = (String) request.getSession().getAttribute(dsKeyField);
                dsKey = StringUtil.isNotBlank(sessionValue) ? sessionValue : request.getHeader(dsKeyField);
            }
            
            // SaaSDataSource手动设置数据源优先级最高，因此如果SaaSDataSource当前栈顶有值，则忽略Request设置值，否则按Request设置值切换上下文
            String currentContext = SaaSDataSource.current();
            if (StringUtil.isBlank(currentContext) && StringUtil.isNotBlank(dsKey)) {
                // 添加该key对应的数据源
                manager.addDataSource(dsKey);
                // 添加成功则设置此标识为true，用于在finally中判断是否需要清理
                requestAddDataSource = true;
                // 切换上下文
                DynamicDataSourceContextHolder.push(dsKey);
            }
            
            return invocation.proceed();
        } catch (Exception e) {
            log.error("An exception occurred during the invocation of @SaaS, the JDBC Connection will switch to latest active or default data source.", e);
            return invocation.proceed();
        } finally {
            // 如果Request模式切添加数据源成功，则需要做最后的清理
            if (requestAddDataSource) {
                DynamicDataSourceContextHolder.poll();
            }
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
        Class<?> declaringClass = dataSourceClassResolver.targetClass(invocation);
        SaaS saas = method.isAnnotationPresent(SaaS.class) ? method.getAnnotation(SaaS.class)
                : AnnotationUtils.findAnnotation(declaringClass, SaaS.class);
        Assert.notNull(saas, "Can not find @SaaS annotation, please ensure that you put the @SaaS annotation in right place.");
        return saas.value();
    }
    
}
