package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class VehicleUserData implements Iterable<Map.Entry<String, String>> {

    private final Map<String, String> codeValuePairs;

    public VehicleUserData() {
        this.codeValuePairs = Maps.newHashMap();
    }

    public boolean put(String code, String value) {
        Preconditions.checkArgument(code != null && !code.isEmpty(), "invalid key for userData map.");
        Preconditions.checkArgument(value != null && !value.isEmpty(), "invalid value for userData map.");
        return codeValuePairs.put(code, value) == null;
    }

    public boolean contains(String code) {
        return codeValuePairs.containsKey(code);
    }

    public String get(String code) {
        return codeValuePairs.get(code);
    }

    public Map<String, String> getCodeValuePairs() {
        return Collections.unmodifiableMap(codeValuePairs);
    }

    @Override
    public String toString() {
        return "VehicleUserData size=" + codeValuePairs.size() + " [codeValuePairs=" + codeValuePairs + "]";
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return getCodeValuePairs().entrySet().iterator();
    }

    public String getString(String separator) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : this) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(separator);
        }
        return sb.toString();
    }

}
