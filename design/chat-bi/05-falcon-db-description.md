# Falcon dev 数据库说明

> 本文件由分析 `/opt/misc/Falcon/dev_data/tables.json` 与 `dev.json` 汇总生成。
> Falcon 官方未提供 per-db 说明文档，此处为人工整理的索引，便于选库测试。

## 概览

| db_id | 领域 | 表数 | 题目数 | 适用场景 |
|-------|------|------|--------|----------|
| 4  | 金融保理 | 1 | 22 | 单表聚合、日期计算 |
| 5  | 独角兽公司 | 1 | 15 | 单表、窗口函数、比例计算 |
| 7  | 游戏销售 (vgsales) | 1 | 9  | 单表、多维聚合 |
| 8  | Google Play 应用商店 | 1 | 16 | 单表、分组排名 |
| 9  | 互联网用户渗透率 | 1 | 8  | 单表、时序连续增长 |
| 14 | 玩具销售 | 4 | 32 | **推荐入门**：多表 JOIN、库存/销售 |
| 15 | 信用卡业务 | 4 | 29 | 欺诈检测、客户分层、比例计算 |
| 16 | 学校成绩 | 4 | 16 | 排名、子查询、日期计算 |
| 17 | 电商订单（巴西风格） | 5 | 14 | 运费/支付/商品多表 JOIN |
| 18 | 全球经济 & 旅游 | 5 | 16 | 多维度国家指标关联 |
| 19 | 酒精消费与预期寿命 | 2 | 10 | 跨表关联、区域聚合 |
| 20 | 打车平台 | 2 | 29 | 司机/行程关联、窗口函数 |
| 21 | 电商（客户/商品/订单） | 3 | 35 | **题量最多**，schema 简洁 |
| 24 | 即时配送（blinkit） | 9 | 17 | **表数最多**，配送/营销/库存 |
| 26 | 足球统计 | 7 | 24 | 赛事/球员/联赛多表，含赔率列 |
| 28 | 在线购物 | 8 | 17 | 订单/支付/物流/评价全链路 |

---

## 详细说明

### db_id=4 — 金融保理

**表：** `finance_factoring_data`（1 张）

| 列名 | 类型 | 说明 |
|------|------|------|
| countryCode | integer | 国家编码 |
| customerID | text | 客户 ID |
| PaperlessDate | text | 无纸化日期 |
| invoiceNumber | integer | 发票号 |
| InvoiceDate / DueDate / SettledDate | text | 开票/到期/结清日期 |
| InvoiceAmount | real | 发票金额 |
| Disputed | text | 是否争议 |
| PaperlessBill | text | 是否无纸化账单 |
| DaysToSettle / DaysLate | integer | 结清天数 / 逾期天数 |

**典型题目：** 纸质账单客户逾期超行业均值的客户；按国家统计争议账单平均处理周期。

---

### db_id=5 — 独角兽公司

**表：** `unicorns_data`（1 张）

| 列名 | 类型 | 说明 |
|------|------|------|
| Company | text | 公司名 |
| Valuation | text | 估值（字符串，含 $） |
| Date Joined | text | 加入独角兽日期 |
| Country / City | text | 国家 / 城市 |
| Industry | text | 行业 |
| Investors | text | 投资方 |

**典型题目：** 估值占所在国家总估值比例超 10% 的公司；洛杉矶 2020 年后加入的公司与全球均值差。

---

### db_id=7 — 游戏销售（vgsales）

**表：** `vgsales`（1 张）

| 列名 | 类型 | 说明 |
|------|------|------|
| Rank | integer | 全球销量排名 |
| Name | text | 游戏名 |
| Platform | text | 平台 |
| Year | integer | 发布年份 |
| Genre | text | 类型 |
| Publisher | text | 发行商 |
| NA_Sales / EU_Sales / JP_Sales / Other_Sales / Global_Sales | real | 各区域及全球销量（百万） |

**典型题目：** 2005 年各类型欧洲销量超 50 万的游戏数；按平台统计欧日销量占比。

---

### db_id=8 — Google Play 应用商店

**表：** `googleplaystore`（1 张）

| 列名 | 类型 | 说明 |
|------|------|------|
| app / category / genres | TEXT | 应用名 / 类别 / 细分类型 |
| rating / reviews | REAL/TEXT | 评分 / 评论数 |
| size / installs | TEXT | 大小 / 安装量 |
| type / price | TEXT | 付费类型 / 价格 |
| content_rating | TEXT | 内容分级 |
| last_updated / current_ver / android_ver | TEXT | 更新日期 / 版本 |

