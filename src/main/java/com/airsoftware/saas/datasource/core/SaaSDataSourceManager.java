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

import com.airsoftware.saas.datasource.provider.SaaSDataSourceProvider;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * SaaS数据源管理器
 *
 * @author bit
 */
public class SaaSDataSourceManager {
    
    @Setter
    private SaaSDataSourceProvider saasDataSourceProvider;
    
    @Setter
    private DynamicRoutingDataSource dynamicRoutingDataSource;
    
    /**
     * 根据key添加数据源
     *
     * @param dsKey 数据源标识
     */
    public void addDataSource(String dsKey) {
        Map<String, DataSource> dsMap = dynamicRoutingDataSource.getDataSources();
        // 如果已被缓存则直接返回
        if (dsMap != null && dsMap.containsKey(dsKey)) {
            return;
        }
        
        // 由开发者自行实现此接口来提供数据源
        DataSource ds = saasDataSourceProvider.createDataSource(dsKey);
        dynamicRoutingDataSource.addDataSource(dsKey, ds);
    }
}
