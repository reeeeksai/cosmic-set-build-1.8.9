package com.example.examplemod;

public class MarkedSlots {
    private static MarkedSlots INSTANCE;
    private final boolean[] marked = new boolean[6];
    private final int[] lastX = new int[6];
    private final int[] lastY = new int[6];
    private final int[] lastW = new int[6];
    private final int[] lastH = new int[6];

    private MarkedSlots() {}

    public static MarkedSlots getInstance() {
        if (INSTANCE == null) INSTANCE = new MarkedSlots();
        return INSTANCE;
    }

    public synchronized void setMarked(int idx, boolean v) {
        if (idx < 0 || idx >= marked.length) return;
        marked[idx] = v;
    }

    public synchronized void setMarkedAt(int idx, boolean v, int x, int y, int w, int h) {
        if (idx < 0 || idx >= marked.length) return;
        marked[idx] = v;
        if (v) {
            lastX[idx] = x;
            lastY[idx] = y;
            lastW[idx] = w;
            lastH[idx] = h;
        } else {
            lastX[idx] = lastY[idx] = lastW[idx] = lastH[idx] = 0;
        }
    }

    public synchronized boolean hasLastRect(int idx) {
        if (idx < 0 || idx >= marked.length) return false;
        return lastW[idx] > 0 && lastH[idx] > 0;
    }

    public synchronized int getLastX(int idx) { return lastX[idx]; }
    public synchronized int getLastY(int idx) { return lastY[idx]; }
    public synchronized int getLastW(int idx) { return lastW[idx]; }
    public synchronized int getLastH(int idx) { return lastH[idx]; }

    public synchronized boolean isMarked(int idx) {
        if (idx < 0 || idx >= marked.length) return false;
        return marked[idx];
    }

    public synchronized void toggle(int idx) {
        if (idx < 0 || idx >= marked.length) return;
        marked[idx] = !marked[idx];
    }
}
