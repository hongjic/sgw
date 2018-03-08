package sgw.core.filters;

import sgw.core.filters.post_routing.DateHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * traversal operations like `getfiltersByType` are thread-safe and no synchronization
 */
public enum FilterMngr {

    Instance;

    private static final String PRE = "pre";
    private static final String POST = "post";

    private CopyOnWriteArrayList<AbstractFilter>[] filters = new CopyOnWriteArrayList[2];

    FilterMngr() {
        filters[0] = new CopyOnWriteArrayList<>();
        filters[1] = new CopyOnWriteArrayList<>();
        addFilters(new DateHeader());
    }

    public void addFilters(AbstractFilter... filters) {
        for (AbstractFilter filter: filters) {
            int index = index(filter.filterType());
            this.filters[index].add(filter);
        }
    }

    // return a list of filters sorted by `order()`
    public List<AbstractFilter> getFiltersByType(String filterType) {
        int index = index(filterType);
        if (index < 0)
            return new ArrayList<>();
        return filters[index];
    }

    // return all filters summary as a string
    // for client use.
    public String summary() {
        StringBuilder builder = new StringBuilder();
        builder.append("Pre Filters:\n");
        for (AbstractFilter filter: filters[index(PRE)]) {
            builder.append("\tname: "+ filter.getClass().getName());
            builder.append("\torder: " + filter.filterOrder());
            builder.append("\n");
        }
        builder.append("Post Filters: \n");
        for (AbstractFilter filter: filters[index(POST)]) {
            builder.append("\tname: "+ filter.getClass().getName());
            builder.append("\torder: " + filter.filterOrder());
            builder.append("\n");
        }

        return builder.toString();
    }

    private int index(String filterType) {
        switch (filterType) {
            case PRE:
                return 0;
            case POST:
                return 1;
            default:
                return -1;
        }
    }
}
