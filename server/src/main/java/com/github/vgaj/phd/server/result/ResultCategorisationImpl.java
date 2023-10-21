package com.github.vgaj.phd.server.result;

public class ResultCategorisationImpl implements ResultCategorisation
{
    private AnalysisResult result;
    public ResultCategorisationImpl(AnalysisResult result)
    {
        this.result = result;
    }

    @Override
    public boolean areAllIntervalsTheSame_c11()
    {
        return result.getRepeatedIntervals().size() == 1;
    }

    @Override
    public boolean areSomeIntervalsTheSame_c12()
    {
        return result.getRepeatedIntervals().size() > 0;
    }

    @Override
    public boolean areAllTransfersTheSameSize_c21()
    {
        return result.getRepeatedTransferSizes().size() == 0;
    }
    @Override
    public boolean areSomeTransfersTheSameSize_c22()
    {
        return result.getRepeatedTransferSizes().size() > 0;
    }

}
