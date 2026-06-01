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
package wang.bigbird.domain.framework.common.similarity.exception;

/**
 * 封装的相似度计算异常
 *
 * @author Bigbird
 */
public class SimilarityException extends RuntimeException {

    public SimilarityException() {
        super();
    }

    public SimilarityException(String message) {
        super(message);
    }

    public SimilarityException(Throwable cause) {
        super(cause);
    }

    public SimilarityException(String message, Throwable cause) {
        super(message, cause);
    }

}
