# REST API

默认地址：`http://127.0.0.1:8080/api`。

除登录和注册接口外，请求头需要 `Authorization: Bearer <token>`。

| 方法 | 地址 | 权限 | 说明 |
|---|---|---|---|
| POST | `/auth/login` | 公开 | 登录并创建数据库会话 |
| POST | `/auth/register` | 公开 | 注册普通用户并创建数据库会话 |
| GET | `/auth/me` | 已登录 | 当前账号 |
| POST | `/auth/logout` | 已登录 | 删除当前会话 |
| GET | `/dashboard` | 已登录 | 首页统计 |
| GET/POST | `/cats` | 已登录 | 查询或新建猫咪档案 |
| GET/PUT | `/cats/{id}` | 已登录 | 查询或修改猫咪档案 |
| GET/POST | `/rescues` | 已登录 | 查询或发起救助 |
| PUT/DELETE | `/rescues/{id}` | 已登录 | 修改或删除救助任务 |
| PATCH | `/rescues/{id}/accept` | 已登录 | 接受救助任务 |
| GET/POST | `/volunteers` | 已登录 | 查询或新建志愿任务 |
| PUT/DELETE | `/volunteers/{id}` | 已登录 | 修改或删除志愿任务 |
| GET | `/admin/metrics` | 仅ADMIN | 后台治理指标 |

普通用户访问 `/admin/**` 时，后端返回 HTTP 403，权限不只依赖前端隐藏菜单。
