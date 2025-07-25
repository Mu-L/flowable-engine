---
id: ch04-Spring
title: Spring integration
---

While you can definitely use Flowable without Spring, we’ve provided some very nice integration features that are explained in this chapter.

## CmmnEngineFactoryBean

The CmmnEngine can be configured as a regular Spring bean. The starting point of the integration is the class org.flowable.cmmn.spring.CmmnEngineFactoryBean. That bean takes a CMMN engine configuration and creates the CMMN engine. This means that the creation and configuration of properties for Spring is the same as documented in the [configuration section](cmmn/ch02-Configuration.md#creating-a-cmmnengine). For Spring integration, the configuration and engine beans will look like this:

    <bean id="cmmnEngineConfiguration" class="org.flowable.cmmn.spring.SpringCmmnEngineConfiguration">
        ...
    </bean>

    <bean id="cmmnEngine" class="org.flowable.cmmn.spring.CmmnEngineFactoryBean">
      <property name="cmmnEngineConfiguration" ref="cmmnEngineConfiguration" />
    </bean>

Note that the cmmnEngineConfiguration bean now uses the org.flowable.cmmn.spring.SpringCmmnEngineConfiguration class.

## Default Spring configuration

The section shown below contains the dataSource, transactionManager, cmmnEngine and the Flowable engine services.

When passing the DataSource to the SpringCmmnEngineConfiguration (using property "dataSource"), Flowable uses a org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy internally, which wraps the passed DataSource. This is done to make sure the SQL connections retrieved from the DataSource and the Spring transactions play well together. This implies that it’s no longer necessary to proxy the dataSource yourself in Spring configuration, although it’s still possible to pass a TransactionAwareDataSourceProxy into the SpringCmmnEngineConfiguration. In this case, no additional wrapping will occur.

**Make sure when declaring a TransactionAwareDataSourceProxy in Spring configuration yourself that you don’t use it for resources that are already aware of Spring transactions (e.g. DataSourceTransactionManager need the un-proxied dataSource).**

    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:context="http://www.springframework.org/schema/context"
           xmlns:tx="http://www.springframework.org/schema/tx"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                                 http://www.springframework.org/schema/beans/spring-beans.xsd
                               http://www.springframework.org/schema/context
                                 http://www.springframework.org/schema/context/spring-context-2.5.xsd
                               http://www.springframework.org/schema/tx
                                 http://www.springframework.org/schema/tx/spring-tx-3.0.xsd">

      <bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
        <property name="driverClass" value="org.h2.Driver" />
        <property name="url" value="jdbc:h2:mem:flowable;DB_CLOSE_DELAY=1000" />
        <property name="username" value="sa" />
        <property name="password" value="" />
      </bean>

      <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
      </bean>

      <bean id="cmmnEngineConfiguration" class="org.flowable.cmmn.spring.SpringCmmnEngineConfiguration">
        <property name="dataSource" ref="dataSource" />
        <property name="transactionManager" ref="transactionManager" />
        <property name="databaseSchemaUpdate" value="true" />
      </bean>

      <bean id="cmmnEngine" class="org.flowable.cmmn.spring.CmmnEngineFactoryBean">
        <property name="cmmnEngineConfiguration" ref="cmmnEngineConfiguration" />
      </bean>

      <bean id="cmmnRepositoryService" factory-bean="cmmnEngine" factory-method="getCmmnRepositoryService" />
      <bean id="cmmnRuntimeService" factory-bean="cmmnEngine" factory-method="getCmmnRuntimeService" />
      <bean id="cmmnTaskService" factory-bean="cmmnEngine" factory-method="getCmmnTaskService" />
      <bean id="cmmnHistoryService" factory-bean="cmmnEngine" factory-method="getCmmnHistoryService" />
      <bean id="cmmnManagementService" factory-bean="cmmnEngine" factory-method="getCmmnManagementService" />

    ...

First, the application context is created using any of the ways supported by Spring. In this example, you could use a classpath XML resource to configure our Spring application context:

    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
        "org/flowable/cmmn/examples/spring/SpringIntegrationTest-context.xml");

or, as it’s a test:

    @ContextConfiguration(
      "classpath:org/flowable/cmmn/spring/test/SpringIntegrationTest-context.xml")

## Expressions

When using the CmmnEngineFactoryBean, all expressions in the CMMN processes will also 'see' all the Spring beans, by default. It’s possible to limit the beans (even none) you want to expose in expressions using a map that you can configure. The example below exposes a single bean (printer), available to use under the key "printer". **To have NO beans exposed at all, just pass an empty list as 'beans' property on the SpringCmmnEngineConfiguration. When no 'beans' property is set, all Spring beans in the context will be available.**

    <bean id="cmmnEngineConfiguration" class="org.flowable.cmmn.spring.SpringCmmnEngineConfiguration">
      ...
      <property name="beans">
        <map>
          <entry key="printer" value-ref="printer" />
        </map>
      </property>
    </bean>

    <bean id="printer" class="org.flowable.cmmn.examples.spring.Printer" />

