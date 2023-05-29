package net.dumbcode.projectnublar.client.gui.tablet.screens;

import com.google.gson.Gson;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EncyclopediaScreen extends TabletPage {
	
	private final int specButtonX = 120;
	private final int specButtonY = 80;
	private HashMap<String, EncyclopediaPage> pages = new HashMap<String, EncyclopediaPage>();
	private EncyclopediaPage homePage = new HomePage(this);
	private EncyclopediaPage currentPage;
	private boolean loadedData = false;
	private Map<?, ?> data;
	private Map<?, ?> dinosaurs;
	private Map<?, ?> plants;
	
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
			this.dinosaurs = (Map<?, ?>) data.get("dinosaurs");
			this.plants = (Map<?, ?>) data.get("plants");
			loadedData = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Register the pages here
		pages.put("species", new SpeciesListPage(this));
	}	
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
		for(int i = 10; i <= 500; i += 10) {
			AbstractGui.fill(stack, i, 0, i+1, 280, 0xFF444444);
		}
		for(int i = 10; i <= 280; i += 10) {
			AbstractGui.fill(stack, 0, i, 500, i+1, 0xFF444444);
		}
		MC.font.drawShadow(stack, this.route, 3, 18, 0xFF00FF00);
		if(!this.route.equals("encyclopedia:/")) {
			AbstractGui.fill(stack, 0, 35, 15, 50, 0xFF000000);
		}
		if(this.currentPage != null) {
			MC.font.drawShadow(stack, this.currentPage.getClass().getSimpleName(), 3, 270, 0xFFFF0000);
			this.currentPage.render(stack, mouseX, mouseY, partialTicks, route);
		} else {
			// Navigate to home page
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
			AbstractGui.fill(stack, 120, 100, 160, 140, 0xFF00FF00);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("/species");
				return true;
			}
			return false;
		}
	}
	
	private class SpeciesListPage extends EncyclopediaPage {
		private GuiScrollBox<SpeciesScrollEntry> scrollBox;
		private List<SpeciesScrollEntry> scrollEntries = new ArrayList<SpeciesScrollEntry>();
		
		public SpeciesListPage(EncyclopediaScreen screen) {
			super(screen);
			this.scrollBox = new GuiScrollBox<SpeciesScrollEntry>(40, 50, 420, 80, 2, () -> this.scrollEntries);
			// Split the list of dinosaurs into separate lists of 5 each to make the rows
			List<Set<String>> rows = partitionSet((Set<String>) this.screen.dinosaurs.keySet(), 5);
			rows.forEach(row -> {
				this.scrollEntries.add(new SpeciesScrollEntry(row, this.screen.dinosaurs));
			});
		}
		
		@Override
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
			//AbstractGui.fill(stack, 120, 100, 160, 140, 0xFFFF0000);
			scrollBox.render(stack, mouseX, mouseY, partialTicks);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("mosasaurus");
				return true;
			}
			return false;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			return scrollBox.mouseScrolled(mouseX, mouseY, amount);
		}
		
		@Override
		public EncyclopediaPage getSubpage(String route) {
			return new DinosaurPage(this.screen, route);
		}
		
		private List<Set<String>> partitionSet(Set<String> set, int partitionSize)
		{
			List<Set<String>> list = new ArrayList<>();
			int setSize = set.size();
			
			Iterator iterator = set.iterator();
			
			while(iterator.hasNext())
			{
				Set newSet = new HashSet();
				for(int j = 0; j < partitionSize && iterator.hasNext(); j++)
				{
					String s = (String)iterator.next();
					newSet.add(s);
				}
				list.add(newSet);
			}
			return list;
		}
	}
	
	private class SpeciesScrollEntry implements GuiScrollboxEntry {
		private List<String> speciesKeys;
		private Map<?, ?> dinos;
		
		public SpeciesScrollEntry(Set<String> speciesKeys, Map<?, ?> dinos) {
			super();
			this.speciesKeys = new ArrayList<>(speciesKeys);
			this.dinos = dinos;
		}
		
		@Override
		public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
			for(int i = 0; i < speciesKeys.size(); i++) {
				int iconX = x + (i * 85);
				AbstractGui.fill(stack, iconX, y, iconX + 80, y + 80, 0xFF36393F);
				MC.font.drawShadow(stack, (String) ((Map<?, ?>) dinos.get(speciesKeys.get(i))).get("name"), iconX + 3, y + 35, 0xFFFFFFFF);
			}
		}
	}
	
	private class DinosaurPage extends EncyclopediaPage {
		public String id;
		private String name;
		private String scientificName;
		private String age;
		private String biomes;
		private String size;
		
		public DinosaurPage(EncyclopediaScreen screen, String id) {
			super(screen);
			this.id = id;
			Map<?, ?> dinoData = (Map<?, ?>) this.screen.dinosaurs.get(id);
			this.name = (String) dinoData.get("name");
			this.scientificName = (String) dinoData.get("scientificName");
			this.age = (String) dinoData.get("age");
			this.biomes = (String) dinoData.get("biomes");
			this.size = (String) dinoData.get("size");
		}
		
		@Override
		public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
			// Draw the info box
			AbstractGui.fill(stack, 50, 50, 260, 240, 0xFF00A5CF);
			// Draw what will later become the image of the dino
			AbstractGui.fill(stack, 280, 50, 460, 240, 0xFF00FF00);
			// Draw the name
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.name", this.name), 55, 60, 0xFFFFFFFF);
			// Draw the scientific name
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.scientific_name", "�o", this.scientificName), 55, 75, 0xFFFFFFFF);
			// Draw the age
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.age", this.age), 55, 90, 0xFFFFFFFF);
			// Draw the biomes
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.biomes", this.biomes), 55, 105, 0xFFFFFFFF);
			// Draw the rarity
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.rarity", "�a", "Uncommon"), 55, 120, 0xFFFFFFFF);
			// Draw the diet
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.diet", "Piscovore"), 55, 135, 0xFFFFFFFF);
			// Draw the size
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.size", this.size), 55, 150, 0xFFFFFFFF);
			// Draw the genome percent
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.genome_percent", "�a", "90%"), 55, 165, 0xFFFFFFFF);
			// Draw genome progress bar
			AbstractGui.fill(stack, 55, 175, 255, 190, 0xFFFF0000);
			AbstractGui.fill(stack, 55, 175, 235, 190, 0xFF00FF00);
			// Draw the cloned assets
			MC.font.drawShadow(stack, ProjectNublar.translate("gui.encyclopedia.cloned_assets", "2"), 55, 195, 0xFFFFFFFF);
		}
		
		private void drawDinosaur() {
			// TODO make it draw the dinosaur
		}
	}
}
