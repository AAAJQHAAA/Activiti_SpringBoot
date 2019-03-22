package com.example.demo.Behavior;

import java.util.Date;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MyUserTaskActivityBehavior extends UserTaskActivityBehavior {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskActivityBehavior.class);
	
	public MyUserTaskActivityBehavior(String userTaskId, TaskDefinition taskDefinition) {
		super(userTaskId, taskDefinition);
	}

	public void execute(ActivityExecution execution,String assignee) throws Exception {
	    TaskEntity task = TaskEntity.createAndInsert(execution);
	    task.setExecution(execution);
	    task.setAssignee(assignee);
	    
	    Expression activeNameExpression = null;
	    Expression activeDescriptionExpression = null;
	    Expression activeDueDateExpression = null;
	    Expression activePriorityExpression = null;
	    Expression activeCategoryExpression = null;
	    Expression activeFormKeyExpression = null;
	    Expression activeSkipExpression = null;
	    Expression activeAssigneeExpression = null;
	    Expression activeOwnerExpression = null;
	    Set<Expression> activeCandidateUserExpressions = null;
	    Set<Expression> activeCandidateGroupExpressions = null;
	    
	    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
	      ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(userTaskId, execution.getProcessDefinitionId());
	      activeNameExpression = getActiveValue(taskDefinition.getNameExpression(), DynamicBpmnConstants.USER_TASK_NAME, taskElementProperties);
	      taskDefinition.setNameExpression(activeNameExpression);
	      activeDescriptionExpression = getActiveValue(taskDefinition.getDescriptionExpression(), DynamicBpmnConstants.USER_TASK_DESCRIPTION, taskElementProperties);
	      taskDefinition.setDescriptionExpression(activeDescriptionExpression);
	      activeDueDateExpression = getActiveValue(taskDefinition.getDueDateExpression(), DynamicBpmnConstants.USER_TASK_DUEDATE, taskElementProperties);
	      taskDefinition.setDueDateExpression(activeDueDateExpression);
	      activePriorityExpression = getActiveValue(taskDefinition.getPriorityExpression(), DynamicBpmnConstants.USER_TASK_PRIORITY, taskElementProperties);
	      taskDefinition.setPriorityExpression(activePriorityExpression);
	      activeCategoryExpression = getActiveValue(taskDefinition.getCategoryExpression(), DynamicBpmnConstants.USER_TASK_CATEGORY, taskElementProperties);
	      taskDefinition.setCategoryExpression(activeCategoryExpression);
	      activeFormKeyExpression = getActiveValue(taskDefinition.getFormKeyExpression(), DynamicBpmnConstants.USER_TASK_FORM_KEY, taskElementProperties);
	      taskDefinition.setFormKeyExpression(activeFormKeyExpression);
	      activeSkipExpression = getActiveValue(taskDefinition.getSkipExpression(), DynamicBpmnConstants.TASK_SKIP_EXPRESSION, taskElementProperties);
	      taskDefinition.setSkipExpression(activeSkipExpression);
//	      activeAssigneeExpression = getActiveValue(taskDefinition.getAssigneeExpression(), DynamicBpmnConstants.USER_TASK_ASSIGNEE, taskElementProperties);
//	      taskDefinition.setAssigneeExpression(activeAssigneeExpression);
	      activeOwnerExpression = getActiveValue(taskDefinition.getOwnerExpression(), DynamicBpmnConstants.USER_TASK_OWNER, taskElementProperties);
	      taskDefinition.setOwnerExpression(activeOwnerExpression);
	      activeCandidateUserExpressions = getActiveValueSet(taskDefinition.getCandidateUserIdExpressions(), DynamicBpmnConstants.USER_TASK_CANDIDATE_USERS, taskElementProperties);
	      taskDefinition.setCandidateUserIdExpressions(activeCandidateUserExpressions);
	      activeCandidateGroupExpressions = getActiveValueSet(taskDefinition.getCandidateGroupIdExpressions(), DynamicBpmnConstants.USER_TASK_CANDIDATE_GROUPS, taskElementProperties);
	      taskDefinition.setCandidateGroupIdExpressions(activeCandidateGroupExpressions);
	      
	    } else {
	      activeNameExpression = taskDefinition.getNameExpression();
	      activeDescriptionExpression = taskDefinition.getDescriptionExpression();
	      activeDueDateExpression = taskDefinition.getDueDateExpression();
	      activePriorityExpression = taskDefinition.getPriorityExpression();
	      activeCategoryExpression = taskDefinition.getCategoryExpression();
	      activeFormKeyExpression = taskDefinition.getFormKeyExpression();
	      activeSkipExpression = taskDefinition.getSkipExpression();
//	      activeAssigneeExpression = taskDefinition.getAssigneeExpression();
	      activeOwnerExpression = taskDefinition.getOwnerExpression();
	      activeCandidateUserExpressions = taskDefinition.getCandidateUserIdExpressions();
	      activeCandidateGroupExpressions = taskDefinition.getCandidateGroupIdExpressions();
	    }
	    
	    task.setTaskDefinition(taskDefinition);

	    if (activeNameExpression != null) {
	      String name = null;
	      try {
	        name = (String) activeNameExpression.getValue(execution);
	      } catch (ActivitiException e) {
	        name = activeNameExpression.getExpressionText();
	        LOGGER.warn("property not found in task name expression " + e.getMessage());
	      }
	      task.setName(name);
	    }

	    if (activeDescriptionExpression != null) {
	      String description = null;
	      try {
	        description = (String) activeDescriptionExpression.getValue(execution);
	      } catch (ActivitiException e) {
	        description = activeDescriptionExpression.getExpressionText();
	        LOGGER.warn("property not found in task description expression " + e.getMessage());
	      }
	      task.setDescription(description);
	    }
	    
	    if (activeDueDateExpression != null) {
	      Object dueDate = activeDueDateExpression.getValue(execution);
	      if (dueDate != null) {
	        if (dueDate instanceof Date) {
	          task.setDueDate((Date) dueDate);
	        } else if (dueDate instanceof String) {
	          BusinessCalendar businessCalendar = Context
	            .getProcessEngineConfiguration()
	            .getBusinessCalendarManager()
	            .getBusinessCalendar(taskDefinition.getBusinessCalendarNameExpression().getValue(execution).toString());
	          task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
	        } else {
	          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + 
	              activeDueDateExpression.getExpressionText());
	        }
	      }
	    }

	    if (activePriorityExpression != null) {
	      final Object priority = activePriorityExpression.getValue(execution);
	      if (priority != null) {
	        if (priority instanceof String) {
	          try {
	            task.setPriority(Integer.valueOf((String) priority));
	          } catch (NumberFormatException e) {
	            throw new ActivitiIllegalArgumentException("Priority does not resolve to a number: " + priority, e);
	          }
	        } else if (priority instanceof Number) {
	          task.setPriority(((Number) priority).intValue());
	        } else {
	          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + 
	              activePriorityExpression.getExpressionText());
	        }
	      }
	    }
	    
	    if (activeCategoryExpression != null) {
	    	final Object category = activeCategoryExpression.getValue(execution);
	    	if (category != null) {
	    		if (category instanceof String) {
	    			task.setCategory((String) category);
	    		} else {
	    			 throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " + 
	    			     activeCategoryExpression.getExpressionText());
	    		}
	    	}
	    }
	    
	    if (activeFormKeyExpression != null) {
	    	final Object formKey = activeFormKeyExpression.getValue(execution);
	    	if (formKey != null) {
	    		if (formKey instanceof String) {
	    			task.setFormKey((String) formKey);
	    		} else {
	    		  throw new ActivitiIllegalArgumentException("FormKey expression does not resolve to a string: " + 
	    		      activeFormKeyExpression.getExpressionText());
	    		}
	    	}
	    }
	    
	    boolean skipUserTask = SkipExpressionUtil.isSkipExpressionEnabled(execution, activeSkipExpression) &&
	        SkipExpressionUtil.shouldSkipFlowElement(execution, activeSkipExpression);
	    
	    if (!skipUserTask) {
	      handleAssignments(activeAssigneeExpression, activeOwnerExpression, activeCandidateUserExpressions, 
	        activeCandidateGroupExpressions, task, execution);
	    }

	    task.fireEvent(TaskListener.EVENTNAME_CREATE);

	    // All properties set, now firing 'create' events
	    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
	      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
	        ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
	    }

	    if (skipUserTask) {
	      task.complete(null, false);
	    }
	  }	
}