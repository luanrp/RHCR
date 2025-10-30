package com.rhcr.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * IndependenceDetection算法的示例用法
 * 
 * 这个类展示了如何使用独立性检测算法来解决多机器人路径规划问题
 */
public class Example {
    
    /**
     * 主函数 - 运行示例
     */
    public static void main(String[] args) {
        System.out.println("=== 独立性检测算法示例 ===\n");
        
        // 示例1：简单的两机器人场景
        simpleExample();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 示例2：多机器人场景
        multiAgentExample();
    }
    
    /**
     * 示例1：简单的两机器人场景
     * 
     * 场景描述：
     * - 机器人1从位置0出发，目标是位置10
     * - 机器人2从位置5出发，目标是位置15
     * - 如果路径不冲突，应该各自独立规划
     */
    private static void simpleExample() {
        System.out.println("示例1：简单的两机器人场景");
        System.out.println("----------------------------------------");
        
        // 创建一个模拟的MAPF求解器
        MAPFSolver mockSolver = new SimpleMockSolver();
        
        // 配置参数
        int kRobust = 0;              // 不使用k-robust
        int window = 10;              // 规划窗口为10个时间步
        boolean holdEndpoints = true; // 保持终点
        
        // 创建独立性检测算法实例
        IndependenceDetection id = new IndependenceDetection(mockSolver, kRobust, window, holdEndpoints);
        
        // 设置起始状态
        List<State> starts = new ArrayList<>();
        starts.add(new State(0, 0, 0));  // 机器人1：位置0，时间0，方向0
        starts.add(new State(5, 0, 0));  // 机器人2：位置5，时间0，方向0
        
        // 设置目标位置
        List<List<GoalLocation>> goals = new ArrayList<>();
        
        // 机器人1的目标
        List<GoalLocation> goals1 = new ArrayList<>();
        goals1.add(new GoalLocation(10, 0));  // 目标位置10，释放时间0
        goals.add(goals1);
        
        // 机器人2的目标
        List<GoalLocation> goals2 = new ArrayList<>();
        goals2.add(new GoalLocation(15, 0));  // 目标位置15，释放时间0
        goals.add(goals2);
        
        // 运行算法
        System.out.println("开始运行独立性检测算法...");
        double timeLimit = 60.0;  // 60秒时间限制
        boolean success = id.run(starts, goals, timeLimit);
        
        // 打印结果
        System.out.println("\n算法执行完成！");
        System.out.println("状态：" + (success ? "成功" : "失败"));
        System.out.println("是否找到解：" + (id.isSolutionFound() ? "是" : "否"));
        System.out.println("运行时间：" + String.format("%.3f", id.getRuntime()) + " 秒");
        
        if (id.isSolutionFound()) {
            System.out.println("\n机器人分组情况：");
            List<Integer> groupIds = id.getGroupIds();
            for (int i = 0; i < groupIds.size(); i++) {
                System.out.println("  机器人 " + i + " -> 组 " + groupIds.get(i));
            }
        }
    }
    
    /**
     * 示例2：多机器人场景
     * 
     * 场景描述：
     * - 4个机器人从不同位置出发，前往不同的目标
     * - 演示算法如何处理可能的冲突
     */
    private static void multiAgentExample() {
        System.out.println("示例2：多机器人场景（4个机器人）");
        System.out.println("----------------------------------------");
        
        // 创建模拟求解器
        MAPFSolver mockSolver = new SimpleMockSolver();
        
        // 配置参数
        int kRobust = 1;              // 使用k-robust=1
        int window = 15;              // 更大的规划窗口
        boolean holdEndpoints = false; // 不保持终点
        
        // 创建算法实例
        IndependenceDetection id = new IndependenceDetection(mockSolver, kRobust, window, holdEndpoints);
        
        // 设置4个机器人的起始状态
        List<State> starts = new ArrayList<>();
        starts.add(new State(0, 0, 0));   // 机器人0
        starts.add(new State(10, 0, 0));  // 机器人1
        starts.add(new State(20, 0, 0));  // 机器人2
        starts.add(new State(30, 0, 0));  // 机器人3
        
        // 设置目标位置
        List<List<GoalLocation>> goals = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            List<GoalLocation> agentGoals = new ArrayList<>();
            // 每个机器人有两个顺序目标
            agentGoals.add(new GoalLocation(15 + i * 5, 0));
            agentGoals.add(new GoalLocation(40 + i * 5, 10));
            goals.add(agentGoals);
        }
        
