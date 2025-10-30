package com.rhcr.algorithm;

import java.util.ArrayList;

/**
 * Path类表示机器人的行走路径
 * 路径由一系列按时间顺序排列的状态组成
 */
public class Path extends ArrayList<State> {
    
    /**
     * 默认构造函数
     */
    public Path() {
        super();
    }

    /**
     * 指定初始容量的构造函数
     * 
     * @param initialCapacity 初始容量
     */
    public Path(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 获取路径的长度（状态数量）
     * 
     * @return 路径长度
     */
    public int getLength() {
        return this.size();
    }

    /**
     * 检查路径是否为空
     * 
     * @return 如果路径为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * 返回路径的字符串表示
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "Path{empty}";
        }
        StringBuilder sb = new StringBuilder("Path{\n");
        for (int i = 0; i < size(); i++) {
            sb.append("  [").append(i).append("] ").append(get(i)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
