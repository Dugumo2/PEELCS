package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.utils.sensitivewords.ContentFilterService;
import com.graduation.peelcs.utils.sensitivewords.FileWordDeny;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ContentFilterServiceTest {

    @Autowired
    private ContentFilterService contentFilterService;

    // 假设您可以从FileWordDeny获取敏感词列表
    @Autowired
    private FileWordDeny fileWordDeny;

    @Test
    public void testContainsSensitiveWords0() {
        // 测试每个敏感词，使用fail()方法在失败时提供详细信息
        String message1 = "间谍专业版";
        if (!contentFilterService.containsSensitiveWords(message1)) {
            fail("未能检测到敏感词：" + message1);
        }

        String message2 = "这里有冰毒";
        if (!contentFilterService.containsSensitiveWords(message2)) {
            fail("未能检测到敏感词：" + message2);
        }

        String message3 = "你真是个傻帽";
        if (!contentFilterService.containsSensitiveWords(message3)) {
            fail("未能检测到敏感词：" + message3);
        }

        String message4 = "滚开，狗东西";
        if (!contentFilterService.containsSensitiveWords(message4)) {
            fail("未能检测到敏感词：" + message4);
        }

        String message5 = "口爆";
        if (!contentFilterService.containsSensitiveWords(message5)) {
            fail("未能检测到敏感词：" + message5);
        }

        // 测试不包含敏感词的情况
        String normalMessage = "这是一段正常文本";
        if (contentFilterService.containsSensitiveWords(normalMessage)) {
            fail("错误地检测到敏感词在正常文本中：" + normalMessage);
        }
    }

    @Test
    public void testListAllSensitiveWords() {
        String normalText = "这是一段正常文本";

        // 使用findAllSensitiveWords方法查找所有敏感词
        List<String> foundWords = contentFilterService.findAllSensitiveWords(normalText);

        // 打印所有找到的敏感词
        System.out.println("在文本「" + normalText + "」中发现的敏感词：");
        if (foundWords.isEmpty()) {
            System.out.println("没有发现敏感词");
        } else {
            for (String word : foundWords) {
                System.out.println("- " + word);
            }
        }

        // 断言此文本不应包含敏感词
        assertTrue(foundWords.isEmpty(), "正常文本不应包含敏感词，但发现了：" + foundWords);
    }

    @Test
    public void debugSensitiveWordDetection() {
        // 1. 先测试完整句子
        String text = "这是一段正常文本";
        List<String> found = contentFilterService.findAllSensitiveWords(text);
        System.out.println("在 '" + text + "' 中发现的敏感词: " + found);

        // 2. 逐字测试 - 在这里设置断点
        for (char c : text.toCharArray()) {
            String singleChar = String.valueOf(c);
            boolean isSensitive = contentFilterService.containsSensitiveWords(singleChar);
            List<String> singleFound = contentFilterService.findAllSensitiveWords(singleChar);
            System.out.println("字符 '" + singleChar + "' 敏感: " + isSensitive +
                    ", 发现词: " + singleFound);
        }
    }

    @Test
    public void findExactSensitiveWord() {


        List<String> allWords = fileWordDeny.deny();

        // 精确查找只有"一"的敏感词
        boolean found = allWords.contains("一");
        System.out.println("敏感词列表中是否包含单字「一」: " + found);

        // 查找所有单字敏感词
        System.out.println("所有单字敏感词:");
        for (String word : allWords) {
            if (word.length() == 1) {
                System.out.println("- " + word);
            }
        }
    }

    @Test
    public void checkForNumericSensitiveWords() {
        List<String> allWords = fileWordDeny.deny();

        // 检查是否包含数字"1"
        boolean contains1 = allWords.contains("1");
        System.out.println("敏感词列表中是否包含「1」: " + contains1);

        // 检查数字相关敏感词
        System.out.println("以下是包含数字的敏感词:");
        for (String word : allWords) {
            if (word.matches(".*\\d+.*")) {
                System.out.println("- " + word);
            }
        }
    }

    @Test
    public void testContainsSensitiveWords() {
        // 测试包含敏感词的情况
        assertTrue(contentFilterService.containsSensitiveWords("间谍专业版"));
        assertTrue(contentFilterService.containsSensitiveWords("这里有冰毒"));
        assertTrue(contentFilterService.containsSensitiveWords("你真是个傻帽"));
        assertTrue(contentFilterService.containsSensitiveWords("滚开，狗东西"));
        assertTrue(contentFilterService.containsSensitiveWords("口爆"));

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