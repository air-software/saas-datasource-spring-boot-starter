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
package com.airsoftware.saas.datasource.context;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * SaaS数据源池工具
 *
 * @author bit
 */
public final class SaaSDataSourcePool {
    
    @Setter
    private static DynamicRoutingDataSource dynamicRoutingDataSource;
    
    /**
     * 获取所有的数据源
     *
     * @return 当前所有数据源
     */
    public static Map<String, DataSource> getAll() {
        return dynamicRoutingDataSource.getDataSources();
    }
    
    /**
     * 获取数据源
     *
     * @param dsKey 数据源Key
     * @return 数据源
     */
    public static DataSource get(String dsKey) {
        return dynamicRoutingDataSource.getDataSource(dsKey);
    }
    
    public static DataSource get(Long dsKey) {
        return get(String.valueOf(dsKey));
    }
    
    public static DataSource get(Integer dsKey) {
        return get(String.valueOf(dsKey));
    }
    
    /**
     * 添加数据源
     *
     * @param dsKey      数据源Key
     * @param dataSource 数据源
     */
    public static void add(String dsKey, DataSource dataSource) {
        dynamicRoutingDataSource.addDataSource(dsKey, dataSource);
    }
    
    /**
     * 删除数据源
     *
     * @param dsKey 数据源Key
     */
    public static void remove(String dsKey) {
        dynamicRoutingDataSource.removeDataSource(dsKey);
    }
    
    public static void remove(Long dsKey) {
        remove(String.valueOf(dsKey));
    }
    
    public static void remove(Integer dsKey) {
        remove(String.valueOf(dsKey));
    }
    
    /**
     * 清空池内所有数据源
     */
    public static void removeAll() throws Exception {
        dynamicRoutingDataSource.destroy();
    }
    
}
