package net.dumbcode.projectnublar.client.commandmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ModelCommandLoader implements ICustomModelLoader {
    INSTANCE;

    private final Pattern filter = Pattern.compile("(.+)##(.+)\\.command");
    private final Pattern command = Pattern.compile("(.+?):(.+?);");

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourcePath().endsWith(".command");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String loc = modelLocation.toString();
        Matcher filter = this.filter.matcher(loc);
        if(!filter.find()) {
            throw new IllegalStateException("No match found");
        }
        ResourceLocation location = new ResourceLocation(filter.group(1));
        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(new ResourceLocation(location.getResourceDomain(), location.getResourcePath().substring("models/".length())));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        Matcher command = this.command.matcher(filter.group(2));
        List<CommandEntry> list = Lists.newArrayList();
        while (command.find()) {
            list.add(new CommandEntry(ModelCommandRegistry.get(command.group(1)), command.group(2).replaceAll("\\s+", "")));
        }

        return new Model(model, list);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @AllArgsConstructor  public class CommandEntry { ModelCommandRegistry.Command command; String data; }

    public class Model implements IModel {
        private final IModel delegate;
        private final List<CommandEntry> commands;

        public Model(IModel delegate, List<CommandEntry> commands) {
            this.delegate = delegate;
            this.commands = commands;
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            IBakedModel model = this.delegate.bake(state, format, bakedTextureGetter);
            for (CommandEntry command : this.commands) {
                command.command.applyChanges(model, command.data);
            }
            return model;
        }

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return this.delegate.getDependencies();
        }

        @Override
        public Collection<ResourceLocation> getTextures() {
            return this.delegate.getTextures();
        }

        @Override
        public IModelState getDefaultState() {
            return this.delegate.getDefaultState();
        }

        @Override
        public IModel process(ImmutableMap<String, String> customData) {
            return new Model(this.delegate.process(customData), this.commands);
        }

        @Override
        public IModel smoothLighting(boolean value) {
            return new Model(this.delegate.smoothLighting(value), this.commands);
        }

        @Override
        public IModel gui3d(boolean value) {
            return new Model(this.delegate.gui3d(value), this.commands);
        }

        @Override
        public IModel uvlock(boolean value) {
            return new Model(this.delegate.uvlock(value), this.commands);
        }

        @Override
        public IModel retexture(ImmutableMap<String, String> textures) {
            return new Model(this.delegate.retexture(textures), this.commands);
        }
    }
}
