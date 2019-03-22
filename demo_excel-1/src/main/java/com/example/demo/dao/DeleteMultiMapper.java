package com.example.demo.dao;

import org.apache.ibatis.annotations.Delete;


public interface DeleteMultiMapper {
	@Delete("delete from act_ru_variable where EXECUTION_ID_ = #{executionId}")
	public void delMultiInstance(String executionId);
	@Delete("delete from act_hi_actinst where EXECUTION_ID_ = #{executionId}")
	public void delMultiInstance2(String executionId);
	@Delete("delete from act_hi_varinst where EXECUTION_ID_ = #{executionId}")
	public void delMultiInstance3(String executionId);
	@Delete("delete from act_hi_detail where EXECUTION_ID_ = #{executionId}")
	public void delMultiInstance4(String executionId);
}
