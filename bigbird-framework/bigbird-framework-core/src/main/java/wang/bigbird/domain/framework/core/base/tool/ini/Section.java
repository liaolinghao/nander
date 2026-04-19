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

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;

/**
 * 是LinkedHashMap的扩展，用来存储一个Section中的所有信息
 * 它比一个LinkedHashMap仅仅多了一个sectionName，这个String用来标记Section的名称
 *
 * @author Bigbird
 */
public class Section extends LinkedHashMap<String, String> {

	/**
	 * 序列化唯一ID
	 */
	private static final long serialVersionUID = -8768561295633727768L;

	/**
	 * 节点名称
	 */
	private String sectionName;

	/**
	 * 默认节点名称为空串
	 */
	public Section() {
		this("");
	}

	/**
	 * 创建指定名称的节点
	 *
	 * @param name
	 *            节点名称
	 */
	public Section(String name) {
		super();
		sectionName = name;
	}

	/**
	 * 获取节点名称
	 *
	 * @return 节点名称
	 */
	public String getName() {
		return sectionName;
	}

	/**
	 * 设置节点名称
	 *
	 * @param name
	 *            节点名称
	 */
	public void setName(String name) {
		sectionName = name;
	}

	/**
	 * 带默认值的返回方法
	 *
	 * @param key
	 *            键
	 * @param defaultVal
	 *            默认值
	 * @return 键对应的数值
	 */
	public String getByDefaultValue(String key, String defaultVal) {
		return StringUtils.isBlank(get(key)) ? defaultVal : get(key);
	}

	@Override
	public String toString() {
		return sectionName;
	}

	@Override
	public int hashCode() {
		return sectionName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Section other = (Section) obj;
		if (sectionName == null) {
			return other.getName() == null;
		}
		return sectionName.equals(other.getName());
	}
}
