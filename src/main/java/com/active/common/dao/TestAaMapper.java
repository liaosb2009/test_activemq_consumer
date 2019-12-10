package com.active.common.dao;


import com.active.common.model.TestAa;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TestAaMapper {


    int insertSelective(TestAa record);

    TestAa selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestAa record);

}