**典型题目：** 各类别价格最高的付费应用；免费应用各内容分级的数量。

---

### db_id=9 — 互联网用户渗透率

**表：** `internet_users_data`（1 张）

| 列名 | 类型 | 说明 |
|------|------|------|
| entity / code | TEXT | 国家名 / 国家代码 |
| year | INTEGER | 年份 |
| cellular_subscription | REAL | 蜂窝网络订阅率 |
| internet_users | REAL | 互联网用户渗透率 |
| no_of_internet_users | INTEGER | 互联网用户数量 |
| broadband_subscription | REAL | 宽带订阅率 |

**典型题目：** 连续三年渗透率增长超 50% 的国家；蜂窝订阅率连续三年全球前十的国家。

---

### db_id=14 — 玩具销售 ⭐ 推荐入门

**表：** `toy_products` / `toy_sales` / `toy_stores` / `toy_inventory`（4 张）

| 表 | 关键列 |
|----|--------|
| toy_products | Product_ID, Product_Name, Product_Category, Product_Cost, Product_Price |
| toy_sales | Sale_ID, Date, Store_ID, Product_ID, Units |
| toy_stores | Store_ID, Store_Name, Store_City, Store_Location, Store_Open_Date |
| toy_inventory | Store_ID, Product_ID, Stock_On_Hand |

**典型题目：** Commercial 区域库存量前十产品；各类别销售额占比；各城市各类别历史总销售额筛选。

---

### db_id=15 — 信用卡业务

**表：** `credit_card_customer_base` / `credit_card_transaction_base` / `credit_card_fraud_base` / `credit_card_card_base`（4 张）

| 表 | 关键列 |
|----|--------|
| customer_base | Cust_ID, Age, Customer_Segment, Customer_Vintage_Group |
| transaction_base | Transaction_ID, Transaction_Date, Credit_Card_ID, Transaction_Value, Transaction_Segment |
| fraud_base | Transaction_ID, Fraud_Flag |
| card_base | Card_Number, Card_Family, Credit_Limit, Cust_ID |

**典型题目：** 钻石客户 2016-12 的平均交易金额；不同客户分组的欺诈交易占比；欺诈交易占信用额度比例排名。

---

### db_id=16 — 学校成绩

**表：** `school_students` / `school_teachers` / `school_marks` / `school_subjects`（4 张）

| 表 | 关键列 |
|----|--------|
| students | StudentID, FirstName, LastName, DateOfBirth, Email |
| teachers | TeacherID, FirstName, LastName, DateOfBirth |
| marks | MarkID, StudentID, SubjectID, TeacherID, MarkObtained, ExamDate |
| subjects | SubjectID, SubjectName |

**典型题目：** 各科目成绩排名前三学生的均分；各科均分高于全校均且教师年龄>40 的科目。

---

### db_id=17 — 电商订单（巴西风格）

**表：** `ecommerce_order_items` / `ecommerce_payments` / `ecommerce_products` / `ecommerce_orders` / `ecommerce_customers`（5 张）

| 表 | 关键列 |
|----|--------|
| order_items | order_id, product_id, seller_id, price, shipping_charges |
| payments | order_id, payment_type, payment_installments, payment_value |
| products | product_id, product_category_name, product_weight_g, 体积三维 |
| orders | order_id, customer_id, order_purchase_timestamp |
| customers | customer_id, customer_city, customer_state |

**典型题目：** 城市订单金额超均值的客户；运费占比超 15% 的商品类别；体积超 10000cm³ 商品的支付方式。

---

### db_id=18 — 全球经济 & 旅游

**表：** `world_economic_tourism` / `cost_of_living` / `richest_countries` / `unemployment` / `corruption`（5 张）

| 表 | 关键列 |
|----|--------|
| tourism | country, tourists_in_millions, receipts_in_billions, percentage_of_gdp |
| cost_of_living | country, cost_index, monthly_income, purchasing_power_index |
| richest_countries | country, gdp_per_capita |
| unemployment | country, unemployment_rate |
| corruption | country, annual_income, corruption_index |

**典型题目：** 月均收入>3000 且游客>1000 万的国家；生活成本指数>100 且旅游业占 GDP>1% 的国家。

---

### db_id=19 — 酒精消费与预期寿命

**表：** `alcohol_life_expectancy_verbose` / `alcohol_drinks`（2 张）

| 表 | 关键列 |
|----|--------|
| life_expectancy_verbose | YearCode, RegionCode/Display, CountryCode/Display, SexCode, DisplayValue（寿命）, Numeric |
| alcohol_drinks | country, beer_servings, spirit_servings, wine_servings, total_litres_of_pure_alcohol |

