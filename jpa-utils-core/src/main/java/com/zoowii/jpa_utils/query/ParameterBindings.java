package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.NamedParameterStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:  change this class to immutable
 */
public class ParameterBindings {
    private List<Object> indexBindings = new ArrayList<Object>();
    private Map<String, Object> mapBindings = new HashMap<String, Object>();

    public ParameterBindings() {
    }

    public ParameterBindings(Object... params) {
        for(Object param : params) {
            this.addIndexBinding(param);
        }
    }

    public List<Object> getIndexBindings() {
        return indexBindings;
    }

    public ParameterBindings addIndexBinding(Object value) {
        this.indexBindings.add(value);
        return this;
    }

    public ParameterBindings addBinding(String key, Object value) {
        this.mapBindings.put(key, value);
        return this;
    }

    public Map<String, Object> getMapBindings() {
        return mapBindings;
    }

    public ParameterBindings extend(ParameterBindings other) {
        ParameterBindings result = addAll(other);
        this.indexBindings = result.indexBindings;
        this.mapBindings = result.mapBindings;
        return this;
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

    public void applyToNamedPrepareStatement(NamedParameterStatement namedParameterStatement) {
        try {
            for (String key : getMapBindings().keySet()) {
                namedParameterStatement.setObject(key, getMapBindings().get(key));
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    public Object[] getIndexParametersArray() {
        return getIndexBindings().toArray();
    }
}
