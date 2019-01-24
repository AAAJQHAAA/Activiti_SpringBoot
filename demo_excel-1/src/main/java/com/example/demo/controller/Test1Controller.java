package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MyProcessDefinition;
import com.example.demo.entity.MyProcessInstance;
import com.example.demo.entity.Mycustom;
import com.example.demo.entity.Mytask;
import com.example.demo.entity.UserTaskProDef;
import com.example.demo.mycustom.MycustomService;
import com.example.demo.utils.ProcessDefinitionTOmyProcessDefinition;
import com.example.demo.utils.ProcessInstanceTOmyProcessInstance;
import com.example.demo.utils.Rollback;
import com.example.demo.utils.TasktoMyTask;
import com.example.demo.utils.UserTaskToUserTaskProDef;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/activiti/test1")
public class Test1Controller {

	//ProcessEngineConfigurationImpl ProcessEngineConfigurationImpl;
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
    /**
     * 查询已经部署的流程定义（获取key）
     * （流程定义表act_re_procdef）
     * @return	List<MyProcessDefinition>
     */
    @ApiOperation(value = "ProcessDefinitionQueryKEY", notes = "流程定义查询key")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/ProcessDefinitionQuery")
    public List<MyProcessDefinition> ProcessDefinitionQuery() {
    	List<ProcessDefinition> list =repositoryService.createProcessDefinitionQuery()
										.orderByProcessDefinitionVersion().desc()
										.list();
        return ProcessDefinitionTOmyProcessDefinition.convertMyProcessDefinition(list);
    }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks/{processDefIdkey}")
    public List<UserTaskProDef> userTasks(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> result = new ArrayList<>();
        List<UserTask> result2 = new ArrayList<>();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	//如果是任务节点
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                    	result.add(sequenceFlow);
                    }
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		result2.add(userTask);
                    }
                }
            }
        }
        return userTaskGetPosition(result2);
    }
    //根据找出排序信息
    private List<UserTaskProDef> userTaskGetPosition(List<UserTask> uts) {
    	List<UserTaskProDef> userTaskProDef=UserTaskToUserTaskProDef.convertMytask(uts);
    		for(UserTaskProDef u:userTaskProDef) {
    			//如果任务（入线只有一根）并且（开始节点为"startevent1"），则其目标任务记为"1"
    			if(u.getIncomingFlows().size()==1&&u.getIncomingFlows().get(0).getSourceRef().equals("startevent1")) {
    				//第一个任务节点设置为1
    				u.getPosition().add("1");
    				for(int j=0;j<u.getOutgoingFlows().size();j++) {
    					for(UserTaskProDef u2:userTaskProDef) {
        					if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
        						u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
        					}
        				}
    				}
    			}else{//任务不是第一个任务节点
    					for(int j=0;j<u.getOutgoingFlows().size();j++) {
    						for(UserTaskProDef u2:userTaskProDef) {
        						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
        							if(u.getPosition().size()>1) {
            							for(int i=0;i<u.getPosition().size();i++) {
                							u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
            							}
        							}else {
            							u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
        							}
        						}
        					}
    					}
 				}
    		}
    		//写入因顺序影响的节点位置
    		for(UserTaskProDef u:userTaskProDef) {
    				if(u.getPosition().size()>1) {
    					for(int j=0;j<u.getOutgoingFlows().size();j++) {
    						for(UserTaskProDef u2:userTaskProDef) {
        						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())&&u.getPosition().size()>u2.getPosition().size()&&u2.getIncomingFlows().size()==1) {
        								u2.getPosition().clear();
            							for(int i=0;i<u.getPosition().size();i++) {
                							u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
            							}
        						}
        					}
    					}
    			}
    		}
    	return userTaskProDef;
   }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks2/{processDefIdkey}")
    public List<UserTaskProDef> userTasks2(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> result = new ArrayList<>();
        List<UserTask> result2 = new ArrayList<>();
        List<Gateway> result3 = new ArrayList<>();
        StartEvent start=new StartEvent();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	if(e instanceof StartEvent) {
                		start=(StartEvent)e;
                	}
                	//如果是线
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                    	result.add(sequenceFlow);
                    }
                	//如果是任务节点
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		result2.add(userTask);
                    }
                	//如果是网关
                	if(e instanceof Gateway) {
                		result3.add((Gateway)e);
                	}
                }
            }
        }
