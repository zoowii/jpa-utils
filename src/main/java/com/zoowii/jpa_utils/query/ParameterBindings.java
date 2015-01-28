package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: 这个类要改成immutable的,因为可能有设置了某个parameter-binding但是最后没有用到全部的情况
 */
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

    public void applyToPrepareStatement(PreparedStatement pstm) {
        try {
            for (int i = 0; i < getIndexBindings().size(); ++i) {
                pstm.setObject(i + 1, getIndexBindings().get(i));
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    public Object[] getIndexParametersArray() {
        return getIndexBindings().toArray();
    }
}
