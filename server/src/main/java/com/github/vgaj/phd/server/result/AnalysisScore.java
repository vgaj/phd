package com.github.vgaj.phd.server.result;

import lombok.Getter;

/**
 * Calculate a store of interest for an Analysis Result
 */
public class AnalysisScore
{
    @Getter
    private int score = 0;
    public AnalysisScore( AnalysisResult result)
    {
        if (result.areAllIntervalsTheSame_c11())
        {
            score += 3;
        }
        if (result.areSomeTransfersTheSameSize_c22())
        {
            score += 2;
        }
        if (result.areAllIntervalsTheSame_c11() && result.areSomeTransfersTheSameSize_c22())
        {
            // in addition to the above
            score += 2;
        }

        if (result.areSomeIntervalsTheSame_c12())
        {
            score += 1;
        }
        if (result.areSomeIntervalsTheSame_c12())
        {
            score += 1;
        }
        if (result.areSomeIntervalsTheSame_c12() && result.areSomeIntervalsTheSame_c12())
        {
            // in addition to the above
            score += 1;
        }

    }

    public String toString()
    {
        return String.format("%d",score);
    }
}
