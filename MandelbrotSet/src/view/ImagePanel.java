
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * Taken from https://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
 * Mouse Listener code from https://stackoverflow.com/questions/11006496/select-an-area-to-capture-using-the-mouse
 */
public class ImagePanel extends JPanel{

    private BufferedImage image;
    private Rectangle selectedRectangle;
    JLabel screenLabel;
    final BufferedImage screenCopy;

    public ImagePanel(BufferedImage image) {
        this.image = image;
        // Listener related code follows
        screenCopy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        screenLabel = new JLabel(new ImageIcon(screenCopy));
        
        this.selectedRectangle = new Rectangle();
        selectedRectangle = null;
        // Listener related code follows
        //JScrollPane screenScroll = new JScrollPane(screenLabel);
        //screenScroll.setPreferredSize(new Dimension((int)(image.getWidth()/3), (int)(image.getHeight()/3)));
        //JPanel panel = new JPanel(new BorderLayout());
        this.add(screenLabel, BorderLayout.CENTER);
        
        repaint(screenCopy);
        screenLabel.repaint();
        
        screenLabel.addMouseMotionListener(new MouseMotionAdapter() {
            Point start = new Point();

            @Override
            public void mouseMoved(MouseEvent me) {
                
                start = me.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                
                Point end = me.getPoint();
                // A little geometry to make sure that the selected rectangle maintains aspect ratio
                if ((double)(me.getX() - start.getX()) / (double)(me.getY() - start.getY()) > 8.0 / 6.0)
                    end = new Point(me.getX(), (int)((6.0 / 8.0) * (double)(me.getX() - start.getX()) + (double)start.getY())); 
                else
                    end = new Point(((int)((8.0 / 6.0) * (double)(me.getY() - start.getY()) + (double)start.getX())), me.getY());
                selectedRectangle = new Rectangle(start,
                        new Dimension(end.x-start.x, end.y-start.y));
                repaint(screenCopy);
                screenLabel.repaint();
            }
        });
    }
    
    public void repaint(BufferedImage copy) {
        Graphics2D g = copy.createGraphics();
        g.drawImage(this.image, 0, 0, null);
        if (selectedRectangle!=null) {
            g.setColor(Color.RED);
            g.draw(selectedRectangle);
            g.setColor(new Color(255,255,255,150));
            g.fill(selectedRectangle);
        }
        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters            
    }
    
    public void setNewImage(BufferedImage image) {
        this.image = image;
        selectedRectangle = null;
        repaint(screenCopy);
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void setSelectedRectangle(Rectangle rectangle) {
        this.selectedRectangle = rectangle;
    }
    
    public Rectangle getSelectedRectangle() {
        return selectedRectangle;
    }

}
