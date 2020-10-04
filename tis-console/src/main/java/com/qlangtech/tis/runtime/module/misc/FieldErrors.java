package com.qlangtech.tis.runtime.module.misc;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * @author: baisui 百岁
 * @create: 2020-10-04 07:28
 **/
public class FieldErrors {
  private Map<Integer, FieldErrorInfo> errors = Maps.newHashMap();

  public FieldErrorInfo getFieldErrorInfo(Integer id) {
    FieldErrorInfo err = null;
    if ((err = errors.get(id)) == null) {
      err = new FieldErrorInfo(id);
      errors.put(id, err);
    }
    return err;
  }

  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  public Collection<FieldErrorInfo> getAllErrs() {
    return this.errors.values();
  }
}
