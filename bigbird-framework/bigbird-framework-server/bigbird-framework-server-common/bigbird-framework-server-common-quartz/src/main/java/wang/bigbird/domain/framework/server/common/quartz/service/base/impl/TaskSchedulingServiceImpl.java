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
package wang.bigbird.domain.framework.server.common.quartz.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.server.common.quartz.service.base.ITaskSchedulingService;

import java.text.ParseException;
import java.util.Map;

/**
 * 任务调度服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class TaskSchedulingServiceImpl implements ITaskSchedulingService {

    @Autowired
    private Scheduler scheduler;

    @Override
    public void addTask(String taskName, String groupName,
                        Class className, Map<?, ?> jobMap, Map<?, ?> triggerMap, String cronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression specification '" + cronExpression + "'");
        }
        JobKey jobKey = JobKey.jobKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(jobKey);
        if (isExisted) {
            return;
        }
        JobBuilder jobBuilder = JobBuilder.newJob(className).withIdentity(taskName, groupName).storeDurably(true);
        if (CollectionUtils.isNotEmpty(jobMap)) {
            jobBuilder.usingJobData(new JobDataMap(jobMap));
        }
        JobDetail jobDetail = jobBuilder.build();
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger().withIdentity(taskName, groupName);
        if (CollectionUtils.isNotEmpty(triggerMap)) {
            triggerBuilder.usingJobData(new JobDataMap(triggerMap));
        }
        CronTrigger trigger = (CronTrigger) triggerBuilder
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Override
    public void pauseTask(String taskName, String groupName) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(triggerKey);
        if (isExisted) {
            scheduler.pauseTrigger(triggerKey);
        }
    }

    @Override
    public void resumeTask(String taskName, String groupName) throws SchedulerException, ParseException {
        TriggerKey triggerKey = TriggerKey.triggerKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(triggerKey);
        if (isExisted) {
            Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
            if (state == Trigger.TriggerState.PAUSED) {
                scheduler.resumeTrigger(triggerKey);
            }
        }
    }

    @Override
    public void deleteTask(String taskName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(jobKey);
        if (isExisted) {
            scheduler.deleteJob(jobKey);
        }
    }

    @Override
    public void rescheduleTask(String taskName, String groupName, String cronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression specification '" + cronExpression + "'");
        }
        JobKey jobKey = JobKey.jobKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(jobKey);
        if (!isExisted) {
            throw new IllegalArgumentException("Invalid taskName-groupName specification '" + taskName + "-" + groupName + "'");
        }
        TriggerKey triggerKey = TriggerKey.triggerKey(taskName, groupName);
        isExisted = scheduler.checkExists(triggerKey);
        if (!isExisted) {
            throw new IllegalArgumentException("Invalid taskName-groupName specification '" + taskName + "-" + groupName + "'");
        }
        JobDataMap jobDataMap = scheduler.getTrigger(triggerKey).getJobDataMap();
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger().withIdentity(taskName, groupName).forJob(jobKey);
        if (CollectionUtils.isNotEmpty(jobDataMap)) {
            triggerBuilder.usingJobData(jobDataMap);
        }
        CronTrigger trigger = (CronTrigger) triggerBuilder
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
                .build();
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    @Override
    public void scheduleTask(String taskName, String groupName, Class className, Map<?, ?> jobMap, Map<?, ?> triggerMap, String cronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression specification '" + cronExpression + "'");
        }
        JobKey jobKey = JobKey.jobKey(taskName, groupName);
        boolean isExisted = scheduler.checkExists(jobKey);
        if (isExisted) {
            rescheduleTask(taskName, groupName, cronExpression);
        } else {
            addTask(taskName, groupName, className, jobMap, triggerMap, cronExpression);
        }
    }

    @Override
    public void shutDownScheduler() throws SchedulerException {
        scheduler.shutdown(true);
    }

}
