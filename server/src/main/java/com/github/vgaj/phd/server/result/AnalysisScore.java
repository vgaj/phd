/*
MIT License

Copyright (c) 2022-2024 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.vgaj.phd.server.result;

import lombok.Getter;

/**
 * Calculate a store of interest for an Analysis Result
 */
public class AnalysisScore
{
    @Getter
    private int score = 0;
    public AnalysisScore( ResultCategorisation resultCategorisation)
    {
        if (resultCategorisation.areAllIntervalsTheSame_c11())
        {
            score += 5;
        }
        else if (resultCategorisation.areSomeIntervalsTheSame_c12())
        {
            score += 2;
        }

        if (resultCategorisation.areAllTransfersTheSameSize_c21())
        {
            score += 5;
        }
        else if (resultCategorisation.areSomeTransfersTheSameSize_c22())
        {
            score += 2;
        }

        if (resultCategorisation.isRuntimeLongEnoughToDecideIfResultIsCurrent() && !resultCategorisation.isResultCurrent())
        {
            score = score / 2;
        }
    }

    public String toString()
    {
        return String.format("%d",score);
    }
}
