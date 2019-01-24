package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.dao.MycustomDao;
import com.example.demo.entity.Mycustom;
import com.example.demo.mycustom.MycustomService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoExcel1ApplicationTests {
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
	
	@Autowired
	ManagementService managementService;
	

	@Test
	public void contextLoads() {
			// 根据流程定义的id取得流程定义(act_re_procdef)
			ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition("rollbackId:1:50004");

			if (definitionEntity == null) {
				System.out.println("流程定义不见了");
			}
			// 获取所有的任务节点
			List<ActivityImpl> activityImpls = definitionEntity.getActivities();
			for (ActivityImpl activityImpl : activityImpls) {
				System.out.println(activityImpl.getId());
				System.out.print("hegiht:"+activityImpl.getHeight());
				System.out.print("width:"+activityImpl.getWidth());
				System.out.print(" x:"+activityImpl.getX());
				System.out.println(" y:"+activityImpl.getY());
				for(PvmTransition inPvmTransition:activityImpl.getIncomingTransitions()) {
					System.out.println("进线");
					System.out.println(inPvmTransition.getSource());
					System.out.println(inPvmTransition.getDestination());
				}
				for(PvmTransition outPvmTransition:activityImpl.getOutgoingTransitions()) {
					System.out.println("出线");
					System.out.println(outPvmTransition.getSource());
					System.out.println(outPvmTransition.getDestination());
				}
				System.out.println("节点完");
			}
//			// （获取节点所有出线）
//			List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
//			List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
//			//将出线保存在oriPvmTransitionList集合中********************************************
//			for (PvmTransition pvmTransition : pvmTransitionList) {
//				oriPvmTransitionList.add(pvmTransition);
//			}
//			// 清除当前活动的出口
//			pvmTransitionList.clear();
//			
//			// （获取节点所有入线）（当前节点与上一节点的连线保存到nextTransitionList集合中）入口
//			List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();
//			// 建立新出口（新出口是上一个节点）
//			List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
//			for (PvmTransition nextTransition : nextTransitionList) {
//				//入线的起点节点将是下一个节点，（根据定义节点id获取上一个节点）
//				PvmActivity nextActivity = nextTransition.getSource();
//				ActivityImpl nextActivityImpl = definition.findActivity(nextActivity.getId());
//				//当前节点创建出线，设置上一节点为出线指向的目标节点
//				TransitionImpl newTransition = currActivity.createOutgoingTransition();
//				newTransition.setDestination(nextActivityImpl);
//				//将新线保存在newTransitions集合中********************************
//				newTransitions.add(newTransition);
//			}
//			
//			// 完成当前任务（会跳到上一节点）
//			List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
//			for (Task task : tasks) {
//				taskService.complete(task.getId());
//				//删除历史任务表中的任务实例（act_hi_taskinst）
//				historyService.deleteHistoricTaskInstance(task.getId());
//			}
//			
//			// 恢复方向，去掉该节点指向上上一节点的线
//			for (TransitionImpl transitionImpl : newTransitions) {
//				currActivity.getOutgoingTransitions().remove(transitionImpl);
//			}
//			//将之前去除的该节点指向的下一节点的线添加
//			for (PvmTransition pvmTransition : oriPvmTransitionList) {
//				pvmTransitionList.add(pvmTransition);
//			}
	}
	//使用mybatis的mapper开发
//	@Autowired
//	MycustomDao mycustomDao;
//	@Test
//	public void testcustom() {
//		Mycustom mycustom = new Mycustom();
//		mycustom.setId("12");
//		mycustom.setDis("sas");
//		mycustom.setName("alksdm");
//		mycustomDao.insertMy_custom(mycustom);
//	}	
	
	@Autowired
	MycustomService mycustomService;
	//xmlMapper开发：使用statement操作
	@Test
	public void testcustom3() {
//		Mycustom mycustom = new Mycustom();
//		mycustom.setId("111122211");
//		mycustom.setDis("sas222211122");
//		mycustom.setName("alksdm22221112");
//		mycustomService.insert0(mycustom);
		System.out.println(mycustomService.select0("1").toString());
	}
	//纯注解开发mapper，使用mapper代理接口驱动
	@Test
	public void testcustom4() {
//		Mycustom mycustom = new Mycustom();
//		mycustom.setId("1211");
//		mycustom.setDis("sas222");
//		mycustom.setName("alksdm222");
//		mycustomService.insert1(mycustom);
		System.out.println(mycustomService.select1("1211").toString());
	}
}

