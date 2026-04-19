/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.injector;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.baomidou.mybatisplus.extension.injector.methods.LogicDeleteByIdWithFill;
import com.github.yulichang.injector.MPJSqlInjector;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;

/**
 * 自定义sql注入器
 *
 * @author Bigbird
 */
public class MySqlInjector extends MPJSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        // mybatis-plus的顶级IService接口有一个saveBatch()方法，
        // 但是它会执行多条insertSql，在数据量大的时候效率会非常差，
        // 如果我们是mysql数据库，又不想自己写mapper.xml，
        // mybatis-plus提供了InsertBatchSomeColumn批量insert方法，
        // 注入后，通过调用insertBatchSomeColumn实现高效插入，执行方式：
        // insert into table values (x1, y1, z1), (x2, y2, z2), (x…, y…, z…);
        // int insertBatchSomeColumn(List<Type> list);
        methodList.add(new InsertBatchSomeColumn(i -> i.getFieldFill() != FieldFill.UPDATE));
        // 根据Id更新固定的某些字段，更新时可以设置哪些字段需要更新，哪些字段不需要更新
        // 如果设置需要更新的字段没有设置值，更新为null
        // int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) Type type);
        methodList.add(new AlwaysUpdateSomeColumnById(i -> i.getFieldFill() != FieldFill.INSERT));
        // 根据id进行逻辑删除数据，并带自动填充功能
        // 当进行逻辑删除时，有些字段根据设置进行更新（比如：注销用户积分清零）
        // int deleteByIdWithFill(Type type);
        methodList.add(new LogicDeleteByIdWithFill());
        // 会忽略数据库中已经存在的数据，如果数据库没有数据，就插入新的数据，
        // 如果有数据的话就跳过这条数据
        // insert ignore into table values (x1, y1, z1), (x2, y2, z2), (x…, y…, z…);
        // int insertIgnoreBatchSomeColumn(List<Type> list);
        methodList.add(new InsertIgnoreBatchSomeColumnInjector(i -> i.getFieldFill() != FieldFill.UPDATE));
        return methodList;
    }

    private static final String BASE_MAPPER_CLASS_NAME = "wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao.BaseMapper";

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        if (BASE_MAPPER_CLASS_NAME.equals(mapperClass.getName())) {
            return;
        } else {
            super.inspectInject(builderAssistant, mapperClass);
        }
    }

}
