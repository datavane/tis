import { createCustomElement } from '@angular/elements';
import { createApplication } from '@angular/platform-browser';
import { JdbcTypeSelectorComponent } from './jdbc-type-selector.component';
import { provideHttpClient } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Web Component包装器
 *
 * 将Angular组件转换为标准Web Component
 */
(async () => {
  try {
    console.log('[TIS POC] Registering JDBC Type Selector Web Component...');

    // 创建独立的Angular应用
    const app = await createApplication({
      providers: [
        provideHttpClient(),
        importProvidersFrom(
          CommonModule,
          FormsModule
        )
      ]
    });

    // 创建Custom Element
    const element = createCustomElement(JdbcTypeSelectorComponent, {
      injector: app.injector
    });

    // 注册到浏览器
    customElements.define('tis-jdbc-type-selector', element);

    console.log('[TIS POC] Web Component registered successfully: tis-jdbc-type-selector');

    // 开发模式：暴露到window对象供调试
    if (typeof window !== 'undefined') {
      (window as any).__TIS_POC_COMPONENT__ = {
        name: 'tis-jdbc-type-selector',
        version: '1.0.0',
        component: element,
        test: () => {
          const el = document.createElement('tis-jdbc-type-selector');
          document.body.appendChild(el);
          console.log('[TIS POC] Test component created:', el);
        }
      };
    }

  } catch (error) {
    console.error('[TIS POC] Failed to register Web Component:', error);
    throw error;
  }
})();