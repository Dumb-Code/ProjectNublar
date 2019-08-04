package net.dumbcode.projectnublar.server.utils;

import com.sun.javafx.application.PlatformImpl;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DialogBox {

    private final FileChooser chooser;
    @Getter private boolean open;

    public DialogBox() {
        this.chooser = new FileChooser();
        this.chooser.setInitialDirectory(Minecraft.getMinecraft().gameDir);
        this.chooser.setTitle("File Chooser");
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

    public <T> void showBox(Type<T> type, Consumer<T> consumer) {
        this.open = true;
        PlatformImpl.startup(() -> {
            T apply = type.func.apply(this.chooser, null);
            this.open = false;
            Minecraft.getMinecraft().addScheduledTask(() -> consumer.accept(apply));
        });
    }

    public static class Type<T> {
        public static final Type<File> SAVE = new Type<>(FileChooser::showSaveDialog);
        public static final Type<File> OPEN = new Type<>(FileChooser::showOpenDialog);
        public static final Type<List<File>> OPEN_MULTIPLE = new Type<>(FileChooser::showOpenMultipleDialog);

        private final BiFunction<FileChooser, Window, T> func;

        Type(BiFunction<FileChooser, Window, T> func) {
            this.func = func;
        }
    }

}
