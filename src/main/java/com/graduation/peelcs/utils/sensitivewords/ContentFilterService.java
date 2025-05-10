package com.graduation.peelcs.utils.sensitivewords;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentFilterService {
    
    private final SensitiveWordBs sensitiveWordBs;
    
    @Autowired
    public ContentFilterService(SensitiveWordBs sensitiveWordBs) {
        this.sensitiveWordBs = sensitiveWordBs;
    }
    
    /**
     * 检查内容是否包含敏感词
     */
    public boolean containsSensitiveWords(String content) {
        return sensitiveWordBs.contains(content);
    }
    
    /**
     * 替换敏感词为星号
     */
    public String filterContent(String content) {
        return sensitiveWordBs.replace(content);
    }
    
    /**
     * 查找所有敏感词
     */
    public List<String> findAllSensitiveWords(String content) {
        return sensitiveWordBs.findAll(content);
    }
}