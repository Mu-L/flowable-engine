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

package org.flowable.cmmn.rest.service.api;

import java.util.Collection;
import java.util.List;

/**
 * Class that represents a bulk action to be performed on a resource.
 *
 * @author Christopher Welsch
 */
public class BulkMoveDeadLetterActionRequest extends RestActionRequest {

    protected Collection<String> jobIds;

    public Collection<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(Collection<String> jobIds) {
        this.jobIds = jobIds;
    }

}
