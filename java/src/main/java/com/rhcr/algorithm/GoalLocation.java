package com.rhcr.algorithm;

/**
 * GoalLocation类表示目标位置
 * 包含目标的位置索引和释放时间
 */
public class GoalLocation {
    /** 目标位置的索引 */
    private int location;
    
    /** 目标的释放时间（机器人最早可以到达的时间） */
    private int releaseTime;

    /**
     * 构造函数
     * 
     * @param location 目标位置索引
     * @param releaseTime 释放时间
     */
    public GoalLocation(int location, int releaseTime) {
        this.location = location;
        this.releaseTime = releaseTime;
    }

    /**
     * 获取目标位置
     * 
     * @return 位置索引
     */
    public int getLocation() {
        return location;
    }

    /**
     * 获取释放时间
     * 
     * @return 释放时间
     */
    public int getReleaseTime() {
        return releaseTime;
    }

    /**
     * 设置目标位置
     * 
     * @param location 位置索引
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * 设置释放时间
     * 
     * @param releaseTime 释放时间
     */
    public void setReleaseTime(int releaseTime) {
        this.releaseTime = releaseTime;
    }

    @Override
    public String toString() {
        return "GoalLocation{" +
               "location=" + location +
               ", releaseTime=" + releaseTime +
               '}';
    }
}
