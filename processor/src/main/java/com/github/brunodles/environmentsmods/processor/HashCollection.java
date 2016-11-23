package com.github.brunodles.environmentsmods.processor;

import java.util.*;

/**
 * Created by bruno on 23/11/16.
 */
public class HashCollection<KEY, VALUE> {
    private HashMap<KEY, Collection<VALUE>> map = new HashMap<>();

    public void add(KEY key, VALUE value) {
        Collection<VALUE> values = add(key);
        values.add(value);
    }

    public Collection<VALUE> add(KEY key) {
        Collection<VALUE> values = map.get(key);
        if (values == null) {
            values = new ArrayList<>();
            map.put(key, values);
        }
        return values;
    }

    public Set<KEY> keySet() {
        return map.keySet();
    }

    public Collection<VALUE> getValues(KEY key) {
        return Collections.unmodifiableCollection(map.get(key));
    }
}
