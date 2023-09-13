package com.qlangtech.tis.plugin.ds;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Types;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-02-18 20:22
 **/
public class TestDataType {

    @Test
    public void testDataTypeSerialize() {
        int decimalDigits = 4;
        int columnSize = 20;
        DataType dt = DataType.create(Types.BINARY, "binary", columnSize);
        dt.setDecimalDigits(decimalDigits);

        DataType actual = DataType.ds(dt.getS());

        Assert.assertEquals(dt.getJdbcType(), actual.getJdbcType());
        Assert.assertEquals(columnSize, actual.getColumnSize());
        Assert.assertEquals(decimalDigits, (int) actual.getDecimalDigits());

        dt.setDecimalDigits(null);
        actual = DataType.ds(dt.getS());

        Assert.assertEquals(dt.getJdbcType(), actual.getJdbcType());
        Assert.assertEquals(columnSize, actual.getColumnSize());
        Assert.assertNull("getDecimalDigits must be null", actual.getDecimalDigits());
    }
}
