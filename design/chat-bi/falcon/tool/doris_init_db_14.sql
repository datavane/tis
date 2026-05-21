-- DB: 14
CREATE DATABASE IF NOT EXISTS falcon_14;
CREATE TABLE IF NOT EXISTS falcon_14.`toy_products` (
  `Product_ID` BIGINT,
  `Product_Name` STRING,
  `Product_Category` STRING,
  `Product_Cost` STRING,
  `Product_Price` STRING
)
DUPLICATE KEY(`Product_ID`)
DISTRIBUTED BY RANDOM BUCKETS 4
PROPERTIES ("replication_num" = "1");
CREATE TABLE IF NOT EXISTS falcon_14.`toy_sales` (
  `Sale_ID` BIGINT,
  `Date` STRING,
  `Store_ID` BIGINT,
  `Product_ID` BIGINT,
  `Units` BIGINT
)
DUPLICATE KEY(`Sale_ID`)
DISTRIBUTED BY RANDOM BUCKETS 4
PROPERTIES ("replication_num" = "1");
CREATE TABLE IF NOT EXISTS falcon_14.`toy_stores` (
  `Store_ID` BIGINT,
  `Store_Name` STRING,
  `Store_City` STRING,
  `Store_Location` STRING,
  `Store_Open_Date` STRING
)
DUPLICATE KEY(`Store_ID`)
DISTRIBUTED BY RANDOM BUCKETS 4
PROPERTIES ("replication_num" = "1");
CREATE TABLE IF NOT EXISTS falcon_14.`toy_inventory` (
  `Store_ID` BIGINT,
  `Product_ID` BIGINT,
  `Stock_On_Hand` BIGINT
)
DUPLICATE KEY(`Store_ID`)
DISTRIBUTED BY RANDOM BUCKETS 4
PROPERTIES ("replication_num" = "1");

