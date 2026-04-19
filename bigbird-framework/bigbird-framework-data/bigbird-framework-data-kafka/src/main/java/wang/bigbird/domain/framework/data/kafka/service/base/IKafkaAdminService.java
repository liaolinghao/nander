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
package wang.bigbird.domain.framework.data.kafka.service.base;

import java.util.concurrent.ExecutionException;

/**
 * kafka管理者服务
 *
 * @author Bigbird
 */
public interface IKafkaAdminService {


    /**
     * 创建主体，如果kafka开启了自动创建topic配置，
     * 那么，当生产者向一个不存在的topic发送数据时，会自动创建一个分区数为1，副本数为1的topic，
     * 但这样会影响topic的管理和维护，因此建议采用该方法，在发送数据前，主动创建topic
     *
     * @param name              主题名称
     * @param numPartitions     分区数
     * @param replicationFactor 副本数
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void createTopic(String name, int numPartitions, short replicationFactor) throws ExecutionException, InterruptedException;

    /**
     * 获取指定主题的消息数量
     *
     * @param name 主题名称
     * @return 消息数量
     * @throws ExecutionException
     * @throws InterruptedException
     */
    long countTopicMessages(String name) throws ExecutionException, InterruptedException;

}
