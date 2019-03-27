package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtil {
	/**
	 * 传入的json格式的字符串
	 * 解析成指定对象集合
	 * 
	 * @param jsonStr 格式是对象数组[{},{}]或者一个对象{}
	 * @param t
	 * @return List<T>
	 */
	@SuppressWarnings({ "hiding", "unchecked" })
	public static <T> List<T> jsonStrToBeanList(String jsonStr,Class<T> t){
		List<T> tList=new ArrayList<>();
		if("[".equals(jsonStr.substring(0, 1))) {
			JSONArray jsonArray = JSONArray.fromObject(jsonStr);
			for (int i = 0; i < jsonArray.size(); i++) {  
	        	T t1=(T) JSONObject.toBean(jsonArray.getJSONObject(i), t);
	        	tList.add(t1);  
	        }
		}else {
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			T t1=(T) JSONObject.toBean(jsonObject, t);
			tList.add(t1);
		}
		return tList;
	}
	
}
