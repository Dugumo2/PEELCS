package com.graduation.peelcs.commen;

/**
 * @author 冯
 * @description
 * @create 2025-04-14 20:43
 */
public class Constant {
    /**
     * Redis Key前缀
     */
    public static class RedisKey {
        /**
         * 邮箱验证码前缀
         */
        public static final String HIS_MAIL_CODE = "his:mail:code:";
        
        /**
         * 番茄钟计时信息前缀
         */
        public static final String POMODORO_TIMER = "pomodoro:timer:";
        
        /**
         * 用户每日发帖积分记录键前缀
         */
        public static final String POST_POINTS_KEY_PREFIX = "forum:post:points:";
        
        /**
         * 用户每日评论积分记录键前缀
         */
        public static final String COMMENT_POINTS_KEY_PREFIX = "forum:comment:points:";
    }
    
    /**
     * 会话类型
     */
    public static class SessionType {
        /**
         * 工作时间
         */
        public static final String WORK = "work";
        
        /**
         * 休息时间
         */
        public static final String BREAK = "break";
    }
    
    /**
     * 会话状态
     */
    public static class SessionState {
        /**
         * 进行中
         */
        public static final String RUNNING = "running";
        
        /**
         * 已暂停
         */
        public static final String PAUSED = "paused";
        
        /**
         * 已完成
         */
        public static final String COMPLETED = "completed";
    }
    
    /**
     * 番茄钟默认设置
     */
    public static class PomodoroSettings {
        /**
         * 默认工作时间（分钟）
         */
        public static final int DEFAULT_WORK_MINUTES = 25;
        
        /**
         * 默认短休息时间（分钟）
         */
        public static final int DEFAULT_SHORT_BREAK_MINUTES = 5;
        
        /**
         * 默认长休息时间（分钟）
         */
        public static final int DEFAULT_LONG_BREAK_MINUTES = 15;
        
        /**
         * 长休息间隔（完成几个番茄后休息较长时间）
         */
        public static final int LONG_BREAK_INTERVAL = 4;
    }
    
    /**
     * 积分设置
     */
    public static class PointsSettings {
        /**
         * 发帖获得的积分
         */
        public static final int POST_POINTS = 10;
        
        /**
         * 评论获得的积分
         */
        public static final int COMMENT_POINTS = 5;
        
        /**
         * 签到获得的积分
         */
        public static final int CHECKIN_POINTS = 3;
        
        /**
         * 每日获得积分的上限次数
         */
        public static final int DAILY_POINTS_LIMIT = 3;
    }
}
