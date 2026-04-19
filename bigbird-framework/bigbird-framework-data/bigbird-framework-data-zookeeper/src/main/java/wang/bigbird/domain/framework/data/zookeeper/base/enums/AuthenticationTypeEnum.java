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
package wang.bigbird.domain.framework.data.zookeeper.base.enums;

/**
 * 认证策略，ZK认证是设置在节点上的，为节点添加认证的方法为：
 * 1、创建认证用户：addauth digest gzsj:gzsj@ZK2025
 * 2、创建节点，此时，节点无需认证权限
 * 3、为节点配置认证权限，setAcl /bigbird-framework auth:gzsj:gzsj@ZK2025:crwda
 * <p>
 * 备注：CREATE、READ、WRITE、DELETE、ADMIN。也就是：增、删、改、查、管理权限，这5种权限简写为crwda，
 * 这5种权限中，delete是指对子节点的删除权限，其它4种权限指对自身节点的操作权限。
 *
 * @author Bigbird
 */
public enum AuthenticationTypeEnum {

    /**
     * 默认方式，所有客户端都拥有指定的权限，相当于全世界都能访问。
     * world 下只有一个 id 选项，就是 anyone，通常组合写法为 world:anyone:[permissons]
     */
    world,
    /**
     * 用户名:明文密码认证方式，只有经过认证的用户才拥有指定的权限。
     * 通常组合写法为 auth:user:password:[permissons]，
     * 使用这种模式时，你需要先进行登录，之后采用 auth 模式设置权限时，
     * user 和 password 都将使用登录的用户名和密码
     */
    auth,
    /**
     * 用户名:密文密码认证方式，只有经过认证的用户才拥有指定的权限。
     * 通常组合写法为 auth:user:BASE64(SHA1(password)):[permissons]，
     * 这种形式下的密码必须通过 SHA1 和 BASE64 进行双重加密
     */
    digest,
    /**
     * IP地址认证，限制只有特定 IP 的客户端才拥有指定的权限。
     * 通常组成写法为 ip:182.168.0.168:[permissions]
     */
    ip;

}
