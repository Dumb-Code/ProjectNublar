package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DinosaurArgument implements ArgumentType<Dinosaur> {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslationTextComponent(ProjectNublar.MODID + ".command.dinosaur.invalid"));

    @Override
    public Dinosaur parse(StringReader reader) throws CommandSyntaxException {
        Dinosaur value = DinosaurHandler.getRegistry().getValue(ResourceLocation.read(reader));
        if(value == null) {
            throw ERROR_INVALID.createWithContext(reader);
        }
        return value;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(DinosaurHandler.getRegistry().getValues().stream().map(d -> Objects.requireNonNull(d.getRegistryName()).toString()), builder);
    }

}
