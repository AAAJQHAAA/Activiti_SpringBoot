package com.example.demo.entity;

public class Mycustom {
	String id;
	String name;
	String dis;
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
	public String getDis() {
		return dis;
	}
	public void setDis(String dis) {
		this.dis = dis;
	}
	@Override
	public String toString() {
		return "Mycustom [id=" + id + ", name=" + name + ", dis=" + dis + "]";
	}
	
}