Now the exposed beans can be used in expressions:

    ...
        <case id="myCase">
            <casePlanModel id="myPlanModel" name="My CasePlanModel">

                <planItem id="planItem1" name="Task One" definitionRef="serviceTask" />
                <planItem id="planItem2" name="The Case" definitionRef="task">
                    <entryCriterion sentryRef="sentry1" />
                </planItem>

                <sentry id="sentry1">
                    <planItemOnPart sourceRef="planItem1">
                        <standardEvent>complete</standardEvent>
                    </planItemOnPart>
                </sentry>

                <task id="serviceTask" flowable:type="java" flowable:expression="${printer.printMessage(var1)}" flowable:resultVariableName="customResponse" />
                <task id="task" name="The Task" isBlocking="true" />

            </casePlanModel>
        </case>

Where Printer looks like this:

    public class Printer {

      public void printMessage(String var) {
        System.out.println("hello " + var);
      }
    }

And the Spring bean configuration (also shown above) looks like this:

    <beans>
      ...

      <bean id="printer" class="org.flowable.cmmn.examples.spring.Printer" />

    </beans>

## Automatic resource deployment

Spring integration also has a special feature for deploying resources. In the CMMN engine configuration, you can specify a set of resources. When the CMMN engine is created, all those resources will be scanned and deployed. There is filtering in place that prevents duplicate deployments. Only when the resources have actually changed will new deployments be deployed to the Flowable DB. This makes sense in a lot of use cases, where the Spring container is rebooted frequently (for example, testing).

Here’s an example:

    <bean id="cmmnEngineConfiguration" class="org.flowable.cmmn.spring.SpringCmmnEngineConfiguration">
      ...
      <property name="deploymentResources"
        value="classpath*:/org/flowable/cmmn/spring/test/autodeployment/autodeploy.*.cmmn" />
    </bean>

    <bean id="cmmnEngine" class="org.flowable.cmmn.spring.CmmnEngineFactoryBean">
      <property name="cmmnEngineConfiguration" ref="cmmnEngineConfiguration" />
    </bean>

By default, the configuration above will group all of the resources matching the filter into a single deployment to the Flowable engine. The duplicate filtering to prevent re-deployment of unchanged resources applies to the whole deployment. In some cases, this may not be what you want. For instance, if you deploy a set of process resources this way and only a single case definition in those resources has changed, the deployment as a whole will be considered new and all of the case definitions in that deployment will be re-deployed, resulting in new versions of each of the case definitions, even though only one was actually changed.

To be able to customize the way deployments are determined, you can specify an additional property in the SpringCmmnEngineConfiguration, deploymentMode. This property defines the way deployments will be determined from the set of resources that match the filter. There are 3 values that are supported by default for this property:

-   default: Group all resources into a single deployment and apply duplicate filtering to that deployment. This is the default value and it will be used if you don’t specify a value.

-   single-resource: Create a separate deployment for each individual resource and apply duplicate filtering to that deployment. This is the value you would use to have each process definition be deployed separately and only create a new case definition version if it has changed.

-   resource-parent-folder: Create a separate deployment for resources that share the same parent folder and apply duplicate filtering to that deployment. This value can be used to create separate deployments for most resources, but still be able to group some by placing them in a shared folder. Here’s an example of how to specify the single-resource configuration for deploymentMode:

<!-- -->

    <bean id="cmmnEngineConfiguration"
        class="org.flowable.cmmn.spring.SpringCmmnEngineConfiguration">
      ...
      <property name="deploymentResources" value="classpath*:/flowable/*.cmmn" />
      <property name="deploymentMode" value="single-resource" />
    </bean>

In addition to using the values listed above for deploymentMode, you may require customized behavior towards determining deployments. If so, you can create a subclass of SpringCmmnEngineConfiguration and override the getAutoDeploymentStrategy(String deploymentMode) method. This method determines which deployment strategy is used for a certain value of the deploymentMode configuration.

## Unit testing

When integrating with Spring, business cases can be tested very easily using the standard standard [Flowable testing facilities](cmmn/ch03-API.md#unit-testing).
The following examples show how a case definition is tested in typical Spring-based JUnit Jupiter test:

**JUnit Jupiter test.**

    @ExtendWith(FlowableCmmnSpringExtension.class)
    @SpringJUnitConfig(CmmnSpringJunitJupiterTest.TestConfiguration.class)
    class MyBusinessCaseTest {

      @Autowired
      private CmmnRepositoryService cmmnRepositoryService;

      @Autowired
      private CmmnRuntimeService cmmnRuntimeService;

      @Test
      @CmmnDeployment
      public void simpleCaseTest() {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("simpleCase")
                .variable("var1", "John Doe")
                .start();

        Assertions.assertNotNull(caseInstance);
      }
    }

