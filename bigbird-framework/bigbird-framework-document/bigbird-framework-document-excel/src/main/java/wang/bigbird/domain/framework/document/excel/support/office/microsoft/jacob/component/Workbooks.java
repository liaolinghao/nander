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
package wang.bigbird.domain.framework.document.excel.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component.Bridge;

/**
 * 代表被操控的所有excel文档集合的控制权，通过该控制权可以创建对单份文档的控制权
 *
 * @author Bigbird
 */
public class Workbooks extends Bridge {

	public Workbooks(ActiveXComponent wordApp, Dispatch instance) {
		super(wordApp, instance);
	}

}
