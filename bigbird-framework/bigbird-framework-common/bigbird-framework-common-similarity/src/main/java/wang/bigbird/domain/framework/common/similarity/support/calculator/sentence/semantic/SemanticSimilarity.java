package wang.bigbird.domain.framework.common.similarity.support.calculator.sentence.semantic;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.base.util.Utils;
import wang.bigbird.domain.framework.common.similarity.support.calculator.ISimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.sentence.AbstractSentenceSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.concept.ConceptSimilarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 基于语义的相似度计算
 * 词形部分不再用"字符串相等"判定共同词，而是用知网（HowNet）概念相似度给每个词对打分；
 * 词序部分不再需要"完全相同的词"，而是把相似度 ≥ 阈值的词对当作"相同词"来考察顺序。
 * Sim(A,B)=0.8×wordSim(A,B)+0.2×ordSim(A,B)
 *
 * @author Bigbird
 */
@Slf4j
public class SemanticSimilarity extends AbstractSentenceSimilarity {

    /**
     * 词形相似度权重
     */
    private static final double WORD_WEIGHT = 0.8D;

    /**
     * 词序相似度权重
     */
    private static final double ORDER_WEIGHT = 0.2D;

    /**
     * 词对相似度达到该阈值时，才参与词序相似度计算
     */
    private static final double ORDER_MATCH_THRESHOLD = 0.6D;
    /**
     * 词语相似度的计算
     */
    private ISimilarity wordSimilarity;

    private SemanticSimilarity() {
        wordSimilarity = ConceptSimilarity.getInstance();
    }

    private static class Holder {
        private static final SemanticSimilarity INSTANCE = new SemanticSimilarity();
    }

