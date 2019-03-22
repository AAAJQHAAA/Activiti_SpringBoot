package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.UserTask;
import org.springframework.beans.BeanUtils;

import com.example.demo.entity.UserTaskActivity;
import com.example.demo.entity.UserTaskProDef;

public class UserTaskToUserTaskProDef {
	public static UserTaskProDef build(UserTask task) {
		UserTaskProDef ti = new UserTaskProDef();
        BeanUtils.copyProperties(task, ti);
        return ti;
    }
	public static List<UserTaskProDef> convertMytask(List<UserTask> tasks) {
        List<UserTaskProDef> customTaskList = new ArrayList<>();
        for (UserTask task : tasks) {
            customTaskList.add(build(task));
        }
        return customTaskList;
    }
	public static UserTaskActivity build1(UserTask task) {
		UserTaskActivity ti = new UserTaskActivity();
        BeanUtils.copyProperties(task, ti);
        return ti;
    }
	public static List<UserTaskActivity> convertMytask1(List<UserTask> tasks) {
        List<UserTaskActivity> customTaskList = new ArrayList<>();
        for (UserTask task : tasks) {
            customTaskList.add(build1(task));
        }
        return customTaskList;
    }
}
