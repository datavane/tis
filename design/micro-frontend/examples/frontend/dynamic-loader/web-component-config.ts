/**
 * Web Component配置接口定义
 */
export interface WebComponentConfig {
  /** Custom Element标签名 (必须包含连字符) */
  tagName: string;

  /** 脚本资源URL */
  scriptUrl: string;

  /** 组件版本号 */
  version: string;

  /** 外部依赖 */
  dependencies?: WebComponentDependency[];

  /** CSS样式URL */
  styleUrls?: string[];

  /** 输入属性定义 */
  inputs?: WebComponentInput[];

  /** 输出事件定义 */
  outputs?: WebComponentOutput[];

  /** 元数据 */
  metadata?: Record<string, any>;
}

export interface WebComponentDependency {
  name: string;
  version: string;
  url?: string;
  description?: string;
}

export interface WebComponentInput {
  name: string;
  type: 'string' | 'object' | 'array' | 'boolean' | 'number';
  required: boolean;
  description?: string;
  properties?: Record<string, any>;
}

export interface WebComponentOutput {
  name: string;
  type: string;
  description?: string;
}
