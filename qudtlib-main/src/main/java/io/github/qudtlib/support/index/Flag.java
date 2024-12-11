package io.github.qudtlib.support.index;

public enum Flag {
    NOTHING(0),
    CASE_INSENSITIVE(1),
    MATCH_PREFIX(2);

    private int bits;

    Flag(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    public static class FlagCombination {
        private int bits = 0;

        public int getBits() {
            return bits;
        }

        public FlagCombination matchPrefix(boolean chosen) {
            bits = combineIfChosen(chosen, MATCH_PREFIX);
            return this;
        }

        public FlagCombination caseInsensitive(boolean chosen) {
            bits = combineIfChosen(chosen, CASE_INSENSITIVE);
            return this;
        }

        private int combineIfChosen(boolean choose, Flag caseInsensitive) {
            return choose ? bits | caseInsensitive.getBits() : bits;
        }
    }

    public static FlagCombination caseInsensitive(boolean choose) {
        return combination().caseInsensitive(choose);
    }

    public static FlagCombination matchPrefix(boolean choose) {
        return combination().matchPrefix(choose);
    }

    public static FlagCombination combination() {
        return new FlagCombination();
    }

    public static int combine(Flag... flags) {
        int result = 0;
        for (Flag flag : flags) {
            result = result | flag.bits;
        }
        return result;
    }

    public static boolean isSet(int bits, Flag flag) {
        return (bits & flag.bits) == flag.bits;
    }

    public static boolean isCaseInsensitive(int bits) {
        return isSet(bits, CASE_INSENSITIVE);
    }

    public static boolean isMatchPrefix(int bits) {
        return isSet(bits, MATCH_PREFIX);
    }
}
