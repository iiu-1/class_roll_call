# 课堂点名系统

基于 Spring Boot + Vue 3 的课堂随机点名与答题统计系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API), Element Plus, Axios, Vue Router, Vite |
| 后端 | Spring Boot 3.4, MyBatis-Plus 3.5, Apache POI |
| 数据库 | MySQL 8.0 |
| 部署 | Nginx（反向代理 + 静态文件服务） |

## 功能

- **学生管理**：CRUD、分页搜索、跨页批量删除、Excel/CSV 批量导入
- **智能点名**：公平算法（被点名少的优先），答错超阈值自动切换高分补抽模式
- **数据统计**：学生总数/点名次数/答对次数/正确率，按正确率排序

## 项目结构

```
class_roll_cal/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/rollcall/
│   │   ├── common/Result.java       # 统一响应
│   │   ├── config/                   # MyBatis-Plus / WebMvc 配置
│   │   ├── controller/               # StudentController, RollCallController, StatisticsController
│   │   ├── dto/                      # ImportResult, RollCallResult, StatisticsDTO
│   │   ├── entity/Student.java       # 学生实体
│   │   ├── mapper/StudentMapper.java # MyBatis-Plus Mapper
│   │   └── service/                  # StudentService, RollCallService + impl
│   └── src/main/resources/
│       ├── application.yml           # 主配置（DB_PASSWORD 通过环境变量注入）
│       ├── application-dev.yml       # 开发环境配置
│       └── static/                   # 前端构建产物
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── api/student.js            # Axios API 封装
│       ├── router/index.js           # 路由配置
│       └── views/                    # StudentList.vue, RollCall.vue, Statistics.vue
├── nginx-1.26.3/               # Nginx 部署
│   ├── conf/nginx.conf              # Nginx 配置（静态文件 + API 代理）
│   └── html/                        # 前端静态文件
└── sql/schema.sql              # 数据库建表脚本
```

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Node.js 20+
- Nginx (可选，开发阶段可直连 Spring Boot)

### 2. 数据库初始化

```sql
mysql -u root -p < sql/schema.sql
```

### 3. 启动后端

```bash
cd backend

# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

> 启动前设置环境变量 `DB_PASSWORD`，或使用默认值（仅开发）：激活 `dev` profile，默认密码见 `application-dev.yml`。

### 4. 启动前端开发服务器

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`，API 请求自动代理到后端 `http://localhost:8080`。

### 5. 生产部署（Nginx）

```bash
# 构建前端
cd frontend && npm run build

# 复制到 Nginx
cp -r dist/* ../nginx-1.26.3/html/

# 启动 Nginx
nginx -c /path/to/nginx-1.26.3/conf/nginx.conf
```

访问 `http://localhost`（Nginx 80 端口）。

## 部署架构

```
浏览器 → Nginx :80
           ├── / → 静态文件（Vue SPA）
           └── /api/* → Spring Boot :8080
```
