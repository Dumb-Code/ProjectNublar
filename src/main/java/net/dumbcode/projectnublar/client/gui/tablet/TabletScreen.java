package net.dumbcode.projectnublar.client.gui.tablet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

public class TabletScreen {
    protected static final Minecraft MC = Minecraft.getMinecraft();
    protected int xSize;
    protected int ySize;
    protected String route;
    protected OpenedTabletScreen parentGui;

    public void setData(int xSize, int ySize, String route) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.route = route;
    }
    
    public void navigateRoute(String link) {
    	String[] linkPieces = link.split("/");
    	List<String> routePieces = new ArrayList<String>();
    	if(link.startsWith("/")) {
    		this.route = this.route.split("/")[0].concat(link);
    		if(!link.endsWith("/")) this.route = this.route.concat("/");
    	} else {
    		Collections.addAll(routePieces, this.route.split("/"));
	    	for(int i = 0; i < linkPieces.length; i++) {
	    		if(linkPieces[i].equals("..")) {
	    			if(routePieces.size() > 1)
	    				routePieces.remove(routePieces.size() - 1);
	    		} else if(linkPieces[i].endsWith(":")) {
	    			// We're swapping to a new screen
	    			
	    		} else {
	    			routePieces.add(linkPieces[i]);
	    		}
	    	}
	    	// Add an extra empty string so that we get the final '/'
	    	routePieces.add("");
	    	this.route = String.join("/", routePieces);
    	}
    }

    public void onSetAsCurrentScreen() {
    }

    public void render(int mouseX, int mouseY, float partialTicks, String route) {
    }

    public void updateScreen() {
    }

    public void onMouseInput(int mouseX, int mouseY) {
    }

    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void onMouseReleased(int mouseX, int mouseY, int state) {
    }

    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    public void onKeyInput() {
    }

    public void onKeyTyped(char typedChar, int keyCode) {
    }

    public void onClosed() {
    }
}
