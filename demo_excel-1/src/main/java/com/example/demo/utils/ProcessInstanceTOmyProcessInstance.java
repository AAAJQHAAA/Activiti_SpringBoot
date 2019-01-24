package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;
import com.example.demo.entity.MyProcessInstance;


public class ProcessInstanceTOmyProcessInstance {
	public static MyProcessInstance build(ProcessInstance pi) {
		MyProcessInstance mpi = new MyProcessInstance();
        BeanUtils.copyProperties(pi, mpi);
        return mpi;
    }
	public static List<MyProcessInstance> convertMyProcessInstance(List<ProcessInstance> pis) {
        List<MyProcessInstance> customTaskList = new ArrayList<>();
        for (ProcessInstance pi : pis) {
            customTaskList.add(build(pi));
        }
        return customTaskList;
    }
}