//        return result;
        return userTaskGetPosition2(start,result2,result3);
    }
    //根据找出排序信息
    private List<UserTaskProDef> userTaskGetPosition2(StartEvent start, List<UserTask> uts, List<Gateway> gts) {
    	List<UserTaskProDef> userTaskProDef=UserTaskToUserTaskProDef.convertMytask(uts);
		for(UserTaskProDef u:userTaskProDef) {
			//如果任务（入线只有一根）并且（开始节点为"startevent1"），则其目标任务记为"1"
			if(u.getIncomingFlows().size()==1&&u.getIncomingFlows().get(0).getSourceRef().equals(start.getId())) {
				//第一个任务节点设置为1
				u.getPosition().add("1");
				for(int j=0;j<u.getOutgoingFlows().size();j++) {
					for(Gateway gt:gts) {//根据出线判断是否是网关
						if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
							int g=1;
							for(UserTaskProDef u2:userTaskProDef) {//找入线是网关的任务
								for(int i=0;i<u2.getIncomingFlows().size();i++) {
									if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
										u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
									}
								}
							}
						}
					}
					//不是网管时，找出下一个节点
					for(UserTaskProDef u2:userTaskProDef) {
						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
							u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
						}
					}
				}
			}else{//任务不是第一个任务节点
					for(int j=0;j<u.getOutgoingFlows().size();j++) {
						for(Gateway gt:gts) {//根据出线判断是否是网关
							if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
								int g=1;
								for(UserTaskProDef u2:userTaskProDef) {//找入线是网关的任务
									for(int i=0;i<u2.getIncomingFlows().size();i++) {
										if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
											u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
										}
									}
								}
							}
						}
						
						for(UserTaskProDef u2:userTaskProDef) {
		    				if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
		    					if(u.getPosition().size()>1) {
		        					for(int i=0;i<u.getPosition().size();i++) {
		            					u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
		        					}
		    					}
		    					if(u.getPosition().size()==1){
		        					u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
		    					}
		    				}
		    			}
					}
				}
			}

		//写入因顺序影响的节点位置
		for(UserTaskProDef u:userTaskProDef) {
				if(u.getPosition().size()>1) {
					for(int j=0;j<u.getOutgoingFlows().size();j++) {
						for(UserTaskProDef u2:userTaskProDef) {
    						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())&&u.getPosition().size()>u2.getPosition().size()&&u2.getIncomingFlows().size()==1) {
    								u2.getPosition().clear();
        							for(int i=0;i<u.getPosition().size();i++) {
            							u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
        							}
    						}
    					}
					}
			}
		}
		//因为任务遍历顺序而导致的上节点在没有值时被遍历了？？？？？？？？
		List<UserTaskProDef> nextIsNull=new ArrayList<>();
		for(UserTaskProDef u:userTaskProDef) {
			if(u.getPosition().size()!=0) {
				for (int j = 0; j < u.getOutgoingFlows().size(); j++) {
					for (UserTaskProDef u2 : userTaskProDef) {
						if (u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())&&u2.getPosition().size()==0) {
							nextIsNull.add(u);
						}
					}
				}
			}
			if(u.getPosition().size()==0) {
				for (int j = 0; j < u.getOutgoingFlows().size(); j++) {
					for (UserTaskProDef u2 : userTaskProDef) {
						if (u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())&&u2.getPosition().size()==0) {
							nextIsNull.add(u);
						}
					}
				}
			}
		}
		for(UserTaskProDef u:nextIsNull) {
			for(int j=0;j<u.getOutgoingFlows().size();j++) {
				for(Gateway gt:gts) {//根据出线判断是否是网关
					if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
						int g=1;
						for(UserTaskProDef u2:userTaskProDef) {//找入线是网关的任务
							for(int i=0;i<u2.getIncomingFlows().size();i++) {
								if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
									u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
								}
							}
						}
					}
				}
				
				for(UserTaskProDef u2:userTaskProDef) {
    				if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
    					if(u.getPosition().size()>1) {
        					for(int i=0;i<u.getPosition().size();i++) {
            					u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
        					}
    					}
    					if(u.getPosition().size()==1){
        					u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
    					}
    				}
    			}
			}
		}
	return userTaskProDef;
   }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks3/{processDefIdkey}")
    public List<UserTaskProDef> userTasks3(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> result = new ArrayList<>();
        List<UserTask> result2 = new ArrayList<>();
        List<Gateway> result3 = new ArrayList<>();
        StartEvent start=new StartEvent();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	if(e instanceof StartEvent) {
                		start=(StartEvent)e;
                	}
                	//如果是线
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                    	result.add(sequenceFlow);
                    }
                	//如果是任务节点
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		result2.add(userTask);
                    }
                	//如果是网关
                	if(e instanceof Gateway) {
                		result3.add((Gateway)e);
                	}
                }
            }
        }
