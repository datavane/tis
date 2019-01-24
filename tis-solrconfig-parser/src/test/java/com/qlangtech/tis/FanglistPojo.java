/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis;

import java.util.List;
import org.apache.solr.client.solrj.beans.Field;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FanglistPojo {

    @Field("buildid")
    private String buildid;

    @Field("pictUrl")
    private String pictUrl;

    @Field("buildName")
    private String buildName;

    @Field("buildAlias")
    private String buildAlias;

    @Field("groupName")
    private String groupName;

    @Field("propertyType")
    private Integer propertyType;

    @Field("developer")
    private String developer;

    @Field("salePhase")
    private Integer salePhase;

    @Field("areaId")
    private Long areaId;

    @Field("areaName")
    private String areaName;

    @Field("plateId")
    private Long plateId;

    @Field("plateName")
    private String plateName;

    @Field("saleOfficeAddr")
    private String saleOfficeAddr;

    @Field("propertyAddress")
    private String propertyAddress;

    @Field("buildFlavour")
    private String buildFlavour;

    @Field("mainApartment")
    private String mainApartment;

    @Field("refPrice")
    private Long refPrice;

    @Field("openTimeView")
    private String openTimeView;

    @Field("openTime")
    private Long openTime;

    @Field("rooms")
    private String rooms;

    @Field("roomsSize")
    private List<Integer> roomsSize;

    @Field("cityId")
    private Long cityId;

    @Field("cityName")
    private String cityName;

    @Field("picUrlNum")
    private Integer picUrlNum;

    @Field("sellerNick")
    private String sellerNick;

    @Field("sellerId")
    private String sellerId;

    @Field("grade")
    private Double grade;

    @Field("lookcnt")
    private Integer lookcnt;

    @Field("pop")
    private Integer pop;

    @Field("trendsTitle")
    private String trendsTitle;

    @Field("trendsUrl")
    private String trendsUrl;

    @Field("coupon")
    private Integer coupon;

    @Field("decorate")
    private Integer decorate;

    @Field("word")
    private String word;

    @Field("priceFlux")
    private Integer priceFlux;

    @Field("status")
    private Integer status;

    public void setBuildid(String buildid) {
        this.buildid = buildid;
    }

    public String getBuildid() {
        return this.buildid;
    }

    public void setPictUrl(String pictUrl) {
        this.pictUrl = pictUrl;
    }

    public String getPictUrl() {
        return this.pictUrl;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildName() {
        return this.buildName;
    }

    public void setBuildAlias(String buildAlias) {
        this.buildAlias = buildAlias;
    }

    public String getBuildAlias() {
        return this.buildAlias;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setPropertyType(Integer propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getPropertyType() {
        return this.propertyType;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDeveloper() {
        return this.developer;
    }

    public void setSalePhase(Integer salePhase) {
        this.salePhase = salePhase;
    }

    public Integer getSalePhase() {
        return this.salePhase;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getAreaId() {
        return this.areaId;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaName() {
        return this.areaName;
    }

    public void setPlateId(Long plateId) {
        this.plateId = plateId;
    }

    public Long getPlateId() {
        return this.plateId;
    }

    public void setPlateName(String plateName) {
        this.plateName = plateName;
    }

    public String getPlateName() {
        return this.plateName;
    }

    public void setSaleOfficeAddr(String saleOfficeAddr) {
        this.saleOfficeAddr = saleOfficeAddr;
    }

    public String getSaleOfficeAddr() {
        return this.saleOfficeAddr;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyAddress() {
        return this.propertyAddress;
    }

    public void setBuildFlavour(String buildFlavour) {
        this.buildFlavour = buildFlavour;
    }

    public String getBuildFlavour() {
        return this.buildFlavour;
    }

    public void setMainApartment(String mainApartment) {
        this.mainApartment = mainApartment;
    }

    public String getMainApartment() {
        return this.mainApartment;
    }

    public void setRefPrice(Long refPrice) {
        this.refPrice = refPrice;
    }

    public Long getRefPrice() {
        return this.refPrice;
    }

    public void setOpenTimeView(String openTimeView) {
        this.openTimeView = openTimeView;
    }

    public String getOpenTimeView() {
        return this.openTimeView;
    }

    public void setOpenTime(Long openTime) {
        this.openTime = openTime;
    }

    public Long getOpenTime() {
        return this.openTime;
    }

    public void setRooms(String rooms) {
        this.rooms = rooms;
    }

    public String getRooms() {
        return this.rooms;
    }

    public List<Integer> getRoomsSize() {
        return roomsSize;
    }

    public void setRoomsSize(List<Integer> roomsSize) {
        this.roomsSize = roomsSize;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getCityId() {
        return this.cityId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return this.cityName;
    }

    public void setPicUrlNum(Integer picUrlNum) {
        this.picUrlNum = picUrlNum;
    }

    public Integer getPicUrlNum() {
        return this.picUrlNum;
    }

    public void setSellerNick(String sellerNick) {
        this.sellerNick = sellerNick;
    }

    public String getSellerNick() {
        return this.sellerNick;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerId() {
        return this.sellerId;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Double getGrade() {
        return this.grade;
    }

    public void setLookcnt(Integer lookcnt) {
        this.lookcnt = lookcnt;
    }

    public Integer getLookcnt() {
        return this.lookcnt;
    }

    public void setPop(Integer pop) {
        this.pop = pop;
    }

    public Integer getPop() {
        return this.pop;
    }

    public void setTrendsTitle(String trendsTitle) {
        this.trendsTitle = trendsTitle;
    }

    public String getTrendsTitle() {
        return this.trendsTitle;
    }

    public void setTrendsUrl(String trendsUrl) {
        this.trendsUrl = trendsUrl;
    }

    public String getTrendsUrl() {
        return this.trendsUrl;
    }

    public void setCoupon(Integer coupon) {
        this.coupon = coupon;
    }

    public Integer getCoupon() {
        return this.coupon;
    }

    public void setDecorate(Integer decorate) {
        this.decorate = decorate;
    }

    public Integer getDecorate() {
        return this.decorate;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return this.word;
    }

    public void setPriceFlux(Integer priceFlux) {
        this.priceFlux = priceFlux;
    }

    public Integer getPriceFlux() {
        return this.priceFlux;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }
}
