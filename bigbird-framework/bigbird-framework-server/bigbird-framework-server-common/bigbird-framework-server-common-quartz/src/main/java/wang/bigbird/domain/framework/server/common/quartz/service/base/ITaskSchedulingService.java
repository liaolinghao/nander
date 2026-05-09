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
package wang.bigbird.domain.framework.server.common.quartz.service.base;

import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.Map;

/**
 * 任务调度服务
 *
 * @author Bigbird
 */
public interface ITaskSchedulingService {

    /**
     * 添加定时任务
     *
     * @param taskName       作业名称
     * @param groupName      组名称
     * @param className      作业类
     * @param jobMap         作业环境参数
     * @param triggerMap     触发器环境参数
     * @param cronExpression 定时运行表达式
     * @throws SchedulerException
     */
    void addTask(String taskName, String groupName,
                 Class className, Map<?, ?> jobMap, Map<?, ?> triggerMap, String cronExpression) throws SchedulerException;

    /**
     * 暂停定时任务
     *
     * @param taskName  作业名称
     * @param groupName 组名称
     * @throws SchedulerException
     */
    void pauseTask(String taskName, String groupName) throws SchedulerException;

    /**
     * 恢复定时任务
     *
     * @param taskName  作业名称
     * @param groupName 组名称
     * @throws SchedulerException
     */
    void resumeTask(String taskName, String groupName) throws SchedulerException, ParseException;

    /**
     * 删除定时任务
     *
     * @param taskName  作业名称
     * @param groupName 组名称
     * @throws SchedulerException
     */
    void deleteTask(String taskName, String groupName) throws SchedulerException;

    /**
     * 修改任务的调度时间
     *
     * @param taskName       作业名称
     * @param groupName      组名称
     * @param cronExpression 定时运行表达式
     * @throws SchedulerException
     */
    void rescheduleTask(String taskName, String groupName, String cronExpression) throws SchedulerException;

    /**
     * 调度定时任务
     *
     * @param taskName       作业名称
     * @param groupName      组名称
     * @param className      作业类
     * @param jobMap         作业环境参数
     * @param triggerMap     触发器环境参数
     * @param cronExpression 定时运行表达式
     * @throws SchedulerException
     */
    void scheduleTask(String taskName, String groupName,
                 Class className, Map<?, ?> jobMap, Map<?, ?> triggerMap, String cronExpression) throws SchedulerException;

    /**
     * 关闭任务调度器
     *
     * @throws SchedulerException
     */
    void shutDownScheduler() throws SchedulerException;

}
