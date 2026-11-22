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
package wang.bigbird.domain.framework.common.similarity.support.calculator.paragraph.layered;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.support.calculator.ISimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.paragraph.AbstractParagraphSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.trigram.TrigramJaccardSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.sentence.semantic.SemanticSimilarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 基于分层对齐的相似度计算
 * 整体流程：
 * 1. 切句：ParagraphSplitter；
 * 2. 粗筛：整段去标点后 Trigram 杰卡德，低于阈值直接短路；
 * 3. 精排：句子级语义相似度 + 贪心最优对齐 + 句子权重 + 加权 Dice；
 * 4. 结构分：候选句匹配位置的相邻逆序归一化；
 * 5. 融合：similarity = 0.8 × 内容分 + 0.2 × 结构分。
 *
 * @author Bigbird
 */
@Slf4j
public class LayeredAlignmentSimilarity extends AbstractParagraphSimilarity {

    /**
     * 粗筛阈值：低于它认为明显不相关，直接短路，节省精排开销
     */
    private static final double ROUGH_THRESHOLD = 0.2D;

    /**
     * 内容分权重
     */
    private static final double CONTENT_WEIGHT = 0.8D;

    /**
     * 结构分权重
     */
    private static final double STRUCT_WEIGHT = 0.2D;

    /**
     * 句子相似度达到该阈值才计入结构分，避免噪声匹配打乱句序
     */
    private static final double STRUCT_MATCH_THRESHOLD = 0.5D;

    /**
     * 首末句权重加成系数，归一化后权重均值保持 1
     */
    private static final double EDGE_BOOST = 0.3D;

    /**
     * 粗筛前清洗：去标点、去空白，保证粗筛与精排口径一致
     */
    private static final Pattern PUNCT_AND_SPACE = Pattern.compile("[\\p{P}\\s]+");

    private final ISimilarity sentenceSimilarity;
    private final ISimilarity roughSimilarity;

    private LayeredAlignmentSimilarity() {
        sentenceSimilarity = SemanticSimilarity.getInstance();
        roughSimilarity = TrigramJaccardSimilarity.getInstance();
    }

    private static class Holder {
        private static final LayeredAlignmentSimilarity INSTANCE = new LayeredAlignmentSimilarity();
    }

    public static LayeredAlignmentSimilarity getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public double doCalculateParagraph(List<String> targetSentences, List<String> candidateSentences) {
        // 1. 粗筛：整段去标点后字符级 trigram，明显不相关直接短路
        String targetPara = normalizeForRough(String.join("", targetSentences));
        String candidatePara = normalizeForRough(String.join("", candidateSentences));
        double rough = roughSimilarity.calculate(targetPara, candidatePara);
        if (rough < ROUGH_THRESHOLD) {
            log.debug("rough={} below {}, short-circuit", rough, ROUGH_THRESHOLD);
            return rough;
        }
        // 2. 精排：句子贪心最优对齐 + 句子权重 + 加权 Dice
        AlignmentResult alignment = optimalAlign(targetSentences, candidateSentences);
        // 3. 结构分：候选句匹配位置的相邻逆序归一化
        double structSim = calculateOrderSimilarity(alignment.candidateToTarget);
        // 4. 融合
        double score = CONTENT_WEIGHT * alignment.weightedDice
                + STRUCT_WEIGHT * structSim;
        log.debug("rough={}, content={}, struct={}, final={}",
                rough, alignment.weightedDice, structSim, score);
        return score;
    }

    /**
     * 粗筛归一化：去掉所有标点与空白。
     * 切句时标点已被丢弃，这里显式清洗是为了让粗筛口径在代码上可读、可验证，
     * 不依赖“切句恰好丢了标点”这一隐含行为。
     */
    private static String normalizeForRough(String text) {
        return PUNCT_AND_SPACE.matcher(text).replaceAll("");
    }

    /**
     * 贪心最优对齐：
     * 构建 n×m 句子相似度矩阵，按分数降序选择，每个句子最多匹配一次；
     * 加权 Dice 归一化，缓解句数不对齐。
     */
    private AlignmentResult optimalAlign(List<String> target, List<String> candidate) {
        int n = target.size();
        int m = candidate.size();
        double[][] scores = new double[n][m];
        List<Cell> cells = new ArrayList<>(n * m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double s = sentenceSimilarity.calculate(target.get(i), candidate.get(j));
                scores[i][j] = s;
                if (s > 0D) {
                    cells.add(new Cell(i, j, s));
                }
            }
        }
        // 分数降序；同分按行列升序，保证结果确定
        cells.sort(Comparator
                .comparingDouble(Cell::getScore).reversed()
                .thenComparingInt(Cell::getRow)
                .thenComparingInt(Cell::getCol));
        double[] wTarget = computeWeights(n);
        double[] wCandidate = computeWeights(m);
        boolean[] usedTarget = new boolean[n];
        boolean[] usedCandidate = new boolean[m];
        // candidateToTarget[j] = i + 1 表示候选第 j 句匹配到目标第 i 句；0 表示不参与结构分
        int[] candidateToTarget = new int[m];
        double weightedSum = 0D;
        for (Cell cell : cells) {
            if (usedTarget[cell.getRow()] || usedCandidate[cell.getCol()]) {
                continue;
            }
            usedTarget[cell.getRow()] = true;
            usedCandidate[cell.getCol()] = true;
            weightedSum += cell.getScore()
                    * wTarget[cell.getRow()]
                    * wCandidate[cell.getCol()];
            if (cell.getScore() >= STRUCT_MATCH_THRESHOLD) {
                candidateToTarget[cell.getCol()] = cell.getRow() + 1;
            }
        }
        double denominator = sum(wTarget) + sum(wCandidate);
        double weightedDice = denominator == 0D ? 0D : 2D * weightedSum / denominator;
        return new AlignmentResult(weightedDice, candidateToTarget);
    }

    /**
     * 句子权重：首末句略高、中间句略低，归一化后均值保持 1。
     * 无权重时退化为等权，加权 Dice 与标准 Dice 一致。
     */
    private double[] computeWeights(int n) {
        double[] weights = new double[n];
        if (n == 0) {
            return weights;
        }
        if (n == 1) {
            weights[0] = 1D;
            return weights;
        }
        double sum = 0D;
        for (int i = 0; i < n; i++) {
            double pos = (double) i / (n - 1);           // 0 ~ 1
            double edge = 1D - Math.abs(2D * pos - 1D);  // 两端 1，中间 0
            weights[i] = 1D + EDGE_BOOST * edge;
            sum += weights[i];
        }
        // 缩放到均值 1、总和 n
        double scale = n / sum;
        for (int i = 0; i < n; i++) {
            weights[i] *= scale;
        }
        return weights;
    }

    /**
     * 结构分：候选句匹配到的目标句位置序列，统计相邻逆序并归一化。
     * 与句子级词序相似度同构，只是把“词位置”换成“句位置”。
     */
    private double calculateOrderSimilarity(int[] candidateToTarget) {
        int previous = 0;
        int reverseOrderCount = 0;
        int matchedSize = 0;
        for (int firstIndex : candidateToTarget) {
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
        return 1D - (double) reverseOrderCount / (matchedSize - 1);
    }

    private static double sum(double[] values) {
        double s = 0D;
        for (double v : values) {
            s += v;
        }
        return s;
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

    private static final class AlignmentResult {
        private final double weightedDice;
        private final int[] candidateToTarget;

        private AlignmentResult(double weightedDice, int[] candidateToTarget) {
            this.weightedDice = weightedDice;
            this.candidateToTarget = candidateToTarget;
        }
    }

}
