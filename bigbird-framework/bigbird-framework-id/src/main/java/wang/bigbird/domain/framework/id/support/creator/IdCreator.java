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
package wang.bigbird.domain.framework.id.support.creator;

import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.id.support.generator.IdGenerator;

/**
 * id策略上下文
 *
 * @author Bigbird
 */
public class IdCreator {

    /**
     * 默认业务标识
     */
    private static final String DEFAULT_TAG = "bigbird";

    /**
     * ID生成器
     */
    private IdGenerator idGenerator;

    /**
     * 基因因子
     */
    private Long factor = 472535999L;

    /**
     * 是否启用基因编码
     */
    private boolean enableGeneCoding = true;

    public IdCreator(IdGenerator idGenerator, Long factor, boolean enableGeneCoding) {
        this.idGenerator = idGenerator;
        if (factor != null) {
            this.factor = factor;
        }
        this.enableGeneCoding = enableGeneCoding;
    }

    /**
     * 获取默认业务提供的唯一ID
     *
     * @return 唯一ID
     */
    public long getUid() {
        return getUid(DEFAULT_TAG);
    }

    /**
     * 获取指定业务提供的唯一ID
     *
     * @param bizTag 指定业务
     * @return 唯一ID
     */
    public long getUid(String bizTag) {
        return idGenerator.getUid(bizTag);
    }

    /**
     * 解析默认业务提供的唯一ID的组成结构
     *
     * @param uid 唯一ID
     * @return 输出json字符串：{\"UID\":\"\",\"timestamp\":\"\",\"workerId\":\"\",\"dataCenterId\":\"\",\"sequence\":\"\"}
     */
    public String parseUid(long uid) {
        return parseUid(uid, DEFAULT_TAG);
    }

    /**
     * 解析指定业务提供的唯一ID的组成结构
     *
     * @param uid    唯一ID
     * @param bizTag 指定业务
     * @return 输出json字符串：{\"UID\":\"\",\"timestamp\":\"\",\"workerId\":\"\",\"dataCenterId\":\"\",\"sequence\":\"\"}
     */
    public String parseUid(long uid, String bizTag) {
        return idGenerator.parseUid(uid, bizTag);
    }

    /**
     * 根据基因因子生成基因id
     *
     * @param primitiveId 原始ID
     * @return 采用基因法对原始ID混淆后的ID
     */
    public long geneId(long primitiveId) {
        if (enableGeneCoding) {
            return DataUtils.geneId(primitiveId, factor);
        }
        return primitiveId;
    }

    /**
     * 基因id还原为原始ID
     *
     * @param uid 基因混淆后的ID
     * @return 基因反编译后还原的原始ID值
     */
    public long restoreId(long uid) {
        if (enableGeneCoding) {
            return DataUtils.restoreId(uid, factor);
        }
        return uid;
    }

}
