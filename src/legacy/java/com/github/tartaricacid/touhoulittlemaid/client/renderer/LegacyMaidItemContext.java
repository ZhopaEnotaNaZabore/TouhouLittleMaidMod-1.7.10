package com.github.tartaricacid.touhoulittlemaid.client.renderer;

/** Render-scoped hand context absent from Forge 1.7 IItemRenderer. Restored for nested renders. */
public final class LegacyMaidItemContext implements AutoCloseable {
    private static final ThreadLocal<Boolean> LEFT = new ThreadLocal<Boolean>();
    private final Boolean previous;
    private LegacyMaidItemContext(boolean left) { previous = LEFT.get(); LEFT.set(left); }
    public static LegacyMaidItemContext enter(boolean left) { return new LegacyMaidItemContext(left); }
    public static boolean isLeft() { return Boolean.TRUE.equals(LEFT.get()); }
    public void close() { if (previous == null) LEFT.remove(); else LEFT.set(previous); }
}
