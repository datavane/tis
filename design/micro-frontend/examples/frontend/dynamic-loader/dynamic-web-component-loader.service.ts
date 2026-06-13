import { Injectable } from '@angular/core';
import { WebComponentConfig } from './web-component-config';

/**
 * 动态Web Component加载器
 *
 * 负责运行时动态加载插件提供的Web Component脚本
 */
@Injectable({ providedIn: 'root' })
export class DynamicWebComponentLoader {

  /** 脚本加载缓存 */
  private scriptCache = new Map<string, Promise<void>>();

  /** 组件注册状态 */
  private componentRegistry = new Map<string, boolean>();

  /** 依赖加载状态 */
  private dependencyCache = new Map<string, Promise<void>>();

  /**
   * 加载并渲染Web Component
   */
  async loadAndRender(
    config: WebComponentConfig,
    container: HTMLElement,
    props: Record<string, any>
  ): Promise<HTMLElement> {

    console.log('[DynamicLoader] Loading component:', config.tagName);

    // 1. 加载依赖
    if (config.dependencies && config.dependencies.length > 0) {
      await this.loadDependencies(config.dependencies);
    }

    // 2. 加载样式
    if (config.styleUrls && config.styleUrls.length > 0) {
      await this.loadStyles(config.styleUrls);
    }

    // 3. 加载脚本
    await this.loadScript(config);

    // 4. 等待注册
    await this.waitForRegistration(config.tagName);

    // 5. 创建实例
    const element = document.createElement(config.tagName);

    // 6. 设置属性
    this.setProperties(element, props);

    // 7. 插入DOM
    container.appendChild(element);

    console.log('[DynamicLoader] Component rendered:', config.tagName);

    return element;
  }

  /**
   * 加载脚本（带缓存）
   */
  private async loadScript(config: WebComponentConfig): Promise<void> {
    const cacheKey = `${config.scriptUrl}?v=${config.version}`;

    if (!this.scriptCache.has(cacheKey)) {
      console.log('[DynamicLoader] Loading script:', cacheKey);
      const promise = this.injectScript(cacheKey);
      this.scriptCache.set(cacheKey, promise);
    }

    return this.scriptCache.get(cacheKey)!;
  }

  /**
   * 注入脚本标签
   */
  private injectScript(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.crossOrigin = 'anonymous';

      script.onload = () => {
        console.log('[DynamicLoader] Script loaded:', url);
        resolve();
      };

      script.onerror = (error) => {
        console.error('[DynamicLoader] Script load failed:', url, error);
        reject(new Error(`Failed to load ${url}`));
      };

      document.head.appendChild(script);
    });
  }

  /**
   * 加载依赖库
   */
  private async loadDependencies(dependencies: WebComponentConfig['dependencies']): Promise<void> {
    const promises = dependencies!.map(dep => {
      if (!dep.url) {
        console.warn('[DynamicLoader] Dependency has no URL:', dep.name);
        return Promise.resolve();
      }

      const cacheKey = `${dep.name}@${dep.version}`;
      if (!this.dependencyCache.has(cacheKey)) {
        console.log('[DynamicLoader] Loading dependency:', dep.name);
        const promise = this.injectScript(dep.url);
        this.dependencyCache.set(cacheKey, promise);
      }

      return this.dependencyCache.get(cacheKey)!;
    });

    await Promise.all(promises);
  }

  /**
   * 加载样式
   */
  private async loadStyles(styleUrls: string[]): Promise<void> {
    const promises = styleUrls.map(url => {
      return new Promise<void>((resolve, reject) => {
        // 检查是否已加载
        const existing = document.querySelector(`link[href="${url}"]`);
        if (existing) {
          resolve();
          return;
        }

        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = url;
        link.onload = () => resolve();
        link.onerror = () => reject(new Error(`Failed to load style: ${url}`));
        document.head.appendChild(link);
      });
    });

    await Promise.all(promises);
  }

  /**
   * 等待Custom Element注册
   */
  private async waitForRegistration(tagName: string): Promise<void> {
    if (this.componentRegistry.has(tagName)) {
      return;
    }

    console.log('[DynamicLoader] Waiting for registration:', tagName);

    await customElements.whenDefined(tagName);
    this.componentRegistry.set(tagName, true);

    console.log('[DynamicLoader] Component registered:', tagName);
  }

  /**
   * 设置组件属性
   */
  private setProperties(element: any, props: Record<string, any>): void {
    for (const [key, value] of Object.entries(props)) {
      element[key] = value;
    }
  }

  /**
   * 预加载脚本（性能优化）
   */
  preload(configs: WebComponentConfig[]): void {
    configs.forEach(config => {
      this.loadScript(config).catch(err => {
        console.warn(`[DynamicLoader] Preload failed for ${config.tagName}:`, err);
      });
    });
  }

  /**
   * 清理缓存（开发模式使用）
   */
  clearCache(): void {
    this.scriptCache.clear();
    this.componentRegistry.clear();
    this.dependencyCache.clear();
    console.log('[DynamicLoader] Cache cleared');
  }
}
