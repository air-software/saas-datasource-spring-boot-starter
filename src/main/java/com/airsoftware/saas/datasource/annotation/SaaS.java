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
package com.airsoftware.saas.datasource.annotation;

import java.lang.annotation.*;

/**
 * 标记需要开启SaaS模式的类或方法，也可以标记在父类上。<br/>
 * <br/>
 * 注意事项：<br/>
 * 1、由于事务内部无法切换数据源，因此如果需要使用事务，则应至少在最外层事务或更上一层的调用方标记此注解，即保证注解在事务开启前发挥作用，以切换到正确的数据源；<br/>
 * 2、本工具不提供分布式事务的实现，也未做过相关测试，如果需要分布式事务请开发者自行实现和测试，理论上本工具兼容分布式事务。<br/>
 *
 * @author bit
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SaaS {
    
    /**
     * 租户标识字段名称，即Request Session或Header中对应的字段名称。<br/>
     * 如果使用 {@link com.airsoftware.saas.datasource.context.SaaSDataSource} 来手动切换数据源，则此值会被忽略。
     */
    String value();
    
}
