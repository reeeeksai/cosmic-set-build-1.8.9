package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;

public class SetsStore {
    public String activeSetId;
    public List<EnchantSet> sets;

    public SetsStore() {
        sets = new ArrayList<EnchantSet>();
    }
}
