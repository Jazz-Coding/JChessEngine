package com.jazz.engine.data.tt;

public enum TTEntryType {
    EXACT, // alpha < score < beta - the score fell in the bounds with no early cut-offs
    UPPER_BOUND, // beta > score - the score resulted in a beta cut-off (i.e. it was better than our opponent's maximum assured option)
    LOWER_BOUND // alpha > score - the score was worse than our minimum assured option at the time
}
