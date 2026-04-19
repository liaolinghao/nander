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
package wang.bigbird.domain.framework.server.web.auth.service.base;

import org.springframework.security.core.Authentication;

/**
 * 非标准JWT解析服务
 *
 * @author Bigbird
 */
public interface INonStandardJwtParserService {

    /**
     * 解析token获取认证对象
     * <p>
     * 该方法用于解析外部系统颁发的token获取认证对象
     *
     * @param token 外部token
     * @return 认证对象
     */
    Authentication getAuthentication(String token);

}
