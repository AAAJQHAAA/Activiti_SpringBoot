package com.example.demo.listener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener2 implements TaskListener{

	@Override
	public void notify(DelegateTask delegateTask) {
		 //添加会签的人员，所有的都审批通过才可进入下一环节
		 List<String> assigneeList = new ArrayList<String>();
		 assigneeList.add("first");
		 assigneeList.add("second");
		 delegateTask.setVariable("publicityList",assigneeList);
		
	}

}
