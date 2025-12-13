package com.example.examplemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;

public class EnchantSetManager {
    private static EnchantSetManager INSTANCE;
    private final Gson gson;
    private final File setsFile;
    private SetsStore store;

    private EnchantSetManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        File cfgDir = new File(mcDir, "config/cosmic_set_builder");
        if (!cfgDir.exists()) cfgDir.mkdirs();
        setsFile = new File(cfgDir, "sets.json");
        load();
    }

    public static EnchantSetManager getInstance() {
        if (INSTANCE == null) INSTANCE = new EnchantSetManager();
        return INSTANCE;
    }

    public synchronized void load() {
        if (!setsFile.exists()) {
            createDefaultStore();
            save();
            return;
        }
        FileReader fr = null;
        try {
            fr = new FileReader(setsFile);
            store = gson.fromJson(fr, SetsStore.class);
            if (store == null) throw new IOException("Parsed store was null");
            // ensure activeSetId valid
            if (store.activeSetId == null || findSet(store.activeSetId) == null) {
                if (store.sets.size() > 0) store.activeSetId = store.sets.get(0).id;
                else {
                    createDefaultStore();
                }
            }
        } catch (Exception e) {
            // backup bad file
            try {
                File bak = new File(setsFile.getParentFile(), "sets.json.bak");
                if (bak.exists()) bak.delete();
                setsFile.renameTo(bak);
            } catch (Exception ex) {
                // ignore
            }
            createDefaultStore();
            save();
        } finally {
            if (fr != null) try { fr.close(); } catch (IOException e) { }
        }
    }

    private void createDefaultStore() {
        store = new SetsStore();
        EnchantSet def = new EnchantSet();
        def.id = "default";
        def.name = "Default";
        def.touch();
        store.sets.add(def);
        store.activeSetId = def.id;
    }

    public synchronized void save() {
        FileWriter fw = null;
        try {
            fw = new FileWriter(setsFile);
            gson.toJson(store, fw);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) try { fw.close(); } catch (IOException e) { }
        }
    }

    public synchronized EnchantSet getActiveSet() {
        if (store == null) load();
        EnchantSet s = findSet(store.activeSetId);
        if (s == null && store.sets.size() > 0) s = store.sets.get(0);
        return s;
    }

    public synchronized List<EnchantSet> getAllSets() {
        if (store == null) load();
        return new ArrayList<EnchantSet>(store.sets);
    }

    public synchronized void setActiveSet(String id) {
        if (store == null) load();
        if (findSet(id) != null) {
            store.activeSetId = id;
            save();
        }
    }

    public synchronized EnchantSet createSet(String name) {
        EnchantSet s = new EnchantSet();
        s.id = UUID.randomUUID().toString();
        s.name = name == null ? "New Set" : name;
        s.touch();
        store.sets.add(s);
        store.activeSetId = s.id;
        save();
        return s;
    }

    public synchronized boolean renameSet(String id, String newName) {
        EnchantSet s = findSet(id);
        if (s == null) return false;
        s.name = newName;
        s.touch();
        save();
        return true;
    }

    public synchronized boolean deleteSet(String id) {
        if (store == null) load();
        EnchantSet s = findSet(id);
        if (s == null) return false;
        if (store.sets.size() <= 1) return false; // never delete last
        Iterator<EnchantSet> it = store.sets.iterator();
        while (it.hasNext()) {
            EnchantSet es = it.next();
            if (es.id.equals(id)) {
                it.remove();
                break;
            }
        }
        if (store.activeSetId.equals(id)) {
            store.activeSetId = store.sets.get(0).id;
        }
        save();
        return true;
    }

    private EnchantSet findSet(String id) {
        if (store == null) load();
        if (id == null) return null;
        for (EnchantSet s : store.sets) {
            if (id.equals(s.id)) return s;
        }
        return null;
    }
}
