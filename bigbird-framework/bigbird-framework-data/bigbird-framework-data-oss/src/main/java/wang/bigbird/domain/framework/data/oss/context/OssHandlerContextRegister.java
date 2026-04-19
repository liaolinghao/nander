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
package wang.bigbird.domain.framework.data.oss.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;
import wang.bigbird.domain.framework.data.oss.support.handler.IOssHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象存储处理器注册室
 *
 * @author Bigbird
 */
@Component
public class OssHandlerContextRegister implements ApplicationContextAware, InitializingBean {

    /**
     * 用于保存对象存储处理器类型与对象存储处理器的映射关系
     */
    private Map<OssTypeEnum, IOssHandler> ossHandlerMap = null;

    private ApplicationContext applicationContext;

    /**
     * 获取对象存储处理器
     *
     * @param ossType 对象存储处理器类型
     * @return 对象存储处理器
     */
    public IOssHandler getOssHandler(OssTypeEnum ossType) {
        if (ossHandlerMap == null) {
            return null;
        }
        return ossHandlerMap.get(ossType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, IOssHandler> bhm = applicationContext.getBeansOfType(IOssHandler.class);
        Collection<IOssHandler> ossHandlers = bhm.values();
        ossHandlerMap = new HashMap<>(CollectionUtils.initialMapCapacity(ossHandlers.size()));
        for (IOssHandler ossHandler : ossHandlers) {
            ossHandlerMap.put(ossHandler.getOssType(), ossHandler);
        }
    }

}