    public static SemanticSimilarity getInstance() {
        return SemanticSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculate(String target, String candidate) {
        // 分词与清洗，后续所有计算都只在"承载语义的词"上进行
        List<String> targetWords = Utils.segClean(target);
        List<String> candidateWords = Utils.segClean(candidate);
        if (targetWords.size() == 0 || candidateWords.size() == 0) {
            return 0D;
        }
        return calculate(targetWords.toArray(new String[targetWords.size()]), candidateWords.toArray(new String[candidateWords.size()]));
    }

    /**
     * 获取两个集合的词形相似度，同时获取相对于第一个句子中的词语顺序，第二个句子词语的顺序变化次数
     *
     * @param targetWords
     * @param candidateWords
     * @return
     */
    public double calculate(String[] targetWords, String[] candidateWords) {
        List<Cell> cells = buildPositiveScoreCells(targetWords, candidateWords);
        MatchResult matchResult = greedyMatch(cells, targetWords.length, candidateWords.length);
        double wordSim = 2D * matchResult.totalScore
                / (targetWords.length + candidateWords.length);
        double ordSim = calculateOrderSimilarity(matchResult.secondToFirst);
        log.debug("wordSim ==> {}, ordSim ==> {}", wordSim, ordSim);
        return WORD_WEIGHT * wordSim + ORDER_WEIGHT * ordSim;
    }

    /**
     * 计算词序相似度。
     * <p>
     * 按 candidate 中词语顺序，观察其匹配到的 target 位置序列。
     * 统计相邻位置发生逆序的次数，并归一化。
     * <p>
     * target    : [我] [爱] [自然] [语言]
     * candidate : [自然] [语言] [我] [爱]
     * secondToFirst = [3, 4, 1, 2]
     * 含义是：候选句第 0 个词”自然”对应目标句第 3 个词，第 1 个词”语言”对应第 4 个词，
     * 第 2 个词“我”对应第 1 个词，第 3 个词”爱”对应第 2 个词。
     *
     * @param secondToFirst 按候选句词序排列，但存的是目标句中的位置。它描述的是——当沿候选句从左到右走时，对应在目标句中的位置序列长什么样。如果两个句子语序一致，这个序列应当单调递增；如果语序被打乱，序列就会出现“下降”。
     * @return
     */
    private double calculateOrderSimilarity(int[] secondToFirst) {
        int previous = 0;
        int reverseOrderCount = 0;
        int matchedSize = 0;
        for (int firstIndex : secondToFirst) {
            if (firstIndex > 0) {
                matchedSize++;
                if (previous > 0 && previous > firstIndex) {
                    reverseOrderCount++;
                }
                previous = firstIndex;
            }
        }
        if (matchedSize == 0) {
            return 0D;
        }
        if (matchedSize == 1) {
            return 1D;
        }
        // matchedSize 个元素之间共有 matchedSize - 1 个相邻对；
        // 相邻逆序次数最多就是 matchedSize - 1（完全逆序时每一对相邻都逆序）；
        // 因此 reverseOrderCount / (matchedSize - 1) 是“相邻逆序比例”，取值范围 [0, 1]；
        // 用 1 减去它，得到“顺序一致比例”，即：
        return 1D - (double) reverseOrderCount / (matchedSize - 1);
    }

    /**
     * 贪心最大匹配。
     * <p>
     * 按词对相似度从高到低选择，若对应的行和列都未被占用，则匹配该词对。
     * 同时按行、列升序，保持与原实现“每轮扫描取第一个最大值”的语义一致。
     */
    private MatchResult greedyMatch(List<Cell> cells, int targetSize, int candidateSize) {
        cells.sort(
                Comparator.comparingDouble(Cell::getScore)
                        .reversed()
                        .thenComparingInt(Cell::getRow)
                        .thenComparingInt(Cell::getCol)
        );
        boolean[] usedTarget = new boolean[targetSize];
        boolean[] usedCandidate = new boolean[candidateSize];
        // secondToFirst[j] = i + 1 表示 candidate 第 j 个词匹配到 target 第 i 个词；
        // 0 表示未参与词序计算。
        int[] secondToFirst = new int[candidateSize];
        double totalScore = 0D;
        for (Cell cell : cells) {
            if (usedTarget[cell.row] || usedCandidate[cell.col]) {
                continue;
            }
            usedTarget[cell.row] = true;
            usedCandidate[cell.col] = true;
            totalScore += cell.score;
            if (cell.score >= ORDER_MATCH_THRESHOLD) {
                // 数组下标 j：候选句 candidate 第 j 个词；
                // 数组值 i + 1：该词匹配到目标句 target 第 i 个词（从 1 开始编号）；
                // 值为 0：该候选词没有匹配，或匹配分数低于 ORDER_MATCH_THRESHOLD = 0.6，不参与词序计算。
                secondToFirst[cell.col] = cell.row + 1;
            }
        }
        return new MatchResult(totalScore, secondToFirst);
    }

    /**
     * 构建所有正分词对。
     * <p>
     * 原实现中 maxScore 初始为 0，非正分词对不会被选中，
     * 因此这里直接过滤掉 score <= 0 的词对，结果等价。
     */
    private List<Cell> buildPositiveScoreCells(String[] targetWords, String[] candidateWords) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < targetWords.length; i++) {
            for (int j = 0; j < candidateWords.length; j++) {
                double score = wordSimilarity.calculate(targetWords[i], candidateWords[j]);
                if (score > 0D) {
                    cells.add(new Cell(i, j, score));
                }
            }
        }
        return cells;
    }


    private static final class Cell {
        private final int row;
        private final int col;
        private final double score;

        private Cell(int row, int col, double score) {
            this.row = row;
            this.col = col;
            this.score = score;
        }

        private int getRow() {
            return row;
        }

        private int getCol() {
            return col;
        }

        private double getScore() {
            return score;
        }
    }

    private static final class MatchResult {
        private final double totalScore;
        private final int[] secondToFirst;

        private MatchResult(double totalScore, int[] secondToFirst) {
            this.totalScore = totalScore;
            this.secondToFirst = secondToFirst;
        }
    }

}
