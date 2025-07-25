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

package org.flowable.engine.impl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.common.engine.impl.test.EnsureCleanDb;
import org.flowable.engine.DynamicBpmnService;
import org.flowable.engine.FormService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.ProcessMigrationService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.history.DefaultHistoryManager;
import org.flowable.engine.impl.history.HistoryManager;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Filip Hrisafov
 */
@EnsureCleanDb(excludeTables = {
    "ACT_GE_PROPERTY",
    "ACT_ID_PROPERTY",
    "FLW_EV_DATABASECHANGELOGLOCK",
    "FLW_EV_DATABASECHANGELOG"
})
public abstract class AbstractFlowableTestCase extends AbstractTestCase {

    protected ProcessEngine processEngine;

    protected static List<String> deploymentIdsForAutoCleanup = new ArrayList<>();

    protected ProcessEngineConfigurationImpl processEngineConfiguration;
    protected RepositoryService repositoryService;
    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected FormService formService;
    protected HistoryService historyService;
    protected IdentityService identityService;
    protected ManagementService managementService;
    protected DynamicBpmnService dynamicBpmnService;
    protected ProcessMigrationService processMigrationService;

    @BeforeEach
    public final void initializeServices(ProcessEngine processEngine) {
        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
        this.processEngine = processEngine;
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
        formService = processEngine.getFormService();
        historyService = processEngine.getHistoryService();
        identityService = processEngine.getIdentityService();
        managementService = processEngine.getManagementService();
        dynamicBpmnService = processEngine.getDynamicBpmnService();
        processMigrationService = processEngine.getProcessMigrationService();
    }

    protected static void cleanDeployments(ProcessEngine processEngine) {
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        for (String autoDeletedDeploymentId : deploymentIdsForAutoCleanup) {
            processEngineConfiguration.getRepositoryService().deleteDeployment(autoDeletedDeploymentId, true);
        }
        deploymentIdsForAutoCleanup.clear();
    }

