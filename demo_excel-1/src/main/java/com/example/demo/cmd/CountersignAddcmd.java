package com.example.demo.cmd;




import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;

import com.example.demo.Behavior.MyUserTaskActivityBehavior;

public class CountersignAddcmd implements Command<Void>{

	protected String taskId;
	
	protected String assignee;
	
    private RuntimeService runtimeService;
	
    private TaskService taskService;
    
    
	
	public CountersignAddcmd(String taskId, String assignee, RuntimeService runtimeService, TaskService taskService) {
		super();
		this.taskId = taskId;
		this.assignee = assignee;
		this.runtimeService = runtimeService;
		this.taskService = taskService;
	}



	@Override
	public Void execute(CommandContext commandContext) {
		Task task=taskService.createTaskQuery().taskId(taskId).active().singleResult();
		Execution execution=runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
		ExecutionEntity ee=(ExecutionEntity) execution;
		//根据父节点创建执行实例
		ExecutionEntity parent=ee.getParent();
		ExecutionEntity newExecution=parent.createExecution();
		newExecution.setActive(true);//设置为激活
		newExecution.setConcurrent(true);//设置为不可缺少
		newExecution.setScope(false);
		TaskEntity te=(TaskEntity) task;
		MyUserTaskActivityBehavior utab=new MyUserTaskActivityBehavior(te.getTaskDefinitionKey(),te.getTaskDefinition());
		try {
			utab.execute(newExecution,assignee);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//修改流程变量
		int nrOfInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfInstances");
		int nrOfActiveInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfActiveInstances");
		runtimeService.setVariable(parent.getId(), "nrOfInstances", nrOfInstances+1);
		runtimeService.setVariable(parent.getId(), "nrOfActiveInstances", nrOfActiveInstances+1);
		newExecution.setVariableLocal("loopCounter", nrOfInstances);
		newExecution.setVariableLocal("assignee", assignee);
		return null;
	}

}
