package com.rhcr.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IndependenceDetection (ID) 算法类
 * 
 * 这是一个用于多机器人路径规划的独立性检测算法。
 * 算法的核心思想是：
 * 1. 初始时将每个机器人视为独立的个体，单独进行路径规划
 * 2. 检测规划后的路径是否存在冲突
 * 3. 如果存在冲突，将冲突的机器人合并为一个组，重新进行联合规划
 * 4. 重复上述过程直到所有机器人的路径都不存在冲突
 * 
 * 这种方法可以有效减少计算复杂度，因为大部分机器人的路径是相互独立的。
 */
public class IndependenceDetection {
    
    /** 所有机器人的起始状态 */
    private List<State> starts;
    
    /** 所有机器人的目标位置列表（每个机器人可能有多个顺序目标） */
    private List<List<GoalLocation>> goalLocations;
    
    /** 算法的时间限制（秒） */
    private double timeLimit;
    
    /** 机器人的数量 */
    private int numOfAgents;
    
    /** 存储每个机器人的最终路径解 */
    private List<Path> solution;
    
    /** 记录每个机器人所属的组ID */
    private List<Integer> groupIds;
    
    /** 算法开始的时间戳 */
    private long startTime;
    
    /** 算法的运行时间 */
    private double runtime;
    
    /** 是否找到解 */
    private boolean solutionFound;
    
    /** k-robust参数，用于冲突检测的鲁棒性 */
    private int kRobust;
    
    /** 规划窗口大小 */
    private int window;
    
    /** 是否保持终点 */
    private boolean holdEndpoints;
    
    /** 底层的MAPF求解器（需要注入） */
    private MAPFSolver solver;

    /**
     * 构造函数
     * 
     * @param solver 底层的多机器人路径规划求解器
     * @param kRobust k-robust参数，用于冲突检测
     * @param window 规划窗口大小
     * @param holdEndpoints 是否保持终点
     */
    public IndependenceDetection(MAPFSolver solver, int kRobust, int window, boolean holdEndpoints) {
        this.solver = solver;
        this.kRobust = kRobust;
        this.window = window;
        this.holdEndpoints = holdEndpoints;
        this.solutionFound = false;
        this.runtime = 0.0;
    }

    /**
     * 运行ID算法的主函数
     * 
     * @param starts 所有机器人的起始状态
     * @param goalLocations 所有机器人的目标位置列表
     * @param timeLimit 算法的时间限制（秒）
     * @return 如果算法成功运行返回true，否则返回false
     */
    public boolean run(List<State> starts, List<List<GoalLocation>> goalLocations, double timeLimit) {
        // 记录开始时间
        this.startTime = System.currentTimeMillis();
        
        // 初始化参数
        this.starts = starts;
        this.goalLocations = goalLocations;
        this.timeLimit = timeLimit;
        this.numOfAgents = starts.size();
        
        // 初始化解和组ID
        this.solution = new ArrayList<>(numOfAgents);
        this.groupIds = new ArrayList<>(numOfAgents);
        
        // 为每个机器人初始化空路径和独立的组ID
        for (int i = 0; i < numOfAgents; i++) {
            solution.add(new Path());
            groupIds.add(i); // 初始时每个机器人都在自己的组中
        }

        // 标记是否所有机器人都成功规划了路径
        solutionFound = true;
        
        // 对每个机器人（组）进行路径规划
        for (int i = 0; i < numOfAgents; i++) {
            if (!planPathsForGroup(i)) {
                solutionFound = false;
            }
        }

        // 计算总运行时间
        this.runtime = (System.currentTimeMillis() - startTime) / 1000.0;
        
        // 打印结果
        printResults();
        
        return true;
    }

