package net.dumbcode.projectnublar.server.block.fossils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

@Getter
public abstract class FossilBlockNeus extends Block implements IItemBlock {

	public static final PropertyEnum<Condition> CONDITION = PropertyEnum.create("condition", Condition.class);

	private final Type type;
	private final Background background;

	public FossilBlockNeus(Type type, Background background) {
		super(Material.ROCK);
		this.type = type;
		this.background = background;

		this.setDefaultState(this.blockState.getBaseState().withProperty(CONDITION, Condition.GOOD));
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Type {
		BONE("bone", BoneFossilBlock::new), PLANT("plant", PlantFossilBlock::new), TRACE("trace", TraceFossilBlock::new);

		private final String name;
		private final Function<Background, FossilBlockNeus> creator;

	}

	public enum Condition implements IStringSerializable {
		POOR("poor"), AVERAGE("average"), GOOD("good");

		private final String name;
		Condition(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	public enum Background implements IStringSerializable {
		STONE("stone"), SANDSTONE("sandstone"), CLAY("clay");

		private final String name;
		Background(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

	}

}
