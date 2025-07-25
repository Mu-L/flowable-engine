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
package org.flowable.idm.engine.test.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Joram Barrez
 */
public class EntitiesTest {

    @Test
    public void verifyVersionInsertHasSpaceAfterNumber() throws Exception {
        Set<String> mappingFilePaths = getAllMappedEntityResources();
        for (String mappingFilePath : mappingFilePaths) {
            List<String> lines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream(mappingFilePath), StandardCharsets.UTF_8);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("1,")) {
                    if ("1,".equals(line)) {
                        fail(mappingFilePath + " has '1,' on one line. This doesn't work with some databases. (line " + (i + 1) + ")");
                    }
                    if (!line.contains("1, ")) {
                        fail(mappingFilePath + " has '1,' but no space follows the comma. This doesn't work with some databases. (line " + (i + 1) + ")");
                    }
                }
            }
        }
    }

    protected Set<String> getAllMappedEntityResources() {
        return getResources((nodeList, resources) -> {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String resource = node.getAttributes().getNamedItem("resource").getTextContent();
                if (!resource.contains("common.xml")) {
                    resources.add(resource);
                }
            }
        });
    }

    protected Set<String> getResources(BiConsumer<NodeList, Set<String>> consumer) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setValidating(false);
            docBuilderFactory.setNamespaceAware(false);
            docBuilderFactory.setExpandEntityReferences(false);
            docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(IdmEngineConfiguration.DEFAULT_MYBATIS_MAPPING_FILE));

            Set<String> resources = new HashSet<>();
            NodeList nodeList = document.getElementsByTagName("mapper");
            consumer.accept(nodeList, resources);

            resources.remove("TableData"); // not an entity

            assertThat(resources.size()).isPositive();
            return resources;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
