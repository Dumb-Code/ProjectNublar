package net.dumbcode.projectnublar.server.utils;

import com.sun.javafx.application.PlatformImpl;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DialogBox {

    private final FileChooser chooser;

    private final Consumer<File> fileConsumer;

    public DialogBox(Consumer<File> fileConsumer) {
        this.chooser = new FileChooser();
        this.chooser.setInitialDirectory(Minecraft.getMinecraft().gameDir);
        this.chooser.setTitle("File Chooser");
        this.fileConsumer = fileConsumer == null ? file -> {} : fileConsumer;
    }

    public DialogBox title(String title) {
        this.chooser.setTitle(title);
        return this;
    }

    public DialogBox root(File root) {
        this.chooser.setInitialDirectory(root);
        return this;
    }

    public DialogBox extension(String desc, boolean active, String... extensions) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(desc, extensions);
        this.chooser.getExtensionFilters().add(filter);
        if(active) {
            this.chooser.setSelectedExtensionFilter(filter);
        }
        return this;
    }

    public void showBox(Type type) {
        PlatformImpl.startup(() -> {
            File apply = type.func.apply(this.chooser, null);
            Minecraft.getMinecraft().addScheduledTask(() -> this.fileConsumer.accept(apply));
        });
    }

    public enum Type {
        SAVE(FileChooser::showSaveDialog),
        OPEN(FileChooser::showOpenDialog);

        private final BiFunction<FileChooser, Window, File> func;

        Type(BiFunction<FileChooser, Window, File> func) {
            this.func = func;
        }
    }

}