**典型题目：** 欧洲男性预期寿命>75 岁的国家及酒类消费量；各区域烈酒消费 TOP3。

---

### db_id=20 — 打车平台（CityRide）

**表：** `city_ride_drivers_data` / `city_ride_rides_data`（2 张）

| 表 | 关键列 |
|----|--------|
| drivers_data | Driver_ID, Name, Age, City, Experience_Years, Average_Rating, Active_Status |
| rides_data | Ride_ID, Driver_ID, City, Date, Distance_km, Duration_min, Fare, Rating, Promo_Code |

**典型题目：** 各城市活跃司机的平均行程次数和评分；服务年限高于城市均值且活跃的司机。

---

### db_id=21 — 电商（客户/商品/订单）⭐ 题量最多

**表：** `e_customers` / `e_products` / `e_orders`（3 张）

| 表 | 关键列 |
|----|--------|
| customers | CustomerID, FirstName, LastName, City, SignupDate |
| products | ProductID, ProductName, Category, Price, StockQuantity |
| orders | OrderID, CustomerID, ProductID, OrderDate, Quantity, OrderStatus, TotalAmount |

**典型题目：** 各类别订单量最多的产品且均价>200 的类别；卡萨布兰卡客户购买工具类的总金额。

---

### db_id=24 — 即时配送（blinkit）— 表数最多

**表：** 9 张（products / marketing_performance / inventory / inventoryNew / customer_feedback / order_items / customers / orders / delivery_performance）

| 表 | 关键列（摘要） |
|----|----------------|
| products | product_id, category, brand, price, mrp, margin_percentage, shelf_life_days |
| marketing_performance | campaign_id, channel, impressions, clicks, conversions, spend, roas |
| inventory / inventoryNew | product_id, date, stock_received, damaged_stock |
| customer_feedback | order_id, customer_id, rating, sentiment, feedback_category |
| order_items | order_id, product_id, quantity, unit_price |
| customers | customer_id, area, customer_segment, total_orders, avg_order_value |
| orders | order_id, promised_delivery_time, actual_delivery_time, payment_method |
| delivery_performance | delivery_partner_id, delivery_time_minutes, distance_km, reasons_if_delayed |

**典型题目：** 订单总金额高于所在客户分群均值的客户；2023 年承诺时间内完成率最高的配送员。

---

### db_id=26 — 足球统计

**表：** `football_appearances` / `teams` / `games` / `teamstats` / `shots` / `leagues` / `players`（7 张）

| 表 | 关键列（摘要） |
|----|----------------|
| appearances | gameID, playerID, goals, xGoals, assists, yellowCard, redCard, leagueID |
| games | gameID, leagueID, season, homeTeamID, awayTeamID, homeGoals, awayGoals, 各家赔率列 |
| teamstats | gameID, teamID, season, xGoals, shots, shotsOnTarget, ppda, result |
| shots | gameID, shooterID, shotResult, xGoal, positionX/Y |
| teams / players / leagues | ID + name |

**典型题目：** 2015 赛季主队预期进球前十比赛；英超各场比赛主队预期进球按日期排序；赛季射正率>40% 的球队。

---

### db_id=28 — 在线购物（全链路）

**表：** `online_shop_reviews` / `shipments` / `order_items` / `customers` / `orders` / `suppliers` / `products` / `payment`（8 张）

| 表 | 关键列（摘要） |
|----|----------------|
| reviews | product_id, customer_id, rating, review_date |
| shipments | order_id, carrier, delivery_date, shipment_status |
| order_items | order_id, product_id, quantity, price_at_purchase |
| customers | customer_id, email, address |
| orders | order_id, order_date, customer_id, total_price |
| suppliers | supplier_id, supplier_name, contact_name |
| products | product_id, product_name, category, price, supplier_id |
| payment | order_id, payment_method, amount, transaction_status |

**典型题目：** 信用卡支付成功率低于均值的州及失败客户邮箱；各承运商配送成功的平均时长排名。

---

## 选库建议

| 场景 | 推荐 db_id |
|------|-----------|
| 快速打通单条流程验证 | **21**（3 表，35 题，schema 简洁） |
| 入门多表 JOIN 测试 | **14**（4 表，32 题，业务直观） |
| 测试复杂多表 + 全链路 | **28**（8 表）或 **24**（9 表） |
| 测试窗口函数 / 排名 | **15**（信用卡）或 **20**（打车） |
| 测试时序 / 连续增长 | **9**（互联网）或 **19**（预期寿命） |
| 压测 JOIN + 赔率列噪声 | **26**（足球，列数最多） |