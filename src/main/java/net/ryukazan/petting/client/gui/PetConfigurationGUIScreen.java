package net.ryukazan.petting.client.gui;

import org.checkerframework.checker.units.qual.s;

import net.ryukazan.petting.world.inventory.PetConfigurationGUIMenu;
import net.ryukazan.petting.init.PettingModScreens;

import net.minecraftforge.client.gui.widget.ForgeSlider;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

public class PetConfigurationGUIScreen extends AbstractContainerScreen<PetConfigurationGUIMenu> implements PettingModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private ForgeSlider s;
	private ForgeSlider walkvalue;

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

	private static final ResourceLocation texture = new ResourceLocation("petting:textures/screens/pet_configuration_gui.png");

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(ms);
		super.render(ms, mouseX, mouseY, partialTicks);
		this.renderTooltip(ms, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, texture);
		this.blit(ms, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/entity_background.png"));
		this.blit(ms, this.leftPos + 162, this.topPos + 7, 0, 0, 151, 186, 151, 186);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 7, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 34, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 7, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 34, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 7, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 34, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/slot.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 34, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 7, this.topPos + 84, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 34, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 7, this.topPos + 52, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 115, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 88, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.setShaderTexture(0, new ResourceLocation("petting:textures/screens/forbidden.png"));
		this.blit(ms, this.leftPos + 61, this.topPos + 21, 0, 0, 26, 26, 26, 26);
		RenderSystem.disableBlend();
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
	protected void renderLabels(PoseStack ms, int mouseX, int mouseY) {
		this.font.draw(ms, Component.translatable("gui.petting.pet_configuration_gui.label_petnames_settings"), 8, 6, -12829636);
		this.font.draw(ms, Component.translatable("gui.petting.pet_configuration_gui.label_follow_distance"), 7, 120, -12829636);
		this.font.draw(ms, Component.translatable("gui.petting.pet_configuration_gui.label_teleport_distance"), 7, 156, -12829636);
	}

	@Override
	public void init() {
		super.init();
		s = new ForgeSlider(this.leftPos + 7, this.topPos + 169, 140, 20, Component.translatable("gui.petting.pet_configuration_gui.s_prefix"), Component.translatable("gui.petting.pet_configuration_gui.s_suffix"), 0, 200, 20, 1, 0, true) {
			@Override
			protected void applyValue() {
				if (!menuStateUpdateActive)
					menu.sendMenuStateUpdate(entity, 2, "s", this.getValue(), false);
			}
		};
		this.addRenderableWidget(s);
		if (!menuStateUpdateActive)
			menu.sendMenuStateUpdate(entity, 2, "s", s.getValue(), false);
		walkvalue = new ForgeSlider(this.leftPos + 7, this.topPos + 133, 140, 20, Component.translatable("gui.petting.pet_configuration_gui.walkvalue_prefix"), Component.translatable("gui.petting.pet_configuration_gui.walkvalue_suffix"), 0, 200, 10,
				1, 0, true) {
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