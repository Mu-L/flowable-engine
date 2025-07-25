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

package org.flowable.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.impl.calendar.BusinessCalendar;
import org.flowable.common.engine.impl.runtime.Clock;
import org.flowable.engine.impl.test.ResourceFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TaskDueDateExtensionsTest extends ResourceFlowableTestCase {

    public TaskDueDateExtensionsTest() {
        super("org/flowable/engine/test/bpmn/usertask/TaskDueDateExtensionsTest.flowable.cfg.xml");
    }

    @Test
    @Deployment
    public void testDueDateExtension() throws Exception {

        Date date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("06-07-1986 12:10:00");
        LocalDate localDate = LocalDate.of(1986, 7, 6);
        LocalDateTime localDateTime = LocalDateTime.of(1986, 7, 6, 12, 10);
        Instant instant = date.toInstant();

        Map<String, Object> variables = new HashMap<>();
        variables.put("dateVariable", date);
        variables.put("instantVariable", instant);
        variables.put("localDateVariable", localDate);
        variables.put("localDateTimeVariable", localDateTime);

        // Start process-instance, passing date that should be used as dueDate
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

        org.flowable.task.api.Task dateTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("dateTask").singleResult();
        assertThat(dateTask.getDueDate()).isEqualTo(date);

        org.flowable.task.api.Task localDateTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("localDateTask").singleResult();
        Date startOfDay = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertThat(localDateTask.getDueDate()).isEqualTo(startOfDay);

        org.flowable.task.api.Task localDateTimeTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("localDateTimeTask").singleResult();
        assertThat(localDateTimeTask.getDueDate()).isEqualTo(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));

        org.flowable.task.api.Task instantTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("instantTask").singleResult();
        assertThat(instantTask.getDueDate()).isEqualTo(date);
    }

    @Test
    @Deployment
    public void testDueDateExtensionWithUnparseableDate() throws Exception {
        int notAValidDate = 0;

        Map<String, Object> variables = new HashMap<>();
        variables.put("dateVariable", notAValidDate);

        assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("dueDateExtensionWithUnparseableDate", variables))
                .isInstanceOf(FlowableIllegalArgumentException.class);
    }

    @Test
    @Deployment
    public void testDueDateStringExtension() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("dateVariable", "1986-07-06T12:10:00");

        // Start process-instance, passing date that should be used as dueDate
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

        org.flowable.task.api.Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        assertThat(task.getDueDate()).isNotNull();
        Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("06-07-1986 12:10:00");
        assertThat(task.getDueDate()).isEqualTo(date);
    }

    @Test
    @Deployment
    public void testRelativeDueDateStringExtension() throws Exception {
        Clock clock = processEngineConfiguration.getClock();
        clock.setCurrentCalendar(new GregorianCalendar(2015, 0, 1));
        Map<String, Object> variables = new HashMap<>();
        variables.put("dateVariable", "P2DT5H40M");

        // Start process-instance, passing ISO8601 duration formatted String
        // that should be used to calculate dueDate
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

        org.flowable.task.api.Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        assertThat(task.getDueDate()).isNotNull();
        Period period = new Period(task.getCreateTime().getTime(), task.getDueDate().getTime());
        assertThat(period.getDays()).isEqualTo(2);
        assertThat(period.getHours()).isEqualTo(5);
        assertThat(period.getMinutes()).isEqualTo(40);
        clock.reset();
    }

    @Test
    @Deployment
    public void testRelativeDueDateStringWithCalendarNameExtension() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("dateVariable", "P2DT5H40M");

        // Start process-instance, passing ISO8601 duration formatted String that should be used to calculate dueDate
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

        org.flowable.task.api.Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        assertThat(task.getDueDate()).isEqualTo(new Date(0));
    }

    public static class CustomBusinessCalendar implements BusinessCalendar {

        @Override
        public Date resolveDuedate(String duedateDescription) {
            return new Date(0);
        }

        @Override
        public Date resolveDuedate(String duedateDescription, int maxIterations) {
            return new Date(0);
        }

        @Override
        public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
            return true;
        }

        @Override
        public Date resolveEndDate(String endDateString) {
            return new Date(0);
        }

    }
}
