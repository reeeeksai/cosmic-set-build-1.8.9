package com.example.examplemod;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantSet {
    public String id;
    public String name;
    public long updatedAt;
    public Map<String, List<String>> desired; // Map by SlotType.name()

    public EnchantSet() {
        desired = new HashMap<String, List<String>>();
        // ensure all slots exist
        for (SlotType s : SlotType.values()) {
            desired.put(s.name(), new ArrayList<String>());
        }
    }

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }
}
