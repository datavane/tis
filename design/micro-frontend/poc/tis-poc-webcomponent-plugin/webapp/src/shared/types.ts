/**
 * 共享类型定义
 */

export interface TuplesProperty {
  viewType(): string;
  mcols?: any[];
  typeMetas?: any[];
}

export interface JdbcTypeItem {
  name: string;
  code: string;
  selected: boolean;
  description?: string;
}

export interface JdbcTypeTuplesProperty extends TuplesProperty {
  items: JdbcTypeItem[];
}
