package com.qlangtech.tis.realtime;

import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.realtime.test.util.DefaultRowValueGetter;
import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

/**
 * @author: baisui 百岁
 * @create: 2020-10-16 15:16
 **/
public abstract class BasicBeanGroup {
    private final BasicRMListener listenerBean;

    public abstract <T> T invoke();

    public BasicBeanGroup(BasicRMListener listenerBean) {
        this.listenerBean = listenerBean;
    }

    protected <T> BasicTestCase.DTO<T> getBean(String path, Class<T> pojoClazz, PojoCUD<T> crud) {
        try {
            DefaultRowValueGetter junitValsExample = deserializeBean(crud.getTableName(), listenerBean.getTableProcessor(crud.getTableName()), path);
            DefaultRowValueGetter vals = deserializeBean(crud.getTableName(), null, /*** 不需要处理 */path);
            T pojo = BasicTestCase.DTO.pojo(vals, pojoClazz);
            crud.initSyncWithDB(pojo);
            return new BasicTestCase.DTO<>(junitValsExample, vals, pojoClazz, pojo, crud);
        } catch (Exception e) {
            throw new RuntimeException("path:" + path, e);
        }
    }

    protected DefaultRowValueGetter deserializeBean(String tableName, Table tableRowProcessor, String path) {
        String line = null;
        try {
            DefaultRowValueGetter i = new DefaultRowValueGetter(tableRowProcessor);
            i.updateProps = new DefaultRowValueGetter.UpdatePropsCollector(tableRowProcessor);
            String colName = null;
            String colValue = null;
            LineIterator it = null;
            try (InputStream input = this.getClass().getResourceAsStream(path)) {
                it = IOUtils.lineIterator(input, TisUTF8.get());
                while (it.hasNext()) {
                    line = it.nextLine();
                    colName = StringUtils.trimToEmpty(StringUtils.substringBefore(line, ":"));
                    colValue = StringUtils.trimToEmpty(StringUtils.substringAfter(line, ":"));
                    if (!"null".equalsIgnoreCase(colValue)) {
                        i.put(colName, colValue);
                        // i.put(BasicPojoConsumer.removeUnderline(colName).toString(), colValue);
                        // BeanUtils.copyProperty(i, BasicPojoConsumer.removeUnderline(colName).toString(), colValue);
                    }
                }
            }
            return i;
        } catch (Exception e) {
            throw new RuntimeException("path:" + path + ",line:" + line, e);
        }
    }

}
