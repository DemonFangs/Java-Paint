import java.awt.*;
import java.util.Map;
import java.awt.font.TextAttribute;
import java.awt.event.*;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import javax.swing.*;
import java.util.Hashtable;
import java.awt.image.BufferedImage;

/**
 *The Paint Panel
 */
public class ToDrawOrNotToDraw extends JComponent 
				implements MouseListener, MouseMotionListener
{
	static final long serialVersionUID = 3L;

	static final private int CURSOR = 0;
	static final private int PENCIL = 1;
	static final private int ERASER = 2;
	static final private int LINE = 3;
	static final private int RECTANGLE = 4;
	static final private int SQUARE = 5;
	static final private int ROUND_RECTANGLE = 6;
	static final private int ROUND_SQUARE = 13;
	static final private int OVAL = 7;
	static final private int CIRCLE = 8;
	static final private int TEXT = 9;
	static final private int SELECTOR = 10;
	
	static final private String DEFAULT_PRIMARY_COLOR = "#000000"; // Tool/Text/Fill Color - Black
	static final private String DEFAULT_SECONDARY_COLOR = "#FFFFFF"; // Background/Opaque - White

	static final private int TEXT_BOLD = 71; //first digit for Bold second digit denotes TEXT
	static final private int TEXT_ITALIC = 72;
	static final private int TEXT_UNDERLINE = 73;
	static final private int TEXT_OPAQUE = 74;

	static final private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	static final private double OFFSET_WIDTH = 39.0;
	static final private double OFFSET_HEIGHT = 123.0;
	
	private Color primaryColor; 
	private Color secondaryColor;
	
	private boolean boldActive = false;
	private boolean italicActive = false;
	private boolean underlineActive = false;
	private boolean textOpaque = false;
	
	private String textFont = new String("Serif"); //set to default until or unless changed by font method
	private int textStyle = Font.PLAIN;
	private int textSize = 18;
	private int imageFileWidth = -1;
	private int imageFileHeight = -1;
	
	private Rectangle selector;
	
	private Image image; //To Draw on
	private Image imageFile;
	private Graphics2D g2D; //Used to draw
	private int initX, initY, currX, currY;
	private int width, height;

	private Font font;
	private Map<TextAttribute, Object> attr;
	private int timesRepainted = 0; // counts the number of repaint cycles

	//Defines which toolbar is active
	private int state;
	private JavaPaintFrame parent;

	private int [] selectorDimension = new int[4];

	//-------------
	// Constructors
	//-------------
	
	/**
	 *Creates a blank image panel to draw upon.
	 *
	 *@param parent A pointer to the parent frame
	 */
	public ToDrawOrNotToDraw(JavaPaintFrame parent)
	{	
		this.parent = parent;
		setTextFont();
		setDoubleBuffered(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		//setMinimumSize(new Dimension(600, 600));
		//Sets the prefered size of the Drawing area. THis allows a scolling bar when resized
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setPreferredSize(new Dimension(dim.width - (int)OFFSET_WIDTH ,dim.height - (int)OFFSET_HEIGHT));
	}
	
	/**
	 *Creates a blank image and draws the passed image on top of the blank image
	 *
	 *@param parent A pointer to the parent frame
	 *@param image The image to draw on the blank image panel
	 */
	public ToDrawOrNotToDraw(JavaPaintFrame parent, Image image)
	{
		this.parent = parent;
		setTextFont();
		imageFile = image;
		setDoubleBuffered(false);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	//--------
	// Methods
	//--------

	/*
	 *Creates a new image to paint upon if there are no image created. Else paints on top of the current image.
	 *Also inserts a imageFile passed from the constructor on top of a blank image
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image == null)
		{
			image = createImage(getSize().width, getSize().height);
			g2D = (Graphics2D) image.getGraphics();
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
			if (imageFile == null) {
				parent.snapShot(this, -1);
			}
		}
		
		g.drawImage(image, 0, 0, null);

		// after first 10 cycles stops repainting the loaded image
		if ((imageFile != null)  && (timesRepainted <= 40))
		{	
			double scaleWidth = screenSize.getWidth() - OFFSET_WIDTH;
			double scaleHeight = screenSize.getHeight() - OFFSET_HEIGHT;
			
			if ((scaleWidth >= (imageFile.getWidth(this)*1.0)) && (scaleHeight >= (imageFile.getHeight(this)*1.0)))
				g2D.drawImage(imageFile, 0, 0, this);
			else
			{
				if (scaleWidth > (imageFile.getWidth(this)*1.0))
					scaleWidth = imageFile.getWidth(this)*1.0;
				else if (scaleHeight > (imageFile.getHeight(this)*1.0))
					scaleHeight = imageFile.getHeight(this)*1.0;

				g2D.drawImage(imageFile, 0, 0, (int)scaleWidth, (int)scaleHeight, parent);
			}
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			repaint();

			// waits 2000ms cycels to repaint
			timesRepainted++;
		}
		
		if ((state == SELECTOR) && (selector != null))
		{
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85F));
			g2D.draw(selector);
		}
	}

	/**
	 *Clears the image/drawing panel by filling the panel with white background and resetting the default color
	 *for tools to black
	 */
	public void clear()
	{
		g2D.setPaint( Color.decode(DEFAULT_SECONDARY_COLOR));
		g2D.fillRect(0,0,getSize().width, getSize().height);
		g2D.setPaint( Color.decode(DEFAULT_PRIMARY_COLOR));
		repaint();
	}
	
	/**
	 *Sets the primary color of the drawn lines according to the passed String hexadecimal value
	 *
	 *@param primaryColor A Color value that will be set as the primary color
	 */
	public void setPrimaryColor(Color primaryColor)
	{
		g2D.setColor(primaryColor);
		this.primaryColor = primaryColor;
	}

	/**
	 *Sets the secondary color of the drawn lines according to the passed String hexadecimal value
	 *
	 *@param secondaryColor A Color value that will be set as the filler/secondary color 
	 */
	public void setSecondaryColor(Color secondaryColor)
	{
		this.secondaryColor = secondaryColor;
	}

	/**
	 *Sets the state to the passed state.
	 *
	 *@param state Represents which tool in the toolbar is selected
	 */
	public void setState(int state)
	{
		this.state = state;
	}

	/**
	 *Set the font family to the passed font family
	 *
	 *@param font The font family to replace with
	 */
	public void setFontType(String font)
	{
		textFont = font;
		setTextFont();
	}

	/**
	 *Set the font size to the passed font size
	 *
	 *@param size The font size to replace with
	 */
	public void setFontSize(int size)
	{
		textSize = size;
		setTextFont();
	}

	/**
	 *Adds/Removes bold to/from the font style
	 *
	 *@param bold If <code>true</code>, bold is added to style, else removes from style
	 */
	public void setBold(boolean bold)
	{
		if (bold)
			textStyle = textStyle + Font.BOLD;
		else
		{
			if (textStyle > 0)
				textStyle = textStyle - Font.BOLD;
		}
		setTextFont();
	}

	/**
	 *Adds/Removes intalic to/from the font style
	 *
	 *@param italic If <code>true</code>, italic is added to style, else removes from style
	 */
	public void setItalic(boolean italic)
	{
		if (italic)
			textStyle = textStyle + Font.ITALIC;
		else
		{
			if (textStyle > 1)
				textStyle = textStyle - Font.ITALIC;
		}
		setTextFont();
	}

	/**
	*Adds/Removes underline to/from the font style
	 *
	 *@param underline If <code>true</code>, underline is added to style, else removes from style
	 */
	public void setUnderline(boolean underline)
	{
		underlineActive = underline;
		attr = new Hashtable<TextAttribute, Object>();
		attr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	}

	/**
	 *Re-initializes the font of the text to draw.
	 */
	public void setTextFont()
	{
		font = new Font(textFont, textStyle, textSize);
	}
	
	/**
	 *Set the value of the textOpaque to the passed value
	 *
	 *@param activity <code>true</code> if the Opaque button is active, otherwise <code>false</code> when Opaque is disabled and transparency is enabled
	 */
	public void setTextOpaque(boolean activity)
	{
		textOpaque = activity;
	}

	/**
	 *Draws and fills a rectangle with the secondary color at the back of the text.
	 *
	 *@param currX The x-axis value of the position of the mouse when clicked
	 *@param currY The y-axis value of the position of the mouse when clicked
	 *@param fontMetrics The font metrics of the Grpahics panel on which the String will be drawn upon
	 *@param text The String to draw on the Graphics panel
	 */
	public void setBackToOpaque(int currX, int currY, FontMetrics fontMetrics, String text)
	{
		int ascend = fontMetrics.getAscent();
		int descend = fontMetrics.getDescent();

		int textWidth = fontMetrics.stringWidth(text);
		int textHeight = descend + ascend;

		g2D.setColor(secondaryColor);
		g2D.fillRect(currX, currY - ascend, textWidth, textHeight);
		g2D.setColor(primaryColor);
		repaint();
	}

	/**
	 *Returns the Dimension of the current image file to load
	 *
	 *@return Returns the dimension of the current image file when the width or the height of the image to load is greater
	 * than the initial size of the frame. Else returns the dimension of the initial frame size.
	 */
	public Dimension getImageDimension()
	{	
		imageFileWidth = imageFile.getWidth(this);
		imageFileHeight = imageFile.getHeight(this);
		
		//System.out.println(imageFileWidth+"\t"+imageFileHeight);
		if ((imageFileWidth > 600) || (imageFileHeight > 600))
		{
			return (new Dimension(imageFileWidth, imageFileHeight));
		}
		return (new Dimension(600, 600));
	}

	/**
	 *Returns the width of the current image file to load
	 *
	 *@return The width of the current image file to load, else -1 if width is unknown. 
	 */
	public int getImageFileWidth()
	{
		return imageFileWidth;
	}

	/**
	 *Returns the height of the current image file to load
	 *
	 *@return The height of the current image file to load, else -1 if the height is unknown.
	 */
	public int getImageFileHeight()
	{
		return imageFileHeight;
	}

	/**
	 * Returns the selector dimension. For more infromation on the Dimension see setSelectorDimension()
	 *
	 * @return Returns the dimension of the selector
	 */
	public int [] getSelectorDimension()
	{
		return selectorDimension;
	}

	/**
	 * Fills the array with the passed values
	 *
	 * @param x The x-axis coordinate of the starting point of the selector rectangle
	 * @param y The y-axis coordinate of the starting point of the selector rectangle
	 * @param width The width of the selector rectangle
	 * @param height The height of the selector rectangle
	 */
	public void setSelectorDimension(int x, int y, int width, int height)
	{
		selectorDimension[0] = x;
		selectorDimension[1] = y;
		selectorDimension[2] = width + 1; 
		selectorDimension[3] = height + 1;
	}

	/**
	 * Copies the current image of on the drawing panel, converts it to BufferedImage and then returns a buffered Image
	 * of the selected region (of the selector)
	 */
	public BufferedImage copy()
	{
		BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage newImage = image.getSubimage(selectorDimension[0]+1, selectorDimension[1]+1, selectorDimension[2]-1, selectorDimension[3]-1);
		return newImage;
	}

	/**
	 * Deletes the content within the selected area in the selector. It is done by filling the selected rectangle
	 * with a same dimension rectangle with white color.
	 */
	public void delete()
	{
		g2D.setColor(Color.WHITE);
		g2D.fillRect(selectorDimension[0], selectorDimension[1], selectorDimension[2], selectorDimension[3]);
		g2D.setColor(primaryColor);
		repaint();
	}

	/**
	 * Creates a border on the whole drawing panel thus selecting it while storing the starting cooridantes
	 * of the rectangle with the width and height in an array called selectorDimension
	 */
	public void selectAll()
	{
		g2D.setColor(Color.BLACK);
		g2D.drawRect(0, 0, this.getWidth(), this.getHeight());
		setSelectorDimension(0, 0, this.getWidth(), this.getHeight());
		g2D.setColor(primaryColor);
		repaint();
	}
	
	//------------------
	//Override Methods
	//------------------
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent me)
	{
		String text;
		currX = me.getX();
		currY = me.getY();

		g2D.setColor(primaryColor);

		if (g2D != null )
		{
			if (state == TEXT)
			{
				text = JOptionPane.showInputDialog(this, "Type text here");
				
				if (text != null)
				{	
					if (underlineActive)
						g2D.setFont(font.deriveFont(attr));
					else
						g2D.setFont(font);

					if (textOpaque)
						setBackToOpaque(currX, currY, g2D.getFontMetrics(), text);
						
					g2D.drawString(text, currX, currY);
					repaint();
					parent.pushUndoStack();
					parent.snapShot(this);
					parent.undo.setEnabled(true);
					parent.setMadeChanges(true);
				}
			}
		} 
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent me)
	{	
		initX = me.getX();
		initY = me.getY();
		
		g2D.setColor(primaryColor);

		if (state == PENCIL)
		{
			g2D.setColor(primaryColor);
			g2D.drawLine(initX, initY, initX, initY);
			g2D.setColor(primaryColor);
			repaint();
			parent.setMadeChanges(true);
		}

	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent me)
	{
		// do nothing
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent me)
	{
		//do nothing
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent me)
	{
		currX = me.getX();
		currY = me.getY();

		g2D.setColor(primaryColor);

		if (g2D != null )
		{
			if (state == PENCIL)
			{
				g2D.setColor(primaryColor);
				g2D.drawLine(initX, initY, currX, currY);
				g2D.setColor(primaryColor);
				repaint();
				initX = currX;
				initY = currY;
			}
			else if (state == ERASER)
			{
				g2D.setColor(Color.WHITE);
				g2D.drawLine(initX, initY, currX, currY);
				g2D.setColor(Color.WHITE);
				repaint();
				initX = currX;
				initY = currY;
			}
			else if (state == LINE)
			{
				g2D.setColor(primaryColor);
				//g2D.drawLine(initX, initY, currX, currY);
				repaint();
				//clear();
			}
		}
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent me)
	{
		currX = me.getX();	
		currY = me.getY();

		g2D.setColor(primaryColor);

		width = Math.abs(initX-currX);
		height = Math.abs(initY-currY);
		
		if (g2D != null )
		{
			if (state == ERASER)
			{
				g2D.setColor(Color.WHITE);
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == PENCIL) {
				g2D.setColor(Color.BLACK);
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			
			else if (state == LINE)
			{
				g2D.setColor(primaryColor);
				g2D.drawLine(initX, initY, currX, currY);
				g2D.setColor(primaryColor);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == RECTANGLE)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillRect(Math.min(initX,currX), Math.min(initY,currY), width, height);
					g2D.setColor(primaryColor);
				}
				g2D.drawRect(Math.min(initX,currX), Math.min(initY,currY), width, height);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == SQUARE)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillRect(Math.min(initX,currX), Math.min(initY,currY), width, width);
					g2D.setColor(primaryColor);
				}
				g2D.drawRect(Math.min(initX,currX), Math.min(initY,currY), width, width);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == ROUND_RECTANGLE)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillRoundRect(Math.min(initX,currX), Math.min(initY,currY), width, height, width/4, height/4);
					g2D.setColor(primaryColor);
				}
				g2D.drawRoundRect(Math.min(initX,currX), Math.min(initY,currY), width, height, width/4, height/4);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == ROUND_SQUARE)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillRoundRect(Math.min(initX,currX), Math.min(initY,currY), width, width, width/4, width/4);
					g2D.setColor(primaryColor);
				}
				g2D.drawRoundRect(Math.min(initX,currX), Math.min(initY,currY), width, width, width/4, width/4);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == OVAL)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillOval(Math.min(initX,currX), Math.min(initY,currY), width, height);
					g2D.setColor(primaryColor);
				}
				g2D.drawOval(Math.min(initX,currX), Math.min(initY,currY), width, height);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == CIRCLE)
			{
				if (textOpaque)
				{
					g2D.setColor(secondaryColor);
					g2D.fillOval(Math.min(initX,currX), Math.min(initY,currY), width, width);
					g2D.setColor(primaryColor);
				}
				g2D.drawOval(Math.min(initX,currX), Math.min(initY,currY), width, width);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				parent.setMadeChanges(true);
			}
			else if (state == SELECTOR)
			{
				g2D.drawRect(Math.min(initX,currX), Math.min(initY,currY), width, height);
				setSelectorDimension(Math.min(initX,currX), Math.min(initY,currY), width, height);
				repaint();
				parent.pushUndoStack();
				parent.snapShot(this);
				parent.undo.setEnabled(true);
				
			}
		}
	}
	
	/**
	 *{@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent me)
	{
		//do nothing
	}
}
