# 共享代码和依赖管理方案

## 问题描述

原方案存在两个严重的维护性问题：

### 问题1：共享代码复制导致不一致
```
主工程：tis.service.ts (v1.0)
插件A：tis.service.ts (v1.0) ← 复制
插件B：tis.service.ts (v1.0) ← 复制

主工程更新：tis.service.ts (v2.0)
插件A：tis.service.ts (v1.0) ← 未同步！
插件B：tis.service.ts (v1.0) ← 未同步！
```

### 问题2：依赖版本同步困难
```
主工程：package.json → @angular/core: ^15.2.0
插件A：package.json → @angular/core: ^15.2.0
插件B：package.json → @angular/core: ^15.2.0
插件C：package.json → @angular/core: ^14.0.0 ← 忘记更新！

主工程升级到Angular 16后，C插件运行失败！
```

---

## 解决方案：三层共享策略

```
┌─────────────────────────────────────────────────────────┐
│  Layer 1: 核心框架依赖 (Externals)                      │
│  Angular, RxJS, Zone.js, ng-zorro-antd                 │
│  → 主应用提供，插件不打包                                │
└─────────────────────────────────────────────────────────┘
              ↓ 插件通过window全局对象访问
┌─────────────────────────────────────────────────────────┐
│  Layer 2: TIS共享SDK (NPM包或UMD)                       │
│  TISService, BasicComponent, 类型定义                   │
│  → 统一版本，统一发布                                     │
└─────────────────────────────────────────────────────────┘
              ↓ 插件通过npm依赖或window访问
┌─────────────────────────────────────────────────────────┐
│  Layer 3: 插件自有代码                                   │
│  组件业务逻辑、样式、资源                                 │
│  → 插件独立开发                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Layer 1: 核心框架依赖 Externals

### 主应用提供全局暴露

**tis-console/src/main.ts:**
```typescript
import * as ngCore from '@angular/core';
import * as ngCommon from '@angular/common';
import * as ngPlatformBrowser from '@angular/platform-browser';
import * as ngForms from '@angular/forms';
import * as rxjs from 'rxjs';
import * as rxjsOperators from 'rxjs/operators';
import * as ngZorro from 'ng-zorro-antd';

// 暴露到window对象供插件使用
(window as any).TIS_SHARED_LIBS = {
  '@angular/core': ngCore,
  '@angular/common': ngCommon,
  '@angular/platform-browser': ngPlatformBrowser,
  '@angular/forms': ngForms,
  'rxjs': rxjs,
  'rxjs/operators': rxjsOperators,
  'ng-zorro-antd': ngZorro,
  
  // 版本信息
  versions: {
    angular: '15.2.0',
    rxjs: '7.8.0',
    ngZorro: '15.1.0'
  }
};

console.log('[TIS] Shared libraries exposed:', Object.keys((window as any).TIS_SHARED_LIBS));
```

### 插件Webpack配置使用Externals

**plugin/webapp/webpack.config.js:**
```javascript
module.exports = {
  // ...其他配置
  
  externals: {
    '@angular/core': 'TIS_SHARED_LIBS["@angular/core"]',
    '@angular/common': 'TIS_SHARED_LIBS["@angular/common"]',
    '@angular/platform-browser': 'TIS_SHARED_LIBS["@angular/platform-browser"]',
    '@angular/forms': 'TIS_SHARED_LIBS["@angular/forms"]',
    'rxjs': 'TIS_SHARED_LIBS.rxjs',
    'rxjs/operators': 'TIS_SHARED_LIBS["rxjs/operators"]',
    'ng-zorro-antd': 'TIS_SHARED_LIBS["ng-zorro-antd"]',
    'ng-zorro-antd/modal': 'TIS_SHARED_LIBS["ng-zorro-antd"].modal',
    'ng-zorro-antd/table': 'TIS_SHARED_LIBS["ng-zorro-antd"].table'
    // ... 其他ng-zorro子模块
  }
};
```

### 插件package.json使用peerDependencies

**plugin/webapp/package.json:**
```json
{
  "name": "@tis/plugin-component",
  "version": "1.0.0",
  
  "peerDependencies": {
    "@angular/core": "^15.0.0",
    "@angular/common": "^15.0.0",
    "@angular/forms": "^15.0.0",
    "rxjs": "^7.0.0",
    "ng-zorro-antd": "^15.0.0"
  },
  
  "devDependencies": {
    "@angular/core": "^15.2.0",
    "@angular/common": "^15.2.0",
    "@angular/forms": "^15.2.0",
    "rxjs": "^7.8.0",
    "ng-zorro-antd": "^15.1.0",
    
    "typescript": "~4.9.5",
    "webpack": "^5.75.0"
  },
  
  "dependencies": {
    // 插件特有依赖（非共享的）
    "lodash": "^4.17.21"
  }
}
```

**优势：**
- ✅ 主应用升级Angular，插件自动使用新版本
- ✅ 不需要同步package.json版本号
- ✅ 插件bundle大小显著减小（从500KB → 50KB）
- ✅ 只有一份Angular代码在内存中

---

## Layer 2: TIS共享SDK

### 方案2A: NPM私有包（推荐）

#### 创建@tis/sdk包

**tis-console/sdk/package.json:**
```json
{
  "name": "@tis/sdk",
  "version": "1.0.0",
  "main": "dist/index.js",
  "types": "dist/index.d.ts",
  
  "peerDependencies": {
    "@angular/core": "^15.0.0",
    "@angular/common": "^15.0.0",
    "rxjs": "^7.0.0"
  }
}
```

**tis-console/sdk/src/index.ts:**
```typescript
// 导出共享服务
export { TISService } from './services/tis.service';
export { TISCoreService } from './services/tis-core.service';

