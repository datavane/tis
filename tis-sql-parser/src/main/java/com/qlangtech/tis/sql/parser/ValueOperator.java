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
package com.qlangtech.tis.sql.parser;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年5月6日
 */
public abstract class ValueOperator {
    // protected final IDataTupleCreator tupleCreator;
    // protected final ColName output;
    // 
    // public ValueOperator(ColName output, IDataTupleCreator expression) {
    // super();
    // this.tupleCreator = expression;
    // this.output = output;
    // }
    // 
    // public String getName() {
    // return output.getAliasName();
    // }
    // 
    // // /**
    // // * 得到流的值
    // // *
    // // * @param dataContext
    // // * @return
    // // */
    // // public Object getValue(IDataContext dataContext) {
    // // return tupleCreator.getVal(output.getName(), dataContext);
    // // }
    // 
    // public abstract void process();
    // 
    // public static ValueOperator createTableValueOperator(ColName output, Table expression) {
    // // return new TableValueOperator(output, expression);
    // return null;
    // }
    // 
    // public static ValueOperator createGatherValueOperator(ColName output, Expression expression,
    // StackableAstVisitorContext<Integer> context) {
    // 
    // StreamTransformVisitor visitor = new StreamTransformVisitor();
    // visitor.process(expression, context);
    // 
    // IDataTupleCreator tupleCreator = new IDataTupleCreator() {
    // 
    // @Override
    // public EntityName getEntityName() {
    // 
    // return null;
    // }
    // 
    // @Override
    // public void generateGroovyScript(FunctionVisitor.FuncFormat rr) {
    // 
    // }
    // 
    // @Override
    // public void accept(IDataTupleCreatorVisitor visitor) {
    // }
    // 
    // // @Override
    // // public Object getVal(String name, IDataContext context) {
    // //
    // // return null;
    // // }
    // 
    // };
    // 
    // return new GatherValueOperator(output, tupleCreator);
    // }
    // 
    // public static ValueOperator createTableTupleOperator(ColName output, Expression expression,
    // StackableAstVisitorContext<Integer> context) {
    // 
    // StreamTransformVisitor visitor = new StreamTransformVisitor();
    // visitor.process(expression, context);
    // 
    // IDataTupleCreator tupleCreator = new IDataTupleCreator() {
    // @Override
    // public EntityName getEntityName() {
    // 
    // return null;
    // }
    // 
    // @Override
    // public void generateGroovyScript(FunctionVisitor.FuncFormat rr) {
    // 
    // // return null;
    // }
    // 
    // @Override
    // public void accept(IDataTupleCreatorVisitor visitor) {
    // }
    // 
    // // @Override
    // // public Object getVal(String name, IDataContext context) {
    // //
    // // return null;
    // // }
    // 
    // };
    // 
    // return new GatherValueOperator(output, tupleCreator);
    // }
    // 
    // ////
    // // private static class TableValueOperator extends ValueOperator {
    // //
    // // public TableValueOperator(ColName output, Table table) {
    // // super(output, table);
    // // }
    // //
    // // @Override
    // // public void process() {
    // // }
    // // }
    // //
    // private static class GatherValueOperator extends ValueOperator {
    // 
    // // private final ValueOperator[] params;
    // 
    // public GatherValueOperator(ColName output, IDataTupleCreator dataTupleCreator) {
    // super(output, dataTupleCreator);
    // 
    // }
    // 
    // @Override
    // public void process() {
    // }
    // }
    // 
    // // public static class EntitiyRef {
    // // // 表實體名稱
    // //
    // //
    // // private SqlTaskNode taskNode;
    // //
    // // public EntitiyRef(String realEntityName) {
    // // super();
    // // this.realEntityName = realEntityName;
    // // }
    // //
    // // public EntitiyRef(String realEntityName, SqlTaskNode taskNode) {
    // // super();
    // // this.realEntityName = realEntityName;
    // // this.taskNode = taskNode;
    // // }
    // //
    // // public SqlTaskNode getTaskNode() {
    // //
    // // return this.taskNode;
    // // }
    // //
    // // public String getRealEntityName() {
    // // return this.realEntityName;
    // // }
    // //
    // // public void setTaskNode(SqlTaskNode taskNode) {
    // // this.taskNode = taskNode;
    // // }
    // //
    // // }
    // 
    // /**
    // * 数据流上下文，流计算中只是设定数据流的管道处理规则（是无状态），context才是具体的数据
    // */
    // public interface IDataContext {
    // 
    // }
}
