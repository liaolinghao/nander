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
package wang.bigbird.domain.framework.id.support.assigner;

import org.springframework.beans.factory.annotation.Autowired;
import wang.bigbird.domain.framework.core.base.tool.SystemClock;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisService;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSortedSetService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis编号分配器，相对于ZK编号分配器，判断活跃节点存在一定延迟
 *
 * @author Bigbird
 */
public class RedisWorkerIdAssigner extends AbstractWorkerIdAssigner {

    /**
     * redis上uid机器节点的key前缀
     */
    public static final String UID_ROOT = "bigbird:uid:";

    /**
     * uid机器节点列表
     */
    public static final String UID_FOREVER = UID_ROOT.concat("forever");

    /**
     * uid活跃节点心跳列表（用于保存活跃节点及活跃心跳）
     */
    public static final String UID_TEMPORARY = UID_ROOT.concat("temporary:");

    @Autowired
    private IRedisService redisService;
    @Autowired
    private IRedisSortedSetService redisSortedSetService;

    public RedisWorkerIdAssigner(Long interval, String pidHome, Integer pidPort) {
        super(interval, pidHome, pidPort);
    }

    @Override
    protected void initEnv() {

    }

    @Override
    protected Long loadWorkerIdFromMiddleware() {
        // 获取所有uid机器节点列表
        List<String> uidWorkers = redisSortedSetService.zrange(UID_FOREVER, 0, -1, String.class);
        Long i = 0L;
        for (String uidWorker : uidWorkers) {
            if (uidWorker.equals(pidName)) {
                // 节点顺序编号作为worker id
                return i;
            }
            i++;
        }
        return null;
    }

    @Override
    protected Long assignWorkerIdByMiddleware() {
        int size = redisSortedSetService.zcard(UID_FOREVER);
        // 使用zset时间排序，保证有序性
        redisSortedSetService.zadd(UID_FOREVER, SystemClock.now(), pidName);
        return (long) size;
    }

    @Override
    protected long lastTime() {
        Long lastTime = redisService.get(pidName, Long.class);
        return lastTime == null ? 0 : lastTime;
    }

    @Override
    protected long averageTime() {
        active.set(true);
        // 获取所有uid机器节点列表
        List<String> uidWorkers = redisSortedSetService.zrange(UID_FOREVER, 0, -1, String.class);
        Long sumTime = 0L;
        Long activeNodes = 0L;
        if (CollectionUtils.isNotEmpty(uidWorkers)) {
            for (String pidName : uidWorkers) {
                String itemTime = redisService.get(UID_TEMPORARY + pidName);
                if (StringUtils.isNotBlank(itemTime)) {
                    sumTime += Long.valueOf(itemTime);
                    activeNodes++;
                }
            }
            if (activeNodes > 0) {
                return sumTime / activeNodes;
            }
        }
        return 0;
    }

    @Override
    protected void registerNode() {
        // redis中没有临时节点一说，所以活跃节点的注册依靠每个节点上传的临时时间判断
        redisService.set(UID_TEMPORARY + pidName, SystemClock.now(), interval * 2, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void report() {
        long value = SystemClock.now();
        redisService.set(pidName, value);
        redisService.set(UID_TEMPORARY + pidName, value, interval * 2, TimeUnit.MILLISECONDS);
    }
}
