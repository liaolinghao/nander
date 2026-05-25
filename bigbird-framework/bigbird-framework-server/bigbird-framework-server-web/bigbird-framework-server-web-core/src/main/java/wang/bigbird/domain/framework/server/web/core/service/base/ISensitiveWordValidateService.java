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

import java.util.List;

/**
 * 敏感词校验服务
 *
 * @author Bigbird
 */
public interface ISensitiveWordValidateService {

    /**
     * 判断文本是否包含敏感词
     *
     * @param value 文本
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String value);

    /**
     * 文本包含的禁用词列表
     *
     * @param value 文本
     * @return 禁用词列表
     */
    List<String> forbidWordList(String value);

}