//        return result;
        return userTaskGetPosition3(start,result,result2,result3);
    }
    private List<UserTaskProDef> userTaskGetPosition3(StartEvent start, List<SequenceFlow> sqfs, List<UserTask> uts, List<Gateway> gts) {
    	List<UserTaskProDef> userTaskProDef=UserTaskToUserTaskProDef.convertMytask(uts);
    	List<UserTaskProDef> userTaskProDefSort=new ArrayList<>();
    	//先根据信息将UserTask节点简单排序
		String target=null;
    	for(SequenceFlow s:sqfs) {
    		if(s.getSourceRef().equals(start.getId())) {//起始线,将其实目标任务装入集合，将目标节点id保存
    			for(UserTaskProDef u:userTaskProDef) {
    				if(s.getTargetRef().equals(u.getId())) {
    						userTaskProDefSort.add(u);
    				}
    			}
    			target=s.getTargetRef();
    		}
    		if(s.getSourceRef().equals(target)) {//以目标节点起始的线，获取该线的指向任务，并将指向任务id保存
    			for(UserTaskProDef u:userTaskProDef) {
    				if(s.getTargetRef().equals(u.getId())) {
    					if(!userTaskProDefSort.contains(u)) {
    						userTaskProDefSort.add(u);
    					}
    				}
    			}
    			target=s.getTargetRef();
    		}
    	}
    	//当由于顺序混乱，而漏掉的节点，再添加
    	int a=1;
    	while(userTaskProDefSort.size()<userTaskProDef.size()&&userTaskProDefSort.size()>0) {
    		UserTaskProDef ulast=userTaskProDefSort.get(userTaskProDefSort.size()-a);//断掉的节点
    		String target2=null;
    		for(SequenceFlow s:sqfs) {
        		if(s.getSourceRef().equals(ulast.getId())) {//与断掉节点匹配的起始线,将其实目标任务装入集合，将目标节点id保存
        			for(UserTaskProDef u:userTaskProDef) {
        				if(s.getTargetRef().equals(u.getId())) {
        					if(!userTaskProDefSort.contains(u)) {
        						userTaskProDefSort.add(u);
        					}
        				}
        			}
					target2=s.getTargetRef();
        		}
        		if(s.getSourceRef().equals(target2)) {//以目标节点起始的线，获取该线的指向任务，并将指向任务id保存
        			for(UserTaskProDef u:userTaskProDef) {
        				if(s.getTargetRef().equals(u.getId())) {
        					if(!userTaskProDefSort.contains(u)) {
        						userTaskProDefSort.add(u);
        					}
        				}
        			}
					target2=s.getTargetRef();
        		}
        	}
    		for(Gateway gt:gts) {//判断是否是网关开始的线，网关可能漏掉节点
    			for(UserTaskProDef u:userTaskProDef) {
    				if(gt.getId().equals(u.getIncomingFlows().get(0).getSourceRef())) {
    					if(!userTaskProDefSort.contains(u)) {
    						userTaskProDefSort.add(u);
    					}
    				}
    			}
			}
    		if(userTaskProDefSort.size()>a) {
        		a++;
    		}else if(userTaskProDefSort.size()==a){//针对死循环，如果最后还是没有找齐，将剩下缺失的节点全部装入
    			List<UserTaskProDef> userTaskProDefSort2=new ArrayList<>();
    			userTaskProDefSort2=userTaskProDef;
    			userTaskProDefSort2.removeAll(userTaskProDefSort);
    			userTaskProDefSort.addAll(userTaskProDefSort2);
    			break;
    		}
    	}
    	//再利用写版本信息将位置顺序写上
    	for(UserTaskProDef u:userTaskProDefSort) {
			//如果任务（入线只有一根）并且（开始节点为"startevent1"），则其目标任务记为"1"
			if(u.getIncomingFlows().size()==1&&u.getIncomingFlows().get(0).getSourceRef().equals(start.getId())) {
				//第一个任务节点设置为1
				u.getPosition().add("1");
				for(int j=0;j<u.getOutgoingFlows().size();j++) {
					for(Gateway gt:gts) {//根据出线判断是否是网关
						if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
							int g=1;
							for(UserTaskProDef u2:userTaskProDefSort) {//找入线是网关的任务
								for(int i=0;i<u2.getIncomingFlows().size();i++) {
									if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
										u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
									}
								}
							}
						}
					}
					//不是网管时，找出下一个节点
					for(UserTaskProDef u2:userTaskProDefSort) {
						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
							u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
						}
					}
				}
			}else{//任务不是第一个任务节点
					for(int j=0;j<u.getOutgoingFlows().size();j++) {
						for(Gateway gt:gts) {//根据出线判断是否是网关
							if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
								int g=1;
								for(UserTaskProDef u2:userTaskProDefSort) {//找入线是网关的任务，进行位置赋值
									for(int i=0;i<u2.getIncomingFlows().size();i++) {
										if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
											u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
										}
									}
								}
							}
						}
						for(UserTaskProDef u2:userTaskProDefSort) {//出线不是网关，对出线任务位置赋值
		    				if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
		    					if(u.getPosition().size()>1) {//入线任务有多个位置
		        					for(int i=0;i<u.getPosition().size();i++) {
		            					u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
		        					}
		    					}
		    					if(u.getPosition().size()==1){//入线任务单个位置
		        					u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
		    					}
		    				}
		    			}
					}
				}
			}
    	//写入因顺序影响的节点位置
    			for(UserTaskProDef u:userTaskProDefSort) {
    					if(u.getPosition().size()>1) {	//最后一个节点，前节点位置数大于该节点位置数是，重写该节点
    						for(int j=0;j<u.getOutgoingFlows().size();j++) {
    							for(UserTaskProDef u2:userTaskProDefSort) {
    	    						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())&&u.getPosition().size()>u2.getPosition().size()&&u2.getIncomingFlows().size()==1) {
    	    								u2.getPosition().clear();
    	        							for(int i=0;i<u.getPosition().size();i++) {
    	            							u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
    	        							}
    	    						}
    	    					}
    						}
    				}
    			}
    	return userTaskProDefSort;
    }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks4/{processDefIdkey}")
    public List<UserTaskProDef> userTasks4(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> result = new ArrayList<>();
        List<UserTask> result2 = new ArrayList<>();
        List<Gateway> result3 = new ArrayList<>();
        StartEvent start=new StartEvent();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	if(e instanceof StartEvent) {
                		start=(StartEvent)e;
                	}
                	//如果是线
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                    	result.add(sequenceFlow);
                    }
                	//如果是任务节点
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		result2.add(userTask);
                    }
                	//如果是网关
                	if(e instanceof Gateway) {
                		result3.add((Gateway)e);
                	}
                }
            }
        }
