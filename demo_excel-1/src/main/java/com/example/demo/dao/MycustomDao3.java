package com.example.demo.dao;




import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.demo.entity.Mycustom;


public interface MycustomDao3 {
	@Select("SELECT * FROM my_custom WHERE id = #{id,jdbcType=VARCHAR}")
    public Mycustom selectMy_custom(String id);
	@Insert("INSERT INTO my_custom(name, dis, id) VALUES (#{name,jdbcType=VARCHAR}, #{dis,jdbcType=VARCHAR}, #{id,jdbcType=VARCHAR})")
    public void insertMy_custom(Mycustom m);
}
