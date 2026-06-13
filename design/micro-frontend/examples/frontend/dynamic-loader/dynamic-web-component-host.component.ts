import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  OnDestroy,
  ViewChild,
  ElementRef,
  SimpleChanges,
  ChangeDetectorRef
} from '@angular/core';
import { DynamicWebComponentLoader } from './dynamic-web-component-loader.service';
import { WebComponentConfig } from './web-component-config';
import { TuplesProperty } from '../../plugin/type.utils';

/**
 * 动态Web Component宿主组件
 *
 * 封装动态加载逻辑，提供标准的Angular组件接口
 */
@Component({
  selector: 'dynamic-web-component-host',
  template: `
    <div #container class="web-component-container">
      <nz-spin *ngIf="loading" [nzTip]="'加载组件中...'"></nz-spin>

      <nz-alert *ngIf="error"
                nzType="error"
                nzMessage="组件加载失败"
                [nzDescription]="errorDescription"
                nzShowIcon>
        <button nz-button nzType="link" (click)="retry()">
          <span nz-icon nzType="reload"></span>
          重试
        </button>
      </nz-alert>
    </div>
  `,
  styles: [`
    .web-component-container {
      position: relative;
      min-height: 100px;
    }

    nz-spin {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 200px;
    }
  `]
})
export class DynamicWebComponentHostComponent implements OnInit, OnChanges, OnDestroy {

  /** Web Component配置 */
  @Input() config: WebComponentConfig;

  /** 表格数据 */
  @Input() tabletView: TuplesProperty;

  /** 错误信息 */
  @Input() error: any;

  /** 数据变更事件 */
  @Output() tabletViewChange = new EventEmitter<TuplesProperty>();

  @ViewChild('container', { static: true })
  container: ElementRef<HTMLElement>;

  loading = false;
  componentError: Error | null = null;

  private componentInstance: HTMLElement | null = null;
  private eventListeners: Array<() => void> = [];

  constructor(
    private loader: DynamicWebComponentLoader,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    await this.loadComponent();
  }

  ngOnChanges(changes: SimpleChanges) {
    // 更新Web Component的属性
    if (this.componentInstance) {
      if (changes['tabletView']) {
        (this.componentInstance as any).tabletView = this.tabletView;
      }
      if (changes['error']) {
        (this.componentInstance as any).error = this.error;
      }
    }
  }

  ngOnDestroy() {
    this.cleanup();
  }

  get errorDescription(): string {
    if (!this.componentError) return '';
    return `${this.componentError.message}\n\n请检查：\n1. 插件是否已安装\n2. 网络连接是否正常\n3. 浏览器控制台错误信息`;
  }

  async retry() {
    this.cleanup();
    await this.loadComponent();
  }

  private async loadComponent() {
    if (!this.config) {
      console.warn('[DynamicHost] No config provided');
      return;
    }

    this.loading = true;
    this.componentError = null;
    this.cdr.detectChanges();

    try {
      this.componentInstance = await this.loader.loadAndRender(
        this.config,
        this.container.nativeElement,
        {
          tabletView: this.tabletView,
          error: this.error
        }
      );

      this.bindOutputs();

    } catch (error) {
      this.componentError = error as Error;
      console.error('[DynamicHost] Failed to load component:', error);
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  private bindOutputs() {
    if (!this.componentInstance || !this.config.outputs) {
      return;
    }

    this.config.outputs.forEach(output => {
      const handler = (event: CustomEvent) => {
        console.log(`[DynamicHost] Event received: ${output.name}`, event.detail);

        if (output.name === 'tabletViewChange') {
          this.tabletViewChange.emit(event.detail);
        }
      };

      this.componentInstance!.addEventListener(output.name, handler as EventListener);

      this.eventListeners.push(() => {
        this.componentInstance!.removeEventListener(output.name, handler as EventListener);
      });
    });
  }

  private cleanup() {
    // 清理事件监听器
    this.eventListeners.forEach(unsubscribe => unsubscribe());
    this.eventListeners = [];

    // 移除组件实例
    if (this.componentInstance) {
      this.componentInstance.remove();
      this.componentInstance = null;
    }
  }
}
