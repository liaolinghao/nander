/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.id.service.base;

/**
 * PidName加载器
 * 常规的依赖中间件获取WorkerId的分配器只适合采用本地宿主机模式部署或者将容器网络设置为host，
 * 否则每当容器重启，网络IP就会自动发生变化，导致频繁重启后，WorkerId值超过31导致服务启动失败。
 * 本加载器即是为解决上述问题而设计，通过分配固定数量的PidName保障WorkerId数值固定并且多实例间不重复。
 *
 * @author Bigbird
 */
public interface IPidNameLoaderService {

    /**
     * 获取PidName
     *
     * @return 应用密钥
     */
    String loadPidName();

}
