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
package org.flowable.engine.runtime;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.flowable.common.engine.api.query.Query;

/**
 * Allows programmatic querying of {@link ProcessInstance}s.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Frederik Heremans
 * @author Falko Menge
 */
public interface ProcessInstanceQuery extends Query<ProcessInstanceQuery, ProcessInstance> {

    /** Select the process instance with the given id */
    ProcessInstanceQuery processInstanceId(String processInstanceId);

    /** Select process instances whose id is in the given set of ids */
    ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds);

    /** Select process instances with the given business key */
    ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);

    /**
     * Select process instance with the given business key, unique for the given process definition
     */
    ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey, String processDefinitionKey);

    /**
     * Select process instances with a business key like the given value.
     */
    ProcessInstanceQuery processInstanceBusinessKeyLike(String businessKeyLike);
    
    /**
     * Select process instances with a business key like the given value, ignoring upper/lower case.
     */
    ProcessInstanceQuery processInstanceBusinessKeyLikeIgnoreCase(String businessKeyLikeIgnoreCase);
    
    /** Select process instances with the given business status */
    ProcessInstanceQuery processInstanceBusinessStatus(String businessStatus);

    /**
     * Select process instances with a business status like the given value.
     */
    ProcessInstanceQuery processInstanceBusinessStatusLike(String businessStatusLike);
    
    /**
     * Select process instances with a business status like the given value, ignoring upper/lower case.
     */
    ProcessInstanceQuery processInstanceBusinessStatusLikeIgnoreCase(String businessStatusLikeIgnoreCase);

    /**
     * Only select process instances that have the given tenant id.
     */
    ProcessInstanceQuery processInstanceTenantId(String tenantId);

    /**
     * Only select process instances with a tenant id like the given one.
     */
    ProcessInstanceQuery processInstanceTenantIdLike(String tenantIdLike);
    
    /**
     * Only select process instances with a tenant id like the given one, ignoring upper/lower case.
     */
    ProcessInstanceQuery processInstanceTenantIdLikeIgnoreCase(String tenantIdLikeIgnoreCase);

    /**
     * Only select process instances that do not have a tenant id.
     */
    ProcessInstanceQuery processInstanceWithoutTenantId();

    /** Only select process instances whose process definition category is processDefinitionCategory. */
    ProcessInstanceQuery processDefinitionCategory(String processDefinitionCategory);
    
    /** Only select process instances whose process definition category is like the given value. */
    ProcessInstanceQuery processDefinitionCategoryLike(String processDefinitionCategoryLike);
    
    /** Only select process instances whose process definition category is like the given value, ignoring upper/lower case. */
    ProcessInstanceQuery processDefinitionCategoryLikeIgnoreCase(String processDefinitionCategoryLikeIgnoreCase);

    /**
     * Select process instances whose process definition name is processDefinitionName
     */
    ProcessInstanceQuery processDefinitionName(String processDefinitionName);
    
    /**
     * Select process instances whose process definition name is like the given value
     */
    ProcessInstanceQuery processDefinitionNameLike(String processDefinitionNameLike);
    
    /**
     * Select process instances whose process definition name is like the given value, ignoring upper/lower case.
     */
    ProcessInstanceQuery processDefinitionNameLikeIgnoreCase(String processDefinitionNameLikeIgnoreCase);

    /**
     * Only select process instances with a certain process definition version. Particularly useful when used in combination with {@link #processDefinitionKey(String)}
     */
    ProcessInstanceQuery processDefinitionVersion(Integer processDefinitionVersion);

    /**
     * Select the process instances which are defined by a process definition with the given key.
     */
    ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);

    /**
     * Select the process instances which are defined by process definitions with the given keys.
     */
    ProcessInstanceQuery processDefinitionKeys(Set<String> processDefinitionKeys);
    
    /**
     * Select the process instances which are defined by process definitions that don't match the given keys.
     */
    ProcessInstanceQuery excludeProcessDefinitionKeys(Set<String> processDefinitionKeys);
    
    /**
     * Select process instances whose process definition key is like the given value
     */
    ProcessInstanceQuery processDefinitionKeyLike(String processDefinitionKeyLike);
    
    /**
     * Select process instances whose process definition key is like the given value, ignoring upper/lower case.
     */
    ProcessInstanceQuery processDefinitionKeyLikeIgnoreCase(String processDefinitionKeyLikeIgnoreCase);

    /**
     * Select the process instances which are defined by a process definition with the given id.
     */
    ProcessInstanceQuery processDefinitionId(String processDefinitionId);

    /**
     * Select the process instances which are defined by process definitions with the given ids.
     */
    ProcessInstanceQuery processDefinitionIds(Set<String> processDefinitionIds);

    /**
     * Select the process instances which are defined by a process definition with the given engine version.
     */
    ProcessInstanceQuery processDefinitionEngineVersion(String processDefinitionEngineVersion);

    /**
     * Select the process instances which are defined by a deployment with the given id.
     */
    ProcessInstanceQuery deploymentId(String deploymentId);

    /**
     * Select the process instances which are defined by one of the given deployment ids
     */
    ProcessInstanceQuery deploymentIdIn(List<String> deploymentIds);

    /**
     * Select the process instances which are a sub process instance of the given super process instance.
     */
    ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId);

    /**
     * Select the process instance that have as sub process instance the given process instance. Note that there will always be maximum only <b>one</b> such process instance that can be the result of
     * this query.
     */
    ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId);

    /**
     * Exclude sub processes from the query result;
     */
    ProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses);
    
    /**
     * Select the process instances which have an active activity instance like the provided id.
     */
    ProcessInstanceQuery activeActivityId(String activityId);
    
    /**
     * Select the process instances which have an active activity instance like the provided ids.
     */
    ProcessInstanceQuery activeActivityIds(Set<String> activityIds);

    /**
     * Select the process instances with which the user with the given id is involved.
     */
    ProcessInstanceQuery involvedUser(String userId);
    
    /**
     * Select the process instances with which the user with the given id and identity link type is involved.
     */
    ProcessInstanceQuery involvedUser(String userId, String identityLinkType);
    
    /**
     * Select the process instances with which the group with the given id and identity link type is involved.
     */
    ProcessInstanceQuery involvedGroup(String groupId, String identityLinkType);

    /**
     * Select the process instances with which the {@link org.flowable.idm.api.Group}s with the given ids are involved.
     */
    ProcessInstanceQuery involvedGroups(Set<String> groupIds);

    /**
     * Only select process instances which have a global variable with the given value. The type of variable is determined based on the value, using types configured in
     * {@link org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl#getVariableTypes()}. Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
     * 
     * @param name
     *            name of the variable, cannot be null.
     */
    ProcessInstanceQuery variableValueEquals(String name, Object value);

    /**
     * Only select process instances which have at least one global variable with the given value. The type of variable is determined based on the value, using types configured in
     * {@link org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl#getVariableTypes()}. Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
     */
    ProcessInstanceQuery variableValueEquals(Object value);

    /**
     * Only select process instances which have a local string variable with the given value, case insensitive.
     * <p>
     * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
     * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
     * </p>
     * 
     * @param name
     *            name of the variable, cannot be null.
     * @param value
     *            value of the variable, cannot be null.
     */
    ProcessInstanceQuery variableValueEqualsIgnoreCase(String name, String value);

    /**
     * Only select process instances which have a global variable with the given name, but with a different value than the passed value. Byte-arrays and {@link Serializable} objects (which are not
     * primitive type wrappers) are not supported.
     * 
     * @param name
     *            name of the variable, cannot be null.
     */
    ProcessInstanceQuery variableValueNotEquals(String name, Object value);

    /**
     * Only select process instances which have a local string variable which is not the given value, case insensitive.
     * <p>
     * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
     * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
     * </p>
     * 
     * @param name
     *            name of the variable, cannot be null.
     * @param value
     *            value of the variable, cannot be null.
     */
    ProcessInstanceQuery variableValueNotEqualsIgnoreCase(String name, String value);

    /**
     * Only select process instances which have a variable value greater than the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not
     * supported.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null.
     */
    ProcessInstanceQuery variableValueGreaterThan(String name, Object value);

    /**
     * Only select process instances which have a global variable value greater than or equal to the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type
     * wrappers) are not supported.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null.
     */
    ProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);

    /**
     * Only select process instances which have a global variable value less than the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are
     * not supported.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null.
     */
    ProcessInstanceQuery variableValueLessThan(String name, Object value);

    /**
     * Only select process instances which have a global variable value less than or equal to the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type
     * wrappers) are not supported.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null.
     */
    ProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value);

    /**
     * Only select process instances which have a global variable value like the given value. This be used on string variables only.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null. The string can include the wildcard character '%' to express like-strategy: starts with (string%), ends with (%string) or contains (%string%).
     */
    ProcessInstanceQuery variableValueLike(String name, String value);

    /**
     * Only select process instances which have a global variable value like the given value (case insensitive). This be used on string variables only.
     * 
     * @param name
     *            variable name, cannot be null.
     * @param value
     *            variable value, cannot be null. The string can include the wildcard character '%' to express like-strategy: starts with (string%), ends with (%string) or contains (%string%).
     */
    ProcessInstanceQuery variableValueLikeIgnoreCase(String name, String value);
    
    /**
     * Only select process instances which have a variable with the given name.
     * 
     * @param name
     *            cannot be null.
     */
    ProcessInstanceQuery variableExists(String name);
    
    /**
     * Only select process instances which does not have a variable with the given name.
     * 
     * @param name
     *            cannot be null.
     */
    ProcessInstanceQuery variableNotExists(String name);

    /**
     * Only select process instances which are suspended, either because the process instance itself is suspended or because the corresponding process definition is suspended
     */
    ProcessInstanceQuery suspended();

    /**
     * Only select process instances which are active, which means that neither the process instance nor the corresponding process definition are suspended.
     */
    ProcessInstanceQuery active();

    /**
     * Only select process instances with the given name.
     */
    ProcessInstanceQuery processInstanceName(String name);

    /**
     * Only select process instances with a name like the given value.
     */
    ProcessInstanceQuery processInstanceNameLike(String nameLike);

    /**
     * Only select process instances with a name like the given value, ignoring upper/lower case.
     */
    ProcessInstanceQuery processInstanceNameLikeIgnoreCase(String nameLikeIgnoreCase);
    

    /**
     * Only select process instances with a root instance with a name like the given value.
     */
    ProcessInstanceQuery processInstanceRootScopeId(String rootScopeId);

    /**
     * Only select process instances with the given parent process instance id.
     */
    ProcessInstanceQuery processInstanceParentScopeId(String parentScopeId);

    /**
     * Only select process instances with the given callback id. 
     */
    ProcessInstanceQuery processInstanceCallbackId(String callbackId);
    
    /**
     * Only select process instances with the given callback type.
     */
    ProcessInstanceQuery processInstanceCallbackType(String callbackType);
    
    /**
     * Only select process instances that are a sub process of the provided case instance id. 
     */
    ProcessInstanceQuery parentCaseInstanceId(String parentCaseInstanceId);

    /**
     * Only select process instances with the given reference id.
     */
    ProcessInstanceQuery processInstanceReferenceId(String referenceId);

    /**
     * Only select process instances with the given callback type.
     */
    ProcessInstanceQuery processInstanceReferenceType(String referenceType);

    /**
     * Localize process name and description to specified locale.
     */
    ProcessInstanceQuery locale(String locale);

    /**
     * Instruct localization to fallback to more general locales including the default locale of the JVM if the specified locale is not found.
     */
    ProcessInstanceQuery withLocalizationFallback();

    /**
     * Include process variables in the process query result
     */
    ProcessInstanceQuery includeProcessVariables();

    /**
     * Include the process variables with the given names into the query result.
     */
    ProcessInstanceQuery includeProcessVariables(Collection<String> variableNames);

    /**
     * Only select process instances that failed due to an exception happening during a job execution.
     */
    ProcessInstanceQuery withJobException();

    /**
     * Begin an OR statement. Make sure you invoke the endOr method at the end of your OR statement. Only one OR statement is allowed, for the second call to this method an exception will be thrown.
     */
    ProcessInstanceQuery or();

    /**
     * End an OR statement. Only one OR statement is allowed, for the second call to this method an exception will be thrown.
     */
    ProcessInstanceQuery endOr();

    /**
     * Only select process instances started before the given time
     */
    ProcessInstanceQuery startedBefore(Date beforeTime);

    /**
     * Only select process instances started after the given time
     */
    ProcessInstanceQuery startedAfter(Date afterTime);

    /**
     * Only select process instances started by the given user id
     */
    ProcessInstanceQuery startedBy(String userId);

    // ordering
    // /////////////////////////////////////////////////////////////////

    /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
    ProcessInstanceQuery orderByProcessInstanceId();

    /**
     * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessInstanceQuery orderByProcessDefinitionKey();

    /**
     * Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessInstanceQuery orderByProcessDefinitionId();

    /**
     * Order by start time (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessInstanceQuery orderByStartTime();

    /**
     * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessInstanceQuery orderByTenantId();
}
