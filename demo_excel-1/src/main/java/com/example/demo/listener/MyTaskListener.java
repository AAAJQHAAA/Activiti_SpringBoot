package com.example.demo.listener;

import java.util.Arrays;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;




public class MyTaskListener implements TaskListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println("************************************delegateTask.getEventName() = " + delegateTask.getEventName());
//			//添加审批的人员，以下任何一人通过即可进入下一环节
//			String[] empLoyees = {"wangba","wangjiu"};
//			delegateTask.addCandidateUsers(Arrays.asList(empLoyees));
			 delegateTask. addCandidateUser("1111");
		     delegateTask. addCandidateUser("2222");
		     delegateTask. addCandidateUser("3333");
		     delegateTask. addCandidateUser("4444");
	}

}
