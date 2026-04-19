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
package wang.bigbird.domain.framework.server.web.auth.domain.pojo.user;

import lombok.Data;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.JwtAuthData;

import java.util.List;

/**
 * 用户认证权限数据
 *
 * @author Bigbird
 */
@Data
public class JwtUser extends JwtAuthData {

    public static final String ACCOUNT = "account";

    public static final String MOBILEPHONE = "mobilephone";

    public static final String EMAIL = "email";

    public static final String NICKNAME = "nickname";

    public static final String NAME = "name";

    public static final String SECURITY_PHONE = "securityPhone";

    public JwtUser() {
        setType(JwtAuthData.USER);
    }

    /**
     * 用户角色集合
     */
    private List<JwtRole> roles;

    /**
     * 用户权限集合
     */
    private List<JwtPermission> permissions;

    /**
     * 用户所属组织集合
     */
    private List<JwtOrg> orgs;

}
