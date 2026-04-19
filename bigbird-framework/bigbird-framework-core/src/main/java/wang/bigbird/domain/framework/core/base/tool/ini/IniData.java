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
package wang.bigbird.domain.framework.core.base.tool.ini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * 这个接口用于定义操作INI格式数据的对象需要实现的方法
 *
 * @author Bigbird
 */
public interface IniData {

	/* ==========================载入数据============================== */

	/**
	 * 通过InputStream，重新加载数据
	 *
	 * @param inputStream
	 *            内容输入流
	 * @throws IOException
	 *             IO异常
	 */
	void reload(InputStream inputStream) throws IOException;

	/**
	 * 通过reader，重新加载数据
	 *
	 * @param reader
	 *            内容读取对象
	 * @throws IOException
	 *             IO异常
	 */
	void reload(Reader reader) throws IOException;

	/**
	 * 通过file，重新加载数据
	 *
	 * @param file
	 *            内容文件
	 */
	void reload(File file);

	/**
	 * 通过file，重新加载数据，如果文件不存在，根据isCreate属性决定是否创建
	 *
	 * @param file
	 *            内容文件
	 * @param isCreate
	 *            是否创建文件
	 */
	void reload(File file, boolean isCreate);

	/**
	 * 通过file，重新加载数据，如果文件不存在，根据isCreate属性决定是否创建，根据local属性决定是否采用本地编码
	 *
	 * @param file
	 *            内容文件
	 * @param isCreate
	 *            是否创建文件
	 * @param local
	 *            是否采用本地编码
	 */
	void reload(File file, boolean isCreate, boolean local);

	/* ==========================获取节点值 ============================== */

	/**
	 * 获取指定section下指定key的字符串值，如果字串不存在返回null
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @return 值
	 */
	String get(String section, String key);

	/**
	 * 获取指定section下指定key的字符串值，如果字串不存在返回缺省字串，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	String get(String section, String key, String defaultVal);

	/**
	 * 获取指定section下指定key的整形数值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	int getInt(String section, String key, int defaultVal);

	/**
	 * 获取指定section下指定key的长整形数值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	long getLong(String section, String key, long defaultVal);

	/**
	 * 获取指定section下指定key的浮点数值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	float getFloat(String section, String key, float defaultVal);

	/**
	 * 获取指定section下指定key的浮点数值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	double getDouble(String section, String key, double defaultVal);

	/**
	 * 获取指定section下指定key的布尔值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 值
	 */
	boolean getBoolean(String section, String key, boolean defaultVal);

	/**
	 * 获取指定section下指定key的日期值，如果值不存在返回缺省值，即defaultVal
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @param format
	 *            日期格式
	 * @return 值
	 */
	Date getDate(String section, String key, Date defaultVal, String format);

	/**
	 * 根据Section的名字，获取Key-Value所在的HashMap
	 *
	 * @param section
	 *            节点名称
	 * @return Section对象
	 */
	LinkedHashMap<String, String> getSection(String section);

	/* ==========================添加节点值 ============================== */

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	String add(String section, String key, String value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	int add(String section, String key, int value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	long add(String section, String key, long value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	float add(String section, String key, float value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	double add(String section, String key, double value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	boolean add(String section, String key, boolean value, boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section与key-value，返回新值；
	 * 如果指定的Section存在，Key不存在，添加新的Key-value，返回新值；
	 * 如果key已经存在，根据overWrite属性，若为false，则不添加，返回原值；若为true，则覆盖原值，返回新值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param format
	 *            日期格式
	 * @param overWrite
	 *            是否覆盖
	 * @return 当前节点保存的对应键值
	 */
	Date add(String section, String key, Date value, String format,
             boolean overWrite);

	/**
	 * 如果指定的Section不存在，添加这个Section
	 *
	 * @param section
	 *            节点名称
	 * @return Section对象
	 */
	LinkedHashMap<String, String> addSection(String section);

	/* ==========================删除节点值 ============================== */

	/**
	 * 如果指定的Section不存在或者Key不存在，返回null； 如果指定的Section存在，key存在，执行remove的功能，返回原值
	 *
	 * @param section
	 *            节点名称
	 * @param key
	 *            键
	 * @return 删除键对应的值
	 */
	String remove(String section, String key);

	/**
	 * 删除指定的Section
	 *
	 * @param section
	 *            节点名称
	 * @return 删除是否成功
	 */
	boolean delSection(String section);

	/**
	 * 删除所有的Section
	 */
	void delAllSection();

	/**
	 * 清除指定的Section中的内容
	 *
	 * @param section
	 *            节点名称
	 * @return 清除是否成功
	 */
	boolean clearSection(String section);

	/* ==========================保存节点值 ============================== */

	/**
	 * 把修改之后的数据存入到默认的文件当中
	 *
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save() throws IOException;

	/**
	 * 把修改之后的数据存入到默认的文件当中
	 *
	 * @param local
	 *            是否采用本地编码
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save(boolean local) throws IOException;

	/**
	 * 把修改之后的数据存入到指定的文件当中
	 *
	 * @param file
	 *            保存的文件
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save(File file) throws IOException;

	/**
	 * 把修改之后的数据存入到指定的文件当中
	 *
	 * @param filePath
	 *            保存的文件路径
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save(String filePath) throws IOException;

	/**
	 * 把修改之后的数据存入到指定的文件当中
	 *
	 * @param filePath
	 *            保存的文件路径
	 * @param local
	 *            是否采用本地编码
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save(String filePath, boolean local) throws IOException;

	/**
	 * 把修改之后的数据存入到指定的文件当中
	 *
	 * @param filePath
	 *            保存的文件路径
	 * @param encode
	 *            文件编码
	 * @throws IOException
	 *             如果把数据保存到文件失败，抛出IOException
	 */
	void save(String filePath, String encode) throws IOException;

	/* ==========================其他方法 ============================== */

	/**
	 * 枚举section
	 *
	 * @return section枚举对象
	 */
	Enumeration<Section> sections();

	/**
	 * 这个方法用来打印所有的ini数据，主要用于测试程序正确性
	 *
	 * @return 数据信息
	 */
	String dumpObject();

	/**
	 * 是否为空
	 *
	 * @return 判断数据是否为空
	 */
	boolean isEmpty();

	/**
	 * 获取节点集合
	 *
	 * @return 节点集合
	 */
	Vector<Section> getSectionList();

}