        // 运行算法
        System.out.println("开始运行独立性检测算法...");
        System.out.println("配置：k-robust=" + kRobust + ", window=" + window + 
                         ", holdEndpoints=" + holdEndpoints);
        
        double timeLimit = 120.0;  // 120秒时间限制
        boolean success = id.run(starts, goals, timeLimit);
        
        // 打印结果
        System.out.println("\n算法执行完成！");
        System.out.println("算法名称：" + id.getName());
        System.out.println("是否找到解：" + (id.isSolutionFound() ? "是" : "否"));
        
        if (id.isSolutionFound()) {
            System.out.println("\n最终分组结果：");
            List<Integer> groupIds = id.getGroupIds();
            
            // 统计每个组的大小
            java.util.Map<Integer, java.util.List<Integer>> groups = new java.util.HashMap<>();
            for (int i = 0; i < groupIds.size(); i++) {
                int groupId = groupIds.get(i);
                groups.computeIfAbsent(groupId, k -> new ArrayList<>()).add(i);
            }
            
            System.out.println("总共 " + groups.size() + " 个独立组：");
            for (java.util.Map.Entry<Integer, java.util.List<Integer>> entry : groups.entrySet()) {
                System.out.println("  组 " + entry.getKey() + "：机器人 " + entry.getValue() + 
                                 " (大小=" + entry.getValue().size() + ")");
            }
            
            // 打印部分路径信息
            System.out.println("\n路径信息：");
            List<Path> solution = id.getSolution();
            for (int i = 0; i < solution.size(); i++) {
                Path path = solution.get(i);
                if (!path.isEmpty()) {
                    System.out.println("  机器人 " + i + "：" + 
                                     path.size() + " 个时间步，从位置 " + 
                                     path.get(0).getLocation() + " 到位置 " + 
                                     path.get(path.size() - 1).getLocation());
                }
            }
        }
    }
    
    /**
     * SimpleMockSolver - 一个简单的模拟MAPF求解器
     * 
     * 这个类提供了MAPFSolver接口的基本实现，用于演示目的。
     * 实际应用中应该使用真实的MAPF算法（如CBS、ECBS、PBS等）。
     */
    private static class SimpleMockSolver implements MAPFSolver {
        private List<Path> solution;
        private int window;
        private boolean holdEndpoints;
        
        public SimpleMockSolver() {
            this.solution = new ArrayList<>();
            this.window = 10;
            this.holdEndpoints = true;
        }
        
        @Override
        public boolean solve(List<State> starts, List<List<GoalLocation>> goalLocations, double timeLimit) {
            // 这是一个简化的模拟实现
            // 为每个机器人生成一条简单的直线路径
            solution = new ArrayList<>();
            
            for (int i = 0; i < starts.size(); i++) {
                Path path = generateSimplePath(starts.get(i), goalLocations.get(i));
                solution.add(path);
            }
            
            return true;
        }
        
        /**
         * 生成一条简单的模拟路径
         */
        private Path generateSimplePath(State start, List<GoalLocation> goals) {
            Path path = new Path();
            
            int currentLoc = start.getLocation();
            int currentTime = start.getTimestep();
            int currentOri = start.getOrientation();
            
            // 添加起始状态
            path.add(new State(currentLoc, currentTime, currentOri));
            
            // 为每个目标生成路径段
            for (GoalLocation goal : goals) {
                int targetLoc = goal.getLocation();
                int steps = Math.abs(targetLoc - currentLoc);
                
                // 生成从当前位置到目标的路径
                for (int step = 1; step <= steps; step++) {
                    currentTime++;
                    currentLoc += (targetLoc > currentLoc) ? 1 : -1;
                    path.add(new State(currentLoc, currentTime, currentOri));
                }
            }
            
            return path;
        }
        
        @Override
        public List<Path> getSolution() {
            return solution;
        }
        
        @Override
        public void addSoftPathConstraint(Path path) {
            // 模拟实现：不做任何处理
        }
        
        @Override
        public void clear() {
            solution.clear();
        }
        
        @Override
        public String getName() {
            return "SimpleMockSolver";
        }
        
        @Override
        public boolean isHoldEndpoints() {
            return holdEndpoints;
        }
        
        @Override
        public void setHoldEndpoints(boolean holdEndpoints) {
            this.holdEndpoints = holdEndpoints;
        }
        
        @Override
        public int getWindow() {
            return window;
        }
        
        @Override
        public void setWindow(int window) {
            this.window = window;
        }
    }
}
