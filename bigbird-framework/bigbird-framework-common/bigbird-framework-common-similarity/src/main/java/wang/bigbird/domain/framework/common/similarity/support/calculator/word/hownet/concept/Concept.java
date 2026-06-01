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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.concept;

import lombok.Data;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.IHownetMeta;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.*;

/**
 * 知网的概念表示类
 * example和英文部分对于相似度的计算不起作用，考虑到内存开销，
 * 在概念的表示中去掉了这部分数据的对应定义
 *
 * @author Bigbird
 */
@Data
public class Concept implements IHownetMeta {
    /**
     * 概念名称
     */
    private String word;
    /**
     * 词性 part of speech
     */
    private String pos;
    /**
     * 定义
     */
    private String define;
    /**
     * 实词（true）比如：
     * 名词：人、山、水、电脑、娱乐、场地、苹果（你的业务主力）
     * 动词：走、看、吃、运行、开发
     * 形容词：美、快、明亮、热闹
     * 数词 / 量词：一、三、个、斤
     * 代词：我、他、这里
     * 有真实、具体的语义，能独立表达概念；
     * 可以单独做句子主干（主语、谓语、宾语）；
     * 是知网语义相似度计算的核心载体
     * 虚词（false），比如：
     * 介词：在、把、被、对、从
     * 连词：和、与、但是、而且
     * 助词：的、地、得、了、着、吗
     * 副词（部分）：很、都、也、才
     * 叹词、拟声词：啊、哎呀、叮咚
     * 无独立实在语义，只起连接、辅助、语气、语法作用；
     * 不能单独做主 / 谓 / 宾，必须依附实词使用；
     * 语义贡献极低，做词语相似度时，虚词一般不参与核心计算、不主导组合逻辑
     */
    private boolean substantive;
    /**
     * 主基本义原
     */
    private String mainSememe;
    /**
     * 其他基本义原
     */
    private String[] secondSememes;
    /**
     * 关系义原
     */
    private String[] relationSememes;
    /**
     * 关系符号描述
     */
    private String[] symbolSememes;
    /**
     * 类型
     */
    static String[][] concept_Type = {
            {"=", "事件"},
            {"aValue|属性值", "属性值"},
            {"qValue|数量值", "数量值"},
            {"attribute|属性", "属性"},
            {"quantity|数量", "数量"},
            {"unit|", "单位"},
            {"%", "部件"}
    };

    public Concept(String word, String pos, String define) {
        this.word = word;
        this.pos = pos;
        this.define = (define == null) ? "" : define.trim();
        // 虚词表示：{***}
        if (define.length() > 0 && define.startsWith(CommonConstants.DELIM_START) && define.endsWith(CommonConstants.DELIM_END)) {
            this.substantive = false;
        } else {
            this.substantive = true;
        }
        initDefine();
    }

    private void initDefine() {
        //其他基本义原
        List<String> secondList = new ArrayList<>();
        //关系义原
        List<String> relationList = new ArrayList<>();
        //符号义原
        List<String> symbolList = new ArrayList<>();
        String tokenString = this.define;
        if (!this.substantive) {
            //如果不是实词，则处理{}中的内容
            tokenString = define.substring(1, define.length() - 1);
        }
        StringTokenizer token = new StringTokenizer(tokenString, CommonConstants.COMMA, false);
        if (token.hasMoreTokens()) {
            this.mainSememe = token.nextToken();
        }
        main_loop:
        while (token.hasMoreTokens()) {
            String item = token.nextToken();
            if (StringUtils.isEmpty(item)) {
                continue;
            }
            //判断符号义原
            String symbol = item.substring(0, 1);
            for (int i = 0; i < Symbol_Descriptions.length; i++) {
                if (symbol.equals(Symbol_Descriptions[i][0])) {
                    symbolList.add(item);
                    continue main_loop;
                }
            }
            //判断第二基本义原
            if (item.indexOf(CommonConstants.EQUAL) > 0) {
                relationList.add(item);
            } else {
                secondList.add(item);
            }
        }
        this.secondSememes = secondList.toArray(new String[secondList.size()]);
        this.relationSememes = relationList.toArray(new String[relationList.size()]);
        this.symbolSememes = symbolList.toArray(new String[symbolList.size()]);
    }

    /**
     * 获取所有义原名称
     *
     * @return 义原名称集合
     */
    public Set<String> getAllSememeNames() {
        Set<String> names = new HashSet<>();
        //主义原
        names.add(getMainSememe());
        //关系义原
        for (String item : getRelationSememes()) {
            names.add(item.substring(item.indexOf(CommonConstants.EQUAL) + 1));
        }
        //符号义原
        for (String item : getSymbolSememes()) {
            names.add(item.substring(1));
        }
        //其他义原集合
        for (String item : getSecondSememes()) {
            names.add(item);
        }
        return names;
    }

    /**
     * 获取概念类型
     *
     * @return 概念类型
     */
    public String getType() {
        for (int i = 0; i < concept_Type.length; i++) {
            if (define.toUpperCase().indexOf(concept_Type[i][0].toUpperCase()) >= 0) {
                return concept_Type[i][1];
            }
        }
        return "普通";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("名称=");
        sb.append(this.word);
        sb.append("; 词性=");
        sb.append(this.pos);
        sb.append("; 定义=");
        sb.append(this.define);
        sb.append("; 第一基本义元:[" + mainSememe);
        sb.append("]; 其他基本义元描述:[");
        for (String sem : secondSememes) {
            sb.append(sem);
            sb.append(";");
        }
        sb.append("]; [关系义元描述:");
        for (String sem : relationSememes) {
            sb.append(sem);
            sb.append(";");
        }
        sb.append("]; [关系符号描述:");
        for (String sem : symbolSememes) {
            sb.append(sem);
            sb.append(";");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return define == null ? word.hashCode() : define.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Concept) {
            Concept c = (Concept) object;
            return word.equals(c.word) && define.equals(c.define);
        } else {
            return false;
        }
    }

}
