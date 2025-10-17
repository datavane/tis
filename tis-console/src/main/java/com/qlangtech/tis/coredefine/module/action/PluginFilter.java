/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.coredefine.module.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginTaggable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginFilter {
  //private final PluginAction pluginAction;
  final Set<IPluginTaggable.PluginTag> tags;
  final Predicate<UpdateSite.Plugin> endTypeMatcher;
  final List<String> extendpoint;
  final String[] queries;

  public static PluginFilter create(PluginAction pluginAction) {
    return new PluginFilter(pluginAction.getExtendpointParam()
      , StringUtils.split(pluginAction.getString("query"), " ")
      , pluginAction.getStringArray("tag")
      , getEndTypeMatcher(pluginAction));
  }

  public static PluginFilter create(String endType, Collection<DescribableImpl> extendpoint) {
    if (StringUtils.isEmpty(endType)) {
      throw new IllegalArgumentException("param endType can not be null");
    }
    return new PluginFilter(extendpoint.stream().map((ep) -> ep.getExtendPoint().getName()).collect(Collectors.toList())
      , new String[0]
      , new String[0]
      , getEndTypeMatcher(endType));
  }

  private PluginFilter(List<String> extendpoint, String[] queries, String[] filterTags, Predicate<UpdateSite.Plugin> endTypeMatcher) {
    //this.pluginAction = pluginAction;
    this.extendpoint = extendpoint;// pluginAction.getExtendpointParam();
    // final String[] filterTags = pluginAction.getStringArray("tag");
    if (filterTags != null && filterTags.length > 0) {
      this.tags = Sets.newHashSet();
      for (String tag : filterTags) {
        tags.add(IPluginTaggable.PluginTag.parse(tag));
      }
    } else {
      this.tags = null;
    }
    this.queries = queries;
    this.endTypeMatcher = endTypeMatcher;
  }

  private static Predicate<UpdateSite.Plugin> getEndTypeMatcher(PluginAction pluginAction) {
    final String endType = pluginAction.getString(IEndTypeGetter.EndType.KEY_END_TYPE);
    return getEndTypeMatcher(endType);
  }

  public static Predicate<UpdateSite.Plugin> getEndTypeMatcher(final String endType) {
    if (StringUtils.isEmpty(endType)) {
      return (plugin) -> true;
    }
    IEndTypeGetter.EndType targetEndType = IEndTypeGetter.EndType.parse(endType);

    return (plugin) -> {
//      if (StringUtils.isEmpty(endType)) {
//        // 需要，将会收集
//        return true;
//      } else {
      //IEndTypeGetter.EndType targetEndType = IEndTypeGetter.EndType.parse(endType);
      return targetEndType.containIn(plugin.endTypes);
      //}
    };
  }


  /**
   * @param plugin
   * @param info
   * @return true 就直接过滤掉了
   */
  public boolean filter(Optional<PluginWrapper> plugin, UpdateSite.Plugin info) {
    if (this.tags != null) {
      //  info.pluginTags
      if (!CollectionUtils.containsAny(info.pluginTags, this.tags)) {
        return true;
      }
    }

    if (CollectionUtils.isNotEmpty(extendpoint)) {
      if (!CollectionUtils.containsAny(info.extendPoints.keySet(), extendpoint)) {
        return true;
      }
    }

    boolean collect = endTypeMatcher.test(info);
    if (!collect) {
      return true;
    }
    return this.filterPlugin((plugin.isPresent() ? plugin.get().getDisplayName() : info.title), (info != null ?
      info.excerpt : null));
  }

  private boolean filterPlugin(String title, String excerpt) {

    List<String> queries = getQueryPluginParam();
    if (CollectionUtils.isEmpty(queries)) {
      return false;
    }
    boolean collect = false;
    for (String searchQ : queries) {
      if (StringUtils.indexOfIgnoreCase(title, searchQ) > -1
        || (StringUtils.isNotBlank(excerpt) && StringUtils.indexOfIgnoreCase(excerpt, searchQ) > -1)) {
        collect = true;
        break;
      }
    }
    // 收集
    return !collect;
  }

  private List<String> getQueryPluginParam() {
    // String[] queries = StringUtils.split(this.getString("query"), " ");
    if (this.queries == null) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(queries);
  }
}
