package dev.kurowater.kuradialmenu.client.util;

public final class MathUtilTest {
    public static void main(String[] args) {
        for (int count : new int[] {4, 8, 12}) {
            double step = 360.0 / count;
            for (int slot = 0; slot < count; slot++) {
                double angle = slot * step;
                double radians = Math.toRadians(angle);
                check(MathUtil.selectSlot(Math.sin(radians) * 100, -Math.cos(radians) * 100,
                        20, count) == slot, "sector center " + count + "/" + slot);
                check(MathUtil.getSlotIndex(angle - step / 2 + 0.0001, count) == slot,
                        "leading edge");
                check(MathUtil.getSlotIndex(angle + step / 2 - 0.0001, count) == slot,
                        "trailing edge");
                check(MathUtil.getSlotIndex(angle + 720, count) == slot, "wrapped positive");
                check(MathUtil.getSlotIndex(angle - 720, count) == slot, "wrapped negative");
            }
            check(MathUtil.selectSlot(0, 0, 20, count) == -1, "center cancels");
            check(MathUtil.selectSlot(19.999, 0, 20, count) == -1, "inside deadzone");
            check(MathUtil.selectSlot(20, 0, 20, count) == count / 4, "deadzone edge selects");
            check(MathUtil.selectSlot(Double.NaN, 0, 20, count) == -1, "invalid cursor cancels");
        }
        check(MathUtil.getSlotIndex(337.5, 8) == 0, "north wrap boundary");
        check(MathUtil.getSlotIndex(22.5, 8) == 1, "clockwise boundary");
        System.out.println("MathUtilTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
