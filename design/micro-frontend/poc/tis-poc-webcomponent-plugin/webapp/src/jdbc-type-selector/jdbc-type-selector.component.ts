import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  ChangeDetectorRef,
  ViewEncapsulation
} from '@angular/core';
import { TuplesProperty, JdbcTypeItem } from '../shared/types';

/**
 * JDBC类型选择器组件（POC）
 *
 * 这是一个简化的POC实现，用于验证Web Component架构
 */
@Component({
  selector: 'app-jdbc-type-internal',
  template: `
    <div class="jdbc-type-selector-poc">
      <div class="header">
        <h4>JDBC类型选择器</h4>
        <span class="badge">Web Component POC</span>
      </div>

      <div class="content">
        <div class="item" *ngFor="let item of items; let i = index">
          <label>
            <input
              type="checkbox"
              [(ngModel)]="item.selected"
              (change)="onItemChange()"
            />
            <span class="item-name">{{ item.name }}</span>
            <span class="item-code">({{ item.code }})</span>
          </label>
          <div class="item-desc" *ngIf="item.description">
            {{ item.description }}
          </div>
        </div>
      </div>

      <div class="footer">
        <span class="selection-info">
          已选择: {{ getSelectedCount() }} / {{ items.length }}
        </span>
      </div>

      <div class="debug" *ngIf="showDebug">
        <details>
          <summary>调试信息</summary>
          <pre>{{ debugInfo() }}</pre>
        </details>
      </div>
    </div>
  `,
  styles: [`
    .jdbc-type-selector-poc {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      padding: 16px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      background: #fff;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;
    }

    .header h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #262626;
    }

    .badge {
      padding: 2px 8px;
      background: #1890ff;
      color: white;
      font-size: 12px;
      border-radius: 2px;
    }

    .content {
      min-height: 100px;
    }

    .item {
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .item:last-child {
      border-bottom: none;
    }

    .item label {
      display: flex;
      align-items: center;
      cursor: pointer;
      user-select: none;
    }

    .item input[type="checkbox"] {
      margin-right: 8px;
      cursor: pointer;
    }

    .item-name {
      font-weight: 500;
      color: #262626;
    }

    .item-code {
      margin-left: 8px;
      color: #8c8c8c;
      font-size: 12px;
    }

    .item-desc {
      margin-left: 24px;
      margin-top: 4px;
      font-size: 12px;
      color: #595959;
    }

    .footer {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
    }

    .selection-info {
      color: #8c8c8c;
      font-size: 13px;
    }

    .debug {
      margin-top: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;
      font-size: 12px;
    }

    .debug pre {
      margin: 8px 0 0 0;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  `],
  encapsulation: ViewEncapsulation.ShadowDom
})
export class JdbcTypeSelectorComponent implements OnInit {

  private _tabletView: TuplesProperty | null = null;
  items: JdbcTypeItem[] = [];
  showDebug = true;

  @Input()
  set tabletView(value: TuplesProperty) {
    console.log('[JdbcTypeSelector] tabletView input:', value);
    this._tabletView = value;
    this.parseTabletView(value);
    this.cdr.detectChanges();
  }

  get tabletView(): TuplesProperty | null {
    return this._tabletView;
  }

  @Input() error: any;

  @Output() tabletViewChange = new EventEmitter<TuplesProperty>();

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    console.log('[JdbcTypeSelector] Component initialized');

    // 如果没有数据，提供mock数据
    if (!this.items || this.items.length === 0) {
      this.items = this.getMockData();
    }
  }

  onItemChange() {
    console.log('[JdbcTypeSelector] Selection changed');
    this.emitChange();
  }

  getSelectedCount(): number {
    return this.items.filter(item => item.selected).length;
  }

  debugInfo(): string {
    return JSON.stringify({
      itemCount: this.items.length,
      selectedCount: this.getSelectedCount(),
      hasError: !!this.error,
      hasTabletView: !!this._tabletView
    }, null, 2);
  }

  private parseTabletView(view: TuplesProperty) {
    if (!view) {
      console.warn('[JdbcTypeSelector] No tabletView provided');
      return;
    }

    // 根据实际TuplesProperty结构解析
    // 这里是简化实现
    const data = view as any;
    if (data.items && Array.isArray(data.items)) {
      this.items = data.items;
    }
  }

  private emitChange() {
    const updatedView: any = {
      ...this._tabletView,
      items: this.items
    };

    console.log('[JdbcTypeSelector] Emitting change:', updatedView);
    this.tabletViewChange.emit(updatedView);
  }

  private getMockData(): JdbcTypeItem[] {
    return [
      {
        name: 'VARCHAR',
        code: 'VARCHAR',
        selected: true,
        description: '可变长度字符串'
      },
      {
        name: 'INTEGER',
        code: 'INTEGER',
        selected: true,
        description: '整数类型'
      },
      {
        name: 'BIGINT',
        code: 'BIGINT',
        selected: false,
        description: '长整数类型'
      },
      {
        name: 'DECIMAL',
        code: 'DECIMAL',
        selected: false,
        description: '精确数值类型'
      },
      {
        name: 'TIMESTAMP',
        code: 'TIMESTAMP',
        selected: true,
        description: '时间戳类型'
      }
    ];
  }
}
