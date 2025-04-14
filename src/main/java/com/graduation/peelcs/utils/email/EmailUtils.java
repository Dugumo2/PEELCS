package com.graduation.peelcs.utils.email;

import cn.hutool.core.util.RandomUtil;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.utils.redis.RedissonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.nickname:系统邮件}")  // 昵称，默认值为"系统邮件"
    private String nickname;

    @Autowired
    private RedissonService redissonService;

    /**
     * 验证码过期时间（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 5 * 60 * 1000; // 5分钟，转换为毫秒

    /**
     * 发送验证码
     * @param to 收件人
     */
    public void sendVerificationCode(String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // 设置发件人，格式为: 昵称<邮箱地址>
            message.setFrom(String.format("%s <%s>", nickname, fromEmail));
            message.setTo(to);
            message.setSubject("验证码邮件");
            // 生成6位随机数字验证码
            String code = RandomUtil.randomNumbers(6);
            // 使用RedissonService存储验证码
            redissonService.setValue(Constant.RedisKey.HIS_MAIL_CODE + to, code, CODE_EXPIRE_MINUTES);

            message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");
            mailSender.send(message);
            log.info("验证码邮件已发送至 {}", to);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码邮件失败", e);
        }
    }
} 