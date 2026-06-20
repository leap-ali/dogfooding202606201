# 三人斗地主在线对战平台

## 项目简介

基于 Vue3 + SpringBoot 的三人斗地主在线对战平台，支持实时对战、积分排行、游客/账号登录等功能。

## 技术栈

### 前端
- Vue 3.3 + Vite 4
- Element Plus 2.3
- Pinia 状态管理
- Socket.IO Client 4.7
- Axios
- SCSS

### 后端
- Spring Boot 2.7
- MyBatis-Plus 3.5
- MySQL 8.0 (兼容5.5+)
- Netty Socket.IO 1.7
- JWT (jjwt 0.9)
- Hutool 工具类

## 项目结构

```
solo1/
├── sql/                    # 数据库初始化脚本
│   └── init.sql
├── ddz-server/             # 后端项目 (SpringBoot)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ddz/
│       │   ├── DdzApplication.java
│       │   ├── config/          # 配置类
│       │   ├── controller/      # 控制层
│       │   ├── service/         # 业务层
│       │   ├── service/impl/    # 业务实现
│       │   ├── mapper/          # 数据层
│       │   ├── entity/          # 实体类
│       │   ├── game/            # 游戏核心逻辑
│       │   ├── socket/          # Socket.IO 处理
│       │   ├── interceptor/     # 拦截器
│       │   └── util/            # 工具类
│       └── resources/
│           └── application.yml
└── ddz-web/                # 前端项目 (Vue3 + Vite)
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/              # 路由
        ├── stores/              # Pinia 状态
        ├── api/                 # HTTP 接口
        ├── socket/              # Socket.IO 通信
        ├── components/          # 组件
        ├── views/               # 页面
        └── styles/              # 全局样式
```

## 快速开始

### 1. 环境准备

- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- MySQL 5.5 或 8.0

### 2. 数据库初始化

```bash
# 在 MySQL 中执行
mysql -u root -p < sql/init.sql
```

默认创建数据库 `ddz`，包含测试用户：
- 普通用户: admin / admin123
- 游客: guest_001 / guest

### 3. 启动后端服务

```bash
cd ddz-server
mvn spring-boot:run
```

后端服务运行在:
- HTTP 接口: http://localhost:8080
- Socket.IO: http://localhost:9092

### 4. 启动前端项目

```bash
cd ddz-web
npm install
npm run dev
```

前端访问: http://localhost:5173

## 功能说明

### 用户模块
- 账号注册登录
- 游客快捷登录
- JWT token 鉴权
- 积分统计

### 房间匹配
- 大厅房间列表
- 创建房间（自定义底分）
- 加入房间
- 自动开局

### 斗地主玩法
- 标准 54 张扑克牌
- 三人轮流抢地主
- 地主拿 3 张底牌
- 支持牌型：单张、对子、三张、三带一、顺子、连对、飞机、炸弹、王炸
- 实时同步出牌状态

### 记录排行
- 个人历史对局记录
- 积分排行榜（前 20 名）
- 积分结算：地主赢 +3倍，农民赢各 +1倍

## 游戏规则

### 牌型说明
| 牌型 | 说明 |
|------|------|
| 单张 | 任意一张单牌 |
| 对子 | 两张相同点数的牌 |
| 三张 | 三张相同点数的牌 |
| 三带一 | 三张相同点数 + 一张单牌 |
| 顺子 | 五张或更多连续单牌（3-A，不含2和王） |
| 连对 | 三对或更多连续对子（3-A，不含2和王） |
| 飞机 | 两个或更多连续三张（3-A，不含2和王） |
| 飞机带单 | 飞机 + 等量单牌 |
| 炸弹 | 四张相同点数的牌 |
| 王炸 | 大小王一起出，最大牌型 |

### 比较规则
- 炸弹可压所有普通牌型
- 王炸最大，可压所有牌
- 同牌型比较主要点数大小
- 顺子、连对、飞机比较最小点数

### 积分规则
- 地主胜利：地主 +3倍底分，每个农民 -1倍底分
- 农民胜利：地主 -3倍底分，每个农民 +1倍底分
- 逃跑扣除双倍积分

## 目录详细说明

### 后端核心模块

- **game/**: 游戏核心逻辑
  - `Card.java` - 扑克牌实体
  - `CardType.java` - 牌型枚举
  - `CardUtils.java` - 牌型分析、比较、提示
  - `GameRoom.java` - 游戏房间状态机
  - `Player.java` - 玩家对象

- **socket/**: Socket.IO 实时通信
  - `SocketEventHandler.java` - 事件处理
  - `GameRoomManager.java` - 房间管理
  - `SocketSessionManager.java` - 会话管理

### 前端核心模块

- **stores/**: Pinia 状态管理
  - `user.js` - 用户状态
  - `game.js` - 游戏状态

- **views/**: 页面
  - `Login.vue` - 登录/注册页
  - `Lobby.vue` - 游戏大厅
  - `RoomWait.vue` - 房间等待
  - `Game.vue` - 对局主页面

- **components/**: 组件
  - `PokerCard.vue` - 扑克牌组件

## 开发说明

### 后端开发
```bash
cd ddz-server
mvn clean compile
mvn spring-boot:run
```

### 前端开发
```bash
cd ddz-web
npm run dev   # 开发模式
npm run build # 生产构建
```

## 注意事项

1. 启动前请确保 MySQL 已运行并已执行 init.sql
2. 如需修改数据库配置，请编辑 `ddz-server/src/main/resources/application.yml`
3. 前端开发模式下通过 Vite 代理访问后端接口
4. 三人斗地主需要三个客户端才能开始游戏，可以开三个浏览器标签页测试
