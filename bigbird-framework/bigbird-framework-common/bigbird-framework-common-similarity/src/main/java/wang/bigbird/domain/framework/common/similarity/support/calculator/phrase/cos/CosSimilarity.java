package wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.cos;

import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.AbstractPhraseSimilarity;

public class CosSimilarity extends AbstractPhraseSimilarity {

    private CosSimilarity() {
    }

    private static class Holder {
        private static final CosSimilarity INSTANCE = new CosSimilarity();
    }

    public static CosSimilarity getInstance() {
        return CosSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculatePhrase(String target, String candidate) {
        return 0;
    }

}
