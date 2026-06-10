# Frontend · AIGC 个性化学习平台

Vue 3 前端应用，为 [AIGC 个性化学习平台](https://github.com/Fjie17/AIGClearningplatform) 提供用户端与管理端交互界面。

> 毕业设计：**基于 AIGC 与多资源整合的个性化学伴平台设计与实现**  
> 技术路线：Vue 3 + Vite + Element Plus + Pinia + Axios

---

## 项目简介

本平台面向中国大学生学习场景，以 **「链接而非搬运」** 为核心理念，整合 B 站、小红书、抖音等公开学习资源链接，结合学习行为分析与通义千问大模型，实现：

- **资源重组**：管理员侧完成资源采集、清洗与 AI 匹配入库
- **学习画像**：问卷测评 + LQAI 学格标签，刻画学习水平与偏好
- **AI 教师**：知识归纳、分步引导，强调思维启发而非直接给答案
- **学习路径**：基于画像生成阶段性学习计划（持续迭代中）
- **AI 陪伴**：学习提醒与进度监督（持续迭代中）
- **学习分析**：学习数据可视化看板（持续迭代中）

前端采用 **前后端分离** 架构，通过 REST API 与 `backend/` 通信，JWT 鉴权，按角色分流至管理端与用户端。

---

## 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3.5 | Composition API、组件化开发 |
| 构建 | Vite 7 | 开发热更新与生产打包 |
| UI | Element Plus 2 | 表格、表单、弹窗等管理端组件 |
| 路由 | Vue Router 4 | 角色路由守卫（USER / ADMIN） |
| 状态 | Pinia 3 | 用户 Token 与登录态 |
| 请求 | Axios | 统一拦截器、JWT 注入、错误处理 |

---

## 目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js          # 开发代理：/api → localhost:9090
├── public/
│   └── vite.svg
└── src/
    ├── main.js
    ├── App.vue
    ├── style.css
    ├── api/                # 接口封装
    │   ├── auth.js
    │   └── resource.js
    ├── components/         # 通用组件
    │   ├── TextUpper/
    │   └── Upload/
    ├── router/
    │   └── index.js        # 路由与权限守卫
    ├── store/
    │   └── user.js         # Pinia 用户状态
    ├── utils/
    │   └── request.js      # Axios 实例与拦截器
    └── views/
        ├── login/          # 登录 / 注册 / 找回密码
        ├── layout/         # AdminLayout、UserLayout
        ├── admin/          # 管理端业务页面
        └── user/           # 用户端业务页面
```

---

## 快速启动

### 1. 环境要求

| 依赖 | 版本建议 |
|------|----------|
| Node.js | 18+ |
| npm | 9+ |

### 2. 安装依赖

```bash
cd frontend
npm install
```

### 3. 启动后端

前端默认将 `/api` 代理到 `http://localhost:9090`，请先按 [backend/README.md](../backend/README.md) 启动 Spring Boot 服务。

### 4. 启动开发服务器

```bash
npm run dev
```

浏览器访问 **http://localhost:5173**，默认跳转登录页。

### 5. 生产构建

```bash
npm run build      # 输出到 dist/
npm run preview    # 本地预览构建结果
```

---

## 页面与路由

### 认证模块

| 路由 | 页面 | 功能 |
|------|------|------|
| `/login` | 登录页 | 邮箱登录、注册、忘记密码、邮箱验证码 |

登录成功后按角色跳转：

- `ADMIN` → `/admin/subject`
- `USER` → `/user/profile`

### 管理端 `/admin`

| 路由 | 页面 | 功能 |
|------|------|------|
| `/admin/subject` | 科目管理 | 学科 CRUD、导入导出、批量删除 |
| `/admin/knowledge` | 知识点管理 | 树状知识结构维护 |
| `/admin/resource` | 资源管理 | 学习资源查询与录入 |
| `/admin/ai-match` | AI 资源匹配 | 爬虫数据清洗、AI 知识点匹配入库 |
| `/admin/assessment-question` | 画像测试题 | 学科评估问卷管理 |

### 用户端 `/user`

| 路由 | 页面 | 功能 |
|------|------|------|
| `/user/profile` | 用户画像 | 测评问卷、LQAI 学格、画像展示 |
| `/user/aiteacher` | AI 教师 | 智能问答与知识引导 |
| `/user/learningpath` | 学习路径 | 个性化学习计划 |
| `/user/companion` | AI 陪伴 | 学习提醒与监督 |
| `/user/analytics` | 学习分析 | 数据看板与统计图表 |
| `/user/information` | 个人信息 | 资料修改与密码更新 |

---

## 前后端联调

`vite.config.js` 已配置开发代理：

```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:9090',
      changeOrigin: true,
    }
  }
}
```

`src/utils/request.js` 统一处理：

- 请求头自动携带 `Authorization: Bearer <token>`
- `401` 时清除登录态并跳转 `/login`
- 统一错误提示（Element Plus `ElMessage`）

主要 API 模块见 [项目根目录 README](../README.md#api-概览) 与 [backend/README.md](../backend/README.md#主要-api-模块)。

---

## 权限说明

路由守卫（`src/router/index.js`）规则：

| 访问路径 | 要求 |
|----------|------|
| `/admin/*` | 已登录且 `role === 'ADMIN'` |
| `/user/*` | 已登录且 `role` 为 `USER` 或 `ADMIN` |
| 未登录 | 重定向至 `/login` |

Token 与用户信息存储于 `localStorage`（`token`、`userInfo`）。

---

## 开发说明

- 路径别名：`@` → `src/`（见 `vite.config.js`）
- 管理端页面以 Element Plus 表格 + 弹窗为主，对接 `/api/admin/*` 接口
- 用户画像页对接 `/api/profile/*`，支持问卷提交与 LQAI 学格判定
- **请勿提交** `node_modules/`、`dist/`、`.env` 等本地文件（见 `.gitignore`）

---

## 相关文档

- [项目总览 README](../README.md)
- [后端 README](../backend/README.md)
- [Apache License 2.0](../LICENSE)

---

## 许可证

[Apache License 2.0](../LICENSE)
