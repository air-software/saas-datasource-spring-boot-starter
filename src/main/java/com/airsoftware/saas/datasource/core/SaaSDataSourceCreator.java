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

import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;

import javax.sql.DataSource;

/**
 * 数据源创建者
 *
 * @author bit
 */

public class SaaSDataSourceCreator {
    
    private DynamicDataSourceProperties properties;
    
    public SaaSDataSourceCreator(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 创建DRUID数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createDruidDataSource(DataSourceProperty dataSourceProperty) {
        DruidDataSourceCreator druidDataSourceCreator = new DruidDataSourceCreator(properties.getDruid());
        dataSourceProperty.setDruid(properties.getDruid());
        return druidDataSourceCreator.createDataSource(dataSourceProperty);
    }
}
