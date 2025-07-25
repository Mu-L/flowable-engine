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
package org.flowable.eventregistry.rest.service.api.repository;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.flowable.eventregistry.api.EventDeployment;
import org.flowable.eventregistry.rest.service.BaseSpringRestTestCase;
import org.flowable.eventregistry.rest.service.api.EventRestUrls;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import net.javacrumbs.jsonunit.core.Option;

/**
 * Test for all REST-operations related to the Deployment collection.
 * 
 * @author Tijs Rademakers
 */
public class DeploymentCollectionResourceTest extends BaseSpringRestTestCase {

    /**
     * Test getting deployments. GET cmmn-repository/deployments
     */
    @Test
    public void testGetDeployments() throws Exception {

        try {
            // Alter time to ensure different deployTimes
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            eventRegistryEngineConfiguration.getClock().setCurrentTime(yesterday.getTime());

            EventDeployment firstDeployment = repositoryService.createDeployment().name("Deployment 1").category("DEF")
                            .addClasspathResource("org/flowable/eventregistry/rest/service/api/repository/simpleEvent.event")
                            .deploy();

            eventRegistryEngineConfiguration.getClock().setCurrentTime(Calendar.getInstance().getTime());
            EventDeployment secondDeployment = repositoryService.createDeployment().name("Deployment 2").category("ABC")
                    .addClasspathResource("org/flowable/eventregistry/rest/service/api/repository/simpleEvent.event").tenantId("myTenant").deploy();

            String baseUrl = EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION);
            assertResultsPresentInDataResponse(baseUrl, firstDeployment.getId(), secondDeployment.getId());

            // Check name filtering
            String url = baseUrl + "?name=" + encode("Deployment 1");
            assertResultsPresentInDataResponse(url, firstDeployment.getId());

            // Check name-like filtering
            url = baseUrl + "?nameLike=" + encode("%ment 2");
            assertResultsPresentInDataResponse(url, secondDeployment.getId());

            // Check category filtering
            url = baseUrl + "?category=DEF";
            assertResultsPresentInDataResponse(url, firstDeployment.getId());

            // Check category-not-equals filtering
            url = baseUrl + "?categoryNotEquals=DEF";
            assertResultsPresentInDataResponse(url, secondDeployment.getId());

            // Check tenantId filtering
            url = baseUrl + "?tenantId=myTenant";
            assertResultsPresentInDataResponse(url, secondDeployment.getId());

            // Check tenantId filtering
            url = baseUrl + "?tenantId=unexistingTenant";
            assertResultsPresentInDataResponse(url);

            // Check tenantId like filtering
            url = baseUrl + "?tenantIdLike=" + encode("%enant");
            assertResultsPresentInDataResponse(url, secondDeployment.getId());

            // Check without tenantId filtering
            url = baseUrl + "?withoutTenantId=true";
            assertResultsPresentInDataResponse(url, firstDeployment.getId());

        } finally {
            // Always cleanup any created deployments, even if the test failed
            List<EventDeployment> deployments = repositoryService.createDeploymentQuery().list();
            for (EventDeployment deployment : deployments) {
                repositoryService.deleteDeployment(deployment.getId());
            }
        }
    }

    @Test
    public void testGetDeploymentsSorting() throws Exception {

        try {
            // Alter time to ensure different deployTimes
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            eventRegistryEngineConfiguration.getClock().setCurrentTime(yesterday.getTime());

            EventDeployment firstDeployment = repositoryService.createDeployment().name("Deployment 1").category("DEF")
                    .addClasspathResource("org/flowable/eventregistry/rest/service/api/repository/simpleEvent.event")
                    .tenantId("acme")
                    .deploy();

            eventRegistryEngineConfiguration.getClock().setCurrentTime(Calendar.getInstance().getTime());
            EventDeployment secondDeployment = repositoryService.createDeployment().name("Deployment 2").category("ABC")
                    .addClasspathResource("org/flowable/eventregistry/rest/service/api/repository/simpleEvent.event")
                    .tenantId("myTenant")
                    .deploy();

            String baseUrl = EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION);
            assertResultsPresentInDataResponse(baseUrl, firstDeployment.getId(), secondDeployment.getId());

            // Check ordering by name
            CloseableHttpResponse response = executeRequest(
                    new HttpGet(SERVER_URL_PREFIX + EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=name&order=asc"),
                    HttpStatus.SC_OK);
            JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
            closeResponse(response);
            assertThatJson(dataNode)
                    .when(Option.IGNORING_EXTRA_FIELDS)
                    .isEqualTo("[{"
                            + "     id: '" + firstDeployment.getId() + "'"
                            + "},"
                            + "{"
                            + "     id: '" + secondDeployment.getId() + "'"
                            + "}]");

            // Check ordering by deploy time
            response = executeRequest(new HttpGet(
                            SERVER_URL_PREFIX + EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=deployTime&order=asc"),
                    HttpStatus.SC_OK);
            dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
            closeResponse(response);
            assertThatJson(dataNode)
                    .when(Option.IGNORING_EXTRA_FIELDS)
                    .isEqualTo("[{"
                            + "     id: '" + firstDeployment.getId() + "'"
                            + "},"
                            + "{"
                            + "     id: '" + secondDeployment.getId() + "'"
                            + "}]");

            // Check ordering by tenantId
            response = executeRequest(new HttpGet(
                            SERVER_URL_PREFIX + EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=tenantId&order=desc"),
                    HttpStatus.SC_OK);
            dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
            closeResponse(response);
            assertThatJson(dataNode)
                    .when(Option.IGNORING_EXTRA_FIELDS)
                    .isEqualTo("[{"
                            + "     id: '" + secondDeployment.getId() + "'"
                            + "},"
                            + "{"
                            + "     id: '" + firstDeployment.getId() + "'"
                            + "}]");

            // Check paging
            response = executeRequest(new HttpGet(SERVER_URL_PREFIX + EventRestUrls.createRelativeResourceUrl(EventRestUrls.URL_DEPLOYMENT_COLLECTION)
                    + "?sort=deployTime&order=asc&start=1&size=1"), HttpStatus.SC_OK);
            JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
            closeResponse(response);
            dataNode = responseNode.get("data");
            assertThatJson(dataNode)
                    .when(Option.IGNORING_EXTRA_FIELDS)
                    .isEqualTo("["
                            + "{"
                            + "     id: '" + secondDeployment.getId() + "'"
                            + "}]");
            assertThatJson(responseNode)
                    .when(Option.IGNORING_EXTRA_FIELDS)
                    .isEqualTo("{"
                            + "     total: 2,"
                            + "     start: 1,"
                            + "     size: 1"
                            + "}");

        } finally {
            // Always cleanup any created deployments, even if the test failed
            List<EventDeployment> deployments = repositoryService.createDeploymentQuery().list();
            for (EventDeployment deployment : deployments) {
                repositoryService.deleteDeployment(deployment.getId());
            }
        }
    }
}
