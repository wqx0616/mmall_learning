package com.mmall.pojo;

import com.mmall.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestPojo {
    private String name;
    private Integer id;

    public static void main(String[] args) {
        TestPojo testPojo = new TestPojo();
        String testPojoJson = JsonUtil.obj2StringPretty(testPojo);
        log.info("testPojoJson:{}", testPojo);
    }
}

