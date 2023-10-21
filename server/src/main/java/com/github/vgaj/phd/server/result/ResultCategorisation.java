package com.github.vgaj.phd.server.result;

public interface ResultCategorisation
{
    boolean areAllIntervalsTheSame_c11();
    boolean areSomeIntervalsTheSame_c12();
    boolean areAllTransfersTheSameSize_c21();
    boolean areSomeTransfersTheSameSize_c22();
}
