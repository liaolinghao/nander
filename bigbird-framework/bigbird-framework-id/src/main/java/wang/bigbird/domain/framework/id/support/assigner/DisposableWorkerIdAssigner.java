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
/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wang.bigbird.domain.framework.id.support.assigner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import wang.bigbird.domain.framework.core.base.util.NetUtils;
import wang.bigbird.domain.framework.id.base.enums.WorkerNodeTypeEnum;
import wang.bigbird.domain.framework.id.base.util.DockerUtils;
import wang.bigbird.domain.framework.id.dao.WorkerNodeMapper;
import wang.bigbird.domain.framework.id.domain.entity.WorkerNode;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author Bigbird
 */
@Slf4j
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {

    @Autowired
    private WorkerNodeMapper workerNodeMapper;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long assignWorkerId() {
        WorkerNode workerNode = buildWorkerNode();
        // add worker node for new (ignore the same IP + PORT)
        workerNodeMapper.insert(workerNode);
        log.info("Add worker node: {}.", workerNode);
        return workerNode.getId();
    }

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNode buildWorkerNode() {
        WorkerNode workerNode = new WorkerNode();
        if (DockerUtils.isDocker()) {
            workerNode.setNodeType(WorkerNodeTypeEnum.CONTAINER.value());
            workerNode.setHostName(DockerUtils.getDockerHost());
            workerNode.setPort(DockerUtils.getDockerPort());
        } else {
            workerNode.setNodeType(WorkerNodeTypeEnum.ACTUAL.value());
            workerNode.setHostName(NetUtils.getIp());
            workerNode.setPort(String.valueOf(NetUtils.getAvailablePort()));
        }
        return workerNode;
    }

}
