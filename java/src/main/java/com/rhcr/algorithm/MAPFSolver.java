package com.rhcr.algorithm;

import java.util.List;

/**
 * MAPFSolver接口
 * 
 * MAPF (Multi-Agent Path Finding) 求解器的通用接口
 * 定义了多机器人路径规划求解器必须实现的方法
 * 
 * 实现这个接口的具体算法可以包括：
 * - CBS (Conflict-Based Search)
 * - ECBS (Enhanced Conflict-Based Search)
 * - PBS (Priority-Based Search)
 * - WHCA* (Windowed Hierarchical Cooperative A*)
 * 等等
 */
public interface MAPFSolver {
    
    /**
     * 求解多机器人路径规划问题
     * 
     * @param starts 所有机器人的起始状态列表
     * @param goalLocations 所有机器人的目标位置列表
     *                      每个机器人可能有多个按顺序访问的目标
     * @param timeLimit 算法的时间限制（秒）
     * @return 如果成功找到解返回true，否则返回false
     */
    boolean solve(List<State> starts, List<List<GoalLocation>> goalLocations, double timeLimit);
    
    /**
     * 获取求解结果
     * 
     * @return 包含所有机器人路径的列表
     */
    List<Path> getSolution();
    
    /**
     * 添加软路径约束
     * 
     * 软约束表示其他机器人已规划的路径，
     * 求解器应尽量避免与这些路径冲突，但在必要时可以违反
     * 
     * @param path 要添加为软约束的路径
     */
    void addSoftPathConstraint(Path path);
    
    /**
     * 清空求解器的内部状态
     * 
     * 在求解新问题之前应调用此方法，以确保求解器处于干净的状态
     */
    void clear();
    
    /**
     * 获取求解器的名称
     * 
     * @return 求解器名称，如 "CBS", "ECBS", "PBS" 等
     */
    String getName();
    
    /**
     * 获取是否保持终点的标志
     * 
     * 如果为true，表示机器人到达最后一个目标后会一直停留在该位置
     * 如果为false，表示只在规划窗口内考虑路径
     * 
     * @return 是否保持终点
     */
    boolean isHoldEndpoints();
    
    /**
     * 设置是否保持终点
     * 
     * @param holdEndpoints 是否保持终点
     */
    void setHoldEndpoints(boolean holdEndpoints);
    
    /**
     * 获取规划窗口大小
     * 
     * 规划窗口定义了求解器向前看的时间步数
     * 
     * @return 窗口大小
     */
    int getWindow();
    
    /**
     * 设置规划窗口大小
     * 
     * @param window 窗口大小
     */
    void setWindow(int window);
}
