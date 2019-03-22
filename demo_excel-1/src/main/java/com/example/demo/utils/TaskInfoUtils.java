package com.example.demo.utils;

import com.example.demo.entity.UserTaskProDef;
import com.example.demo.entity.UserTaskActivity;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

import java.util.ArrayList;
import java.util.List;

public class TaskInfoUtils {
    /**
     * 根据线找出排序信息
     *
     * @param startEvent
     * @param sequenceFlowList
     * @param userTaskList
     * @param gatewayList
     * @return List<UserTaskActivity>
     */
    public static List<UserTaskActivity> userTaskGetPosition(StartEvent startEvent, List<SequenceFlow> sequenceFlowList, List<UserTask> userTaskList, List<Gateway> gatewayList) {
        List<UserTaskActivity> userTaskActivityList = UserTaskToUserTaskProDef.convertMytask1(userTaskList);
        List<UserTaskActivity> userTaskActivityListSort = new ArrayList<>();
        //先根据信息将UserTask节点简单排序
        userTaskActivityListSort = simpleSortWithoutGateway(userTaskActivityList, userTaskActivityListSort, startEvent.getId(), sequenceFlowList);
        //当由于顺序混乱或者网关问题而漏掉的节点，再添加
        int a = 1;
        while (userTaskActivityListSort.size() < userTaskActivityList.size() && userTaskActivityListSort.size() > 0) {
            UserTaskActivity ulast = userTaskActivityListSort.get(userTaskActivityListSort.size() - 1);//断掉的节点
            userTaskActivityListSort = simpleSortWithoutGateway(userTaskActivityList, userTaskActivityListSort, ulast.getId(), sequenceFlowList);
            if (!gatewayList.isEmpty()) {
                userTaskActivityListSort = simpleSortAddGateway(userTaskActivityList, userTaskActivityListSort, gatewayList);
            }
            if (userTaskActivityListSort.size() > a) {
                a++;
            } else if (userTaskActivityListSort.size() == a) {//针对死循环，如果最后还是没有找齐，将剩下缺失的节点全部装入
                List<UserTaskActivity> userTaskProDefSort2 = new ArrayList<>();
                userTaskProDefSort2 = userTaskActivityList;
                userTaskProDefSort2.removeAll(userTaskActivityListSort);
                userTaskActivityListSort.addAll(userTaskProDefSort2);
                break;
            }
        }
        //再利用写位置信息将位置顺序写上
        for (UserTaskActivity u : userTaskActivityListSort) {
            //起始节点指向的目标任务记为"1"
            if (u.getIncomingFlows().size() == 1 && u.getIncomingFlows().get(0).getSourceRef().equals(startEvent.getId())) {
                //第一个任务节点设置为1
                u.setSortStr("1");
                userTaskActivityListSort = writeLocation(u, gatewayList, userTaskActivityListSort);
            } else {//任务不是第一个任务节点
                userTaskActivityListSort = writeLocation(u, gatewayList, userTaskActivityListSort);
            }
        }
        return userTaskActivityListSort;
    }

    public static UserTaskActivity selectTargetTask(List<UserTaskActivity> userTaskActivityList, String target) {
        for (UserTaskActivity u : userTaskActivityList) {
            if (target.equals(u.getId())) {
                return u;
            }
        }
        return null;
    }

    /**
     * 忽略网关排序
     *
     * @param userTaskActivityList
     * @param userTaskActivityListSort
     * @param startId
     * @param sequenceFlowList
     * @return userTaskActivityListSort
     */
    public static List<UserTaskActivity> simpleSortWithoutGateway(List<UserTaskActivity> userTaskActivityList, List<UserTaskActivity> userTaskActivityListSort, String startId, List<SequenceFlow> sequenceFlowList) {
        String target = null;
        UserTaskActivity targetTask = new UserTaskActivity();
        for (SequenceFlow s : sequenceFlowList) {
            //起始线,将其实目标任务装入集合，将目标节点id保存
            if (s.getSourceRef().equals(startId)) {
                targetTask = selectTargetTask(userTaskActivityList, s.getTargetRef());
                if (targetTask != null) {
                    userTaskActivityListSort.add(targetTask);
                }
                target = s.getTargetRef();
            }
            //以目标节点起始的线，获取该线的指向任务，并将指向任务id保存
            if (s.getSourceRef().equals(target)) {
                targetTask = selectTargetTask(userTaskActivityList, target);
                if (targetTask != null && !userTaskActivityListSort.contains(targetTask)) {
                    userTaskActivityListSort.add(targetTask);
                }
                target = s.getTargetRef();
            }
        }
        return userTaskActivityListSort;
    }

    /**
     * 添加网关之后的节点
     *
     * @param userTaskActivityList
     * @param userTaskActivityListSort
     * @param gatewayList
     * @return userTaskActivityListSort
     */
    public static List<UserTaskActivity> simpleSortAddGateway(List<UserTaskActivity> userTaskActivityList, List<UserTaskActivity> userTaskActivityListSort, List<Gateway> gatewayList) {
        for (Gateway gt : gatewayList) {//判断是否是网关开始的线，网关可能漏掉节点
            for (UserTaskActivity u : userTaskActivityList) {
                if (gt.getId().equals(u.getIncomingFlows().get(0).getSourceRef())) {
                    if (!userTaskActivityListSort.contains(u)) {
                        userTaskActivityListSort.add(u);
                    }
                }
            }
        }
        return userTaskActivityListSort;
    }

    /**
     * 写入排序序号
     *
     * @param u
     * @param gatewayList
     * @param userTaskActivityListSort
     * @return
     */
    public static List<UserTaskActivity> writeLocation(UserTaskActivity u, List<Gateway> gatewayList, List<UserTaskActivity> userTaskActivityListSort) {
        for (int j = 0; j < u.getOutgoingFlows().size(); j++) {
            for (Gateway gt : gatewayList) {//根据出线判断是否是网关
                if (gt.getId().equals(u.getOutgoingFlows().get(j).getTargetRef())) {
                    int g = 1;
                    for (UserTaskActivity u2 : userTaskActivityListSort) {//找入线是网关的任务
                        for (int i = 0; i < u2.getIncomingFlows().size(); i++) {
                            if (u2.getIncomingFlows().get(i).getSourceRef().equals(gt.getId())) {
                                if (u2.getSortStr() == null) {
                                    u2.setSortStr(u.getSortStr() + "." + (g++));
                                }
                            }
                        }
                    }
                }
            }
            //不是网管时，找出下一个节点
            for (UserTaskActivity u2 : userTaskActivityListSort) {
                if (u.getOutgoingFlows().get(j).getTargetRef().equals(u2.getId())) {
                    if (u2.getSortStr() == null) {
                        u2.setSortStr(u.getSortStr() + "." + (j + 1));
                    }
                }
            }
        }
        return userTaskActivityListSort;
    }
    
public static List<UserTaskProDef> userTaskGetPosition1(StartEvent start, List<SequenceFlow> sqfs, List<UserTask> uts, List<Gateway> gts) {
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
}
