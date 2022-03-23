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
package com.airsoftware.saas.datasource.provider;

import javax.sql.DataSource;

/**
 * 数据源提供者，由开发者自行实现
 *
 * @author bit
 */
public interface SaaSDataSourceProvider {
    
    /**
     * 创建数据源
     *
     * @param dsKey 数据源标识，即在SaaSDataSource上下文、Request Session或Header中的设置值，开发者可利用此值来查询并返回自己想要提供和切换的数据源。
     * @return 数据源
     */
    DataSource createDataSource(String dsKey);
}
