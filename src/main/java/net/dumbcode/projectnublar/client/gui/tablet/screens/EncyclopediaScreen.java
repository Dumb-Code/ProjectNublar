package net.dumbcode.projectnublar.client.gui.tablet.screens;

import com.google.gson.Gson;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.ModelComponent;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EncyclopediaScreen extends TabletPage {

	private final int specButtonX = 120;
	private final int specButtonY = 80;
	private HashMap<String, EncyclopediaPage> pages = new HashMap<>();
	private EncyclopediaPage homePage = new HomePage(this);
	private EncyclopediaPage currentPage;
	private Map<?, ?> data;

	private static final Logger logger =  LogManager.getLogger();


	@Override
	public void onSetAsCurrentScreen() {
		// Load the data for the encyclopedia entries
		try {
			Gson gson = new Gson();
			InputStream stream = EncyclopediaScreen.MC.getResourceManager().getResource(new ResourceLocation(ProjectNublar.MODID, "lang/encyclopedia/en_us.json")).getInputStream();
			Scanner s = new Scanner(stream).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			s.close();
			this.data = gson.fromJson(result, Map.class);
		} catch (IOException e) {
			logger.warn("Could not load lang file for {}, defaulting to en_us", "lang");
			e.printStackTrace();
		}
		
		// Register the pages here
		pages.put("species", new SpeciesListPage(this));
	}	
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
		/*for(int i = 10; i <= 500; i += 10) {
			AbstractGui.fill(stack, i, 0, i+1, 280, 0xFF444444);
		}
		for(int i = 10; i <= 280; i += 10) {
			AbstractGui.fill(stack, 0, i, 500, i+1, 0xFF444444);
		}*/
		MC.font.drawShadow(stack, this.route, 3, 18, 0xFF00FF00);
		if(!this.route.equals("encyclopedia:/")) {
			AbstractGui.fill(stack, 0, 35, 15, 50, 0xFF000000);
		}
		if(this.currentPage != null) {
			MC.font.drawShadow(stack, this.currentPage.getClass().getSimpleName(), 3, 270, 0xFFFF0000);
			this.currentPage.render(stack, mouseX, mouseY, partialTicks, route);
		} else {
			// Navigate to home page if the current page is null (likely when loading for the first time)
			this.navigateRoute("/");
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if(this.currentPage != null) {
			return this.currentPage.mouseScrolled(mouseX, mouseY, amount);
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if(this.currentPage != null) {
			return this.currentPage.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if(mouseX >= 0 && mouseY >= 35 && mouseX <= 15 && mouseY <= 50) {
			this.navigateRoute("..");
			return true;
		}
		return false;
	}
	
	@Override
	public void navigateRoute(String link) {
		super.navigateRoute(link);
		this.updatePage();
	}
	
	public void updatePage() {
		String[] routePieces = this.route.split("/");
		if(routePieces.length > 1) {
			if(routePieces.length > 2) {
				this.currentPage = pages.get(routePieces[1]).getSubpage(routePieces[2]);
			} else {
				this.currentPage = pages.get(routePieces[1]);
			}
		} else {
			this.currentPage = this.homePage;
		}
	}
	
	@RequiredArgsConstructor
	private class EncyclopediaPage {
		protected final EncyclopediaScreen screen;
		
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {}
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			return false;
		}
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return false; }
		public EncyclopediaPage getSubpage(String route) {
			return this;
		}
	}
	
	private class HomePage extends EncyclopediaPage {
		public HomePage(EncyclopediaScreen screen) {
			super(screen);
		}
		
		@Override
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
			AbstractGui.fill(stack, left+160, top+140, left+120, top+110, 0xFF00FF00);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			if(mouseX >= left+120 && mouseY >= top+100 && mouseX <= left+160 && mouseY <= top+140) {
				this.screen.navigateRoute("/species");
				return true;
			}
			return false;
		}
	}
	
	private class SpeciesListPage extends EncyclopediaPage {
		private GuiScrollBox<SpeciesScrollEntry> scrollBox;
		private List<SpeciesScrollEntry> scrollEntries = new ArrayList<>();

		public SpeciesListPage(EncyclopediaScreen screen) {
			super(screen);
			this.scrollBox = new GuiScrollBox<>(left+40, top+50, 420, 80, 2, () -> this.scrollEntries);
			//this.scrollBox.disableDefaultCellRendering();
			// Split the list of dinosaurs into separate lists of 5 each to make the rows
			Iterator<Dinosaur> dinoIterator = DinosaurHandler.getRegistry().iterator();
			List<Dinosaur> dinosaurs = new ArrayList<>();
			while(dinoIterator.hasNext()) {
				dinosaurs.add(dinoIterator.next());
				if(dinosaurs.size() == 5) {
					this.scrollEntries.add(new SpeciesScrollEntry(new ArrayList<>(dinosaurs), this.screen));
					dinosaurs.clear();
				}
			}
			// Make sure to add the last row if the dinosaurs aren't a perfect multiple of 5
			if(dinosaurs.size() > 0) {
				this.scrollEntries.add(new SpeciesScrollEntry(new ArrayList<>(dinosaurs), this.screen));
				dinosaurs.clear();
			}
		}
		
		@Override
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
			scrollBox.render(stack, mouseX, mouseY, partialTicks);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			return scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			return scrollBox.mouseScrolled(mouseX, mouseY, amount);
		}
		
		@Override
		public EncyclopediaPage getSubpage(String route) {
			return new DinosaurPage(this.screen, route);
		}
	}
	
	private class SpeciesScrollEntry implements GuiScrollboxEntry {
		private List<Dinosaur> dinos;
		private Map<?, ?> langData;
		private EncyclopediaScreen screen;


		public SpeciesScrollEntry(List<Dinosaur> dinos, EncyclopediaScreen screen) {
			super();
			this.dinos = dinos;
			this.langData = screen.data;
			this.screen = screen;
		}
		
		@Override
		public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
			for(int i = 0; i < this.dinos.size(); i++) {
				int iconX = x + (i * 85);
				AbstractGui.fill(stack, iconX, y, iconX + 80, y + 80, 0xFF36393F);

				String dinosaurName = this.dinos.get(i).getFormattedName();

				TranslationTextComponent localizedName = ProjectNublar.translate("dino." + dinosaurName + ".name");
				int textHeight = MC.font.wordWrapHeight(String.valueOf(localizedName), 75);
				MC.font.drawWordWrap(localizedName, iconX + 3, y + 40 - (textHeight / 2), 75, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
			for(int i = 0; i < this.dinos.size(); i++) {
				int iconX = (i * 85);
				if(relMouseX >= iconX && relMouseX <= iconX + 80) {
					EncyclopediaScreen.logger.info("Species {} was clicked", this.dinos.get(i).getFormattedName());
					this.screen.navigateRoute(this.dinos.get(i).getFormattedName());
					return true;
				}
			}
			return false;
		}
	}
	
	private class DinosaurPage extends EncyclopediaPage {
		public String id;
		private TranslationTextComponent name;
		private String scientificName;
		private String age;
		private String biomes;
		private String size;
		private Dinosaur dino;
		private DinosaurEntity entity;

		private final Framebuffer framebuffer;

		public DinosaurPage(EncyclopediaScreen screen, String id) {
			super(screen);
			this.dino = DinosaurHandler.getRegistry().getValue(new ResourceLocation("projectnublar:" + id));
			this.entity = this.dino.createEntity(MC.level);
			if(this.dino == null) {
				// Go back to the species list if they somehow navigated to a dinosaur that doesn't exist
				this.screen.navigateRoute("/species");
			}
			this.id = id;
			Map<?, ?> dinoData = (Map<?, ?>) this.screen.data.get(id);
			this.name = ProjectNublar.translate("dino." + id + ".name");
			this.scientificName = (String) dinoData.get("scientificName");
			this.age = (String) dinoData.get("age");
			this.biomes = this.dino.getDinosaurInfomation().getBiomeTypes().stream().map(BiomeDictionary.Type::getName).collect(Collectors.joining("; "));
			this.size = (String) dinoData.get("size");

			this.framebuffer = new Framebuffer(180, 190, true, false);
		}
		
		@Override
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
			// Draw the info box
			AbstractGui.fill(stack, left+50, top+50, left+260, top+240, 0xFF00A5CF);
			// Draw what will later become the image of the dino
			AbstractGui.fill(stack, left+280, top+50, left+460, top+240, 0xFF00FF00);
			// Draw the name
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.name", this.name), left+55, top+60, 0xFFFFFFFF);
			// Draw the scientific name
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.scientific_name", "�o", this.scientificName), left+55, top+75, 0xFFFFFFFF);
			// Draw the age
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.age", this.age), left+55, top+90, 0xFFFFFFFF);
			// Draw the biomes
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.biomes"), left+55, top+105, 0xFFFFFFFF);
			int extraLength = MC.font.wordWrapHeight(this.biomes, 200);
			extraLength -= 15; // Subtract the extra 15 that I already factored in from the first line
			MC.font.drawWordWrap(ITextProperties.of(this.biomes), left+55, top+116, 200,0xFFFFFFFF);
			// Draw the rarity
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.rarity", "�a", "Uncommon"), left+55, top+135 + extraLength, 0xFFFFFFFF);
			// Draw the diet
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.diet", "Piscovore"), left+55, top+150 + extraLength, 0xFFFFFFFF);
			// Draw the size
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.size", this.size), left+55, top+165 + extraLength, 0xFFFFFFFF);
			// Draw the genome percent
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.genome_percent", "�6", "100%"), left+55, top+180 + extraLength, 0xFFFFFFFF);
			// Draw the cloned assets
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.cloned_assets", "2"), left+55, top+195 + extraLength, 0xFFFFFFFF);
			drawDinosaur();
		}
		
		private void drawDinosaur() {
			// TODO make it draw the dinosaur
			this.framebuffer.bindWrite(true);
			MatrixStack matrixstack = new MatrixStack();
			matrixstack.scale(30, 30, 30);
			matrixstack.mulPose(Vector3f.XP.rotationDegrees(0));
			matrixstack.mulPose(Vector3f.YP.rotationDegrees(90));

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);

			DCMModel model = entity.getOrExcept(EntityComponentTypes.MODEL).getModelCache();
			model.renderImmediate(matrixstack, 0x00F000F0, entity.get(EntityComponentTypes.MODEL).get().getTexture().getLocation());

			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableBlend();

			Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
		}
	}
}
