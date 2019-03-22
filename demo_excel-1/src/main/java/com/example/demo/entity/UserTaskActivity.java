package com.example.demo.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.activiti.bpmn.model.SequenceFlow;

import java.util.List;

@JsonIgnoreProperties(value = { "incomingFlows","outgoingFlows"})
public class UserTaskActivity {
    private String id;
    private String name;
    protected List<SequenceFlow> incomingFlows;
    protected List<SequenceFlow> outgoingFlows;
    private String sortStr;

    public UserTaskActivity() {

    }

    public UserTaskActivity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortStr() {
        return sortStr;
    }

    public void setSortStr(String sortStr) {
        this.sortStr = sortStr;
    }

    public List<SequenceFlow> getIncomingFlows() {
        return incomingFlows;
    }

    public void setIncomingFlows(List<SequenceFlow> incomingFlows) {
        this.incomingFlows = incomingFlows;
    }

    public List<SequenceFlow> getOutgoingFlows() {
        return outgoingFlows;
    }

    public void setOutgoingFlows(List<SequenceFlow> outgoingFlows) {
        this.outgoingFlows = outgoingFlows;
    }
}
