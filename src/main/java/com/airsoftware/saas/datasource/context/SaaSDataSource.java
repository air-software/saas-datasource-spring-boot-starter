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

/**
 * SaaS数据源切换上下文
 * 用于手动切换数据源，可在同一调用流程内多次切换数据源，注意在开启了事务的方法内切换无效。
 *
 * @author bit
 */
public class SaaSDataSource {
    
    private static ThreadLocal<String> HOLDER = ThreadLocal.withInitial(() -> "");
    
    /**
     * 获取当前数据源标识
     */
    public static String current() {
        return HOLDER.get();
    }
    
    /**
     * 切换至对应数据源
     * @param dsKey 数据源标识
     */
    public static void switchTo(String dsKey) {
        HOLDER.set(dsKey);
    }
    
    /**
     * 切换至对应数据源
     * @param dsKey 数据源标识，兼容Long型ID
     */
    public static void switchTo(Long dsKey) {
        HOLDER.set(String.valueOf(dsKey));
    }
    
    /**
     * 清空线程
     */
    public static void clear() {
        HOLDER.remove();
    }
    
}
