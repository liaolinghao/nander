package wang.bigbird.domain.framework.common.similarity.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;

/**
 * 词语相似度计算结果
 * <p>
 * 封装两个词语之间的相似度计算结果，包含完整的上下文信息
 * </p>
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordSimilarityBO {

    /**
     * 目标词（Target Word）
     * <p>
     * 需要被匹配、查找或验证的词语。
     * 在相似度计算中作为主动方，通常是用户输入或查询条件。
     */
    private String targetWord;

    /**
     * 候选词（Candidate Word）
     * <p>
     * 用于与目标词进行比较的候选词语。
     * 在相似度计算中作为被动方，通常来自候选列表或数据库。
     */
    private String candidateWord;

    /**
     * 使用的相似度算法
     * <p>
     * 说明相似度值是通过哪种算法计算得出的
     */
    private WordSimilarityAlgorithmEnum algorithm;

    /**
     * 相似度值
     * <p>
     * 计算结果，范围：0.0 - 1.0
     * </p>
     * <ul>
     *   <li>1.0：完全相同</li>
     *   <li>0.8 - 1.0：高度相似</li>
     *   <li>0.5 - 0.8：中度相似</li>
     *   <li>0.0 - 0.5：低度相似或不相似</li>
     * </ul>
     */
    private double similarity;

    /**
     * 判断是否达到高度相似阈值（>= 0.8）
     */
    public boolean isHighSimilarity() {
        return similarity >= 0.8;
    }

    /**
     * 判断是否达到中度相似阈值（0.5 <= similarity < 0.8）
     */
    public boolean isMediumSimilarity() {
        return similarity >= 0.5 && similarity < 0.8;
    }

    /**
     * 判断是否达到低度相似阈值（0.3 <= similarity < 0.5）
     */
    public boolean isLowSimilarity() {
        return similarity >= 0.3 && similarity < 0.5;
    }

}
