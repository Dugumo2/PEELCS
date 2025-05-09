package com.graduation.peelcs.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.deny.WordDenyEmpty;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SensitiveWordCompleteConfig {

    /**
     * 配置敏感词库
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        // 创建实例
        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance()
                // 使用默认的敏感词库
                .wordDeny(WordDenys.defaults())
                // 文本处理配置
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(false)
                
                // 特殊检测配置
                .enableNumCheck(true)
                .enableEmailCheck(true)
                .enableUrlCheck(true)
                .enableIpv4Check(true)
                .numCheckLen(8)
                
                // 字符忽略与结果处理配置
                .charIgnore(SensitiveWordCharIgnores.specialChars())
                .wordResultCondition(WordResultConditions.englishWordMatch())
                .init();
        
        return sensitiveWordBs;
    }

}