package com.example.demo.entity;

import java.util.Map;

public class MyProcessInstance {

	protected String id;
	  
	  

	  protected boolean ended;
	  

	  protected String activityId;
	  

	  protected String processInstanceId;
	  

	  protected String parentId;
	  

	  protected String superExecutionId;
	  
	 

	protected String processDefinitionId;
	  

	protected String processDefinitionName;
	  

	protected String processDefinitionKey;
	  

	protected Integer processDefinitionVersion;
	  

	protected String deploymentId;
	  

	protected String businessKey;
	  

	protected boolean suspended;
	  

	protected Map<String, Object> processVariables;
	  

	protected String tenantId;
	  

	protected String name;
	  

	protected String description;
	  

	protected String localizedName;
	  

	protected String localizedDescription;


	public String getProcessDefinitionId() {
		return processDefinitionId;
	}


	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}


	public String getProcessDefinitionName() {
		return processDefinitionName;
	}


	public void setProcessDefinitionName(String processDefinitionName) {
		this.processDefinitionName = processDefinitionName;
	}


	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}


	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}


	public Integer getProcessDefinitionVersion() {
		return processDefinitionVersion;
	}


	public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
		this.processDefinitionVersion = processDefinitionVersion;
	}


	public String getDeploymentId() {
		return deploymentId;
	}


	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}


	public String getBusinessKey() {
		return businessKey;
	}


	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}


	public boolean isSuspended() {
		return suspended;
	}


	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}


	public Map<String, Object> getProcessVariables() {
		return processVariables;
	}


	public void setProcessVariables(Map<String, Object> processVariables) {
		this.processVariables = processVariables;
	}


	public String getTenantId() {
		return tenantId;
	}


	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getLocalizedName() {
		return localizedName;
	}


	public void setLocalizedName(String localizedName) {
		this.localizedName = localizedName;
	}


	public String getLocalizedDescription() {
		return localizedDescription;
	}


	public void setLocalizedDescription(String localizedDescription) {
		this.localizedDescription = localizedDescription;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public boolean isEnded() {
		return ended;
	}


	public void setEnded(boolean ended) {
		this.ended = ended;
	}


	public String getActivityId() {
		return activityId;
	}


	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}


	public String getProcessInstanceId() {
		return processInstanceId;
	}


	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}


	public String getParentId() {
		return parentId;
	}


	public void setParentId(String parentId) {
		this.parentId = parentId;
	}


	public String getSuperExecutionId() {
		return superExecutionId;
	}


	public void setSuperExecutionId(String superExecutionId) {
		this.superExecutionId = superExecutionId;
	}
	
}
