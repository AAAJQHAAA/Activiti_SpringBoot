package com.example.demo.cmd;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;

import com.example.demo.Behavior.MySequentialMultiInstanceBehavior;

public class SerialCountersignAddcmd implements Command<Void>{

	protected String taskId;
	
	protected String assignee;
	
    private RuntimeService runtimeService;
	
    private TaskService taskService;
    
    private Boolean isBefore;
    
    
	public SerialCountersignAddcmd(String taskId, String assignee, RuntimeService runtimeService, TaskService taskService,Boolean isBefore) {
		super();
		this.taskId = taskId;
		this.assignee = assignee;
		this.runtimeService = runtimeService;
		this.taskService = taskService;
		this.isBefore = isBefore;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Void execute(CommandContext commandContext) {
		Task task=taskService.createTaskQuery().taskId(taskId).active().singleResult();
		ProcessDefinitionEntity deployedProcessDefinition = commandContext
		        									.getProcessEngineConfiguration()
		        									.getDeploymentManager()
		        									.findDeployedProcessDefinitionById(task.getProcessDefinitionId());
		ActivityImpl activity=deployedProcessDefinition.findActivity(task.getTaskDefinitionKey());
		Execution execution=runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
		ExecutionEntity ee=(ExecutionEntity) execution;
		TaskEntity te=(TaskEntity) task;
		
		//修改流程变量
		List<String> assigneeList=new ArrayList<>();
		assigneeList=(List<String>) runtimeService.getVariable(ee.getId(), "assigneeList");
		if(isBefore) {
			//插入指定位置之前
			assigneeList.add(assigneeList.indexOf(te.getAssignee()),assignee);
		}else {
			//插入指定位置之后
			assigneeList.add(assigneeList.indexOf(te.getAssignee())+1,assignee);
		}
		runtimeService.setVariable(ee.getId(), "assigneeList", assigneeList);
		//将现有任务删除
		commandContext.getTaskEntityManager().deleteTask(te, TaskEntity.DELETE_REASON_DELETED, true);
		ee.removeTask(te);
		//给Activity设置自定义behavior
		SequentialMultiInstanceBehavior sequentialMultiInstanceBehavior=(SequentialMultiInstanceBehavior)activity.getActivityBehavior();
		AbstractBpmnActivityBehavior userTaskActivityBehavior=sequentialMultiInstanceBehavior.getInnerActivityBehavior();
		MySequentialMultiInstanceBehavior mySequentialMultiInstanceBehavior=new MySequentialMultiInstanceBehavior(activity,userTaskActivityBehavior,true);
		BeanUtils.copyProperties(sequentialMultiInstanceBehavior, mySequentialMultiInstanceBehavior);
		userTaskActivityBehavior.setMultiInstanceActivityBehavior(mySequentialMultiInstanceBehavior);
		activity.setActivityBehavior(mySequentialMultiInstanceBehavior);
		//执行实例设置Activity，再转信号执行
		ee.setActivity(activity);
		ee.signal(null, null);
		//要将Behavior重写一下，实现串行加签功能

		return null;
	}
}
