package net.dumbcode.projectnublar.client.gui.tablet.screens;

import java.util.HashMap;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.minecraft.client.gui.Gui;

public class EncyclopediaScreen extends TabletScreen {
	
	private final int specButtonX = 120;
	private final int specButtonY = 80;
	private HashMap<String, EncyclopediaPage> pages = new HashMap<String, EncyclopediaPage>();
	private EncyclopediaPage homePage = new HomePage(this);
	private EncyclopediaPage currentPage;
	
	@Override
	public void onSetAsCurrentScreen() {
		// Register the pages here
		pages.put("species", new SpeciesListPage(this));
	}	
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks, String route) {
		MC.fontRenderer.drawString(this.route, 3, 18, 0xFF000000);
		Gui.drawRect(0, 35, 15, 50, 0x44000000);
		if(this.currentPage != null) {
			MC.fontRenderer.drawString(this.currentPage.getClass().getSimpleName(), 3, 80, 0xFF000000);
			this.currentPage.render(mouseX, mouseY, partialTicks, route);
		} else {
			// Navigate to home page
			this.navigateRoute("/");
		}
	}
	
	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(this.currentPage != null) {
			this.currentPage.onMouseClicked(mouseX, mouseY, mouseButton);
		}
		if(mouseX >= 0 && mouseY >= 35 && mouseX <= 15 && mouseY <= 50) {
			this.navigateRoute("..");
		}
    }
	
	@Override
	public void navigateRoute(String link) {
		super.navigateRoute(link);
		this.updatePage();
	}
	
	public void updatePage() {
		String[] routePieces = this.route.split("/");
		if(routePieces.length > 1) {
			this.currentPage = pages.get(routePieces[1]);
		} else {
			this.currentPage = this.homePage;
		}
	}
	
	public void renderHome(int mouseX, int mouseY, float partialTicks) {
		Gui.drawRect(this.specButtonX, this.specButtonY, this.specButtonX + 80, this.specButtonY + 80, 0xFF000000);
	}
	
	@RequiredArgsConstructor
	private class EncyclopediaPage {
		protected final EncyclopediaScreen screen;
		
		public void render(int mouseX, int mouseY, float partialTicks, String route) {}
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {}
		public void updatePage() {};
	}
	
	private class HomePage extends EncyclopediaPage {
		public HomePage(EncyclopediaScreen screen) {
			super(screen);
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			Gui.drawRect(120, 100, 160, 140, 0xFF00FF00);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("/species");
			}
		}
	}
	
	private class SpeciesListPage extends EncyclopediaPage {
		public SpeciesListPage(EncyclopediaScreen screen) {
			super(screen);
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			Gui.drawRect(120, 100, 160, 140, 0xFF00FF00);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("1");
			}
		}
	}
}
