package net.dumbcode.projectnublar.server.tablet;

import lombok.Value;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class TabletBGImageHandler {

    public static final float SCREEN_RATIO = 16F/9F;

    public static void addNewEntry(EntityPlayer player, BufferedImage image, String uuid) {
        File playerFolder = new File(player.world.getSaveHandler().getWorldDirectory(), "tablet_backgrounds/" + player.getUniqueID());
        if(!playerFolder.exists() && !playerFolder.mkdirs()) {
            DumbLibrary.getLogger().error("Unable to create folder: {}", playerFolder.getAbsolutePath());
            return;
        }
        File mainImageFile = new File(playerFolder, uuid + ".png");
        try {
            ImageIO.write(image, "PNG", mainImageFile);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write to file " + mainImageFile, e);
        }

        BufferedImage resized = TextureUtils.resize(image, (int) (20 * SCREEN_RATIO), 20);
        File iconImageFile = new File(playerFolder, uuid + "_icon.png");
        try {
            ImageIO.write(resized, "PNG", iconImageFile);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write to file " + iconImageFile, e);
        }
    }

    public static List<IconEntry> getAllIcons(boolean global, EntityPlayer player) {
        return global ? getAllIcons(player.world) : getAllIcons(player);
    }

    private static List<IconEntry> getAllIcons(World world) {
        List<IconEntry> entries = new ArrayList<>();
        File mainFolder = new File(world.getSaveHandler().getWorldDirectory(), "tablet_backgrounds/");
        File[] list = mainFolder.listFiles(File::isDirectory);
        if(list != null) {
            for (File file : list) {
                getAllIcons(file, entries);
            }
        }
        return entries;
    }

    private static List<IconEntry> getAllIcons(EntityPlayer player) {
        return getAllIcons(new File(player.world.getSaveHandler().getWorldDirectory(), "tablet_backgrounds/" + player.getUniqueID()), new ArrayList<>());
    }

    private static List<IconEntry> getAllIcons(File folder, List<IconEntry> list) {
        File[] files = folder.listFiles((dir, name) -> name.endsWith("_icon.png"));
        if(files != null) {
            for (File file : files) {
                list.add(new IconEntry(folder.getName(), file.getName().substring(0, file.getName().length() - "_icon.png".length())));
            }
        }
        return list;
    }

    public Optional<BufferedImage> getFullImage(World world, String uploaderUUID, String imageHash, boolean icon) {
        File file = new File(world.getSaveHandler().getWorldDirectory(), "tablet_backgrounds/" + uploaderUUID + File.separator + imageHash + (icon ? "_icon" : "") + ".png");
        if(uploaderUUID.isEmpty() || imageHash.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(ImageIO.read(file));
        } catch (IOException e) {
            ProjectNublar.getLogger().error("Unable to load file " + file, e);
        }
        return Optional.empty();
    }

    @Value
    public static class IconEntry {
        private final String uploaderUUID;
        private final String imageHash;
    }
}
