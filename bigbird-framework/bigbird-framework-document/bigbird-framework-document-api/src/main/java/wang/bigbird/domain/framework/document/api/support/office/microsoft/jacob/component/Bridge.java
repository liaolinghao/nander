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
package wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

/**
 * 封装JAVA程序与Microsoft Office应用程序之间建立起来的桥梁，
 * 通过桥梁中的属性可以实现对Microsoft Office应用程序的手工模拟操作
 * 此时，Microsoft Office应用程序相当于被JAVA程序操控的提线木偶
 *
 * @author Bigbird
 */
public class Bridge {

    /**
     * 打开的Microsoft Office应用程序实例
     */
    protected ActiveXComponent app;
    /**
     * 调度处理类，封装了一些操作来操作office，可理解为JAVA获得的对Office应用某种实例的一个控制权
     */
    protected Dispatch dispatch;

    /**
     * 封装了应用程序实例和控制权的构造方法，相当于获得了提线木偶的提棍
     *
     * @param app      打开的Microsoft Office应用程序实例
     * @param dispatch 调度处理类，封装了一些操作来操作office，可理解为JAVA获得的对Office应用某种实例的一个控制权
     */
    public Bridge(ActiveXComponent app, Dispatch dispatch) {
        this.app = app;
        this.dispatch = dispatch;
    }

    public Dispatch getDispatch() {
        return dispatch;
    }

}
