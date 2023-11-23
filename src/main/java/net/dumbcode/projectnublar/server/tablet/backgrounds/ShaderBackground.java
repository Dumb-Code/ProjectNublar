package net.dumbcode.projectnublar.server.tablet.backgrounds;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.NonNull;
import net.dumbcode.dumblibrary.client.shader.GlslSandboxShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.function.Supplier;

@Getter
public class ShaderBackground implements TabletBackground {

    public static final String KEY = "shader";
    private static Minecraft MC = Minecraft.getInstance();

    private GlslSandboxShader shader;
    private boolean needsUpdating;
    private String url = "";
    private ScreenSize size = ScreenSize.FIT_TO_GUI_LOW_RES;

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("url", this.url);
        nbt.putString("screen_size", this.size.name());
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.setUrl(nbt.getString("url"));
        if(nbt.contains("screen_size", Constants.NBT.TAG_STRING)) {
            this.size = ScreenSize.valueOf(nbt.getString("screen_size"));
        }
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeUtf(this.url);
        buf.writeByte(this.size.ordinal());
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        this.setUrl(buf.readUtf());
        this.size = ScreenSize.values()[buf.readByte() % ScreenSize.values().length];
    }

    public void setUrl(String url) {
        this.needsUpdating = !this.url.equals(url);
        this.url = url;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY) {
        if(this.needsUpdating) {
            this.needsUpdating = false;
            this.dispose();
            this.shader = GlslSandboxShader.createShader(this.url);
        }

        if(this.shader != null) {
            float modifier = this.size.modifier.get();
            int shaderWidth = (int) (width * modifier);
            int shaderHeight = (int) (height * modifier);
            if(this.shader.getScreenWidth() != shaderWidth || this.shader.getScreenHeight() != shaderHeight) {
                this.shader.init(shaderWidth, shaderHeight);
            }
            this.shader.render(mouseX - x, mouseY - y);
            this.shader.startShader();
            stack.blit(x, y, 0, 0, width, height, width, height);
            this.shader.endShader();
        }
    }

    @Override
    public void dispose() {
        if(this.shader != null) {
            this.shader.dispose();
        }
    }

    public void setSize(@NonNull ScreenSize size) {
        this.size = size;
    }

    @Getter
    public enum ScreenSize {
        FIT_TO_SCREEN(() -> 1F / MC.getWindow().calculateScale(MC.options.guiScale, MC.isEnforceUnicode())),
        FIT_TO_GUI(() -> 1F),
        FIT_TO_GUI_LOW_RES(() -> 1/2F),
        FIT_TO_GUI_LOWEST_RES(() -> 1/4F);
        private final String translationKey;
        private final Supplier<Float> modifier;

        ScreenSize(Supplier<Float> modifier) {
            this.translationKey = "projectnublar.gui.shader." + this.name().toLowerCase();
            this.modifier = modifier;
        }
    }
}
