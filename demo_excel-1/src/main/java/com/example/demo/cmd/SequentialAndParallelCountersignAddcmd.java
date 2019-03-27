package com.example.demo.cmd;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Behavior.MyParallelMultiInstanceBehavior;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.el.JuelExpression;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;

import com.example.demo.Behavior.MySequentialMultiInstanceBehavior;

public class SequentialAndParallelCountersignAddcmd implements Command<String>{

	protected String taskId;
	
	protected String assignee;

    private RuntimeService runtimeService;

    private TaskService taskService;
    
    private Boolean isBefore;
    
    
	public SequentialAndParallelCountersignAddcmd(String taskId, String assignee, RuntimeService runtimeService, TaskService taskService, Boolean isBefore) {
		super();
		this.taskId = taskId;
		this.assignee = assignee;
		this.runtimeService = runtimeService;
		this.taskService = taskService;
		this.isBefore = isBefore;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String execute(CommandContext commandContext) {
		Task task=taskService.createTaskQuery().taskId(taskId).active().singleResult();
		ProcessDefinitionEntity deployedProcessDefinition = commandContext
		        									.getProcessEngineConfiguration()
		        									.getDeploymentManager()
		        									.findDeployedProcessDefinitionById(task.getProcessDefinitionId());
		ActivityImpl activity=deployedProcessDefinition.findActivity(task.getTaskDefinitionKey());
		ActivityBehavior activityBehavior=activity.getActivityBehavior();
		Execution execution=runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
		ExecutionEntity ee=(ExecutionEntity) execution;
		TaskEntity te=(TaskEntity) task;
		String expressionCollectionName=getCollectionName(activity);
		if (activityBehavior instanceof SequentialMultiInstanceBehavior) {
			//串行前或后加签
			//将现有任务删除
			commandContext.getTaskEntityManager().deleteTask(te, TaskEntity.DELETE_REASON_DELETED, true);
			ee.removeTask(te);
			//给Activity设置自定义behavior
			SequentialMultiInstanceBehavior sequentialMultiInstanceBehavior=(SequentialMultiInstanceBehavior)activityBehavior;
			AbstractBpmnActivityBehavior userTaskActivityBehavior=sequentialMultiInstanceBehavior.getInnerActivityBehavior();
			MySequentialMultiInstanceBehavior mySequentialMultiInstanceBehavior=new MySequentialMultiInstanceBehavior(activity,userTaskActivityBehavior,true);
			BeanUtils.copyProperties(sequentialMultiInstanceBehavior, mySequentialMultiInstanceBehavior);
			userTaskActivityBehavior.setMultiInstanceActivityBehavior(mySequentialMultiInstanceBehavior);
			activity.setActivityBehavior(mySequentialMultiInstanceBehavior);
			//修改流程变量
			List<Object> assigneeList=new ArrayList<>();
			assigneeList=(List<Object>) runtimeService.getVariable(ee.getId(), expressionCollectionName);
			int index=assigneeList.indexOf(Integer.parseInt(te.getAssignee()));
			if(index<0){
				index=assigneeList.indexOf(te.getAssignee());
			}
			if(isBefore) {
				//插入指定位置之前
				assigneeList.add(index,assignee);
			}else {
				//插入指定位置之后
				assigneeList.add(index+1,assignee);
			}
			runtimeService.setVariable(ee.getId(), expressionCollectionName, assigneeList);
			//执行实例设置Activity，再转信号执行
			ee.setActivity(activity);
			ee.signal(null, null);
			//要将Behavior重写一下，实现串行加签功能
		}else if(activityBehavior instanceof ParallelMultiInstanceBehavior){
			//并行会签加签
			//根据父节点创建执行实例
			ExecutionEntity parent=ee.getParent();
			ExecutionEntity newExecution=parent.createExecution();
			newExecution.setActive(true);//设置为激活
			newExecution.setConcurrent(true);//设置为不可缺少
			newExecution.setScope(false);
			//修改流程变量
			List<Object> assigneeList=new ArrayList<>();
			assigneeList=(List<Object>) runtimeService.getVariable(ee.getId(), expressionCollectionName);
			assigneeList.add(assignee);
			runtimeService.setVariable(parent.getId(), expressionCollectionName, assigneeList);
			int nrOfInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfInstances");
			int nrOfActiveInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfActiveInstances");
			runtimeService.setVariable(parent.getId(), "nrOfInstances", nrOfInstances+1);
			runtimeService.setVariable(parent.getId(), "nrOfActiveInstances", nrOfActiveInstances+1);
			newExecution.setVariableLocal("loopCounter", nrOfInstances);
			newExecution.setVariableLocal("assignee", assignee);

			ParallelMultiInstanceBehavior parallelMultiInstanceBehavior=(ParallelMultiInstanceBehavior)activityBehavior;
			AbstractBpmnActivityBehavior userTaskActivityBehavior=parallelMultiInstanceBehavior.getInnerActivityBehavior();
			MyParallelMultiInstanceBehavior myParallelMultiInstanceBehavior=new MyParallelMultiInstanceBehavior(activity,userTaskActivityBehavior);
			BeanUtils.copyProperties(parallelMultiInstanceBehavior, myParallelMultiInstanceBehavior);
			userTaskActivityBehavior.setMultiInstanceActivityBehavior(myParallelMultiInstanceBehavior);
			try {
				myParallelMultiInstanceBehavior.myexecuteOriginalBehavior(newExecution, nrOfInstances);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			return "该任务不是会签节点";
		}
		return "加签成功";
	}

	/**
	 * 找多实例Activity中设置的集合名字
	 *
	 * @param activity
	 * @return
	 */
	public String getCollectionName(ActivityImpl activity){

		JuelExpression collectionExpression=null;
		if (activity.getActivityBehavior() instanceof SequentialMultiInstanceBehavior) {
			SequentialMultiInstanceBehavior sequentialMultiInstanceBehavior = (SequentialMultiInstanceBehavior) activity.getActivityBehavior();
			collectionExpression = (JuelExpression) sequentialMultiInstanceBehavior.getCollectionExpression();
		}else if(activity.getActivityBehavior() instanceof ParallelMultiInstanceBehavior){
			ParallelMultiInstanceBehavior parallelMultiInstanceBehavior = (ParallelMultiInstanceBehavior) activity.getActivityBehavior();
			collectionExpression = (JuelExpression) parallelMultiInstanceBehavior.getCollectionExpression();
		}
		String expressionText=collectionExpression.getExpressionText();
		expressionText=expressionText.replace("$", "");
		expressionText=expressionText.replace("{", "");
		expressionText=expressionText.replace("}", "");
		return expressionText;
	}
}
