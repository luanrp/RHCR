# RHCR Java版本 - 独立性检测算法

## 简介

这是RHCR (Rolling-Horizon Collision Resolution) 项目中**独立性检测 (Independence Detection, ID)** 算法的Java实现版本。

## 算法说明

### 什么是独立性检测算法？

独立性检测（ID）是一种用于多机器人路径规划（MAPF）的高效算法。其核心思想是：

1. **初始独立规划**：首先假设所有机器人都是相互独立的，为每个机器人单独规划路径
2. **冲突检测**：检查规划出的路径之间是否存在冲突（如两个机器人在同一时间占据同一位置）
3. **组合并重规划**：如果检测到冲突，将冲突的机器人合并为一个组，对整个组进行联合路径规划
4. **迭代执行**：重复上述过程，直到所有机器人的路径都不存在冲突

### 算法优势

- **降低计算复杂度**：大多数实际场景中，机器人的路径是相互独立的，只需要对少数冲突的机器人进行联合规划
- **可扩展性强**：能够处理大量机器人的路径规划问题
- **灵活性高**：可以与不同的底层MAPF求解器（如CBS、ECBS、PBS等）结合使用

## 项目结构

```
java/
└── src/
    └── main/
        └── java/
            └── com/
                └── rhcr/
                    └── algorithm/
                        ├── State.java                    # 状态类，表示机器人的位置、时间和方向
                        ├── Path.java                     # 路径类，表示机器人的行走路径
                        ├── GoalLocation.java             # 目标位置类
                        ├── MAPFSolver.java               # MAPF求解器接口
                        └── IndependenceDetection.java    # 独立性检测算法主类
```

## 核心类说明

### 1. State (状态类)

表示机器人在某一时刻的状态，包含：
- `location`: 位置索引
- `timestep`: 时间戳
- `orientation`: 朝向

### 2. Path (路径类)

表示机器人的完整路径，是一个State对象的有序列表。

### 3. GoalLocation (目标位置类)

表示机器人的目标位置，包含：
- `location`: 目标位置索引
- `releaseTime`: 最早可到达时间（用于时序约束）

### 4. MAPFSolver (求解器接口)

定义了MAPF求解器的通用接口，包括：
- `solve()`: 求解路径规划问题
- `getSolution()`: 获取求解结果
- `addSoftPathConstraint()`: 添加软约束（其他机器人的路径）
- `clear()`: 清空求解器状态

### 5. IndependenceDetection (独立性检测算法)

算法的主要实现类，核心方法包括：
- `run()`: 运行算法的入口方法
- `planPathsForGroup()`: 为指定的机器人组规划路径
- `hasConflicts()`: 检测两条路径是否存在冲突

## 冲突检测机制

算法支持两种冲突检测模式：

### 1. Hold Endpoints 模式

机器人到达终点后会一直停留，检测：
- **顶点冲突**：两个机器人在同一时间出现在同一位置
- **边冲突**：两个机器人在相邻时间步交换位置
- **终点冲突**：移动的机器人与已停留在终点的机器人冲突

### 2. Window 模式（窗口模式）

只检测规划窗口内的冲突，支持：
- **标准冲突检测**：检测顶点冲突和边冲突
- **k-robust冲突检测**：考虑时间不确定性，在时间窗口[t-k, t+k]内检测冲突

## 使用示例

```java
// 创建底层MAPF求解器（需要自行实现或使用现有实现）
MAPFSolver solver = new YourMAPFSolverImplementation();

// 创建独立性检测算法实例
int kRobust = 0;           // k-robust参数
int window = 10;           // 规划窗口大小
boolean holdEndpoints = true;  // 是否保持终点

IndependenceDetection id = new IndependenceDetection(solver, kRobust, window, holdEndpoints);

// 准备输入数据
List<State> starts = new ArrayList<>();
starts.add(new State(0, 0, 0));  // 机器人1的起始状态
starts.add(new State(5, 0, 0));  // 机器人2的起始状态

List<List<GoalLocation>> goals = new ArrayList<>();
List<GoalLocation> goals1 = new ArrayList<>();
goals1.add(new GoalLocation(10, 0));  // 机器人1的目标
goals.add(goals1);

List<GoalLocation> goals2 = new ArrayList<>();
goals2.add(new GoalLocation(15, 0));  // 机器人2的目标
goals.add(goals2);

// 运行算法
double timeLimit = 60.0;  // 60秒时间限制
boolean success = id.run(starts, goals, timeLimit);

// 获取结果
if (success && id.isSolutionFound()) {
    List<Path> solution = id.getSolution();
    System.out.println("找到解！");
    for (int i = 0; i < solution.size(); i++) {
        System.out.println("机器人 " + i + " 的路径：");
        System.out.println(solution.get(i));
    }
}
```

## 编译说明

本代码使用标准Java语法编写，兼容Java 8及以上版本。

### 编译命令

```bash
cd java/src/main/java
javac com/rhcr/algorithm/*.java
```

### 使用Maven构建（可选）

如果需要使用Maven进行项目管理，可以创建对应的pom.xml文件。

## 代码规范

本实现遵循以下Java编码规范：

1. **命名规范**
   - 类名：大驼峰命名法（PascalCase）
   - 方法名和变量名：小驼峰命名法（camelCase）
   - 常量：全大写加下划线（CONSTANT_CASE）

2. **注释规范**
   - 所有公共类、方法都有完整的Javadoc注释
   - 关键算法逻辑有详细的中文行内注释
   - 注释说明了参数含义、返回值和功能

3. **代码组织**
   - 合理的包结构
   - 清晰的职责划分
   - 遵循单一职责原则

4. **Java最佳实践**
   - 使用接口定义契约
   - 适当的访问修饰符
   - 重写equals()和hashCode()方法
   - 提供getter和setter方法

## 原始C++版本参考

原始的C++实现位于：
- 头文件：`inc/ID.h`
- 实现文件：`src/ID.cpp`

## 许可证

本Java实现遵循与原RHCR项目相同的许可证（USC Research License）。

## 参考文献

[1] Jiaoyang Li, Andrew Tinka, Scott Kiesel, Joseph W. Durham, T. K. Satish Kumar and Sven Koenig. 
    "Lifelong Multi-Agent Path Finding in Large-Scale Warehouses." 
    In Proceedings of the AAAI Conference on Artificial Intelligence (AAAI), 2021.

[2] Jiaoyang Li, Andrew Tinka, Scott Kiesel, Joseph W. Durham, T. K. Satish Kumar and Sven Koenig. 
    "Lifelong Multi-Agent Path Finding in Large-Scale Warehouses (extended abstract)." 
    In Proceedings of the International Joint Conference on Autonomous Agents and Multiagent Systems (AAMAS), 2020.
