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
package wang.bigbird.domain.framework.server.web.core.service.base;

/**
 * 密码校验服务
 *
 * @author Bigbird
 */
public interface IPwdValidateService {

    /**
     * 判断是否弱密码
     *
     * @param pwd 密码
     * @return 是否弱密码
     */
    boolean isWeakPwd(String pwd);

    /**
     * 是否历史密码
     *
     * @param ownerId 密码拥有者标识
     * @param pwd     密码
     * @return 是否历史密码
     */
    boolean isHistoryPwd(String ownerId, String pwd);

}