    /**
     * 为指定的组进行路径规划
     * 
     * 这个方法会：
     * 1. 收集组内所有机器人的起始状态和目标
     * 2. 调用底层求解器为这个组规划路径
     * 3. 检测新规划的路径与其他组的路径是否冲突
     * 4. 如果有冲突，合并冲突的组并重新规划
     * 
     * @param groupId 要规划路径的组ID
     * @return 如果成功规划返回true，否则返回false
     */
    private boolean planPathsForGroup(int groupId) {
        // 收集当前组的机器人信息
        List<State> currStarts = new ArrayList<>();
        List<List<GoalLocation>> currGoalLocations = new ArrayList<>();
        List<Integer> currAgents = new ArrayList<>();
        
        // 遍历所有机器人，找出属于当前组的机器人
        for (int i = 0; i < numOfAgents; i++) {
            if (groupIds.get(i) == groupId) {
                // 属于当前组的机器人
                currStarts.add(starts.get(i));
                currGoalLocations.add(goalLocations.get(i));
                currAgents.add(i);
            } else if (!solution.get(i).isEmpty()) {
                // 不属于当前组但已有路径的机器人，将其路径作为软约束
                solver.addSoftPathConstraint(solution.get(i));
            }
        }

        // 检查剩余时间
        this.runtime = (System.currentTimeMillis() - startTime) / 1000.0;
        double remainingTime = timeLimit - runtime;
        
        // 调用底层求解器为当前组规划路径
        boolean success = solver.solve(currStarts, currGoalLocations, remainingTime);
        
        if (!success) {
            return false;
        }

        // 获取求解器返回的路径并更新solution
        List<Path> groupSolution = solver.getSolution();
        
        // 检查求解器是否返回了正确数量的路径
        if (groupSolution == null || groupSolution.size() != currAgents.size()) {
            return false;
        }
        
        // 创建groupSolution的副本，因为调用solver.clear()后原来的列表可能被清空
        List<Path> groupSolutionCopy = new ArrayList<>(groupSolution);
        
        for (int i = 0; i < currAgents.size(); i++) {
            solution.set(currAgents.get(i), groupSolutionCopy.get(i));
        }
        
        // 清理求解器状态，准备下一次调用
        solver.clear();

        // 检测新规划的路径是否与其他组的路径冲突
        for (int i = 0; i < currAgents.size(); i++) {
            Path path1 = groupSolutionCopy.get(i);
            
            // 与所有其他组的机器人路径进行冲突检测
            for (int j = 0; j < numOfAgents; j++) {
                // 跳过当前组的机器人
                if (groupIds.get(j) == groupId) {
                    continue;
                }
                
                Path path2 = solution.get(j);
                
                // 如果path2为空，说明该机器人还未规划路径，跳过
                if (path2 == null || path2.isEmpty()) {
                    continue;
                }
                
                // 检测是否存在冲突
                if (hasConflicts(path1, path2)) {
                    // 存在冲突，需要合并组
                    int otherGroupId = groupIds.get(j);
                    
                    // 将当前组的所有机器人合并到另一个组
                    for (int id : currAgents) {
                        groupIds.set(id, otherGroupId);
                    }
                    
                    // 递归地为合并后的组重新规划路径
                    return planPathsForGroup(otherGroupId);
                }
            }
        }
        
        // 没有冲突，规划成功
        return true;
    }