//        return result;
        return userTaskGetPosition4(start,result,result2,result3);
    }
    private List<UserTaskProDef> userTaskGetPosition4(StartEvent start, List<SequenceFlow> sqfs, List<UserTask> uts, List<Gateway> gts) {
    	List<UserTaskProDef> userTaskProDef=UserTaskToUserTaskProDef.convertMytask(uts);
    	List<UserTaskProDef> userTaskProDefSort=new ArrayList<>();
    	//先根据信息将UserTask节点简单排序
		String target=null;
    	for(SequenceFlow s:sqfs) {
    		if(s.getSourceRef().equals(start.getId())) {//起始线,将其实目标任务装入集合，将目标节点id保存
    			for(UserTaskProDef u:userTaskProDef) {
    				if(s.getTargetRef().equals(u.getId())) {
    						userTaskProDefSort.add(u);
    				}
    			}
    			target=s.getTargetRef();
    		}
    		if(s.getSourceRef().equals(target)) {//以目标节点起始的线，获取该线的指向任务，并将指向任务id保存
    			for(UserTaskProDef u:userTaskProDef) {
    				if(s.getTargetRef().equals(u.getId())) {
    					if(!userTaskProDefSort.contains(u)) {
    						userTaskProDefSort.add(u);
    					}
    				}
    			}
    			target=s.getTargetRef();
    		}
    	}
    	//当由于顺序混乱，而漏掉的节点，再添加
    	int a=1;
    	while(userTaskProDefSort.size()<userTaskProDef.size()&&userTaskProDefSort.size()>0) {
    		UserTaskProDef ulast=userTaskProDefSort.get(userTaskProDefSort.size()-a);//断掉的节点
    		String target2=null;
    		for(SequenceFlow s:sqfs) {
        		if(s.getSourceRef().equals(ulast.getId())) {//与断掉节点匹配的起始线,将其实目标任务装入集合，将目标节点id保存
        			for(UserTaskProDef u:userTaskProDef) {
        				if(s.getTargetRef().equals(u.getId())) {
        					if(!userTaskProDefSort.contains(u)) {
        						userTaskProDefSort.add(u);
        					}
        				}
        			}
					target2=s.getTargetRef();
        		}
        		if(s.getSourceRef().equals(target2)) {//以目标节点起始的线，获取该线的指向任务，并将指向任务id保存
        			for(UserTaskProDef u:userTaskProDef) {
        				if(s.getTargetRef().equals(u.getId())) {
        					if(!userTaskProDefSort.contains(u)) {
        						userTaskProDefSort.add(u);
        					}
        				}
        			}
					target2=s.getTargetRef();
        		}
        	}
    		for(Gateway gt:gts) {//判断是否是网关开始的线，网关可能漏掉节点
    			for(UserTaskProDef u:userTaskProDef) {
    				if(gt.getId().equals(u.getIncomingFlows().get(0).getSourceRef())) {
    					if(!userTaskProDefSort.contains(u)) {
    						userTaskProDefSort.add(u);
    					}
    				}
    			}
			}
    		if(userTaskProDefSort.size()>a) {
        		a++;
    		}else if(userTaskProDefSort.size()==a){//针对死循环，如果最后还是没有找齐，将剩下缺失的节点全部装入
    			List<UserTaskProDef> userTaskProDefSort2=new ArrayList<>();
    			userTaskProDefSort2=userTaskProDef;
    			userTaskProDefSort2.removeAll(userTaskProDefSort);
    			userTaskProDefSort.addAll(userTaskProDefSort2);
    			break;
    		}
    	}
    	//再利用写版本信息将位置顺序写上
    	for(UserTaskProDef u:userTaskProDefSort) {
			//如果任务（入线只有一根）并且（开始节点为"startevent1"），则其目标任务记为"1"
			if(u.getIncomingFlows().size()==1&&u.getIncomingFlows().get(0).getSourceRef().equals(start.getId())) {
				//第一个任务节点设置为1
				u.getPosition().add("1");
				u.setSort(1);
				u.setSortStr("1");
				for(int j=0;j<u.getOutgoingFlows().size();j++) {
					for(Gateway gt:gts) {//根据出线判断是否是网关
						if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
							int g=1;
							for(UserTaskProDef u2:userTaskProDefSort) {//找入线是网关的任务
								for(int i=0;i<u2.getIncomingFlows().size();i++) {
									if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
										if(u2.getSortStr()==null) {
											u2.setSortStr(u.getSortStr()+"."+g);
										}
										u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
										if(u2.getSort()==0) {
				    						u2.setSort(u.getSort()+1);
				    					}
									}
								}
							}
						}
					}
					//不是网管时，找出下一个节点
					for(UserTaskProDef u2:userTaskProDefSort) {
						if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
							u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
							if(u2.getSort()==0) {
	    						u2.setSort(u.getSort()+1);
	    					}
							if(u2.getSortStr()==null) {
								u2.setSortStr(u.getSortStr()+"."+(j+1));
							}
						}
					}
				}
			}else{//任务不是第一个任务节点
					for(int j=0;j<u.getOutgoingFlows().size();j++) {
						for(Gateway gt:gts) {//根据出线判断是否是网关
							if(gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
								int g=1;
								for(UserTaskProDef u2:userTaskProDefSort) {//找入线是网关的任务，进行位置赋值
									for(int i=0;i<u2.getIncomingFlows().size();i++) {
										if(u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
											if(u2.getSortStr()==null) {
												u2.setSortStr(u.getSortStr()+"."+g);
											}
											u2.getPosition().add(u.getPosition().get(0)+"."+(g++));
											if(u2.getSort()==0) {
					    						u2.setSort(u.getSort()+1);
					    					}
										}
									}
								}
							}
						}
						for(UserTaskProDef u2:userTaskProDefSort) {//出线不是网关，对出线任务位置赋值
		    				if(u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
		    					if(u.getPosition().size()>1) {//入线任务有多个位置
		        					for(int i=0;i<u.getPosition().size();i++) {
		            					u2.getPosition().add(u.getPosition().get(i)+"."+(j+1));
		        					}
		    					}
		    					if(u.getPosition().size()==1){//入线任务单个位置
		        					u2.getPosition().add(u.getPosition().get(0)+"."+(j+1));
		    					}
		    					if(u2.getSort()==0) {
		    						u2.setSort(u.getSort()+1);
		    					}
		    					if(u2.getSortStr()==null) {
									u2.setSortStr(u.getSortStr()+"."+(j+1));
								}
		    				}
		    			}
					}
				}
			}
    	//写入因顺序影响的节点位置
		for (UserTaskProDef u : userTaskProDefSort) {
			if (u.getPosition().size() > 1) { // 最后一个节点，前节点位置数大于该节点位置数是，重写该节点
				for (int j = 0; j < u.getOutgoingFlows().size(); j++) {
					for (UserTaskProDef u2 : userTaskProDefSort) {
						if (u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())
								&& u.getPosition().size() > u2.getPosition().size()
								&& u2.getIncomingFlows().size() == 1) {
							u2.getPosition().clear();
							for (int i = 0; i < u.getPosition().size(); i++) {
								u2.getPosition().add(u.getPosition().get(i) + "." + (j + 1));
							}
						}
					}
				}
			}
		}
    	return userTaskProDefSort;
    }
    
	/**
     * 流程定义Key启动并判断是否完成第一个任务
     * @param processDefIdkey （流程定义Key）
     * @param startUser （启动人）
     * @param startAndCompleteFirst	（是否完成第一个任务）
     * @param data	（入参Requestbody传入）
     * @return Mytask（返回第一个任务）
     */
    @ApiOperation(value = "startProcess", notes = "启动流程")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value="/startProcess/{processDefIdkey}",produces=MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Mytask start(@PathVariable(value = "processDefIdkey") String processDefIdkey,
                            @RequestParam(value = "startUser") String startUser, 
                            @RequestParam(value = "startAndCompleteFirst") boolean startAndCompleteFirst,
                            @RequestBody Map<String, Object> data) {
        return internalStartProcess(processDefIdkey, startUser, data, startAndCompleteFirst);
    }
	private Mytask internalStartProcess(String processDefIdkey, String startUser, Map<String, Object> data,
			boolean startAndCompleteFirst) {
		//1，将入参放入map集合中
		Map<String, Object> vars = new HashMap<>();
        vars.putAll(data);
        vars.put("startUser", startUser);
        //2，设置开启人
		identityService.setAuthenticatedUserId(startUser);
		//3，启动流程（返回流程实例对象：对应act_ru_execution表记录对象）
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefIdkey, vars);
        //4，根据当前流程实例ID获取当前活动的任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).active().singleResult();
        //5，是否完成该任务
        if (startAndCompleteFirst) {
        	//6，查看办理人是否为空，将启动人设为办理人
            if (task.getAssignee() == null) {
                taskService.setAssignee(task.getId(), startUser);
            }
            taskService.complete(task.getId());
        }
        //不管完成与否，都会返回第一个任务
		return TasktoMyTask.build(task);
	}
	/**
     * 查询开启的流程实例（获取流程实例ID）
     * （正在执行的流程表act_ru_execution）
     * @return	List<MyProcessInstance>
     */
    @ApiOperation(value = "ProcessInstanceQueryID", notes = "流程定义查询key")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/ProcessInstanceQuery")
    public List<MyProcessInstance> ProcessInstanceQuery() {
    	List<ProcessInstance> list =runtimeService.createProcessInstanceQuery()
    									.orderByProcessDefinitionKey().desc().list();
        return ProcessInstanceTOmyProcessInstance.convertMyProcessInstance(list);
    }
	/**
     * 流程实例Id查活动任务
     * @param processInstanceId
     * @return	List<Mytask>
     */
    @ApiOperation(value = "activeTasks", notes = "活动任务查询")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/activeTasks/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Mytask> activeTasks(@PathVariable String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        return TasktoMyTask.convertMytask(tasks);
    }
    /**
     * 完成任务
     * @param taskId （任务ID）
     * @param data	（入参Requestbody传入）
     * @return	String
     */
    @ApiOperation(value = "completeTask", notes = "完成/提交任务")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/completeTask/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String completeTask(@PathVariable String taskId, 
    						 	@RequestBody Map<String, Object> data) {
        Map<String, Object> vars = new HashMap<>();
        vars.putAll(data);
        //（false时）将vars存为整个流程实例的流程变量
        taskService.complete(taskId, vars, false);
        return "成功完成任务";
    }
	/**
     * 流程实例Id查历史任务（已完成和待完成的一个或者是全部完成的任务）
     * @param processInstanceId
     * @return	List<Mytask>
     */
    @ApiOperation(value = "historyTasksQuery", notes = "历史任务查询")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/historyTasksQuery/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Mytask> historyTasksQuery(@PathVariable String processInstanceId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        					.processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();//这里按照创建时间排序
        return TasktoMyTask.convertMytask2(tasks);
    }
    /**
     * 任务指定办理人
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskClaim", notes = "任务指定用户", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskClaim/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskClaim(@PathVariable(value = "taskId") String taskId,
                          @PathVariable(value = "assignee") String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            taskService.setAssignee(taskId, assignee);//将原有的办理人覆盖
        } else {
            throw new IllegalArgumentException(taskId + "任务不存在");
        }
    }

    /**
     * 任务转办
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskTransfer", notes = "任务转办", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskTransfer/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskTransfer(@PathVariable(value = "taskId") String taskId,
                             @PathVariable(value = "assignee") String assignee,
                             @RequestBody Map<String, Object> data) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
        	taskService.setOwner(taskId, task.getAssignee());//设置任务之前拥有者
            if (data != null) {
                taskService.setVariables(taskId, data);
                taskService.setVariablesLocal(taskId, data);
            }
            taskService.setAssignee(taskId, assignee);//设置任务办理人
        }else {
            throw new IllegalArgumentException(taskId + "任务不存在");
        }
    }
    /**
     * 任务声明办理人（之前没有办理人）
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskClaim2", notes = "任务指定用户", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskClaim2/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskClaim(@PathVariable(value = "taskId") String taskId,
                          @PathVariable(value = "assignee") String assignee,
                          @RequestBody Map<String, Object> data) {
        this.taskClaim(taskId, assignee);//之前没有办理人，声明办理人
        if (data != null) {
            taskService.setVariables(taskId, data);
            taskService.setVariablesLocal(taskId, data);
        }
    }
    /**
     * 转化流程定义为model可用的xml文件(bpmn文件)
     * @param processDefId
     * @return
     */
    @ApiOperation(value = "getBpmnxml", notes = "转化流程定义为editor可用的xml文件", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/bpmntoxml/{processDefId}", produces = MediaType.APPLICATION_XML_VALUE)
    public String getBpmnJson(@PathVariable(value="processDefId") String processDefIdkey) {
        BpmnModel model = repositoryService.getBpmnModel(processDefIdkey);
        return new String(new BpmnXMLConverter().convertToXML(model));
    }


    /**
     * 任务回退一步(方式一)(个人感觉比较垃圾：不弄了)
     * @param remark
     * @param taskId    当前任务id
     * @return
     * 操作：1，删除历史任务表正在运行的任务（当前任务）
     * 		2，将历史任务表中上一个任务的end_time,doration,delete_reason三个字段设置为空
     * 		3，运行时的身份链接表（因为处理人除了直接设置指定人处理的方式，act_ru_identitylink表没有数据）
     * 			》根据testId查出对应记录ID
     * 			》将ID对应字段的testId设置为空
     * 			》
     * 		4，更新运行任务表当前任务回退到跳转之前的任务TASK_DEF_KEY_,FORM_KEY_,NAME_,ID_四个字段和历史任务表中的任务一致
     * 		5，更新运行实例表中的当前运行流程实例中的ACT_ID_字段，该字段和运行任务表中任务定义id即TASK_DEF_KEY_字段一致
     */
//  @RequestMapping(value="rollBack",method =  RequestMethod.POST )
//  @ResponseBody
//  public MessageBody rollBack(@RequestParam(value="remark",required=false)String remark,
//          				@RequestParam(value="taskId",required=true)String taskId) throws Exception{
//      Task task=workFlowService.getTaskById(taskId);
//      ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
//      //4.使用流程实例对象获取BusinessKey
//      String business_key = pi.getBusinessKey();
//      String execId=task.getExecutionId();
//      CheckSheetProHis his=new CheckSheetProHis();
//      his.setId(ToolUtils.getUUID());
//      his.setCheckProId(business_key);
//      his.setOptime(DateUtil.toString(new Date()));
//      if(remark!=null){
//          his.setShenpiremark(remark);
//      }
//      his.setShenpiaction(task.getName()+"-撤銷");
//      his.setExecutor(UCHome.getLabUser().getName());
//
//      String processInstanceId = task.getProcessInstanceId();
//      List<HistoricTaskInstance> list = historyService
//          .createHistoricTaskInstanceQuery()
//          .processInstanceId(processInstanceId).finished()
//          .orderByTaskCreateTime().desc().list();
//      String parentTaskId="";
//      if (list != null && list.size() > 0) {
//          //按照完成时间排序取第一个 回退到该节点
//          parentTaskId=list.get(0).getId();
//          for (HistoricTaskInstance hti : list) {
//              System.out.print("taskId:" + hti.getId()+"，");
//              System.out.print("name:" + hti.getName()+"，");
//              System.out.print("pdId:" + hti.getProcessDefinitionId()+"，");
//              System.out.print("assignee:" + hti.getAssignee()+"，");
//          }
//      }
//      //workFlowService.TaskRollBack(taskId);
//      if(StringUtils.isNotBlank(parentTaskId)){
//          //插入审批记录
//          //退回任务
//          checkSheetClient.rollBackTask(taskId,parentTaskId,execId);
//          //记录日志
//          checkSheetProHisClient.insert(his);
//      }else{
//          return MessageBody.getMessageBody(false,"上级任务为空！");
//      }
//      return MessageBody.getMessageBody(true,"撤销成功!");
//  }
    /**
     * 任务跳转回退（方式二）
     * 不删除任何历史记录（可以在历史表中标明任务跳转节点）
     * 改变流程定义的任务出线，完成任务后自动回退到之前的节点
     */
    @Autowired
    Rollback rollback;
    @PostMapping(value = "/taskJump/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mytask taskJump(@PathVariable(value = "taskId") String taskId,
    						@RequestParam(value = "jumpTotaskdefkey") String jumpTotaskdefkey) {
    	rollback.rollBackToAssignWorkFlow(taskId, jumpTotaskdefkey);
        Task task=taskService.createTaskQuery().taskDefinitionKey(jumpTotaskdefkey).active().singleResult();
        return TasktoMyTask.build(task);
    }

    @Autowired
	MycustomService mycustomService;
    @ApiOperation(value = "customMappers", notes = "自定义操作数据库", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public String customMappers() {
    	Mycustom mycustom = new Mycustom();
		mycustom.setId("1211");
		mycustom.setDis("sas222");
		mycustom.setName("alksdm222");
		mycustomService.insert0(mycustom);
		return "66666666666";
    }
    /**
     * model是前端编辑页面显示的界面流程图
     * 导入bpmn文件到model中
     * 导入json格式的流程定义文件
     * 导出json格式的流程定义文件
     * 创建新模型
     * 删除model，如果model已经发布（deploy）则不能删除（发布的意思是部署了的流程定义）
     * 获取model的分组
     * 获取模板id对应的流程
     * 获取流程实例
     * 获取流程节点信息
     */
    
}
