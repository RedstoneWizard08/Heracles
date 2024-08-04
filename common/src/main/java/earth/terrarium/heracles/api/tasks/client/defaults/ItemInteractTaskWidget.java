package earth.terrarium.heracles.api.tasks.client.defaults;

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack;
import com.teamresourceful.resourcefullib.common.codecs.predicates.NbtPredicate;
import earth.terrarium.heracles.api.client.ItemDisplayWidget;
import earth.terrarium.heracles.api.client.WidgetUtils;
import earth.terrarium.heracles.api.client.theme.QuestScreenTheme;
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter;
import earth.terrarium.heracles.api.tasks.defaults.ItemInteractTask;
import earth.terrarium.heracles.common.handlers.progress.TaskProgress;
import earth.terrarium.heracles.common.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ItemInteractTaskWidget(
    ItemInteractTask task, TaskProgress<NumericTag> progress, List<ItemStack> stacks
) implements ItemDisplayWidget {

    private static final String DESC = "task.heracles.item_interaction.desc.singular";

    public ItemInteractTaskWidget(ItemInteractTask task, TaskProgress<NumericTag> progress) {
        this(
            task,
            progress,
            task.item().getValue().map(
                item -> {
                    ItemStack stack = item.getDefaultInstance();
                    if (!NbtPredicate.isEmpty(task.nbt().tag())) stack.getOrCreateTag().merge(task.nbt().tag());
                    return List.of(stack);
                },
                tag -> ModUtils.getValue(Registries.ITEM, tag).stream().map(ItemStack::new).toList()
            )
        );
    }

    @Override
    public void render(GuiGraphics graphics, ScissorBoxStack scissor, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int iconSize = 32;
        this.task.icon().renderOrStack(this.getCurrentItem(), graphics, x + 5, y + 5, iconSize, mouseX, mouseY);
        graphics.drawString(
            font,
            task.titleOr(TaskTitleFormatter.create(this.task)), x + iconSize + 16, y + 6, QuestScreenTheme.getTaskTitle(),
            false
        );
        graphics.drawString(
            font,
            Component.translatable(DESC, Component.keybind("key.use")), x + iconSize + 16, y + 8 + font.lineHeight, QuestScreenTheme.getTaskDescription(),
            false
        );
        WidgetUtils.drawProgressText(graphics, x, y, width, this.task, this.progress);
        int height = getHeight(width);
        WidgetUtils.drawProgressBar(graphics, x + iconSize + 16, y + height - font.lineHeight - 5, x + width - 5, y + height - 6, this.task, this.progress);
    }

    @Override
    public ItemStack getCurrentItem() {
        if (this.stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = Math.max(0, (int) ((System.currentTimeMillis() / 1000) % this.stacks.size()));
        return this.stacks.get(index);
    }

    @Override
    public int getHeight(int width) {
        return 42;
    }
}
