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
package org.flowable.cmmn.test.itemcontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.flowable.cmmn.api.runtime.PlanItemInstanceState.ACTIVE;

import java.util.Arrays;
import java.util.List;

import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.test.FlowableCmmnTestCase;
import org.junit.jupiter.api.Test;

/**
 * Testing dynamic, expression based plan item name with local, collection based variables as well as case based ones.
 *
 * @author Micha Kiener
 */
public class DynamicPlanItemNameWithRepetitionTest extends FlowableCmmnTestCase {

    @Test
    @CmmnDeployment
    public void testDynamicNameWithRepetitionCollectionAndCaseVariable() {
        List<String> myCollection = Arrays.asList("A", "B", "C");
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("dynamicPlanItemNameTest")
                .variable("myCollection", myCollection)
                .variable("foo", "FooValue")
                .start();

        List<PlanItemInstance> planItemInstances = getPlanItemInstances(caseInstance.getId());
        assertThat(planItemInstances).hasSize(3);
        assertPlanItemInstanceState(planItemInstances, "Task (A / 0 - FooValue)", ACTIVE);
        assertPlanItemInstanceState(planItemInstances, "Task (B / 1 - FooValue)", ACTIVE);
        assertPlanItemInstanceState(planItemInstances, "Task (C / 2 - FooValue)", ACTIVE);
    }
}
