package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;

import com.example.demo.entity.Mytask;

public class TasktoMyTask {
	

	public static Mytask build(Task task) {
		Mytask ti = new Mytask();
        BeanUtils.copyProperties(task, ti);
        return ti;
    }
	public static Mytask build2(HistoricTaskInstance task) {
		Mytask ti = new Mytask();
        BeanUtils.copyProperties(task, ti);
        return ti;
    }
	public static List<Mytask> convertMytask(List<Task> tasks) {
        List<Mytask> customTaskList = new ArrayList<>();
        for (Task task : tasks) {
            customTaskList.add(build(task));
        }
        return customTaskList;
    }
	public static List<Mytask> convertMytask2(List<HistoricTaskInstance> tasks) {
        List<Mytask> customTaskList = new ArrayList<>();
        for (HistoricTaskInstance task : tasks) {
            customTaskList.add(build2(task));
        }
        return customTaskList;
    }
}
