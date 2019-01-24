package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;

import com.example.demo.entity.MyProcessDefinition;

public class ProcessDefinitionTOmyProcessDefinition {
	public static MyProcessDefinition build(ProcessDefinition pd) {
		MyProcessDefinition mpd = new MyProcessDefinition();
        BeanUtils.copyProperties(pd, mpd);
        return mpd;
    }
	public static List<MyProcessDefinition> convertMyProcessDefinition(List<ProcessDefinition> pds) {
        List<MyProcessDefinition> customTaskList = new ArrayList<>();
        for (ProcessDefinition pd : pds) {
            customTaskList.add(build(pd));
        }
        return customTaskList;
    }
}
