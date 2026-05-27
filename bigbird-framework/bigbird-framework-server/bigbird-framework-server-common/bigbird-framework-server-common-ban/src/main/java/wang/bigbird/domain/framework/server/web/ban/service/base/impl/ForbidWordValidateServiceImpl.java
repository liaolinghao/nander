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
package wang.bigbird.domain.framework.server.web.ban.service.base.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.common.forbidden.service.base.IForbidWordService;
import wang.bigbird.domain.framework.server.web.ban.service.base.IForbidWordValidateService;

import java.util.List;

/**
 * 禁用词校验服务
 *
 * @author Bigbird
 */
@Service
public class ForbidWordValidateServiceImpl implements IForbidWordValidateService {

    @Autowired
    private IForbidWordService forbidWordService;

    @Override
    public boolean containsForbidWord(String value) {
        return forbidWordService.include(value);
    }

    @Override
    public List<String> forbidWordList(String value) {
        return forbidWordService.forbidWordList(value);
    }

}
