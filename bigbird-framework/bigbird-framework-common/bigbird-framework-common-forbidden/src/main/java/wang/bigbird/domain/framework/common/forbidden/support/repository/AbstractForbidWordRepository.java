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
package wang.bigbird.domain.framework.common.forbidden.support.repository;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.forbidden.support.core.Dfa;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * 读取禁用词，维护DFA数据结构为禁用词检测提供服务
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractForbidWordRepository {

    protected Dfa dfa;

    private static final ThreadFactory NAMED_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNamePrefix("dfa-refresh-thread-").build();
    private static final ExecutorService THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(1024), NAMED_THREAD_FACTORY, new ThreadPoolExecutor.AbortPolicy());

    /**
     * 传入一个空的DFA实例
     *
     * @param dfa DFA实例
     */
    protected AbstractForbidWordRepository(Dfa dfa) {
        this.dfa = dfa;
    }

    /**
     * 获取DFA实例
     *
     * @return
     */
    public Dfa getDfa() {
        return dfa;
    }

    /**
     * 加载禁用词
     *
     * @return 禁用词迭代器
     */
    protected abstract Iterator<String> loadForbidWords();

    /**
     * 重新加载禁用词
     *
     * @param replace 是否重新构造一个DFA并替换当前的DFA
     */
    public void refresh(boolean replace) {
        if (replace) {
            dfa.clear();
            THREAD_POOL_EXECUTOR.execute(() -> {
                log.info("Asynchronous update forbidden words...");
                dfa.addWord(loadForbidWords());
            });
        } else {
            log.info("Synchronous update forbidden words...");
            dfa.addWord(loadForbidWords());
        }
    }

    /**
     * 增加禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
     *
     * @param words 禁用词列表
     * @return 是否成功
     */
    public boolean addForbidWord(String... words) {
        dfa.addWord(Arrays.asList(words).iterator());
        return true;
    }

    /**
     * 删除禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
     *
     * @param words 禁用词列表
     * @return 是否成功
     */
    public boolean removeForbidWord(String... words) {
        dfa.removeWord(Arrays.asList(words).iterator());
        return true;
    }

}