    /**
     * 检测两条路径是否存在冲突
     * 
     * 冲突的定义：
     * 1. 顶点冲突：两个机器人在同一时间步出现在同一位置
     * 2. 边冲突：两个机器人在相邻时间步交换位置
     * 3. k-robust冲突：考虑时间窗口的鲁棒性检测
     * 
     * @param path1 第一条路径
     * @param path2 第二条路径
     * @return 如果存在冲突返回true，否则返回false
     */
    private boolean hasConflicts(Path path1, Path path2) {
        // 如果任一路径为空，不存在冲突
        if (path1.isEmpty() || path2.isEmpty()) {
            return false;
        }

        // 情况1：hold_endpoints模式（保持终点）
        if (holdEndpoints) {
            int minPathLength = Math.min(path1.size(), path2.size());
            
            // 检查共同时间步内的冲突
            for (int timestep = 0; timestep < minPathLength; timestep++) {
                int loc1 = path1.get(timestep).getLocation();
                int loc2 = path2.get(timestep).getLocation();
                
                // 顶点冲突：同一时间在同一位置
                if (loc1 == loc2) {
                    return true;
                }
                
                // 边冲突：相邻时间步交换位置
                if (timestep < minPathLength - 1) {
                    int nextLoc1 = path1.get(timestep + 1).getLocation();
                    int nextLoc2 = path2.get(timestep + 1).getLocation();
                    if (loc1 == nextLoc2 && loc2 == nextLoc1) {
                        return true;
                    }
                }
            }

            // 检查较长路径与较短路径终点的冲突
            if (path1.size() < path2.size()) {
                int loc1 = path1.get(path1.size() - 1).getLocation();
                for (int timestep = minPathLength; timestep < path2.size(); timestep++) {
                    int loc2 = path2.get(timestep).getLocation();
                    if (loc1 == loc2) {
                        return true;
                    }
                }
            } else if (path2.size() < path1.size()) {
                int loc2 = path2.get(path2.size() - 1).getLocation();
                for (int timestep = minPathLength; timestep < path1.size(); timestep++) {
                    int loc1 = path1.get(timestep).getLocation();
                    if (loc1 == loc2) {
                        return true;
                    }
                }
            }
        } 
        // 情况2：窗口模式（只检查规划窗口内的冲突）
        else {
            // 确定检查范围
            int size1 = Math.min(window + 1, path1.size());
            int size2 = Math.min(window + 1, path2.size());
            
            for (int timestep = 0; timestep < size1; timestep++) {
                // 如果path2已经结束且超出k-robust范围，停止检查
                if (size2 <= timestep - kRobust) {
                    break;
                }
                
                // k-robust冲突检测（考虑时间不确定性）
                if (kRobust > 0) {
                    int loc = path1.get(timestep).getLocation();
                    
                    // 检查时间窗口[timestep-kRobust, timestep+kRobust]内的冲突
                    int startTime = Math.max(0, timestep - kRobust);
                    int endTime = Math.min(timestep + kRobust, size2 - 1);
                    
                    for (int i = startTime; i <= endTime; i++) {
                        if (loc == path2.get(i).getLocation()) {
                            return true;
                        }
                    }
                } 
                // 标准冲突检测（不考虑时间不确定性）
                else {
                    if (timestep >= size2) {
                        break;
                    }
                    
                    int loc1 = path1.get(timestep).getLocation();
                    int loc2 = path2.get(timestep).getLocation();
                    
                    // 顶点冲突
                    if (loc1 == loc2) {
                        return true;
                    }
                    
                    // 边冲突
                    if (timestep < size1 - 1 && timestep < size2 - 1) {
                        int nextLoc1 = path1.get(timestep + 1).getLocation();
                        int nextLoc2 = path2.get(timestep + 1).getLocation();
                        if (loc1 == nextLoc2 && loc2 == nextLoc1) {
                            return true;
                        }
                    }
                }
            }
        }
        
        // 没有检测到冲突
        return false;
    }

    /**
     * 打印算法运行结果
     */
    private void printResults() {
        System.out.print("ID算法:");
        
        if (solutionFound) {
            System.out.print("成功,");
        } else if (runtime > timeLimit) {
            System.out.print("超时,");
        } else {
            System.out.print("无解,");
        }

        if (solutionFound) {
            // 统计组的信息
            Map<Integer, Integer> groupSizes = new HashMap<>();
            for (int groupId : groupIds) {
                groupSizes.put(groupId, groupSizes.getOrDefault(groupId, 0) + 1);
            }
            
            int numOfGroups = groupSizes.size();
            int largestGroup = groupSizes.values().stream().max(Integer::compareTo).orElse(0);
            
            System.out.printf("运行时间=%.3f秒, 组数=%d, 最大组大小=%d, 规划窗口=%d%n",
                            runtime, numOfGroups, largestGroup, window);
        } else {
            System.out.printf("运行时间=%.3f秒%n", runtime);
        }
    }

    /**
     * 获取算法名称
     * 
     * @return 算法名称
     */
    public String getName() {
        return "ID+" + (solver != null ? solver.getName() : "Unknown");
    }

    // Getter方法
    
    /**
     * 获取最终的路径解
     * 
     * @return 包含所有机器人路径的列表
     */
    public List<Path> getSolution() {
        return solution;
    }

    /**
     * 获取运行时间
     * 
     * @return 运行时间（秒）
     */
    public double getRuntime() {
        return runtime;
    }

    /**
     * 是否找到解
     * 
     * @return 如果找到解返回true，否则返回false
     */
    public boolean isSolutionFound() {
        return solutionFound;
    }

    /**
     * 获取组ID列表
     * 
     * @return 每个机器人的组ID
     */
    public List<Integer> getGroupIds() {
        return groupIds;
    }

    /**
     * 清空算法状态
     */
    public void clear() {
        solution.clear();
        groupIds.clear();
        runtime = 0.0;
        solutionFound = false;
    }
}
