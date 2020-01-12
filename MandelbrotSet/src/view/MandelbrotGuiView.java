
package view;

import controller.IMandelbrotController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import model.IMandelbrotModel;

public class MandelbrotGuiView implements Observer, ActionListener {
    
    private IMandelbrotModel model;
    private IMandelbrotController controller;
    
    private JFrame mainFrame;
    private ImagePanel mandelbrotViewPanel;
    private JPanel mandelbrotControlPanel;
    
    private static int DEFAULT_FRAME_WIDTH = 800;
    private static int DEFAULT_FRAME_HEIGHT = 700;
    private static int DEFAULT_CONTROLPANEL_HEIGHT = 50;
    private static int DEFAULT_VIEWPANEL_HEIGHT = 600;
    
    private JMenuItem loadMenuItem;
    private JMenuItem saveMenuItem;
    private JButton undoButton;
    private JButton redoButton;
    private JButton resetButton;
    private JButton generateButton;
    private JButton changeColourMappingButton;
    private JLabel maxIterationTextLabel;
    private JTextField maxIterationTextField;
    private JLabel magnificationTextLabel;
    private JLabel magnificationValueLabel;
    private JMenuBar menu;
    
    private int minRealPixel;
    private int maxRealPixel;
    private int minImaginaryPixel;
    private int maxImaginaryPixel;
    private int maxIterations;
    
    private double currentRatio;
    
    boolean changedScale;
    
    public MandelbrotGuiView(IMandelbrotModel model, IMandelbrotController controller) {
        
        this.model = model;
        this.controller = controller;
         // The model must calculate according to the appropriate size
        model.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_VIEWPANEL_HEIGHT);  

        mainFrame = new JFrame("Mandelbrot Viewer");
        
        mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
        mainFrame.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT );
        
        
        currentRatio = 1;
        
        addControlElements();
        addViewElements();
        
        mainFrame.setResizable(false);
        mainFrame.paintAll(mainFrame.getGraphics());
        mainFrame.setVisible( true );
        
        addActionListeners(this);
        ((Observable) model).addObserver(this);
    }
    
    private void addViewElements() {
        mandelbrotViewPanel = new ImagePanel(model.getCurrentImage());
        mandelbrotViewPanel.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_VIEWPANEL_HEIGHT);
        
        minRealPixel = 1;
        maxRealPixel = DEFAULT_FRAME_WIDTH;
        minImaginaryPixel = 1;
        maxImaginaryPixel = DEFAULT_VIEWPANEL_HEIGHT;
        maxIterations = Integer.parseInt(maxIterationTextField.getText());
        mainFrame.getContentPane().add(mandelbrotViewPanel, BorderLayout.CENTER);
    }
    
    private void addControlElements() {
        mandelbrotControlPanel = new JPanel();
        mandelbrotControlPanel.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_CONTROLPANEL_HEIGHT);
        mandelbrotControlPanel.setBackground(Color.orange);
        
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        resetButton = new JButton("Reset");
        changeColourMappingButton = new JButton("Change Mapping");
        generateButton = new JButton("Generate Image");
        maxIterationTextLabel = new JLabel("Max Iterations:");
        maxIterationTextField = new JTextField(4);
        maxIterationTextField.setText("50");
        magnificationTextLabel = new JLabel("Magnification:");
        magnificationValueLabel = new JLabel("1 : " + String.format("%.1f", currentRatio)); // http://javadevnotes.com/java-double-to-string-examples
        // Create and load menu
        menu = new JMenuBar();
        JMenu file = new JMenu("File");
        loadMenuItem = new JMenuItem("Load");
        saveMenuItem = new JMenuItem("Save");
        file.add(loadMenuItem);
        file.add(saveMenuItem);
        menu.add(file);
        mainFrame.setJMenuBar(menu);
        // Add rest of the components to Control JPanel
        mandelbrotControlPanel.add(undoButton);
        mandelbrotControlPanel.add(redoButton);
        mandelbrotControlPanel.add(resetButton);
        mandelbrotControlPanel.add(Box.createRigidArea(new Dimension(5,0)));   // https://stackoverflow.com/questions/8335997/how-can-i-add-a-space-in-between-two-buttons-in-a-boxlayout
        mandelbrotControlPanel.add(changeColourMappingButton);
        mandelbrotControlPanel.add(Box.createRigidArea(new Dimension(5,0)));        
        mandelbrotControlPanel.add(maxIterationTextLabel);
        mandelbrotControlPanel.add(maxIterationTextField);
        mandelbrotControlPanel.add(Box.createRigidArea(new Dimension(5,0)));
        mandelbrotControlPanel.add(generateButton);
        //mandelbrotControlPanel.add(Box.createRigidArea(new Dimension(5,0)));
        mandelbrotControlPanel.add(magnificationTextLabel);
        mandelbrotControlPanel.add(magnificationValueLabel);
        mainFrame.getContentPane().add(mandelbrotControlPanel, BorderLayout.NORTH);
    }
	
    public void addActionListeners(ActionListener al) {
    		loadMenuItem.addActionListener(event -> controller.controlLoadFromFile());
            //loadMenuItem.addActionListener(al);
            saveMenuItem.addActionListener(event -> controller.controlSaveToFile());
    		//saveMenuItem.addActionListener(al);
            undoButton.addActionListener(event -> controller.controlUndo());
            //undoButton.addActionListener(al);
            redoButton.addActionListener(event -> controller.controlRedo());
            //redoButton.addActionListener(al);
            resetButton.addActionListener(event -> {
                controller.controlReset();
                maxIterationTextField.setText("50");
                currentRatio = 1;
            });
            //resetButton.addActionListener(al);
            generateButton.addActionListener(event -> {
                // Read Max Iterations value from the user
                maxIterations = Integer.parseInt(maxIterationTextField.getText()); 
                changedScale = false;
                // https://docs.oracle.com/javase/7/docs/api/java/awt/Rectangle.html
                Rectangle selectedRectangle = mandelbrotViewPanel.getSelectedRectangle();
                if (selectedRectangle != null) {   // If user has selected an area for zoom
                    minRealPixel = (int) selectedRectangle.getMinX();
                    maxRealPixel = (int) selectedRectangle.getMaxX();
                    minImaginaryPixel = (int) selectedRectangle.getMinY();
                    maxImaginaryPixel = (int) selectedRectangle.getMaxY();
                    changedScale = true;
                    currentRatio = currentRatio * (double)DEFAULT_FRAME_WIDTH / 
                            (double)(maxRealPixel - minRealPixel);
                    
                }// Recreate image through controller. Controller reads pixels, model 
                //will transform to real values
                controller.controlGenerate(minRealPixel, maxRealPixel,
                        minImaginaryPixel, maxImaginaryPixel, maxIterations, 
                        changedScale, currentRatio);                    
            });
           //generateButton.addActionListener(al);
            changeColourMappingButton.addActionListener(event -> controller.changeColourMapping());
            //changeColourMappingButton.addActionListener(al);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                if (changedScale) {
                    BufferedImage[] multipleImages = model.getMultipleImages();
                    for (int i = 0; i < multipleImages.length; i++) {
                        mandelbrotViewPanel.setNewImage(multipleImages[i]);
                        mainFrame.paintAll(mainFrame.getGraphics());
                        //every 20 miliseconds
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MandelbrotGuiView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    changedScale = false;
                }
                mandelbrotViewPanel.setNewImage(model.getCurrentImage());  // Read new image from the model
                magnificationValueLabel.setText("1 : " + String.format("%.1f", model.getCurrentRatio())); // http://javadevnotes.com/java-double-to-string-examples inform about current ratio
                maxIterationTextField.setText(Integer.toString(model.getCurrentMaxIterations()));
                currentRatio = model.getCurrentRatio(); // store the new current ratio
		mainFrame.paintAll(mainFrame.getGraphics());
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
//        if(e.getSource() == loadMenuItem) {
//            controller.controlLoadFromFile();
//        } 
//        else if(e.getSource() == saveMenuItem) {
//            controller.controlSaveToFile();
//        } 
//        else if(e.getSource() == undoButton) {
//            controller.controlUndo();
//        }
//        else if(e.getSource() == redoButton) {
//            controller.controlRedo();
//        }
//        else if(e.getSource() == resetButton) {
//            controller.controlReset();
//            maxIterationTextField.setText("50");
//            currentRatio = 1;
//        }
//        else if(e.getSource() == changeColourMappingButton) {
//            controller.changeColourMapping();
//        }
//        else if(e.getSource() == generateButton) {
//            // Read Max Iterations value from the user
//            maxIterations = Integer.parseInt(maxIterationTextField.getText()); 
//            changedScale = false;
//            // https://docs.oracle.com/javase/7/docs/api/java/awt/Rectangle.html
//            Rectangle selectedRectangle = mandelbrotViewPanel.getSelectedRectangle();
//            if (selectedRectangle != null) {   // If user has selected an area for zoom
//                minRealPixel = (int) selectedRectangle.getMinX();
//                maxRealPixel = (int) selectedRectangle.getMaxX();
//                minImaginaryPixel = (int) selectedRectangle.getMinY();
//                maxImaginaryPixel = (int) selectedRectangle.getMaxY();
//                changedScale = true;
//                currentRatio = currentRatio * (double)DEFAULT_FRAME_WIDTH / 
//                        (double)(maxRealPixel - minRealPixel);
//                
//            }// Recreate image through controller. Controller reads pixels, model 
//            //will transform to real values
//            controller.controlGenerate(minRealPixel, maxRealPixel,
//                    minImaginaryPixel, maxImaginaryPixel, maxIterations, 
//                    changedScale, currentRatio);    
//        }
    }
}
