package com.graduation.peelcs.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ContentFilterServiceTest {

    @Autowired
    private ContentFilterService contentFilterService;
    
    @Test
    public void testContainsSensitiveWords() {
        // 测试包含敏感词的情况
        assertTrue(contentFilterService.containsSensitiveWords("这里有冰毒"));
        assertTrue(contentFilterService.containsSensitiveWords("你真是个傻帽"));
        assertTrue(contentFilterService.containsSensitiveWords("滚开，狗东西"));
        
        // 测试不包含敏感词的情况
        assertFalse(contentFilterService.containsSensitiveWords("这是一段正常文本"));
    }

    @Test
    public void testFilterContent() {
        // 测试敏感词过滤
        assertEquals("这里有**", contentFilterService.filterContent("这里有冰毒"));
        assertEquals("你真是个**", contentFilterService.filterContent("你真是个傻帽"));
        assertEquals("滚开，***", contentFilterService.filterContent("滚开，狗东西"));
        assertEquals("这是一段正常文本", contentFilterService.filterContent("这是一段正常文本"));
    }

    @Test
    public void testFindAllSensitiveWords() {
        // 测试查找所有敏感词
        List<String> result1 = contentFilterService.findAllSensitiveWords("这里有冰毒，你这个傻帽");
        assertEquals(2, result1.size());
        assertTrue(result1.contains("冰毒"));
        assertTrue(result1.contains("傻帽"));

        List<String> result2 = contentFilterService.findAllSensitiveWords("滚开，狗东西");
        assertEquals(1, result2.size());
        assertTrue(result2.contains("狗东西"));

        List<String> result3 = contentFilterService.findAllSensitiveWords("这是一段正常文本");
        assertTrue(result3.isEmpty());
    }
}