package com.rhcr.algorithm;

import java.util.Objects;

/**
 * State类表示机器人在特定时间步的状态
 * 包含位置、时间戳和方向信息
 */
public class State {
    /** 机器人所在的位置索引 */
    private int location;
    
    /** 时间戳，表示到达该位置的时间步 */
    private int timestep;
    
    /** 机器人的朝向（方向） */
    private int orientation;

    /**
     * 默认构造函数，初始化为无效状态
     */
    public State() {
        this.location = -1;
        this.timestep = -1;
        this.orientation = -1;
    }

    /**
     * 带参数的构造函数
     * 
     * @param location 位置索引
     * @param timestep 时间戳
     * @param orientation 方向
     */
    public State(int location, int timestep, int orientation) {
        this.location = location;
        this.timestep = timestep;
        this.orientation = orientation;
    }

    /**
     * 拷贝构造函数
     * 
     * @param other 要拷贝的状态对象
     */
    public State(State other) {
        this.location = other.location;
        this.timestep = other.timestep;
        this.orientation = other.orientation;
    }

    /**
     * 创建一个等待状态，位置和方向不变，时间步加1
     * 
     * @return 新的等待状态
     */
    public State waitState() {
        return new State(this.location, this.timestep + 1, this.orientation);
    }

    // Getter方法
    public int getLocation() {
        return location;
    }

    public int getTimestep() {
        return timestep;
    }

    public int getOrientation() {
        return orientation;
    }

    // Setter方法
    public void setLocation(int location) {
        this.location = location;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * 重写equals方法，用于状态比较
     * 两个状态相等当且仅当它们的位置、时间戳和方向都相同
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return location == state.location &&
               timestep == state.timestep &&
               orientation == state.orientation;
    }

    /**
     * 重写hashCode方法，与equals方法保持一致
     */
    @Override
    public int hashCode() {
        return Objects.hash(location, timestep, orientation);
    }

    /**
     * 返回状态的字符串表示
     */
    @Override
    public String toString() {
        return "State{" +
               "location=" + location +
               ", timestep=" + timestep +
               ", orientation=" + orientation +
               '}';
    }
}
