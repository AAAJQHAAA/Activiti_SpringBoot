package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.SequenceFlow;

public class UserTaskProDef {
		protected String id;
		protected String name;
		protected  List<SequenceFlow>	incomingFlows;
		protected List<SequenceFlow> outgoingFlows;
		protected List<String> position=new ArrayList<>();
		protected int sort;
		protected String sortStr;
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

		public List<String> getPosition() {
			return position;
		}

		public void setPosition(List<String> position) {
			this.position = position;
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

		public int getSort() {
			return sort;
		}

		public void setSort(int sort) {
			this.sort = sort;
		}

		public String getSortStr() {
			return sortStr;
		}

		public void setSortStr(String sortStr) {
			this.sortStr = sortStr;
		}
	    
}
