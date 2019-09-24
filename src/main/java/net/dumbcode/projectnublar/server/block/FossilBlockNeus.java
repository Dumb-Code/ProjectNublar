package net.dumbcode.projectnublar.server.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FossilBlockNeus extends Block implements IItemBlock {

	public static final PropertyEnum<Background> BLOCK = PropertyEnum.create("block", Background.class);
	public static final PropertyEnum<Condition> CONDITION = PropertyEnum.create("condition", Condition.class);
	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
	public static final PropertyEnum<Texture> TEXTURE = PropertyEnum.create("texture", Texture.class);

	public FossilBlockNeus() {
		super(Material.ROCK);
		this.setDefaultState(this.getDefaultState()
				.withProperty(BLOCK, Background.STONE)
				.withProperty(CONDITION, Condition.GOOD)
				.withProperty(TYPE, Type.BONE)
				.withProperty(TEXTURE, Texture.SKULL)
		);
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
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

	public enum Type implements IStringSerializable {
		BONE("bone"), PLANT("plant"), TRACE("trace");

		private final String name;

		Type(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	public enum Texture implements IStringSerializable {
		SKULL("skull"), RIBS("ribs"), FEET("feet"), ARMS("arms"), LEGS("legs"), SPINE("spine"), TAIL("tail"),
		LEAF("leaf"), WOOD("wood"), FRUIT("fruit"),
		SHELL("shell"), FOOTPRINT("footprint"), AMMONITE("ammonite"), FISH("fish"), TOOTH("tooth"), LIZARD("lizard");

		private final String name;

		Texture(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
