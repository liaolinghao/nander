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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.sememe;

import lombok.Data;

/**
 * 描述知网义原的基本对象
 * sememe cn="成功" define="{experiencer,scope}" en="succeed" id="1-1-2-1-4-5"/>
 * 义原的id表明了义原之间的上下位关系和义原的深度。
 *
 * @author Bigbird
 */
@Data
public class Sememe {

    /**
     * 义原编号
     */
    private String id;
    /**
     * 中文名称
     */
    private String cnWord;
    /**
     * 英文名称
     */
    private String enWord;
    /**
     * 定义
     */
    private String define;

    /**
     * 获取义原类型
     *
     * @return 义原类型
     */
    public int getType() {
        char c = id.charAt(0);
        switch (c) {
            case '1':
                return SememeType.Event;
            case '2':
                return SememeType.Entity;
            case '3':
                return SememeType.Attribute;
            case '4':
                return SememeType.Quantity;
            case '5':
                return SememeType.AValue;
            case '6':
                return SememeType.QValue;
            case '7':
                return SememeType.SecondaryFeature;
            case '8':
                return SememeType.Syntax;
            case '9':
                return SememeType.EventRoleAndFeatures;
            default:
                return SememeType.Unknown;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("义原编号=");
        sb.append(id);
        sb.append(";中文名称=");
        sb.append(cnWord);
        sb.append(";英文名称=");
        sb.append(enWord);
        sb.append(";定义=");
        sb.append(define);
        return sb.toString();
    }

}
