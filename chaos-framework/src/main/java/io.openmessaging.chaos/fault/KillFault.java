/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.openmessaging.chaos.fault;

import io.openmessaging.chaos.ChaosControl;
import io.openmessaging.chaos.driver.MQChaosNode;
import io.openmessaging.chaos.generator.FaultOperation;
import io.openmessaging.chaos.generator.KillFaultGenerator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fault which kill the process on particular node
 */
public class KillFault implements Fault {

    private volatile List<FaultOperation> faultOperations;

    private Map<String, MQChaosNode> nodesMap;

    private KillFaultGenerator killFaultGenerator;

    private String mode;

    private static final Logger logger = LoggerFactory.getLogger(ChaosControl.class);

    public KillFault(Map<String, MQChaosNode> nodesMap, String mode) {
        this.nodesMap = nodesMap;
        this.mode = mode;
        this.killFaultGenerator = new KillFaultGenerator(nodesMap.keySet(), mode);
    }

    @Override public synchronized void invoke() {
        logger.info("Invoke {} fault....", mode);
        faultOperations = killFaultGenerator.generate();
        for (FaultOperation operation : faultOperations) {
            logger.info("Kill node {} processes...", operation.getNode());
            MQChaosNode mqChaosNode = nodesMap.get(operation.getNode());
            mqChaosNode.kill();
        }
    }

    @Override public synchronized void recover() {
        if (faultOperations == null)
            return;
        logger.info("Recover {} fault....", mode);
        for (FaultOperation operation : faultOperations) {
            logger.info("Restart node {} processes...", operation.getNode());
            MQChaosNode mqChaosNode = nodesMap.get(operation.getNode());
            mqChaosNode.start();
        }
        faultOperations = null;
    }
}