// 导出共享组件基类
export { BasicFormComponent } from './components/basic-form.component';
export { BasicTuplesViewComponent } from './components/basic-tuples-view.component';

// 导出类型定义
export * from './types/plugin.types';
export * from './types/tuples.types';

// 导出工具函数
export * from './utils/plugin.utils';
```

**构建脚本：**
```bash
cd tis-console/sdk
npm run build  # 编译TypeScript
npm publish --registry=http://your-private-npm-registry
```

#### 插件使用SDK

**plugin/webapp/package.json:**
```json
{
  "dependencies": {
    "@tis/sdk": "^1.0.0"
  }
}
```

**plugin/webapp/src/component.ts:**
```typescript
import { TISService } from '@tis/sdk';
import { BasicTuplesViewComponent } from '@tis/sdk';

// 直接使用，无需复制代码
export class MyComponent extends BasicTuplesViewComponent {
  constructor(private tisService: TISService) {
    super();
  }
}
```

**优势：**
- ✅ 单一数据源（Single Source of Truth）
- ✅ 版本统一管理（通过npm版本）
- ✅ 自动依赖管理
- ✅ 可以使用semver版本控制

### 方案2B: 主应用UMD暴露（轻量方案）

如果不想搭建私有npm仓库：

**tis-console/src/sdk-exports.ts:**
```typescript
import { TISService } from './common/tis.service';
import { BasicTuplesViewComponent } from './common/multi-selected/basic.tuples.view.component';
import * as PluginTypes from './common/tis.plugin';

// 暴露TIS SDK到全局
(window as any).TIS_SDK = {
  version: '1.0.0',
  
  services: {
    TISService: TISService,
  },
  
  components: {
    BasicTuplesViewComponent: BasicTuplesViewComponent,
  },
  
  types: PluginTypes,
  
  utils: {
    // ... 工具函数
  }
};
```

**插件Webpack配置:**
```javascript
externals: {
  '@tis/sdk': 'TIS_SDK'
}
```

**插件代码:**
```typescript
import { TISService } from '@tis/sdk';
// Webpack会将这行转换为：
// const TISService = window.TIS_SDK.services.TISService;
```

**优势：**
- ✅ 无需私有npm仓库
- ✅ 零网络依赖
- ⚠️ 需要维护类型定义文件

---

## 版本兼容性管理

### 版本检查机制

**主应用启动时：**
```typescript
// tis-console/src/main.ts
(window as any).TIS_RUNTIME = {
  version: '4.3.0',
  sdkVersion: '1.0.0',
  angular: '15.2.0',
  
  // 兼容性检查
  checkCompatibility(pluginRequirements: {
    minSdkVersion?: string;
    minAngularVersion?: string;
  }): boolean {
    // 使用semver检查版本兼容性
    return semver.satisfies(this.sdkVersion, pluginRequirements.minSdkVersion);
  }
};
```

**插件加载时检查：**
```typescript
// plugin/webapp/src/index.ts
const requirements = {
  minSdkVersion: '1.0.0',
  minAngularVersion: '15.0.0'
};

