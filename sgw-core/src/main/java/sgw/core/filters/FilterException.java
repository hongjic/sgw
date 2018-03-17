package sgw.core.filters;

import sgw.core.util.FastMessage;

public class FilterException extends Exception {

    private final FastMessage fastM;

    public FilterException(FastMessage fastM) {
        this.fastM = fastM;
    }

    public FastMessage getFastMessage() {
        return fastM;
    }
}