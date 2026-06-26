# 移动端后台入口提示词模板

下面提示词用于让 AI 生成一套精简、可落地、可快速迭代的移动端轻量入口框架。适用于后台管理系统的移动端入口、PDA / 条码枪入口、Android WebView、移动 H5 等场景。

```text
你是一名资深移动前端架构师、uni-app 工程专家和企业级移动端后台入口产品设计专家。请基于当前主流移动端工程实践，为我设计并实现一套“移动端后台轻量入口框架”。

一、总体目标

设计一套精简、实用、可复用、可快速迭代的移动端入口框架。

系统只承担以下职责：
1. 登录。
2. 退出登录。
3. 获取当前用户信息、角色、菜单树和权限码。
4. 首页九宫格功能入口。
5. 根据权限码控制九宫格入口显示。
6. token 会话处理。
7. 统一请求、统一错误处理、统一 loading 和空状态。

必须避免：
1. 不做完整后台管理系统。
2. 不设计用户管理、角色管理、菜单管理、日志管理、系统配置等 PC 后台页面。
3. 不设计统计首页、欢迎页、大图展示、营销式页面、无业务价值卡片。
4. 不设计复杂工作台、报表、图表、审批流、消息中心。
5. 不保留过时兼容参数、备用请求格式、隐式 fallback 请求路径。
6. 不使用 mock 数据作为真实接口 fallback。

最终目标：
移动端第一屏就是九宫格功能入口。九宫格只作为后续业务功能的入口容器，不承载额外展示内容。

二、技术栈约束

固定技术栈：
- uni-app
- Vue 3
- TypeScript
- Vite
- Vant 4
- Pinia
- Axios
- Sass / SCSS

要求：
1. 使用 Vue 3 Composition API。
2. 使用 TypeScript 定义接口请求、响应、用户信息、权限码、九宫格入口等类型。
3. UI 组件基于 Vant 4，不混用多套移动端 UI 框架。
4. 请求层统一使用 Axios 封装，不提供 fetch 兼容分支。
5. 样式使用 SCSS，页面样式保持轻量、清晰、适合移动端触控。
6. H5 / Android WebView / PDA 浏览器优先。
7. 如果后续需要小程序或原生 App 发布，再单独处理平台差异，不在第一版中引入多端复杂适配。

三、产品范围

第一版必须形成完整闭环：
登录 -> 保存 token 和会话字段 -> 调用 GET /api/auth/me 获取用户、角色、菜单树和权限码 -> 渲染首页九宫格 -> 点击功能入口进入业务页面 -> 退出登录。

必须包含：

1. 登录页
   - 用户名。
   - 密码。
   - 登录按钮。
   - loading 状态。
   - 登录失败提示。
   - 不展示设备类型选择。
   - 不展示宣传图、欢迎大图、无用说明。

2. 首页九宫格
   - 展示可用功能入口。
   - 每个入口包含图标、名称、路由、权限码、启用状态、排序。
   - 根据权限码控制入口显示。
   - 没有权限的入口不显示。
   - 没有可用入口时显示简短空状态。
   - 不展示统计卡片、banner、报表、图表、欢迎语。

3. 退出登录
   - 调用后端退出登录接口。
   - 清理 token、用户信息、权限码、九宫格入口状态。
   - 返回登录页。

4. 会话处理
   - 使用后端 Sa-Token 单 token 会话。
   - 默认不实现 access token + refresh token 双 token。
   - 不实现 refresh token 和请求队列重放。
   - 有效请求由后端完成活跃续期。
   - 前端保存 token、expireAt、idleTimeout、maxSessionDuration、deviceType。
   - 收到 401 后清理登录态并跳转登录页。

5. 设备类型
   - 登录时自动携带 deviceType。
   - deviceType 由当前运行端自动设置或由具体客户端固定写入。
   - deviceType 不允许用户手动选择。
   - deviceType 不限制为固定枚举，客户端传什么值，后端在线用户就展示什么值。
   - deviceType 仅用于会话识别、在线用户展示和日志追踪，不作为权限判断依据。

四、接口契约

必须复用后台后端接口，不单独设计移动端认证接口。

1. 登录

接口：
- POST /api/auth/login

请求参数：
- username
- password
- deviceType

响应字段：
- token
- expireAt
- idleTimeout
- maxSessionDuration
- deviceType

规则：
- deviceType 由客户端自动设置，不展示给用户选择。
- 登录失败显示后端 message。
- 登录成功后立即调用 GET /api/auth/me 初始化用户上下文。

2. 当前用户上下文

接口：
- GET /api/auth/me

响应字段：
- 用户 ID。
- 用户名。
- 姓名。
- 头像可选。
- 部门信息。
- 角色列表。
- 权限码列表。
- 菜单树。

规则：
- 该接口是移动端初始化权限上下文的唯一接口。
- 不再单独请求菜单树或权限码接口。
- 前端根据权限码过滤九宫格入口。

3. 退出登录

接口：
- POST /api/auth/logout

规则：
- 成功后清理本地登录态。
- 失败时也要允许用户清理本地登录态并返回登录页。

4. 错误响应

统一错误结构：
{
  "code": 400001,
  "message": "用户名或密码错误",
  "data": null
}

规则：
- 401：清理登录态并跳转登录页。
- 403：显示无权限提示。
- 业务错误：显示后端 message。
- 系统错误：显示通用错误提示。

五、九宫格入口设计

九宫格入口可以先由前端本地配置，后续业务模块明确后再改成接口配置。

入口结构：
```ts
export interface MobileFeatureEntry {
  title: string
  icon: string
  path: string
  permissionCode: string
  enabled: boolean
  sort: number
}
```

规则：
1. enabled 为 false 时不显示。
2. permissionCode 为空时表示登录用户可见。
3. permissionCode 不为空时，必须存在于 GET /api/auth/me 返回的权限码列表中才显示。
4. 按 sort 升序显示。
5. 点击入口后跳转 path。
6. path 对应业务页面可以暂时为空壳页面，但必须提示“功能建设中”或由后续业务实现替换。
7. 九宫格每行 3 个入口。
8. 图标使用 Vant Icon 或本地轻量图标配置。
9. 不使用大图标背景、复杂动画、营销式视觉。

六、页面设计

1. 登录页

路径：
- /pages/login/index

页面元素：
- 系统名称。
- 用户名输入框。
- 密码输入框。
- 登录按钮。
- 错误提示。

交互：
- 输入用户名和密码后点击登录。
- 登录按钮显示 loading。
- 登录成功后进入首页。
- 登录失败保留用户名，清空密码可选。
- 不出现 deviceType 选择框。

2. 首页

路径：
- /pages/home/index

页面元素：
- 顶部简洁栏。
- 当前用户名称。
- 退出登录按钮。
- 九宫格入口。

交互：
- 点击九宫格入口跳转对应页面。
- 无权限入口不显示。
- 没有可用入口时显示空状态。
- 退出登录需要二次确认。

3. 功能占位页

路径：
- /pages/feature-placeholder/index

用途：
- 用于暂未实现的九宫格入口。
- 页面只显示简短提示，不做额外内容。

七、状态管理

使用 Pinia：

1. authStore
   - token。
   - expireAt。
   - idleTimeout。
   - maxSessionDuration。
   - deviceType。
   - login。
   - logout。
   - clearSession。

2. userStore
   - 用户信息。
   - 角色列表。
   - 权限码列表。
   - 菜单树。
   - loadCurrentUser。

3. featureStore
   - 本地九宫格入口配置。
   - 根据权限码过滤入口。
   - 按 sort 排序。

要求：
- token 和会话字段需要持久化。
- 用户退出后必须清理所有敏感状态。
- 页面临时 loading、表单输入不放入全局 store。

八、请求封装

必须封装统一 request：

1. baseURL。
2. timeout。
3. token 注入。
4. 统一响应拆包。
5. 401 处理。
6. 403 处理。
7. 业务错误提示。
8. 网络错误提示。

要求：
- API 模块调用统一 request。
- 页面不直接调用 axios。
- 不做 refresh token。
- 不做 mock fallback。
- 不做多种响应结构兼容。

九、项目目录结构

请设计清晰目录结构，例如：

src/
  api/
    auth.ts
  components/
    FeatureGrid/
    EmptyState/
  composables/
    useDeviceType.ts
    useAuthGuard.ts
  pages/
    login/
      index.vue
    home/
      index.vue
    feature-placeholder/
      index.vue
  stores/
    auth.ts
    user.ts
    feature.ts
  styles/
    index.scss
    variables.scss
  types/
    api.ts
    auth.ts
    feature.ts
  utils/
    request.ts
    storage.ts
    permissions.ts
  features/
    entries.ts

要求：
- 认证、请求、状态、九宫格配置边界清晰。
- 九宫格入口配置集中维护。
- 不把权限判断散落到每个页面。

十、验收标准

P0 验收标准：
1. 正确账号密码可以登录。
2. 登录请求自动携带 deviceType。
3. 登录成功后保存 token、expireAt、idleTimeout、maxSessionDuration、deviceType。
4. 登录成功后调用 GET /api/auth/me。
5. 首页按权限码展示九宫格入口。
6. 无权限入口不显示。
7. 点击九宫格入口可以跳转。
8. 退出登录后清理本地登录态并回到登录页。
9. token 失效后跳转登录页。
10. 页面没有统计卡片、欢迎大图、无用说明。

十一、最终输出要求

请完整输出以下内容：

1. 移动端系统设计总览。
2. 技术栈。
3. 页面清单。
4. 接口契约。
5. 九宫格入口设计。
6. 状态管理设计。
7. 请求封装设计。
8. 项目目录结构。
9. 关键代码示例：
   - request 封装。
   - authStore。
   - userStore。
   - featureStore。
   - 登录页。
   - 首页九宫格。
   - deviceType 自动设置。
10. 验收标准。

十二、额外约束

必须避免以下问题：
1. 不要设计 PC 后台管理页面。
2. 不要设计用户、角色、菜单、日志、系统配置管理页面。
3. 不要设计 dashboard、统计卡片、banner、大图展示。
4. 不要让用户手动选择 deviceType。
5. 不要实现 refresh token。
6. 不要使用 mock 数据作为真实接口 fallback。
7. 不要把九宫格做成复杂工作台。
8. 不要把权限判断散落在页面组件里。

最终目标：
生成一套可以直接指导移动端轻量入口开发落地的提示词方案，并能进一步指导生成可运行代码。要求功能极简、接口契约清晰、会话处理一致、九宫格入口可扩展、适合后续业务功能逐步接入。
```

