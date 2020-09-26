/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.test.shop.pojo;

import com.qlangtech.tis.realtime.transfer.AbstractRowValueGetter;
import java.io.Serializable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MallShop extends AbstractRowValueGetter implements Serializable {

    private String id;

    /**
     * prop:ŚļóťďļŚģěšĹďID
     */
    private String shopEntityId;

    /**
     * prop:ŚēÜŚúąŚģěšĹďID
     */
    private String mallEntityId;

    /**
     * prop:ÁĪĽŚěčÔľĆ1001ÔľöŚēÜŚúļÔľõ 2001ÔľöŤĀĒÁõü
     */
    private Integer mallType;

    /**
     * prop:ŚļóťďļšłéŚēÜŚúąÁöĄŚÖ≥Á≥ĽÁä∂śÄĀ
     */
    private Boolean status;

    private String areaId;

    /**
     * prop:śĒ∂ťď∂ÁĪĽŚěč
     */
    private Boolean cashType;

    /**
     * prop:ŚąõŚĽļśó∂ťóī
     */
    private Integer createTime;

    /**
     * prop:šŅģśĒĻśó∂ťóī
     */
    private Integer opTime;

    /**
     * prop:ÁČąśú¨ŚŹ∑
     */
    private Integer lastVer;

    /**
     * prop:śėĮŚź¶śúČśēą
     */
    private Boolean isValid;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * get:ŚļóťďļŚģěšĹďID
     */
    public String getShopEntityId() {
        return shopEntityId;
    }

    /**
     * set:ŚļóťďļŚģěšĹďID
     */
    public void setShopEntityId(String shopEntityId) {
        this.shopEntityId = shopEntityId == null ? null : shopEntityId.trim();
    }

    /**
     * get:ŚēÜŚúąŚģěšĹďID
     */
    public String getMallEntityId() {
        return mallEntityId;
    }

    /**
     * set:ŚēÜŚúąŚģěšĹďID
     */
    public void setMallEntityId(String mallEntityId) {
        this.mallEntityId = mallEntityId == null ? null : mallEntityId.trim();
    }

    /**
     * get:ÁĪĽŚěčÔľĆ1001ÔľöŚēÜŚúļÔľõ 2001ÔľöŤĀĒÁõü
     */
    public Integer getMallType() {
        return mallType;
    }

    /**
     * set:ÁĪĽŚěčÔľĆ1001ÔľöŚēÜŚúļÔľõ 2001ÔľöŤĀĒÁõü
     */
    public void setMallType(Integer mallType) {
        this.mallType = mallType;
    }

    /**
     * get:ŚļóťďļšłéŚēÜŚúąÁöĄŚÖ≥Á≥ĽÁä∂śÄĀ
     */
    public Boolean getStatus() {
        return status;
    }

    /**
     * set:ŚļóťďļšłéŚēÜŚúąÁöĄŚÖ≥Á≥ĽÁä∂śÄĀ
     */
    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId == null ? null : areaId.trim();
    }

    /**
     * get:śĒ∂ťď∂ÁĪĽŚěč
     */
    public Boolean getCashType() {
        return cashType;
    }

    /**
     * set:śĒ∂ťď∂ÁĪĽŚěč
     */
    public void setCashType(Boolean cashType) {
        this.cashType = cashType;
    }

    /**
     * get:ŚąõŚĽļśó∂ťóī
     */
    public Integer getCreateTime() {
        return createTime;
    }

    /**
     * set:ŚąõŚĽļśó∂ťóī
     */
    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    /**
     * get:šŅģśĒĻśó∂ťóī
     */
    public Integer getOpTime() {
        return opTime;
    }

    /**
     * set:šŅģśĒĻśó∂ťóī
     */
    public void setOpTime(Integer opTime) {
        this.opTime = opTime;
    }

    /**
     * get:ÁČąśú¨ŚŹ∑
     */
    public Integer getLastVer() {
        return lastVer;
    }

    /**
     * set:ÁČąśú¨ŚŹ∑
     */
    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }

    /**
     * get:śėĮŚź¶śúČśēą
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * set:śėĮŚź¶śúČśēą
     */
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
}
