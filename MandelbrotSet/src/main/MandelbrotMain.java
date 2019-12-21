package main;

import controller.IMandelbrotController;
import controller.MandelbrotController;
import model.IMandelbrotModel;
import model.MandelbrotModel;
import view.MandelbrotGuiView;


public class MandelbrotMain {
    
    public static void main(String args[]) {
        
        //create Model
		IMandelbrotModel model = new MandelbrotModel();

		// Create controller 
		IMandelbrotController controller = new MandelbrotController(model);
		
		// Create View (GUI)
		new MandelbrotGuiView(model, controller);
    }
    
}
