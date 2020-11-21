package net.dumbcode.projectnublar.client.gui.tablet.screens;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyDinoScreen extends TabletPage {

    private static final int HALF_PIPE_WIDTH = 25;
    private static final int TICKS_BETWEEN_PIPES = 30;
    private static final int PIPE_PIXELS_PER_TICK = 5;
    private static final int HALF_PIPE_OPENING_WIDTH = 50;

    private static final int PLAYER_JUMP_STRENGTH = 10;
    private static final int PLAYER_X_COORD = 100;
    private static final int HALF_PLAYER_WIDTH = 10;
    private static final int GRAVITY = 3; //pixels per tick squared

    private static final Random RAND = new Random();

    private int tickTime;
    private int counter;

    private final List<Pipe> pipes = new ArrayList<>();
    private final Player player = new Player();

    @Override
    public void render(int mouseX, int mouseY, float partialTicks, String route) {
        for (Pipe pipe : this.pipes) {
            int xPos = (int) (pipe.xPosition - PIPE_PIXELS_PER_TICK * partialTicks);

            Gui.drawRect(xPos - HALF_PIPE_WIDTH, 0, xPos + HALF_PIPE_WIDTH, this.ySize - pipe.heightOpening - HALF_PIPE_OPENING_WIDTH, 0xFF000000);
            Gui.drawRect(xPos - HALF_PIPE_WIDTH, this.ySize - pipe.heightOpening + HALF_PIPE_OPENING_WIDTH, xPos + HALF_PIPE_WIDTH, this.ySize, 0xFF000000);
        }

        int playerY = (int) (this.player.prevYPosition + (this.player.yPosition - this.player.prevYPosition) * partialTicks);
        Gui.drawRect(PLAYER_X_COORD - HALF_PLAYER_WIDTH, this.ySize - playerY - HALF_PLAYER_WIDTH, PLAYER_X_COORD + HALF_PLAYER_WIDTH, this.ySize - playerY + HALF_PLAYER_WIDTH, 0xFF00FF00);

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.xSize / 2F, 20, 0);
        GlStateManager.scale(3F, 3F, 3F);
        String text = String.valueOf(this.counter);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, -Minecraft.getMinecraft().fontRenderer.getStringWidth(text) / 2F, 0, 0xBCBCBC);
        GlStateManager.popMatrix();
    }

    @Override
    public void updateScreen() {
        this.updatePipes();
        this.updatePlayer();
    }

    private void updatePipes() {
        if(this.tickTime++ % TICKS_BETWEEN_PIPES == 0) {
            this.pipes.add(new Pipe(RAND.nextInt(this.ySize - 100) + 50));
        }
        this.pipes.removeIf(pipe -> {
            if(!pipe.counted && pipe.xPosition + HALF_PIPE_WIDTH < PLAYER_X_COORD - HALF_PLAYER_WIDTH) {
                this.counter++;
                pipe.counted = true;
            }
            return (pipe.xPosition -= PIPE_PIXELS_PER_TICK) < -HALF_PIPE_WIDTH;
        });

    }

    private void updatePlayer() {
        this.player.prevYPosition = this.player.yPosition;

        this.player.yPosition += this.player.yVelocity;
        this.player.yVelocity -= GRAVITY;

        this.checkDead();
    }

    private void checkDead() {
        boolean dead = false;
        for (Pipe pipe : this.pipes) {
            if(Math.abs(PLAYER_X_COORD - pipe.xPosition) < HALF_PLAYER_WIDTH + HALF_PIPE_WIDTH && Math.abs(this.player.prevYPosition - pipe.heightOpening) > HALF_PIPE_OPENING_WIDTH - HALF_PLAYER_WIDTH) {
                dead = true;
                break;
            }
        }

        dead |= this.player.prevYPosition < HALF_PLAYER_WIDTH || this.player.prevYPosition > this.ySize - HALF_PLAYER_WIDTH;

        if(dead) {
            this.player.yPosition = this.player.prevYPosition = 150;
            this.player.yVelocity = Math.max(this.player.yVelocity, 0);
            this.counter = 0;
            this.pipes.clear();
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.jump();
        super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_SPACE) {
            this.jump();
        }
        super.onKeyTyped(typedChar, keyCode);
    }

    private void jump() {
        this.player.yVelocity = PLAYER_JUMP_STRENGTH;
    }

    @RequiredArgsConstructor
    private class Pipe {
        private final int heightOpening;
        private int xPosition = xSize + HALF_PIPE_WIDTH;
        private boolean counted;
    }

    private class Player {
        int yPosition = 150;
        int prevYPosition = this.yPosition;
        int yVelocity;
    }
}
