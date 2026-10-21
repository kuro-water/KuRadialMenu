package dev.kurowater.kuradialmenu.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** A paged, searchable picker. It only changes the caller's pending YACL value. */
public final class KeybindPickerScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private final Screen parent;
    private final String selectedId;
    private final Consumer<String> selection;
    private final List<Entry> entries = new ArrayList<>();
    private final List<ButtonWidget> rows = new ArrayList<>();
    private List<Entry> filtered = List.of();
    private TextFieldWidget search;
    private ButtonWidget previous;
    private ButtonWidget next;
    private String query = "";
    private int page;
    private int contentWidth;

    public KeybindPickerScreen(Screen parent, String selectedId, Consumer<String> selection) {
        super(Text.translatable("config.kuradialmenu.picker.title"));
        this.parent = parent;
        this.selectedId = selectedId;
        this.selection = selection;
        entries.add(new Entry("", Text.translatable("config.kuradialmenu.slot.none").getString(), ""));
        MinecraftClient minecraft = MinecraftClient.getInstance();
        for (KeyBinding binding : minecraft.options.allKeys) {
            String id = binding.getTranslationKey();
            // Reopening the radial menu is not a useful action and can create input loops.
            if (!id.equals("key.kuradialmenu.open_menu")) {
                entries.add(new Entry(id, displayName(id).getString(), displayName(binding.getCategory()).getString()));
            }
        }
        entries.subList(1, entries.size()).sort(Comparator.comparing(Entry::label).thenComparing(Entry::id));
    }

    /** Resource packs and older versions may lack a translation for a registered binding. */
    public static Text displayName(String id) {
        if (id == null || id.isEmpty()) {
            return Text.translatable("config.kuradialmenu.slot.none");
        }
        if (I18n.hasTranslation(id)) {
            return Text.translatable(id);
        }
        // Keep the stable ID visible instead of guessing the action from the physical key.
        return Text.literal(id);
    }

    @Override
    protected void init() {
        contentWidth = Math.min(420, width - 24);
        int left = (width - contentWidth) / 2;
        search = new TextFieldWidget(textRenderer, left, 32, contentWidth, 20,
                Text.translatable("config.kuradialmenu.picker.search"));
        search.setMaxLength(128);
        search.setPlaceholder(Text.translatable("config.kuradialmenu.picker.search"));
        search.setText(query);
        addDrawableChild(search);
        rows.clear();
        int rowCount = Math.max(1, (height - 122) / ROW_HEIGHT);
        for (int i = 0; i < rowCount; i++) {
            final int row = i;
            rows.add(addDrawableChild(ButtonWidget.builder(Text.empty(), button -> choose(row))
                    .dimensions(left, 66 + i * ROW_HEIGHT, contentWidth, 20).build()));
        }
        previous = addDrawableChild(ButtonWidget.builder(Text.translatable("config.kuradialmenu.picker.previous"), button -> {
            page--;
            updateRows();
        }).dimensions(left, height - 50, contentWidth / 2 - 4, 20).build());
        next = addDrawableChild(ButtonWidget.builder(Text.translatable("config.kuradialmenu.picker.next"), button -> {
            page++;
            updateRows();
        }).dimensions(left + contentWidth / 2 + 4, height - 50, contentWidth / 2 - 4, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
                .dimensions(width / 2 - 75, height - 26, 150, 20).build());
        search.setChangedListener(value -> {
            query = value;
            page = 0;
            filter();
        });
        filter();
        setInitialFocus(search);
    }

    private void filter() {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        filtered = entries.stream().filter(entry -> entry.matches(needle)).toList();
        updateRows();
    }

    // TextFieldWidget needs ticking for its caret animation before 1.21.
    //? if <1.21 {
    /*@Override
    public void tick() {
        super.tick();
        search.tick();
    }
    *///?}

    private void updateRows() {
        page = Math.max(0, Math.min(page, Math.max(0, (filtered.size() - 1) / rows.size())));
        for (int i = 0; i < rows.size(); i++) {
            ButtonWidget button = rows.get(i);
            int index = page * rows.size() + i;
            button.visible = index < filtered.size();
            if (button.visible) {
                Entry entry = filtered.get(index);
                String label = (entry.id.equals(selectedId) ? "✓ " : "") + entry.label;
                if (!entry.category.isEmpty()) {
                    label += " — " + entry.category;
                }
                button.setMessage(Text.literal(textRenderer.trimToWidth(label, contentWidth - 16)));
                button.setTooltip(Tooltip.of(Text.literal(entry.label + "\n" + entry.id + "\n" + entry.category)));
            }
        }
        previous.active = page > 0;
        next.active = (page + 1) * rows.size() < filtered.size();
    }

    private void choose(int row) {
        int index = page * rows.size() + row;
        if (index < filtered.size()) {
            selection.accept(filtered.get(index).id);
            close();
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    //? if >=1.20 {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, resultText(), width / 2, 55, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fill(matrices, 0, 0, width, height, 0xE0101010);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 12, 0xFFFFFF);
        drawCenteredTextWithShadow(matrices, textRenderer, resultText(), width / 2, 55, 0xAAAAAA);
        super.render(matrices, mouseX, mouseY, delta);
    }
    *///?}

    private Text resultText() {
        return Text.translatable("config.kuradialmenu.picker.results", filtered.size(), page + 1,
                Math.max(1, (filtered.size() + rows.size() - 1) / rows.size()));
    }

    private record Entry(String id, String label, String category) {
        boolean matches(String query) {
            return id.toLowerCase(Locale.ROOT).contains(query)
                    || label.toLowerCase(Locale.ROOT).contains(query)
                    || category.toLowerCase(Locale.ROOT).contains(query);
        }
    }
}
