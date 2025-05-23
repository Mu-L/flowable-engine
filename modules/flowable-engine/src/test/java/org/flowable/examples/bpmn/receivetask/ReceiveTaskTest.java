/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.examples.bpmn.receivetask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.Test;

/**
 * @author Joram Barrez
 */
public class ReceiveTaskTest extends PluggableFlowableTestCase {

    @Test
    @Deployment
    public void testWaitStateBehavior() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();
        assertThat(execution).isNotNull();

        runtimeService.trigger(execution.getId());
        assertProcessEnded(pi.getId());
    }
    
    @Test
    @Deployment
    public void testSkipExpression() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
        variables.put("skipExpression", true);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask", variables);
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();
        assertThat(execution).isNull();

        assertProcessEnded(pi.getId());
    }

}
