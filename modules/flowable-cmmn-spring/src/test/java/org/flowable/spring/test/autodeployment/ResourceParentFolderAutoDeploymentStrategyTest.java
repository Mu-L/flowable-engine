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

package org.flowable.spring.test.autodeployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.flowable.cmmn.spring.autodeployment.ResourceParentFolderAutoDeploymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;

/**
 * @author Tiese Barrell
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResourceParentFolderAutoDeploymentStrategyTest extends AbstractAutoDeploymentStrategyTest {

    private ResourceParentFolderAutoDeploymentStrategy classUnderTest;

    @Mock
    private File parentFile1Mock;

    @Mock
    private File parentFile2Mock;

    private final String parentFilename1 = "parentFilename1";
    private final String parentFilename2 = "parentFilename2";

    @BeforeEach
    @Override
    public void before() throws Exception {
        super.before();
        classUnderTest = new ResourceParentFolderAutoDeploymentStrategy();
        assertThat(classUnderTest).isNotNull();

        when(parentFile1Mock.getName()).thenReturn(parentFilename1);
        when(parentFile1Mock.isDirectory()).thenReturn(true);
        when(parentFile2Mock.getName()).thenReturn(parentFilename2);
        when(parentFile2Mock.isDirectory()).thenReturn(true);
    }

    @Test
    public void testHandlesMode() {
        assertThat(classUnderTest.handlesMode(ResourceParentFolderAutoDeploymentStrategy.DEPLOYMENT_MODE)).isTrue();
        assertThat(classUnderTest.handlesMode("other-mode")).isFalse();
        assertThat(classUnderTest.handlesMode(null)).isFalse();
    }

    @Test
    public void testDeployResources_Separate() {
        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2 };

        when(fileMock1.getParentFile()).thenReturn(parentFile1Mock);
        when(fileMock2.getParentFile()).thenReturn(parentFile2Mock);

        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(2)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename1);
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename2);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(2)).deploy();
    }

    @Test
    public void testDeployResources_Joined() {
        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2 };

        when(fileMock1.getParentFile()).thenReturn(parentFile1Mock);
        when(fileMock2.getParentFile()).thenReturn(parentFile1Mock);

        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(1)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename1);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).deploy();
    }

    @Test
    public void testDeployResources_AllInOne() {
        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2, resourceMock3 };

        when(fileMock1.getParentFile()).thenReturn(parentFile1Mock);
        when(fileMock2.getParentFile()).thenReturn(parentFile1Mock);
        when(fileMock3.getParentFile()).thenReturn(parentFile1Mock);

        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(1)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename1);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).deploy();
    }

    @Test
    public void testDeployResources_Mixed() {
        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2, resourceMock3 };

        when(fileMock1.getParentFile()).thenReturn(parentFile1Mock);
        when(fileMock2.getParentFile()).thenReturn(parentFile2Mock);
        when(fileMock3.getParentFile()).thenReturn(parentFile1Mock);

        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(2)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename1);
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + parentFilename2);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(2)).deploy();
    }

    @Test
    public void testDeployResources_NoParent() {

        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2, resourceMock3 };
        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(3)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + resourceName1);
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + resourceName2);
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + resourceName3);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(3)).deploy();
    }

    @Test
    public void testDeployResourcesNoResources() {
        final Resource[] resources = new Resource[] {};
        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, never()).createDeployment();
        verify(deploymentBuilderMock, never()).name(deploymentNameHint);
        verify(deploymentBuilderMock, never()).addInputStream(isA(String.class), isA(InputStream.class));
        verify(deploymentBuilderMock, never()).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, never()).deploy();
    }

    @Test
    public void testDeployResourcesIOExceptionWhenCreatingMapFallsBackToResourceName() throws Exception {
        when(resourceMock3.getFile()).thenThrow(new IOException());
        when(resourceMock3.getFilename()).thenReturn(resourceName3);

        final Resource[] resources = new Resource[] { resourceMock3 };
        classUnderTest.deployResources(deploymentNameHint, resources, cmmnEngineMock);

        verify(repositoryServiceMock, times(1)).createDeployment();
        verify(deploymentBuilderMock, times(1)).name(deploymentNameHint + "." + resourceName3);
        verify(deploymentBuilderMock, times(1)).deploy();
    }

}
