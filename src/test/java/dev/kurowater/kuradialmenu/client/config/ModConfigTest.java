package dev.kurowater.kuradialmenu.client.config;

import java.util.Arrays;
import java.util.List;

/** Dependency-free regression checks; run with java -ea after compiling. */
public final class ModConfigTest {
    public static void main(String[] args) {
        ModConfig config = new ModConfig();
        config.slotCount = Integer.MAX_VALUE;
        config.slotActions = null;
        config.innerRadius = Integer.MAX_VALUE;
        config.outerRadius = Integer.MIN_VALUE;
        config.centerDeadzone = Integer.MAX_VALUE;
        config.validate();
        check(config.slotCount == 8 && config.slotActions.size() == 8, "invalid count/null list");
        check(config.slotActions.get(0).equals("key.inventory"), "null list restores defaults");
        check(config.outerRadius > config.innerRadius, "outer radius must exceed inner");
        check(config.centerDeadzone <= config.innerRadius, "deadzone must fit center hole");
        check(config.outerRadius <= 300, "outer radius upper limit");

        config.slotCount = 4;
        config.slotActions = Arrays.asList(null, " key.jump ", "", "missing.mod.action", "key.drop");
        config.validate();
        check(config.slotActions.equals(List.of("", "key.jump", "", "missing.mod.action")),
                "null sanitized, whitespace trimmed, unknown action preserved, excess truncated");
        config.slotCount = 12;
        config.adjustSlotActionsSize();
        check(config.slotActions.size() == 12 && config.slotActions.get(11).isEmpty(), "grow with empty slots");
        config.slotActions = null;
        config.resetToDefault();
        check(config.slotCount == 8 && config.slotActions.size() == 8, "reset works after null assignment");
        config.innerRadius = 10;
        config.centerDeadzone = 50;
        config.validate();
        check(config.centerDeadzone == 10, "visible ring not swallowed by deadzone");
        System.out.println("ModConfigTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
