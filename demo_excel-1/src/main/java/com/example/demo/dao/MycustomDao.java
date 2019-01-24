package com.example.demo.dao;


import org.apache.ibatis.annotations.Mapper;


import com.example.demo.entity.Mycustom;

@Mapper
public interface MycustomDao {
	//@Select("select pid from push where title = #{title}")
    public Mycustom selectMy_custom(String id);
    //@Insert("insert into push (pid,title,content,apnBadge,apnSound,apnAlert) " +
     //       "values (#{pid,jdbcType=INTEGER}, #{title,jdbcType=VARCHAR},#{content,jdbcType=VARCHAR},#{apnBadge,jdbcType=INTEGER},#{apnSound,jdbcType=VARCHAR},#{apnAlert,jdbcType=VARCHAR})")
    public void insertMy_custom(Mycustom m);
}
