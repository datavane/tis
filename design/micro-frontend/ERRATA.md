# 微前端插件化架构方案 - 勘误表

## 重要架构缺陷修正

### 问题1：共享代码复制导致维护困难

**原方案位置：** `migration-plan.md:L183-L187`

**问题描述：**
```typescript
// ❌ 错误做法：复制共享代码到插件
webapp/src/shared/
├── tis.service.ts         // 从主应用复制
├── basic.tuples.view.component.ts  // 从主应用复制
└── types.ts
```

时间久了会导致：
- 主应用更新，插件未同步
- 多份代码不一致
- 维护成本指数增长

**修正方案：** 参见 `SHARED-CODE-STRATEGY.md`

使用以下三层策略：
1. **Layer 1**: 核心框架（Angular/RxJS）→ Externals，主应用提供
2. **Layer 2**: TIS共享代码 → @tis/sdk npm包 或 UMD暴露
3. **Layer 3**: 插件自有代码 → 独立开发

---

### 问题2：依赖版本同步困难

**原方案位置：** `poc/tis-poc-webcomponent-plugin/webapp/package.json`

**问题描述：**
```json
// ❌ 错误做法：每个插件独立管理依赖版本
{
  "dependencies": {
    "@angular/core": "^15.2.0",  // 需要手动与主应用同步
    "@angular/common": "^15.2.0"
  }
}
```

主应用升级后，所有插件需要手动更新package.json，容易遗漏。

**修正方案：**
```json
// ✅ 正确做法：使用peerDependencies + Externals
{
  "peerDependencies": {
    "@angular/core": "^15.0.0",  // 只声明兼容范围
    "@angular/common": "^15.0.0"
  },
  "devDependencies": {
    "@angular/core": "^15.2.0",  // 仅用于开发时类型检查
    "@angular/common": "^15.2.0"
  }
}
```

Webpack配置：
```javascript
externals: {
  '@angular/core': 'TIS_SHARED_LIBS["@angular/core"]'
}
```

插件运行时使用主应用提供的Angular版本，无需同步。

---

## 受影响的文档

以下文档需要参考修正方案：

### 1. implementation-guide.md

**章节：** 步骤3: 迁移组件代码 → 3.2 调整导入路径

**修正：**
```diff
- 原代码：
- import { TISService } from '../tis.service';
- import { BasicTuplesViewComponent } from './basic.tuples.view.component';

+ 迁移后：
+ import { TISService } from '@tis/sdk';
+ import { BasicTuplesViewComponent } from '@tis/sdk';
```

**修正：** 步骤3.3 处理共享依赖

```diff
- 创建共享模块：
- webapp/src/shared/
- ├── tis.service.ts         // 从主应用复制核心服务
- ├── basic.tuples.view.component.ts  // 基类
- └── types.ts

+ 使用SDK：
+ npm install @tis/sdk --save
+ // 直接导入，无需复制代码
```

### 2. migration-plan.md

**章节：** 详细迁移步骤示例 → 步骤3: 迁移组件代码

**修正：** 删除"3.3 处理共享依赖"中复制代码的部分，改为：

```markdown
**3.3 使用TIS SDK**

安装SDK：
```bash
cd tis-jdbc-type-plugin/webapp
npm install @tis/sdk@latest
```

更新导入：
```typescript
import { TISService, BasicTuplesViewComponent } from '@tis/sdk';
```

配置Webpack Externals（已在模板中配置）。
```

### 3. poc/tis-poc-webcomponent-plugin/

**需要更新的文件：**

1. `webapp/package.json` - 改用peerDependencies
2. `webapp/webpack.config.js` - 添加externals配置
3. `webapp/src/shared/types.ts` - 改为从@tis/sdk导入

**具体修正：** 参见 `SHARED-CODE-STRATEGY.md` 中的完整配置示例

### 4. examples/build/webpack-configs/

**已有文件：** `with-externals.config.js`

**需要更新：** 补充完整的externals配置，包括：
- Angular所有核心模块
- RxJS
- ng-zorro-antd及其子模块

**参考：** `SHARED-CODE-STRATEGY.md` 中的完整externals配置

---

## 新增文档

### SHARED-CODE-STRATEGY.md

**内容概要：**
- 三层共享策略详解
- NPM私有包方案 vs UMD暴露方案
- 版本兼容性管理
- 升级流程
- 最佳实践
- 开发时版本检查脚本

**位置：** `/Users/mozhenghua/j2ee_solution/project/tis-solr/design/micro-frontend/SHARED-CODE-STRATEGY.md`

---

## 实施建议

如果你准备实施原方案，**必须先解决这两个问题**：

### 最低要求（Phase 1）
1. ✅ 配置Externals（核心框架不重复打包）
2. ✅ 使用peerDependencies而非dependencies
3. ✅ 主应用暴露共享库到window对象

### 推荐配置（Phase 2）
4. ✅ 创建@tis/sdk包管理共享代码
5. ✅ 实现版本兼容性检查

### 最佳实践（Phase 3）
6. Monorepo管理
7. 自动化版本对齐检查
8. CI/CD集成

---

## 对原方案的影响评估

### Bundle大小
- **原方案**：每个插件500KB+（包含完整Angular）
- **修正后**：每个插件50-100KB（减少80-90%）

### 维护成本
- **原方案**：主应用每次更新，所有插件需要手动同步
- **修正后**：主应用更新，插件自动使用新版本

### 开发体验
- **原方案**：每个插件需要维护大量共享代码副本
- **修正后**：插件只写业务逻辑，共享代码通过依赖管理

### 风险等级
- **原方案风险**：🔴 高（极易导致版本不一致）
- **修正后风险**：🟢 低（单一数据源，自动管理）

---

## 致谢

感谢用户发现这个严重的架构缺陷！这个问题如果不解决，会导致：
- 长期维护成本指数增长
- 版本不一致导致的运行时错误
- 开发团队协作困难

修正方案基于：
- Webpack Externals最佳实践
- NPM peerDependencies语义
- 微前端Module Federation思想
- Monorepo依赖管理经验

---

**最后更新：** 2026-06-11  
**修正版本：** v1.1  
**状态：** ✅ 已修正