if (!(window as any).TIS_RUNTIME.checkCompatibility(requirements)) {
  console.error('[Plugin] Incompatible runtime version');
  throw new Error('Plugin requires TIS SDK >= 1.0.0');
}
```

### 插件元数据声明

**plugin配置JSON:**
```json
{
  "webComponent": {
    "tagName": "my-component",
    "scriptUrl": "/plugin-assets/my-plugin/bundle.js",
    "version": "1.0.0",
    
    "requirements": {
      "tis": ">=4.3.0",
      "sdk": "^1.0.0",
      "angular": "^15.0.0"
    }
  }
}
```

**主应用加载前检查：**
```typescript
async loadComponent(config: WebComponentConfig) {
  // 检查兼容性
  if (!this.isCompatible(config.requirements)) {
    throw new Error(`Plugin incompatible. Requires: ${JSON.stringify(config.requirements)}`);
  }
  
  // 继续加载
  await this.loadScript(config);
}
```

---

## 升级流程

### 场景：主应用升级Angular 15 → 16

**步骤1：升级主应用**
```bash
cd tis-console
ng update @angular/core@16 @angular/cli@16
npm install
npm run build
```

**步骤2：升级SDK**
```bash
cd tis-console/sdk
# 更新peerDependencies
npm version minor  # 1.0.0 → 1.1.0
npm publish
```

**步骤3：更新插件（可选）**

插件**无需立即更新**，因为：
- 插件使用externals，自动使用主应用的Angular 16
- SDK通过peerDependencies，兼容Angular 15和16

如果需要使用Angular 16新特性：
```bash
cd plugin/webapp
npm install @tis/sdk@^1.1.0 --save-dev
npm run build
```

**优势：**
- ✅ 主应用升级后，旧插件仍可运行（向后兼容）
- ✅ 插件可按需升级，不强制同步
- ✅ 通过版本号管理兼容性

---

## 最佳实践

### 1. 使用Monorepo管理SDK

**项目结构：**
```
tis-project/
├── tis-console/           # 主应用
├── tis-sdk/               # 共享SDK
│   ├── package.json
│   ├── src/
│   └── dist/
└── plugins/
    ├── tis-ontology-plugin/
    └── tis-jdbc-plugin/
```

**lerna.json:**
```json
{
  "packages": ["tis-console", "tis-sdk", "plugins/*"],
  "version": "independent",
  "npmClient": "npm"
}
```

**统一更新依赖：**
```bash
lerna exec -- npm install @angular/core@16
```

### 2. SDK版本语义化

```
主版本（Major）：破坏性变更
  1.0.0 → 2.0.0：API接口变化，插件需要修改代码

次版本（Minor）：新增功能
  1.0.0 → 1.1.0：新增服务/工具，向后兼容

修订版本（Patch）：Bug修复
  1.0.0 → 1.0.1：修复bug，完全兼容
```

### 3. 类型定义共享

**发布@tis/types包：**
```json
{
  "name": "@tis/types",
  "version": "1.0.0",
  "types": "index.d.ts"
}
```

**插件使用：**
```typescript
import { TuplesProperty, PluginType } from '@tis/types';
```

### 4. 开发时版本对齐检查

**插件package.json添加scripts：**
```json
{
  "scripts": {
    "check-versions": "node scripts/check-peer-deps.js",
    "prebuild": "npm run check-versions"
  }
}
```

**check-peer-deps.js:**
```javascript
const packageJson = require('../package.json');
const mainPackageJson = require('../../tis-console/package.json');

const checkVersion = (dep) => {
  const pluginVer = packageJson.peerDependencies[dep];
  const mainVer = mainPackageJson.dependencies[dep];
  
  if (!semver.satisfies(mainVer, pluginVer)) {
    console.error(`❌ ${dep} version mismatch!`);
    console.error(`  Plugin requires: ${pluginVer}`);
    console.error(`  Main app uses: ${mainVer}`);
    process.exit(1);
  }
};

['@angular/core', '@angular/common', 'rxjs'].forEach(checkVersion);
console.log('✅ All peer dependencies compatible');
```

---

## 总结

### 核心原则

1. **Don't Repeat Yourself (DRY)**
   - 共享代码通过npm包或UMD暴露
   - 永远不要复制粘贴代码

2. **Single Source of Truth**
   - 主应用是依赖版本的唯一权威
   - SDK是共享逻辑的唯一权威

3. **Explicit Dependencies**
   - 通过package.json明确声明依赖
   - 通过externals避免重复打包

### 对比原方案

| 方面 | 原方案 | 新方案 |
|------|--------|--------|
| 共享代码 | 复制到每个插件 | 通过@tis/sdk或UMD共享 |
| 依赖管理 | 每个插件独立维护 | externals + peerDependencies |
| 版本同步 | 手动同步，易出错 | 自动使用主应用版本 |
| Bundle大小 | 500KB+ | 50KB（减少90%） |
| 升级成本 | 所有插件必须同步 | 插件可按需升级 |
| 维护性 | ❌ 差 | ✅ 优秀 |

### 实施优先级

**Phase 1（必须）：**
- ✅ Externals配置（核心框架不重复打包）
- ✅ peerDependencies（版本声明）

**Phase 2（推荐）：**
- ✅ 创建@tis/sdk包
- ✅ 版本检查机制

**Phase 3（可选）：**
- Monorepo管理
- 自动化版本对齐检查

---

这样就完全解决了代码重复和版本同步的问题！🎉
