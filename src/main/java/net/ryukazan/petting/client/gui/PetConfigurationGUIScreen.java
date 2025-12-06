package net.ryukazan.petting.client.gui;

import org.checkerframework.checker.units.qual.s;

import net.ryukazan.petting.world.inventory.PetConfigurationGUIMenu;
import net.ryukazan.petting.procedures.PetEntityReturnProcedure;
import net.ryukazan.petting.init.PettingModScreens;

import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

public class PetConfigurationGUIScreen extends AbstractContainerScreen<PetConfigurationGUIMenu> implements PettingModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private ExtendedSlider s;
	private ExtendedSlider walkvalue;

	public PetConfigurationGUIScreen(PetConfigurationGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 322;
		this.imageHeight = 202;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		if (elementType == 2 && elementState instanceof Number n) {
			if (name.equals("s"))
				s.setValue(n.doubleValue());
			else if (name.equals("walkvalue"))
				walkvalue.setValue(n.doubleValue());
		}
		menuStateUpdateActive = false;
	}

	private static final ResourceLocation texture = ResourceLocation.parse("petting:textures/screens/pet_configuration_gui.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (PetEntityReturnProcedure.execute() instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + -760, this.topPos + -816, this.leftPos + 1240, this.topPos + 1184, 30, -livingEntity.getBbHeight() / (2.0f * livingEntity.getScale()),
					0f + (float) Math.atan((this.leftPos + 240 - mouseX) / 40.0), (float) Math.atan((this.topPos + 135 - mouseY) / 40.0), livingEntity);
		}
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/entity_background.png"), this.leftPos + 162, this.topPos + 7, 0, 0, 151, 186, 151, 186);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 7, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 34, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 61, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 88, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 115, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 7, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 34, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 61, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 88, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 115, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 7, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 34, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 61, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 88, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/slot.png"), this.leftPos + 115, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 115, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 88, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 61, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 34, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 7, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 115, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 88, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 61, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 34, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 7, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 115, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 88, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("petting:textures/screens/forbidden.png"), this.leftPos + 61, this.topPos + 21, 0, 0, 26, 26, 26, 26);
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return (this.getFocused() != null && this.isDragging() && button == 0) ? this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY) : super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.petting.pet_configuration_gui.label_petnames_settings"), 8, 6, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.petting.pet_configuration_gui.label_follow_distance"), 7, 120, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.petting.pet_configuration_gui.label_teleport_distance"), 7, 156, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		s = new ExtendedSlider(this.leftPos + 7, this.topPos + 169, 140, 20, Component.translatable("gui.petting.pet_configuration_gui.s_prefix"), Component.translatable("gui.petting.pet_configuration_gui.s_suffix"), 0, 200, 20, 1, 0, true) {
			@Override
			protected void applyValue() {
				if (!menuStateUpdateActive)
					menu.sendMenuStateUpdate(entity, 2, "s", this.getValue(), false);
			}
		};
		this.addRenderableWidget(s);
		if (!menuStateUpdateActive)
			menu.sendMenuStateUpdate(entity, 2, "s", s.getValue(), false);
		walkvalue = new ExtendedSlider(this.leftPos + 7, this.topPos + 133, 140, 20, Component.translatable("gui.petting.pet_configuration_gui.walkvalue_prefix"), Component.translatable("gui.petting.pet_configuration_gui.walkvalue_suffix"), 0, 200,
				10, 1, 0, true) {
			@Override
			protected void applyValue() {
				if (!menuStateUpdateActive)
					menu.sendMenuStateUpdate(entity, 2, "walkvalue", this.getValue(), false);
			}
		};
		this.addRenderableWidget(walkvalue);
		if (!menuStateUpdateActive)
			menu.sendMenuStateUpdate(entity, 2, "walkvalue", walkvalue.getValue(), false);
	}
}