    protected static void validateHistoryData(ProcessEngine processEngine) {
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        HistoryService historyService = processEngine.getHistoryService();
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().finished().list();

            for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
                
                assertNotNull(historicProcessInstance.getProcessDefinitionId(), "Historic process instance has no process definition id");
                assertNotNull(historicProcessInstance.getProcessDefinitionKey(), "Historic process instance has no process definition key");
                assertNotNull(historicProcessInstance.getProcessDefinitionVersion(), "Historic process instance has no process definition version");
                assertNotNull(historicProcessInstance.getDeploymentId(), "Historic process instance has no deployment id");
                assertNotNull(historicProcessInstance.getStartActivityId(), "Historic process instance has no start activity id");
                assertNotNull(historicProcessInstance.getStartTime(), "Historic process instance has no start time");
                assertNotNull(historicProcessInstance.getEndTime(), "Historic process instance has no end time");

                String processInstanceId = historicProcessInstance.getId();

                // tasks
                List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(processInstanceId).list();
                
                if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
                    for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
                        assertEquals(processInstanceId, historicTaskInstance.getProcessInstanceId());
                        if (historicTaskInstance.getClaimTime() != null) {
                            assertNotNull(historicTaskInstance.getWorkTimeInMillis(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no work time");
                        }
                        assertNotNull(historicTaskInstance.getId(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no id");
                        assertNotNull(historicTaskInstance.getProcessInstanceId(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no process instance id");
                        assertNotNull(historicTaskInstance.getExecutionId(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no execution id");
                        assertNotNull(historicTaskInstance.getProcessDefinitionId(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no process definition id");
                        assertNotNull(historicTaskInstance.getTaskDefinitionKey(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no task definition key");
                        assertNotNull(historicTaskInstance.getCreateTime(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no create time");
                        assertNotNull(historicTaskInstance.getStartTime(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no start time");
                        assertNotNull(historicTaskInstance.getEndTime(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no end time");
                    }
                }

                // activities
                List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId).list();
                if (historicActivityInstances != null && historicActivityInstances.size() > 0) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        assertEquals(processInstanceId, historicActivityInstance.getProcessInstanceId());
                        assertNotNull(historicActivityInstance.getActivityId(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no activity id");
                        assertNotNull(historicActivityInstance.getActivityType(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no activity type");
                        assertNotNull(historicActivityInstance.getProcessDefinitionId(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no process definition id");
                        assertNotNull(historicActivityInstance.getProcessInstanceId(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no process instance id");
                        assertNotNull(historicActivityInstance.getExecutionId(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no execution id");
                        assertNotNull(historicActivityInstance.getStartTime(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no start time");
                        assertNotNull(historicActivityInstance.getEndTime(), "Historic activity instance " + historicActivityInstance.getId() + " / " + historicActivityInstance.getActivityId() + " has no end time");
                        if (historicProcessInstance.getEndTime() == null) {
                            assertActivityInstancesAreSame(historicActivityInstance,
                                processEngine.getRuntimeService().createActivityInstanceQuery().activityInstanceId(historicActivityInstance.getId()).singleResult()
                            );
                        }
                    }
                }
            }

        }
    }

    public void assertProcessEnded(final String processInstanceId) {
        assertProcessEnded(processInstanceId, 10000);
    }

    public void assertProcessEnded(final String processInstanceId, long timeout) {
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (processInstance != null) {
            throw new AssertionError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
        }

        // Verify historical data if end times are correctly set
        if (HistoryTestHelper.isHistoryLevelAtLeast(HistoryLevel.AUDIT, processEngineConfiguration, timeout)) {

            // process instance
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            assertEquals(processInstanceId, historicProcessInstance.getId());
            assertNotNull(historicProcessInstance.getStartTime(), "Historic process instance has no start time");
            assertNotNull(historicProcessInstance.getEndTime(), "Historic process instance has no end time");

            // tasks
            List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId).list();
            if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
                for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
                    assertEquals(processInstanceId, historicTaskInstance.getProcessInstanceId());
                    assertNotNull(historicTaskInstance.getStartTime(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no start time");
                    assertNotNull(historicTaskInstance.getEndTime(), "Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no end time");
                }
            }

            // activities
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId).list();
            if (historicActivityInstances != null && historicActivityInstances.size() > 0) {
                for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                    assertEquals(processInstanceId, historicActivityInstance.getProcessInstanceId());
                    assertNotNull(historicActivityInstance.getStartTime(), historicActivityInstance.getId() + " Historic activity instance '" + historicActivityInstance.getActivityId() +"' has no start time");
                    assertNotNull(historicActivityInstance.getEndTime(), historicActivityInstance.getId() + " Historic activity instance '" + historicActivityInstance.getActivityId() + "' has no end time");
                }
            }
        }

        // runtime activities
        assertEquals(0L, runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId).count());
    }

    public static void assertActivityInstancesAreSame(HistoricActivityInstance historicActInst, ActivityInstance activityInstance) {
        assertEquals(historicActInst.getId(), activityInstance.getId());
        assertEquals(historicActInst.getActivityId(), activityInstance.getActivityId());
        assertEquals(historicActInst.getEndTime(), activityInstance.getEndTime());
        assertEquals(historicActInst.getProcessDefinitionId(), activityInstance.getProcessDefinitionId());
        assertEquals(historicActInst.getStartTime(), activityInstance.getStartTime());
        assertEquals(historicActInst.getExecutionId(), activityInstance.getExecutionId());
        assertEquals(historicActInst.getActivityType(), activityInstance.getActivityType());
        assertEquals(historicActInst.getProcessInstanceId(), activityInstance.getProcessInstanceId());
        assertEquals(historicActInst.getAssignee(), activityInstance.getAssignee());
        assertEquals(historicActInst.getCompletedBy(), activityInstance.getCompletedBy());
        assertEquals(historicActInst.getTransactionOrder(), activityInstance.getTransactionOrder());
        assertEquals(historicActInst.getDurationInMillis(), activityInstance.getDurationInMillis());
        assertEquals(historicActInst.getTenantId(), activityInstance.getTenantId());
        assertEquals(historicActInst.getDeleteReason(), activityInstance.getDeleteReason());
        assertEquals(historicActInst.getActivityName(), activityInstance.getActivityName());
        assertEquals(historicActInst.getCalledProcessInstanceId(), activityInstance.getCalledProcessInstanceId());
        assertEquals(historicActInst.getTaskId(), activityInstance.getTaskId());
        assertEquals(historicActInst.getTime(), activityInstance.getTime());
    }

    public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

    public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration, maxMillisToWait, intervalMillis, condition);
    }

    public void executeJobExecutorForTime(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.executeJobExecutorForTime(processEngineConfiguration, maxMillisToWait, intervalMillis);
    }

    public void waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

    public void waitForJobExecutorToProcessAllJobsAndAllTimerJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }
    
    public void waitForJobExecutorToProcessAllHistoryJobs(long maxMillisToWait, long intervalMillis) {
        HistoryTestHelper.waitForJobExecutorToProcessAllHistoryJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }
    
    public void waitForHistoryJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        HistoryTestHelper.waitForJobExecutorToProcessAllHistoryJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

    /**
     * Since the 'one task process' is used everywhere the actual process content doesn't matter, instead of copying around the BPMN 2.0 xml one could use this method which gives a {@link BpmnModel}
     * version of the same process back.
     */
    public BpmnModel createOneTaskTestProcess() {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = createOneTaskProcess();
        model.addProcess(process);

        return model;
    }
    
    public BpmnModel createOneTaskTestProcessWithCandidateStarterGroup() {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = createOneTaskProcess();
        process.getCandidateStarterGroups().add("testGroup");
        model.addProcess(process);

        return model;
    }
    
    protected org.flowable.bpmn.model.Process createOneTaskProcess() {
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId("oneTaskProcess");
        process.setName("The one task process");

        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("The start");
        process.addFlowElement(startEvent);

        UserTask userTask = new UserTask();
        userTask.setName("The Task");
        userTask.setId("theTask");
        userTask.setAssignee("kermit");
        process.addFlowElement(userTask);

        EndEvent endEvent = new EndEvent();
        endEvent.setId("theEnd");
        endEvent.setName("The end");
        process.addFlowElement(endEvent);

        process.addFlowElement(new SequenceFlow("start", "theTask"));
        process.addFlowElement(new SequenceFlow("theTask", "theEnd"));
        
        return process;
    }

    public BpmnModel createTwoTasksTestProcess() {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        model.addProcess(process);
        process.setId("twoTasksProcess");
        process.setName("The two tasks process");

        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        process.addFlowElement(startEvent);

        UserTask userTask = new UserTask();
        userTask.setName("The First Task");
        userTask.setId("task1");
        userTask.setAssignee("kermit");
        process.addFlowElement(userTask);

        UserTask userTask2 = new UserTask();
        userTask2.setName("The Second Task");
        userTask2.setId("task2");
        userTask2.setAssignee("kermit");
        process.addFlowElement(userTask2);

        EndEvent endEvent = new EndEvent();
        endEvent.setId("theEnd");
        process.addFlowElement(endEvent);

        process.addFlowElement(new SequenceFlow("start", "task1"));
        process.addFlowElement(new SequenceFlow("start", "task2"));
        process.addFlowElement(new SequenceFlow("task1", "theEnd"));
        process.addFlowElement(new SequenceFlow("task2", "theEnd"));

        return model;
    }

    /**
     * Creates and deploys the one task process. See {@link #createOneTaskTestProcess()}.
     * 
     * @return The process definition id (NOT the process definition key) of deployed one task process.
     */
    public String deployOneTaskTestProcess() {
        BpmnModel bpmnModel = createOneTaskTestProcess();
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("oneTasktest.bpmn20.xml", bpmnModel).deploy();

        deploymentIdsForAutoCleanup.add(deployment.getId()); // For auto-cleanup

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        return processDefinition.getId();
    }
    
    public String deployOneTaskTestProcessWithCandidateStarterGroup() {
        BpmnModel bpmnModel = createOneTaskTestProcessWithCandidateStarterGroup();
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("oneTasktest.bpmn20.xml", bpmnModel).deploy();

        deploymentIdsForAutoCleanup.add(deployment.getId()); // For auto-cleanup

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        return processDefinition.getId();
    }

    public String deployTwoTasksTestProcess() {
        BpmnModel bpmnModel = createTwoTasksTestProcess();
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("twoTasksTestProcess.bpmn20.xml", bpmnModel).deploy();

        deploymentIdsForAutoCleanup.add(deployment.getId()); // For auto-cleanup

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        return processDefinition.getId();
    }

    //
    // HELPERS
    //
    
    protected void deleteDeployments() {
        boolean isAsyncHistoryEnabled = processEngineConfiguration.isAsyncHistoryEnabled();

        HistoryManager asyncHistoryManager = null;
        if (isAsyncHistoryEnabled) {
            processEngineConfiguration.setAsyncHistoryEnabled(false);
            asyncHistoryManager = processEngineConfiguration.getHistoryManager();
            processEngineConfiguration.setHistoryManager(new DefaultHistoryManager(processEngineConfiguration));
        }
        
        for (org.flowable.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
        
        if (isAsyncHistoryEnabled) {
            processEngineConfiguration.setAsyncHistoryEnabled(true);
            processEngineConfiguration.setHistoryManager(asyncHistoryManager);
        }
    }
    
    protected void deleteDeployment(String deploymentId) {
        boolean isAsyncHistoryEnabled = processEngineConfiguration.isAsyncHistoryEnabled();
        HistoryManager asyncHistoryManager = null;
        if (isAsyncHistoryEnabled) {
            processEngineConfiguration.setAsyncHistoryEnabled(false);
            asyncHistoryManager = processEngineConfiguration.getHistoryManager();
            processEngineConfiguration.setHistoryManager(new DefaultHistoryManager(processEngineConfiguration));
        }
        
        repositoryService.deleteDeployment(deploymentId, true);
        
        if (isAsyncHistoryEnabled) {
            processEngineConfiguration.setAsyncHistoryEnabled(true);
            processEngineConfiguration.setHistoryManager(asyncHistoryManager);
        }
    }

    protected void assertHistoricTasksDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String... taskNames) {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            for (String taskName : taskNames) {
                List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(processInstance.getId()).taskName(taskName).list();
                assertFalse(historicTaskInstances.isEmpty());
                for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
                    assertNotNull(historicTaskInstance.getEndTime());
                    if (expectedDeleteReason == null) {
                        assertNull(historicTaskInstance.getDeleteReason());
                    } else {
                        assertTrue(historicTaskInstance.getDeleteReason().startsWith(expectedDeleteReason));
                    }
                }
            }
        }
    }

    protected void assertHistoricActivitiesDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String... activityIds) {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            for (String activityId : activityIds) {
                List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                        .activityId(activityId).processInstanceId(processInstance.getId()).list();
                assertFalse(historicActivityInstances.isEmpty(), "Could not find historic activities");
                for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                    assertNotNull(historicActivityInstance.getEndTime());
                    if (expectedDeleteReason == null) {
                        assertNull(historicActivityInstance.getDeleteReason());
                    } else {
                        assertTrue(historicActivityInstance.getDeleteReason().startsWith(expectedDeleteReason));
                    }
                }
            }
        }
    }

    protected void completeTask(Task task) {
        taskService.complete(task.getId());
    }

    protected static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        Objects.requireNonNull(list1);
        Objects.requireNonNull(list2);
        return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
    }

    protected static<T> Map<String, List<T>> groupListContentBy(List<T> source, Function<T, String> classifier) {
        return source.stream().collect(Collectors.groupingBy(classifier));
    }

    protected String getJobActivityId(Job job) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jobConfigurationMap = objectMapper.readValue(job.getJobHandlerConfiguration(), new TypeReference<>() {

            });
            return (String) jobConfigurationMap.get("activityId");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ProcessDefinition deployProcessDefinition(String name, String path) {
        Deployment deployment = repositoryService.createDeployment()
            .name(name)
            .addClasspathResource(path)
            .deploy();
        
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId()).singleResult();

        return processDefinition;
    }

    protected ProcessDefinition deployProcessDefinition(String name, String path, String tenantId) {
        Deployment deployment = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(path)
                .tenantId(tenantId)
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionTenantId(tenantId)
                .singleResult();

        return processDefinition;
    }

    protected void completeProcessInstanceTasks(String processInstanceId) {
        List<Task> tasks;
        do {
            tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            tasks.forEach(this::completeTask);
        } while (!tasks.isEmpty());
    }
}
