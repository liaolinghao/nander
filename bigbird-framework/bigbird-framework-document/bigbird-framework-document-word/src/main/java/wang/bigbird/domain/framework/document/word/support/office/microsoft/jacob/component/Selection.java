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
package wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component.Bridge;

import java.io.File;

/**
 * 代表当前操控的单份文档中的选定范围对象或者插入点对象的控制权
 *
 * @author Bigbird
 */
public class Selection extends Bridge {

    public Selection(ActiveXComponent wordApp, Dispatch instance) {
        super(wordApp, instance);
    }

    /**
     * 查找字符串
     *
     * @param toFindText 待查找文本
     * @return 是否找到
     */
    public boolean find(String toFindText) {
        if (StringUtils.isBlank(toFindText)) {
            return false;
        }
        // 从selection所在位置开始查询
        Dispatch find = ActiveXComponent.call(dispatch, "Find").toDispatch();
        // 设置要查找的内容
        Dispatch.put(find, "Text", toFindText);
        // 向前查找
        Dispatch.put(find, "Forward", "True");
        // 设置格式
        Dispatch.put(find, "Format", "True");
        // 大小写匹配
        Dispatch.put(find, "MatchCase", "True");
        // 全字匹配
        Dispatch.put(find, "MatchWholeWord", "True");
        // 查找并选中
        return Dispatch.call(find, "Execute").getBoolean();
    }

    /**
     * 用新文本替换旧文本，只替换一次
     *
     * @param oldText 旧文本
     * @param newText 新文本
     * @return 是否替换成功
     */
    public boolean replaceText(String oldText, String newText) {
        if (find(oldText)) {
            Dispatch.put(dispatch, "Text", newText);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 用新文本替换旧文本，全部替换
     *
     * @param oldText 旧文本
     * @param newText 新文本
     */
    public void replaceAllText(String oldText, String newText) {
        while (find(oldText)) {
            Dispatch.put(dispatch, "Text", newText);
            Dispatch.call(dispatch, "MoveRight");
        }
    }

    /**
     * 把插入点移动到文档首位置
     */
    public void moveStart() {
        Dispatch.call(dispatch, "HomeKey", new Variant(6));
    }

    /**
     * 把插入点移动到文档末尾
     */
    public void moveEnd() {
        Dispatch.call(dispatch, "EndKey", new Variant(6));
    }

    /**
     * 在指定文本标签处插入图片
     *
     * @param textMark  文本标签
     * @param imageFile 图片文件
     */
    public boolean insertImageAtTextMark(String textMark, File imageFile) {
        if (find(textMark)) {
            Dispatch.call(Dispatch.get(dispatch, "InLineShapes").toDispatch(),
                    "AddPicture", imageFile.getAbsolutePath());
            return true;
        } else {
            return false;
        }
    }

}
