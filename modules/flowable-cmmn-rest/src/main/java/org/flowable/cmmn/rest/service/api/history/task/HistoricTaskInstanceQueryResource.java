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

package org.flowable.cmmn.rest.service.api.history.task;

import java.util.Map;

import org.flowable.common.rest.api.DataResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "History Task" }, description = "Manage History Task Instances", authorizations = { @Authorization(value = "basicAuth") })
public class HistoricTaskInstanceQueryResource extends HistoricTaskInstanceBaseResource {

    @ApiOperation(value = "Query for historic task instances", tags = {"History Task", "Query" },
            nickname = "queryHistoricTaskInstance", notes = "All supported JSON parameter fields allowed are exactly the same as the parameters found for getting a collection of historic task instances, but passed in as JSON-body arguments rather than URL-parameters to allow for more advanced querying and preventing errors with request-uri’s that are too long. On top of that, the query allows for filtering based on process variables. The taskVariables and processVariables properties are JSON-arrays containing objects with the format as described here.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sort", dataType = "string", value = "The field to sort by. Defaults to 'taskInstanceId'.", allowableValues = "deleteReason,duration,endTime,executionId,taskInstanceId,caseDefinitionId,caseInstanceId,start,assignee,taskDefinitionKey,description,dueDate,name,owner,priority,tenantId,startTime", paramType = "body"),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates request was successful and the tasks are returned"),
            @ApiResponse(code = 404, message = "Indicates an parameter was passed in the wrong format. The status-message contains additional information.") })
    @PostMapping(value = "/cmmn-query/historic-task-instances", produces = "application/json")
    public DataResponse<HistoricTaskInstanceResponse> queryProcessInstances(@RequestBody HistoricTaskInstanceQueryRequest queryRequest, @ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams) {

        return getQueryResponse(queryRequest, allRequestParams);
    }
}
