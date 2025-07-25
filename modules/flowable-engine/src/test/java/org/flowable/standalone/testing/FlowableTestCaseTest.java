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

package org.flowable.standalone.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;

/**
 * @author Joram Barrez
 */
@Deprecated(forRemoval = true, since = "7.2.0")
public class FlowableTestCaseTest extends FlowableTestCase {

    @Deployment
    public void testSimpleProcess() {
        runtimeService.startProcessInstanceByKey("simpleProcess");

        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("My Task");

        taskService.complete(task.getId());
        assertThat(runtimeService.createProcessInstanceQuery().count()).isZero();
    }
}
