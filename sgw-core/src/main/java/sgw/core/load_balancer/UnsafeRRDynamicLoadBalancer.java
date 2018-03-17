package sgw.core.load_balancer;

import org.apache.http.annotation.NotThreadSafe;

import java.util.*;

@NotThreadSafe
public class UnsafeRRDynamicLoadBalancer<T> implements DynamicLoadBalancer<T> {

    private List<T> itemList = new ArrayList<>();
    private Map<T, Integer> itemMap = new HashMap<>();
    private int count;

    public UnsafeRRDynamicLoadBalancer() {
        count = 0;
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public T next() {
        if (itemList.size() == 0)
            return null;
        if (count == itemList.size()) count = 0;
        return itemList.get(count ++);
    }

    @Override
    public int add(T item) {
        itemList.add(item);
        itemMap.put(item, itemList.size() - 1);
        return itemList.size();
    }

    @Override
    public int remove(T item) {
        int index = itemMap.remove(item);
        T endItem = itemList.get(itemList.size() - 1);
        itemList.set(index, endItem);
        itemList.remove(itemList.size() - 1);
        itemMap.put(endItem, index);
        return itemList.size();
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayList<>(itemList).iterator();
    }
}
