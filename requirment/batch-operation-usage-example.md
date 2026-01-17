# TIS 列表批量操作功能使用说明

## 功能概述

已经在 `PaginationComponent` 组件中成功添加了批量选择和删除功能，该功能模仿了阿里云 OSS 资源列表的交互体验。

## 主要功能特性

1. **条件显示**: 只有当 `enable-rows-manage` 属性设置为 `true` 时，批量操作功能才会显示
2. **批量选择**: 支持全选/取消全选，以及单个项目的选择
3. **底部操作栏**: 选中项目后，底部会显示操作栏，包含选中数量和删除按钮
4. **删除确认**: 点击删除按钮会弹出确认对话框，需要用户确认后才执行删除操作
5. **状态管理**: 切换页面时会自动清空选择状态

## 使用方法

### 1. 在组件模板中启用批量操作

```html
<tis-page
    [rows]="dataList"
    [pager]="pager"
    [enable-rows-manage]="true"
    (batch-delete)="onBatchDelete($event)"
    (go-page)="goPage($event)">

    <!-- 定义列 -->
    <tis-col field="id" title="ID"></tis-col>
    <tis-col field="name" title="名称"></tis-col>
    <tis-col field="status" title="状态"></tis-col>
    <!-- 更多列定义... -->
</tis-page>
```

### 2. 在组件类中处理批量删除事件

```typescript
export class YourListComponent {
    dataList: any[] = [];
    pager: Pager;

    // 处理批量删除事件
    onBatchDelete(selectedItems: any[]): void {
        console.log('需要删除的项目:', selectedItems);

        // 调用后端API执行删除操作
        this.service.batchDelete(selectedItems).subscribe(
            result => {
                // 删除成功后刷新列表
                this.loadData();
                // 显示成功消息
                this.notification.success('删除成功', `成功删除了 ${selectedItems.length} 项`);
            },
            error => {
                // 处理错误
                this.notification.error('删除失败', error.message);
            }
        );
    }

    // 处理分页
    goPage(page: number): void {
        this.loadData(page);
    }
}
```

## 新增的属性和事件

### 输入属性
- `@Input("enable-rows-manage") enableRowsManage: boolean`: 是否启用批量操作功能，默认为 `false`

### 输出事件
- `@Output('batch-delete') batchDeleteEmitter`: 批量删除事件，返回选中的数据项数组

## 实现细节

### 核心功能模块

1. **选择状态管理**
   - `selectedRows: Set<any>`: 存储选中的数据项
   - `isAllChecked: boolean`: 全选状态
   - `isIndeterminate: boolean`: 部分选中状态

2. **选择方法**
   - `onAllChecked(checked: boolean)`: 处理全选/取消全选
   - `onItemChecked(row: any)`: 处理单个项目选择
   - `updateCheckAllState()`: 更新全选复选框状态
   - `clearAllSelection()`: 清空所有选择

3. **批量操作**
   - `onBatchDelete()`: 显示确认对话框并触发删除事件

### UI 组件

1. **表头复选框**: 用于全选/取消全选
2. **行复选框**: 用于选择单个数据项
3. **底部操作栏**: 显示选中数量和操作按钮，具有粘性定位效果

## 样式特性

- 底部操作栏采用粘性定位 (`position: sticky`)
- 蓝色顶部边框标识
- 阴影效果增强视觉层次
- 响应式布局，自适应不同屏幕尺寸

## 注意事项

1. 数据项会自动添加 `_checked` 属性来跟踪选择状态
2. 切换分页时会自动清空选择状态
3. 使用了 Angular 的 OnPush 变更检测策略，通过 `ChangeDetectorRef` 手动触发更新
4. 依赖 `ng-zorro-antd` 的 Modal 服务来显示确认对话框

## 测试建议

1. 测试全选功能是否正常工作
2. 测试单个选择和取消选择
3. 测试分页切换时选择状态是否正确清空
4. 测试删除确认对话框的取消和确认操作
5. 测试在没有数据时的边界情况
6. 测试大量数据时的性能表现