package com.example.demo.entity;

public class Processing_Result {
	int id;
	String action;
	String target;
	String data;
	String startUser;
	String assignee1;
	String variables1;
	String assignee2;
	String variables2;
	String result;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getStartUser() {
		return startUser;
	}
	public void setStartUser(String startUser) {
		this.startUser = startUser;
	}
	public String getAssignee1() {
		return assignee1;
	}
	public void setAssignee1(String assignee1) {
		this.assignee1 = assignee1;
	}
	public String getVariables1() {
		return variables1;
	}
	public void setVariables1(String variables1) {
		this.variables1 = variables1;
	}
	public String getAssignee2() {
		return assignee2;
	}
	public void setAssignee2(String assignee2) {
		this.assignee2 = assignee2;
	}
	public String getVariables2() {
		return variables2;
	}
	public void setVariables2(String variables2) {
		this.variables2 = variables2;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	@Override
	public String toString() {
		return "Processing_Result [id=" + id + ", action=" + action + ", target=" + target + ", data=" + data
				+ ", startUser=" + startUser + ", assignee1=" + assignee1 + ", variables1=" + variables1
				+ ", assignee2=" + assignee2 + ", variables2=" + variables2 + ", result=" + result + "]";
	}
}
