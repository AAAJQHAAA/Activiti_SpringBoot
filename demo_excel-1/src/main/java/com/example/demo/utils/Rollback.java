package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Rollback {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private RepositoryService repositoryService;

	// 退回上一个节点
	public String rollBackWorkFlow(String taskId) {

		try {
			Map<String, Object> variables;
			// 根据任务id取得当前任务.当前任务节点(act_hi_taskinst)
			HistoricTaskInstance currTask =historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
			// 根据流程实例id取得流程实例，流程实例(act_ru_execution)
			ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
			if (instance == null) {
				return "流程实例已经结束或者出了问题";
			}
			variables = instance.getProcessVariables();
			// 根据流程定义的id取得流程定义(act_re_procdef)
			ProcessDefinitionEntity definition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(currTask.getProcessDefinitionId());
			if (definition == null) {
				return "流程定义不见了";
			}
			
			// 根据任务定义key取得当前任务对应活动(流程定义的节点)
			ActivityImpl currActivity = definition.findActivity(currTask.getTaskDefinitionKey());
			// （获取节点所有出线）
			List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
			List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
			//将出线保存在oriPvmTransitionList集合中********************************************
			for (PvmTransition pvmTransition : pvmTransitionList) {
				oriPvmTransitionList.add(pvmTransition);
			}
			// 清除当前活动的出口
			pvmTransitionList.clear();
			
			// （获取节点所有入线）（当前节点与上一节点的连线保存到nextTransitionList集合中）入口
			List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();
			// 建立新出口（新出口是上一个节点）
			List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
			for (PvmTransition nextTransition : nextTransitionList) {
				//入线的起点节点将是下一个节点，（根据定义节点id获取上一个节点）
				PvmActivity nextActivity = nextTransition.getSource();
				ActivityImpl nextActivityImpl = definition.findActivity(nextActivity.getId());
				//当前节点创建出线，设置上一节点为出线指向的目标节点
				TransitionImpl newTransition = currActivity.createOutgoingTransition();
				newTransition.setDestination(nextActivityImpl);
				//将新线保存在newTransitions集合中********************************
				newTransitions.add(newTransition);
			}
			
			// 完成当前任务（会跳到上一节点）
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
			for (Task task : tasks) {
				taskService.complete(task.getId(), variables);
				//删除历史任务表中的任务实例（act_hi_taskinst）
				historyService.deleteHistoricTaskInstance(task.getId());
			}
			
			// 恢复方向，去掉该节点指向上上一节点的线
			for (TransitionImpl transitionImpl : newTransitions) {
				currActivity.getOutgoingTransitions().remove(transitionImpl);
			}
			//将之前去除的该节点指向的下一节点的线添加
			for (PvmTransition pvmTransition : oriPvmTransitionList) {
				pvmTransitionList.add(pvmTransition);
			}
			return "SUCCESS";
		} catch (Exception e) {
			return "ERROR";
		}
	}
	// 回退到指定节点（destTaskkey：Destination的key，就是任务定义id/key,跳转可能是跳转到还没执行过的节点）
	public String rollBackToAssignWorkFlow(String taskId, String destTaskkey) {
		try {
			Map<String, Object> variables;
			// 取得当前任务.当前任务节点
			HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
			// 取得流程实例，流程实例
			ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
			if (instance == null) {
				return "ERROR";
			}
			variables = instance.getProcessVariables();
			// 取得流程定义实体
			ProcessDefinitionEntity definition = (ProcessDefinitionEntity) repositoryService
					.getProcessDefinition(currTask.getProcessDefinitionId());
			if (definition == null) {
				return "ERROR";
			}
			// 取得当前活动节点
			ActivityImpl currActivity = definition.findActivity(currTask.getTaskDefinitionKey());
			// 取得上一步活动
			// 也就是节点间的连线
			// 获取来源节点的关系
			//			List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();
			
			// 清除当前活动的出口，并保存
			List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
			List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
			for (PvmTransition pvmTransition : pvmTransitionList) {
				oriPvmTransitionList.add(pvmTransition);
			}
			pvmTransitionList.clear();
			
			// 建立新出口
			List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
			
			ActivityImpl nextActivityImpl = definition.findActivity(destTaskkey);
			TransitionImpl newTransition = currActivity.createOutgoingTransition();
			newTransition.setDestination(nextActivityImpl);
			
			newTransitions.add(newTransition);
			// 完成任务
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
			for (Task task : tasks) {
				taskService.complete(task.getId(), variables);
				//historyService.deleteHistoricTaskInstance(task.getId());
			}
			// 恢复方向
			for (TransitionImpl transitionImpl : newTransitions) {
				currActivity.getOutgoingTransitions().remove(transitionImpl);
			}
			for (PvmTransition pvmTransition : oriPvmTransitionList) {

				pvmTransitionList.add(pvmTransition);
			}

			return "SUCCESS";
		} catch (Exception e) {
			return "ERROR";
		}
	}
}
