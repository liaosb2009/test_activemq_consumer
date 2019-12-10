package com.active.common.dao;


import com.active.common.model.ActivemqHistory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ActivemqHistoryMapper {

    int insertSelective(ActivemqHistory record);

    ActivemqHistory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ActivemqHistory record);

}