package com.example.demo.entity;

import java.util.Date;

public class Mytask {
	    /**
	     * 任务ID
	     */
	    protected String id;

	    /**
	     * 任务初始责任人，在任务代理/委派的情况下，才有值
	     */
	    protected String owner;

	    /**
	     * 任务分配人员
	     */
	    protected String assignee;

	    /**
	     * 父任务ID
	      */
	    protected String parentTaskId;

	    /**
	     * 任务名称
	     */
	    protected String name;

	    /**
	     * 任务描述
	     */
	    protected String description;

	    /**
	     * 优先级
	     */
	    protected int priority = 50;

	    /**
	     * 创建时间
	     */
	    protected Date createTime; // The time when the task has been created

	    /**
	     * 到期时间
	     */
	    protected Date dueDate;

	    /**
	     * 挂起状态，1 活动 2 挂起
	     */
	    protected int suspensionState;

	    /**
	     * 分组标识
	     */
	    protected String category;

	    /**
	     * 流程实例ID
	     */
	    protected String processInstanceId;

	    /**
	     * 执行id，一般和流程实例id相同，在出现网关分支任务时会有不同
	     */
	    protected String executionId;

	    /**
	     * 任务定义key/ID
	     */
	    protected String taskDefinitionKey;

	    /**
	     * 表单key，可用于存储表单信息
	     */
	    protected String formKey;

	    public String getOwner() {
	        return owner;
	    }

	    public void setOwner(String owner) {
	        this.owner = owner;
	    }

	    public String getAssignee() {
	        return assignee;
	    }

	    public void setAssignee(String assignee) {
	        this.assignee = assignee;
	    }

	    public String getParentTaskId() {
	        return parentTaskId;
	    }

	    public void setParentTaskId(String parentTaskId) {
	        this.parentTaskId = parentTaskId;
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

	    public int getPriority() {
	        return priority;
	    }

	    public void setPriority(int priority) {
	        this.priority = priority;
	    }

	    public Date getCreateTime() {
	        return createTime;
	    }

	    public void setCreateTime(Date createTime) {
	        this.createTime = createTime;
	    }

	    public Date getDueDate() {
	        return dueDate;
	    }

	    public void setDueDate(Date dueDate) {
	        this.dueDate = dueDate;
	    }

	    public int getSuspensionState() {
	        return suspensionState;
	    }

	    public void setSuspensionState(int suspensionState) {
	        this.suspensionState = suspensionState;
	    }

	    public String getCategory() {
	        return category;
	    }

	    public void setCategory(String category) {
	        this.category = category;
	    }

	    public String getExecutionId() {
	        return executionId;
	    }

	    public void setExecutionId(String executionId) {
	        this.executionId = executionId;
	    }

	    public String getTaskDefinitionKey() {
	        return taskDefinitionKey;
	    }

	    public void setTaskDefinitionKey(String taskDefinitionKey) {
	        this.taskDefinitionKey = taskDefinitionKey;
	    }

	    public String getFormKey() {
	        return formKey;
	    }

	    public void setFormKey(String formKey) {
	        this.formKey = formKey;
	    }

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getProcessInstanceId() {
	        return processInstanceId;
	    }

	    public void setProcessInstanceId(String processInstanceId) {
	        this.processInstanceId = processInstanceId;
	    }
	
}
