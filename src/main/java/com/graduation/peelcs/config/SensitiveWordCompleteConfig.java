package com.graduation.peelcs.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.graduation.peelcs.utils.sensitivewords.FileWordAllow;
import com.graduation.peelcs.utils.sensitivewords.FileWordDeny;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SensitiveWordCompleteConfig {

    private final FileWordDeny fileWordDeny;
    private final FileWordAllow fileWordAllow;

    @Autowired
    public SensitiveWordCompleteConfig(FileWordDeny fileWordDeny, FileWordAllow fileWordAllow) {
        this.fileWordDeny = fileWordDeny;
        this.fileWordAllow = fileWordAllow;
    }

    /**
     * 配置敏感词库
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        // 创建实例
        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.chains(
                        WordDenys.defaults(),  // 使用默认敏感词库
                        fileWordDeny           // 使用自定义文件敏感词库
                ))
                .wordAllow(WordAllows.chains(WordAllows.defaults(), fileWordAllow))

                // 文本处理配置
                .ignoreCase(true)              // 忽略大小写，如"ABC"和"abc"视为相同
                .ignoreWidth(true)             // 忽略全角半角，如"ＡＢＣ"和"ABC"视为相同
                .ignoreNumStyle(true)          // 忽略数字样式，如"1"和"一"、"壹"视为相同 - 问题很可能在这里！
                .ignoreChineseStyle(true)      // 忽略中文样式，如"话"和"話"视为相同
                .ignoreEnglishStyle(true)      // 忽略英文样式，如各种变形字母
                .ignoreRepeat(false)           // 不忽略重复字符

                // 特殊检测配置
                .enableNumCheck(true)          // 启用数字检测，如检测是否包含电话号码
                .enableEmailCheck(true)        // 启用邮箱检测
                .enableUrlCheck(true)          // 启用URL检测
                .enableIpv4Check(true)         // 启用IPv4地址检测
                .numCheckLen(8)                // 设置数字检测的最小长度为8

                // 字符忽略与结果处理配置
                .charIgnore(SensitiveWordCharIgnores.specialChars())  // 忽略特殊字符
                .wordResultCondition(WordResultConditions.englishWordMatch())  // 仅当英文单词完全匹配时才判定为敏感词
                .init();

        return sensitiveWordBs;
    }

}