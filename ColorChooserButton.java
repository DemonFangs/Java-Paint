import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/*This class is our colorChoser class. WHen the Button is created
* this class will atomatically attach a color choser class to it 
* allowing us to set and modify colors for each component in out
* JavaPaintFrame class. 
*/
public class ColorChooserButton extends JButton
{
	static final long serialVersionUID = 4L;
    //hold the coloe that is chosen
    private Color current;
    
    //Constructor with default color c. 
    public ColorChooserButton(Color c) 
    {
        setSelectedColor(c); 
        addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                //Call the JColorChoser to show the dialog.
                Color newColor = JColorChooser.showDialog(null, "Choose a color", current);
                setSelectedColor(newColor);
            }
        });
    }

    //Get the selected color from the color choser
    public Color getSelectedColor() 
    {
            return current;
    }

    //Main way to set the selected color
    public void setSelectedColor(Color newColor) 
    {
       setSelectedColor(newColor, true);
    }

    //This will set the selected color to the new chosen color
    public void setSelectedColor(Color newColor, boolean notify) 
    {
        if (newColor == null) return;
        current = newColor;
        setIcon(createIcon(current, 16, 16));
        repaint();

        if (notify) 
        {
            // Notify everybody that may be interested.
            for (ColorChangedListener l : listeners) 
            {
                l.colorChanged(newColor);
            }
        }
    }

    //This will be implemented in JavaPaintFrame.java for color changes
    public static interface ColorChangedListener
    {
        public void colorChanged(Color newColor);
    }

    //A list of all the listeners
    private List<ColorChangedListener> listeners = new ArrayList<ColorChangedListener>();

    //Add all the listeners into the list for furter changes
    public void addColorChangedListener(ColorChangedListener toAdd)
    {
        listeners.add(toAdd);
    }

    //Creates the icon for each of the buttons
    public static  ImageIcon createIcon(Color main, int width, int height) 
    {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);            
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}
