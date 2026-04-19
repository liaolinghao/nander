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
package wang.bigbird.domain.framework.server.web.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.SystemUtils;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 供外部调用的服务健康状态接口
 *
 * @author Bigbird
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 正常状态
     */
    public static final String HEALTH_STATUS_NORMAL = "0";


    @GetMapping(value = "/check")
    public RespResult<Map<String, Object>> check() {
        Map<String, Object> healthStatus = new HashMap(CollectionUtils.initialMapCapacity(4));
        // 磁盘使用信息
        healthStatus.put("disk", SystemUtils.disk());
        // 内存使用信息
        healthStatus.put("memory", SystemUtils.memory());
        // CPU使用信息
        healthStatus.put("cpu", SystemUtils.cpu());
        // TODO 健康检查如果考虑完善的话，需要考虑容器是否成功初始化，业务是否正常，数据库访问是否正常等各种情况，需要继续完善
        healthStatus.put("status", HEALTH_STATUS_NORMAL);
        return RespResult.ok(healthStatus);
    }
}
