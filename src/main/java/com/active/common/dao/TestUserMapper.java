package com.active.common.dao;


import com.active.common.model.TestUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TestUserMapper {


    int insertSelective(TestUser record);

    TestUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TestUser record);

}