package net.dumbcode.projectnublar.server.tablet;

import lombok.Value;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public class TabletBGImageHandler {

    private static final FolderName TABLET_BACKROUNDS = new FolderName("tablet_backgrounds");

    public static final float SCREEN_RATIO = 16F/9F;

    public static void addNewEntry(PlayerEntity player, NativeImage image, String uuid) {
        Path playerFolder = getTabletBackgrounds().resolve(player.getUUID().toString());
        if(!Files.exists(playerFolder)) {
            try {
                Files.createDirectories(playerFolder);
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to create folder: " + playerFolder.toAbsolutePath(), e);
            }
            return;
        }
        Path mainImageFile = playerFolder.resolve(uuid + ".png");
        try {
            image.writeToFile(mainImageFile);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write to file " + mainImageFile, e);
        }

        NativeImage resized = TextureUtils.resize(image, (int) (20 * SCREEN_RATIO), 20);
        Path iconImageFile = playerFolder.resolve(uuid + "_icon.png");
        try {
            resized.writeToFile(iconImageFile);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write to file " + iconImageFile, e);
            }
    }

    private final Path getTabletBackgrounds() {
        return ServerLifecycleHooks.getCurrentServer().getWorldPath(TABLET_BACKROUNDS);
    }

    public static List<IconEntry> getAllIcons(boolean global, PlayerEntity player) {
        return global ? getAllIcons() : getAllIcons(player);
    }

    private static List<IconEntry> getAllIcons() {
        List<IconEntry> entries = new ArrayList<>();
        Path mainFolder = getTabletBackgrounds();
        try(Stream<Path> stream = Files.walk(mainFolder, 2)) {
            stream
                .filter(Files::isDirectory)
                .forEach(p -> getAllIcons(p, entries));
        } catch (IOException e) {
            ProjectNublar.LOGGER.error("Unable to walk icons", e);
        }
        return entries;
    }

    private static List<IconEntry> getAllIcons(PlayerEntity player) {
        return getAllIcons(getTabletBackgrounds().resolve(player.getUUID().toString()), new ArrayList<>());
    }

    private static List<IconEntry> getAllIcons(Path folder, List<IconEntry> list) {
        String folderName = FilenameUtils.getName(folder.toString());
        if(Files.exists(folder)) {
            try(Stream<Path> stream = Files.walk(folder, 1)) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toString().endsWith("_icon.png"))
                    .forEach(p -> list.add(new IconEntry(folderName, FilenameUtils.getBaseName(p.toString()))));
            } catch (IOException e) {
                ProjectNublar.LOGGER.error("Unable to walk icons", e);
            }
        }
        return list;
    }

    public Optional<NativeImage> getFullImage(World world, String uploaderUUID, String imageHash, boolean icon) {
        Path file = getTabletBackgrounds().resolve(uploaderUUID).resolve(imageHash + (icon ? "_icon" : "") + ".png");
        if(uploaderUUID.isEmpty() || imageHash.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(NativeImage.read(Files.newInputStream(file)));
        } catch (IOException e) {
            ProjectNublar.LOGGER.error("Unable to load file " + file, e);
        }
        return Optional.empty();
    }

    @Value
    public static class IconEntry {
        private final String uploaderUUID;
        private final String imageHash;
    }
}
