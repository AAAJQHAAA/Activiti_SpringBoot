package com.example.demo.cmd;

import java.util.Map;

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.task.IdentityLinkType;

public class BackAndJumpCmd implements Command<String>{

	String currentTaskId;
	
	String jumpToActivityId;
	
	String operate;
	
	Map<String, Object> vars;
	
	public BackAndJumpCmd(String currentTaskId, String jumpToActivityId, String operate, Map<String, Object> vars) {
		super();
		this.currentTaskId = currentTaskId;
		this.jumpToActivityId = jumpToActivityId;
		this.operate = operate;
		this.vars = vars;
	}

	@SuppressWarnings("unused")
	@Override
	public String execute(CommandContext commandContext) {
		TaskEntity currentTask = commandContext.getTaskEntityManager().findTaskById(currentTaskId);
		ExecutionEntity ee = currentTask.getExecution();
		if (currentTask == null) {
			return "没有该任务" + currentTaskId;
		}
		if (vars != null) {
			currentTask.setVariablesLocal(vars);
			currentTask.setVariables(vars);
		}
		// 根据操作operate判断是删除还是完成任务
		if ("delete".equals(operate)) {
			//删除任务
			commandContext.getTaskEntityManager().deleteTask(currentTask, "jumpTo:" + jumpToActivityId, false);
			ee.removeTask(currentTask);
		} else if ("complete".equals(operate)) {
			//完成任务的一些操作
			currentTask.fireEvent(TaskListener.EVENTNAME_COMPLETE);
			if (Authentication.getAuthenticatedUserId() != null && currentTask.getProcessInstanceId() != null) {
				currentTask.getProcessInstance().involveUser(Authentication.getAuthenticatedUserId(),
						IdentityLinkType.PARTICIPANT);
			}
			if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
				Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder
						.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, this, vars, true));
			}
			commandContext.getTaskEntityManager().deleteTask(currentTask, TaskEntity.DELETE_REASON_COMPLETED, false);
			ee.removeTask(currentTask);
		}
		ProcessDefinitionImpl processDefinition = currentTask.getProcessInstance().getProcessDefinition();
		ActivityImpl activity = processDefinition.findActivity(jumpToActivityId);
		if (activity == null) {
			return "跳转节点不存在，跳转任务失败";
		}
		//创建新任务
		ee.setActivity(activity);
		ee.performOperation(AtomicOperation.ACTIVITY_START);
		return "跳转完成";
	}

}
