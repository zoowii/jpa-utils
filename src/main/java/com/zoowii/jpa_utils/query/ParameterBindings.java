package com.zoowii.jpa_utils.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterBindings {
    private List<Object> indexBindings = new ArrayList<Object>();
    private Map<String, Object> mapBindings = new HashMap<String, Object>();

    public List<Object> getIndexBindings() {
        return indexBindings;
    }

    public void addIndexBinding(Object value) {
        this.indexBindings.add(value);
    }

    public void addBinding(String key, Object value) {
        this.mapBindings.put(key, value);
    }

    public Map<String, Object> getMapBindings() {
        return mapBindings;
    }

    public ParameterBindings addAll(ParameterBindings other) {
        if (other == null) {
            return this;
        }
        ParameterBindings result = new ParameterBindings();
        result.indexBindings.addAll(this.indexBindings);
        result.indexBindings.addAll(other.indexBindings);
        result.mapBindings.putAll(this.mapBindings);
        result.mapBindings.putAll(other.mapBindings);
        return result;
    }
}
