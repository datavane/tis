/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package com.qlangtech.tis.trigger.biz.dal.dao;


/**
 * @author 百岁（baisui@taobao.com）
 * @date 2012-7-2
 */
public abstract class JobConstant {
  public static final byte JOB_TYPE_FULL_DUMP = 1;
  public static final byte JOB_INCREASE_DUMP = 2;

  public static final String STOPED = "Y";
  public static final String STOPED_NOT = "N";

  //public final String DOMAIN_TERMINAOTR = "terminator";

  public static final String DOMAIN_TIS = "tis";

  static {
    //DOMAIN_TERMINAOTR = Config.getProjectName();
  }

}
