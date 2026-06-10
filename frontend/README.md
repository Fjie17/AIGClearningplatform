# Frontend · AIGC 个性化学习平台

Vue 3 前端项目目录，**待接入**。

## 计划技术栈

- Vue 3 + Vite
- Axios（对接 `backend/` REST API）
- 组件库待定

## 接入步骤

1. 将本地 Vue 3 项目复制到本目录
2. 添加 `.gitignore`（忽略 `node_modules/`、`dist/`、`.env`）
3. 在 `vite.config.js` 配置 API 代理：

```js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
    },
  },
})
```

4. 提交到仓库：`git add frontend/ && git commit -m "Add Vue 3 frontend"`

详细说明见 [项目根目录 README](../README.md#如何添加前端)。
