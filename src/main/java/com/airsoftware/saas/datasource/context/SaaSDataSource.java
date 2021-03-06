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

import com.airsoftware.saas.datasource.core.SaaSDataSourceManager;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.Setter;
import org.springframework.core.NamedThreadLocal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SaaS数据源手动切换工具<br/>
 * 用于手动切换数据源，可在同一调用流程内多次切换数据源，注意在开启了事务的方法内切换无效且可能导致异常。
 *
 * @author bit
 */
public final class SaaSDataSource {
    
    @Setter
    private static SaaSDataSourceManager manager;
    
    /**
     * 使用栈存储手动切换的数据源<br/>
     * 在一次调用流程中，如果启用了手动切换，则后续只会以手动切换为准，除非显式调用clearCurrent或clearAll。<br/>
     * 如果一次调用流程中多次设置手动切换，则clearCurrent会移除栈顶数据，clearAll会清空整个栈。
     */
    private static final ThreadLocal<Deque<String>> DS_KEY_HOLDER = new NamedThreadLocal<Deque<String>>("saas-datasource") {
        @Override
        protected Deque<String> initialValue() {
            return new ArrayDeque<>();
        }
    };
    
    /**
     * 获取当前栈顶的数据源标识
     */
    public static String current() {
        return DS_KEY_HOLDER.get().peek();
    }
    
    /**
     * 切换至对应数据源
     *
     * @param dsKey 数据源标识
     */
    public static void switchTo(String dsKey) {
        set(dsKey);
    }
    
    /**
     * 切换至对应数据源
     *
     * @param dsKey 数据源标识，兼容Long型ID
     */
    public static void switchTo(Long dsKey) {
        set(String.valueOf(dsKey));
    }
    
    /**
     * 切换至对应数据源
     *
     * @param dsKey 数据源标识，兼容Integer型ID
     */
    public static void switchTo(Integer dsKey) {
        set(String.valueOf(dsKey));
    }
    
    /**
     * 添加数据源，并手动设置上下文
     *
     * @param dsKey
     */
    private static void set(String dsKey) {
        // 设置时即添加数据源
        manager.addDataSource(dsKey);
        // 入栈
        DS_KEY_HOLDER.get().push(dsKey);
        // 设置DynamicDataSource上下文，实际数据库操作时以此为准，其内部也是一个栈
        DynamicDataSourceContextHolder.push(dsKey);
    }
    
    /**
     * 移除当前数据源<br/>
     * 如果当前线程是连续手动切换数据源，只会移除掉当前栈顶生效的数据源。
     */
    public static void clearCurrent() {
        Deque<String> deque = DS_KEY_HOLDER.get();
        deque.poll();
        if (deque.isEmpty()) {
            DS_KEY_HOLDER.remove();
        }
        DynamicDataSourceContextHolder.poll();
    }
    
    /**
     * 清空所有手动设置的数据源<br/>
     * 如果当前线程是连续手动切换数据源，则会清空整个栈。
     */
    public static void clearAll() {
        Deque<String> deque = DS_KEY_HOLDER.get();
        // 根据本工具当前栈的深度，将DynamicDataSource上下文中手动设置进去的数据源依次出栈
        int size = deque.size();
        for (int i = 0; i < size; i++) {
            DynamicDataSourceContextHolder.poll();
        }
        // 清空本工具的栈，并移除线程
        deque.clear();
        DS_KEY_HOLDER.remove();
    }
    
    /**
     * 强制移除所有数据源，包含DynamicDataSource上下文中的数据源。
     */
    public static void removeAll() {
        Deque<String> deque = DS_KEY_HOLDER.get();
        // 清空本工具的栈，并移除线程
        deque.clear();
        DS_KEY_HOLDER.remove();
        // 移除DynamicDataSource上下文
        DynamicDataSourceContextHolder.clear();
    }
    
}
