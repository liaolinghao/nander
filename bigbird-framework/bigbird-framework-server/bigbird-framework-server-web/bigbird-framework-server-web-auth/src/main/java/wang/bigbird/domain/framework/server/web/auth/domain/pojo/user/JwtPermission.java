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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.Serializable;

/**
 * 用户权限
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtPermission implements Serializable {

    private Long id;

    private String name;

    /**
     * 权限表达式，
     * 可以为具体的某一项权限，比如：admin:analysis:sales
     * 也可以为某一类权限，比如：admin:analysis:* 或者 admin:*
     */
    private String pattern;

    /**
     * 解析权限表达式是否匹配某一项权限，匹配规则如下：
     * 如果权限表达式为"*"，标识任意权限，直接返回匹配。
     * 如果权限表达式为"admin:*"，标识admin下的任意权限
     * 如果权限表达式为"admin:analysis:*"，标识admin下的analysis下的任意权限
     * 以此类推
     *
     * @param permission 具体的某一项权限
     * @return 是否匹配
     */
    public boolean match(String permission) {
        if (StringUtils.isBlank(permission) || StringUtils.isBlank(pattern)) {
            return false;
        }
        if (CommonConstants.ANY.equals(pattern)) {
            return true;
        }
        String[] ps = pattern.split(CommonConstants.COLON);
        String[] ps2 = permission.split(CommonConstants.COLON);
        if (ps.length > ps2.length) {
            return false;
        }
        for (int i = 0; i < ps.length; i++) {
            if (!CommonConstants.ANY.equals(ps[i]) && !ps[i].equals(ps2[i])) {
                return false;
            }
        }
        return true;
    }
}
