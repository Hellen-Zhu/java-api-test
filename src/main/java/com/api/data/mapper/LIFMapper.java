package com.api.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface LIFMapper {

    @Select("select component,suite_result from auto_testngresult where runid = '${runId}'")
    List<LinkedHashMap> fetchAutoTestngresult(String runId);

    @Select("select * from auto_case where label = '${label}' and enable = true")
    List<LinkedHashMap> fetchAutoCaseByLabel(String label);

    @Select("select ${selectCondition} from auto_case where enable = true and (${filterCondition})")
    List<LinkedHashMap> fetchAutoCaseByDynamicCondition(String selectCondition, String filterCondition);

}