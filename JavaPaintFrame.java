import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Iterator;

/**
 * The Main window with a menu, toolbars and the Paint panel
 */
public class JavaPaintFrame extends JFrame implements ActionListener, KeyListener
{
	static final long serialVersionUID = 2L;

	static final private String TITLE = "To Draw Or Not To Draw";
	static final private String[] EXTENSIONS = { ".jpeg", ".png", ".jpg", ".gif" };
	static final private char[] CHRARS_NOT_SUPPORTED = { '`', '#', '%', '&', '{', '}', '\\', '?', '<', '>', '/', '|', '"' };

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
	static final private int PRIMARY_COLOR = 11;
	static final private int SECOND_COLOR = 12;

	static final private int TEXT_BOLD = 71; // first digit for Bold second digit denotes TEXT
	static final private int TEXT_ITALIC = 72;
	static final private int TEXT_UNDERLINE = 73;
	static final private int TEXT_OPAQUE = 74;

	static final private String DEFAULT_FONT = new String("Serif");
	static final private int DEFAULT_FONT_STYLE = Font.PLAIN;
	static final private int DEFAULT_FONT_SIZE = 18;

	private int INITIAL_FRAME_WIDTH;
	private int INITIAL_FRAME_HEIGHT;

	static final private String ABOUTUS = new String("This program is a built for EECS 3461 as Assignment #2\n"
					+ "The goal is to create a MS Paint like program with simple tools to draw\n Auther: Jhan Perera");

	// Odd count represents button active, Even count represents button deactive
	private int boldCount = 0;
	private int italicCount = 0;
	private int underlineCount = 0;
	private int textOpaqueCount = 0;
	private int rectCount = -1;
	private int ovalCount = -1;
	private int roundRectCount = -1;

	private JComboBox<String> fontFamilyList;
	private int textStyle = Font.PLAIN;
	private JComboBox<String> textSize;
	private JButton bold, italic, underline;
	private JButton textOpaqueButton;

	private ColorChooserButton buttonColor1, buttonColor2;
	private Color defaultColor = Color.BLACK, newPrimColor, newSecColor;
	// Hardcode the PreferredSize Dimenstions
	private Dimension PreSize = new Dimension(31, 31);

	JMenuItem newFile, open, close, save, saveAs, exit; // File Menu items open, close, save, saveAs, exit;
	JMenuItem undo, redo, cut, copy, paste, delete, selectAll; // Edit Menu Items
	//JMenuItem zoomIn, zoomOut; // View Menu Items
	JMenuItem about; // Help Menu Items

	File f;
	JFileChooser openFile, fileSaver;
	JScrollPane scrollPane;
	JEditorPane editArea;
	JPanel blank, tool;
	JToolBar toolBar, fontBar;

	JButton rectangle, oval, round_rectangle;

	ToDrawOrNotToDraw drawMaybe;

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	// ---------- AVI's Changes---------
	private String title;
	private String default_fileName = "Do'Noh.png";
	private String default_path = System.getProperty("user.dir") + "/My Drawings/";
	private String imageFilePath;
	private boolean newButtonPressed = false;
	private boolean openButtonPressed = false;
	boolean madeChanges = false; // dataChanged
	boolean savedOnce = false;

	// --------------BILAL's Changes---------------

	private Stack<Integer> UndoS = new Stack<Integer>();
	private Stack<Integer> RedoS = new Stack<Integer>();
	private int StackNumberUnS = 0;
	public int Flaggerator;

	public JavaPaintFrame()
	{
		// Set Look and feel of the frame to the system default
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}

		// Set the icon
		Image icon = Toolkit.getDefaultToolkit().getImage("Images/mainIcon.png");
		this.setIconImage(icon);

		// create scroll pane
		/*
		 * Due to the scroll pane it is easier to switch between which panel
		 * should be active. Any panel inside scroll panel is active. Initially
		 * no panel should be active till the "New" button is selected, so blank
		 * panel is created to deactivate the drawMaybe panel
		 */
		this.setTitle(TITLE);
		// Open the windows in full screen
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		// this.setSize(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT);
		this.pack();

		// Get the size of the screen and set the paint area that size
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		INITIAL_FRAME_WIDTH = dim.width;
		INITIAL_FRAME_HEIGHT = dim.height;

		setMinimumSize(new Dimension(600, 600));
		setMaximumSize(new Dimension(dim.width, dim.height));
		setPreferredSize(new Dimension(dim.width, dim.height));

		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// create a blank panel to put in the scrollpane when no file is open
		blank = new JPanel();
		blank.setOpaque(true);
		blank.setBackground(Color.gray);
		scrollPane.setViewportView(blank);

		// The Menu bar - Root of all the menus
		JMenuBar mb = new JMenuBar();
		this.setJMenuBar(mb);

		// -----------------
		// Different Menus on Menu bar
		// -----------------

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		mb.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		mb.add(editMenu);

		/*JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_I);
		mb.add(viewMenu);*/

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		mb.add(helpMenu);

		// ------------------
		// File menu items
		// ------------------

		newFile = new JMenuItem("New", new ImageIcon("Images/new.gif"));
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		open = new JMenuItem("Open...", new ImageIcon("Images/open.gif"));
		open.setMnemonic(KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		close = new JMenuItem("Close", new ImageIcon("Images/close.gif"));
		close.setMnemonic(KeyEvent.VK_C);
		close.setEnabled(false);

		save = new JMenuItem("Save", new ImageIcon("Images/save.gif"));
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.setEnabled(false);

		saveAs = new JMenuItem("Save as...", new ImageIcon("Images/blank.gif"));
		saveAs.setMnemonic(KeyEvent.VK_A);
		saveAs.setEnabled(false);

		exit = new JMenuItem("Exit", new ImageIcon("Images/exit.gif"));
		exit.setMnemonic(KeyEvent.VK_X);

		fileMenu.add(newFile);
		fileMenu.add(open);
		fileMenu.add(close);
		fileMenu.addSeparator();
		fileMenu.add(save);
		fileMenu.add(saveAs);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		// ------------------
		// Edit menu items
		// ------------------

		undo = new JMenuItem("Undo", new ImageIcon("Images/undo.gif"));
		undo.setMnemonic(KeyEvent.VK_U);
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undo.setEnabled(false);

		redo = new JMenuItem("Redo", new ImageIcon("Images/redo.gif"));
		redo.setMnemonic(KeyEvent.VK_R);
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redo.setEnabled(false);

		cut = new JMenuItem("Cut", new ImageIcon("Images/cut.gif"));
		cut.setMnemonic(KeyEvent.VK_T);
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		cut.setEnabled(false);

		copy = new JMenuItem("Copy", new ImageIcon("Images/copy.gif"));
		copy.setMnemonic(KeyEvent.VK_C);
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		copy.setEnabled(false);

		paste = new JMenuItem("Paste", new ImageIcon("Images/paste.gif"));
		paste.setMnemonic(KeyEvent.VK_P);
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		paste.setEnabled(false);

		delete = new JMenuItem("Delete", new ImageIcon("Images/delete.gif"));
		delete.setMnemonic(KeyEvent.VK_D);
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)); // No modifyer needed for this
		delete.setEnabled(false);

		selectAll = new JMenuItem("Select All", new ImageIcon("Images/selectAll.gif"));
		selectAll.setMnemonic(KeyEvent.VK_A);
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK)); // No modifyer needed for this
		selectAll.setEnabled(false);

		editMenu.add(undo);
		editMenu.add(redo);
		editMenu.addSeparator();
		editMenu.add(cut);
		editMenu.add(copy);
		editMenu.add(paste);
		editMenu.add(delete);
		editMenu.addSeparator();
		editMenu.add(selectAll);

		/* Due to Lack of time we were unable to implement this section of the application
		//------------------
		// View menu items
		// ------------------

		zoomIn = new JMenuItem("Zoom In", new ImageIcon("Images/zoomIn.gif"));
		zoomIn.setMnemonic(KeyEvent.VK_PLUS);
		zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, ActionEvent.CTRL_MASK));
		zoomIn.setEnabled(false);

		zoomOut = new JMenuItem("Zoom Out", new ImageIcon("Images/zoomOut.gif"));
		zoomOut.setMnemonic(KeyEvent.VK_MINUS);
		zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		zoomOut.setEnabled(false);

		viewMenu.add(zoomIn);
		viewMenu.add(zoomOut);
		*/
		
		// ------------------
		// Help menu items
		// ------------------

		about = new JMenuItem("About", new ImageIcon("Images/about.gif"));
		about.setMnemonic(KeyEvent.VK_A);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.ALT_MASK));

		helpMenu.add(about);

		// ---------------
		// Toolbar buttons
		// ---------------

		// Create the toolbar.
		toolBar = new JToolBar("", JToolBar.VERTICAL);
		addToolbarButtons(toolBar);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		// Font toolbar
		fontBar = new JToolBar("Text Font");
		addTextToolbarButtons(fontBar);
		fontBar.setFloatable(false);
		fontBar.setRollover(false);

		// --------------------
		// Listeners
		// --------------------

		// For Menu Items
		newFile.addActionListener(this);
		open.addActionListener(this);
		close.addActionListener(this);
		save.addActionListener(this);
		saveAs.addActionListener(this);
		exit.addActionListener(this);
		undo.addActionListener(this);
		redo.addActionListener(this);
		cut.addActionListener(this);
		copy.addActionListener(this);
		paste.addActionListener(this);
		delete.addActionListener(this);
		selectAll.addActionListener(this);
		//zoomIn.addActionListener(this);
		//zoomOut.addActionListener(this);
		about.addActionListener(this);

		// ---------------------
		// Layout Initialization
		// ---------------------

		// put components in a panel
		JPanel contentPane = new JPanel(new BorderLayout());
		// panel.add(buttons, BorderLayout.NORTH);
		contentPane.add(toolBar, BorderLayout.WEST);
		contentPane.add(fontBar, BorderLayout.NORTH);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// make panel this JFrame's content pane
		this.setContentPane(contentPane);

		// set up a file chooser to present *.txt and *.java files in the current directory

		openFile = new JFileChooser(new File("."));
		fileSaver = new JFileChooser(new File("."));

		MyFileFilter imageFileter = new MyFileFilter(EXTENSIONS);
		openFile.addChoosableFileFilter(imageFileter);
		openFile.setFileFilter(imageFileter);
		fileSaver.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSaver.setFileFilter(imageFileter);
	}

	/**
	 * Adds buttons in the passed toolbar
	 *
	 * @param toolBar The toolbar to add buttons in
	 */
	protected void addToolbarButtons(JToolBar toolBar)
	{
		JButton toolButton = null;

		toolButton = makeToolbarButtons("cursor", CURSOR, "Cursor");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("pencil", PENCIL, "Pencil");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("eraser", ERASER, "Eraser");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("line", LINE, "Line");
		toolBar.add(toolButton);

		rectangle = new JButton();
		rectangle.setActionCommand("" + RECTANGLE);
		rectangle.setToolTipText("Change To Square");
		rectangle.setIcon(new ImageIcon("Images/rectangle.gif", "Rectangle"));
		rectangle.setSelectedIcon(new ImageIcon("Images/square.gif"));
		rectangle.addActionListener(this);
		toolBar.add(rectangle);

		round_rectangle = new JButton();
		round_rectangle.setActionCommand("" + ROUND_RECTANGLE);
		round_rectangle.setToolTipText("Change To Rounded Square");
		round_rectangle.setIcon(new ImageIcon("Images/round_rectangle.gif", "Round Rectangle"));
		round_rectangle.setSelectedIcon(new ImageIcon("Images/round_square.gif"));
		round_rectangle.addActionListener(this);
		toolBar.add(round_rectangle);

		oval = new JButton();
		oval.setActionCommand("" + OVAL);
		oval.setToolTipText("Change To Circle");
		oval.setIcon(new ImageIcon("Images/oval.gif", "Oval"));
		oval.setSelectedIcon(new ImageIcon("Images/circle.gif"));
		oval.addActionListener(this);
		toolBar.add(oval);

		toolButton = makeToolbarButtons("text", TEXT, "Text");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("selector", SELECTOR, "Selector");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("defaultColor", PRIMARY_COLOR, "Primary Color");
		toolBar.add(toolButton);

		toolButton = makeToolbarButtons("defaultColor2", SECOND_COLOR, "Fill Color");
		toolBar.add(toolButton);

	}

	/**
	 * Adds buttons to the passed font toolbar
	 *
	 * @param fontBar The font toolBar to add JButtons in
	 */
	protected void addTextToolbarButtons(JToolBar fontBar)
	{
		JButton textToolButton = null;

		GraphicsEnvironment gEnv = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		String[] fontFamily = gEnv.getAvailableFontFamilyNames();
		int selectedFontIndex = getIndexOf(fontFamily, DEFAULT_FONT);

		// textToolButton = makeToolbarButtons("bold", TEXT_BOLD, "Bold");
		bold = new JButton();
		bold.setActionCommand("" + TEXT_BOLD);
		bold.setToolTipText("Bold");
		bold.setIcon(new ImageIcon("Images/bold.gif", "Bold"));
		bold.setSelectedIcon(new ImageIcon("Images/selected_bold.gif"));
		bold.addActionListener(this);
		fontBar.add(bold);

		// textToolButton = makeToolbarButtons("italic", TEXT_ITALIC, "Italic");
		italic = new JButton();
		italic.setActionCommand("" + TEXT_ITALIC);
		italic.setToolTipText("Italic");
		italic.setIcon(new ImageIcon("Images/italic.gif", "Italic"));
		italic.setSelectedIcon(new ImageIcon("Images/selected_italic.gif"));
		italic.addActionListener(this);
		fontBar.add(italic);

		// textToolButton = makeToolbarButtons("underline", TEXT_UNDERLINE, "Underline");
		underline = new JButton();
		underline.setActionCommand("" + TEXT_UNDERLINE);
		underline.setToolTipText("underline");
		underline.setIcon(new ImageIcon("Images/underline.gif", "Underline"));
		underline.setSelectedIcon(new ImageIcon("Images/selected_underline.gif"));
		underline.addActionListener(this);
		fontBar.add(underline);

		fontFamilyList = new JComboBox<String>(fontFamily);
		fontFamilyList.setPreferredSize(fontFamilyList.getPreferredSize());

		if (selectedFontIndex > 0)
			fontFamilyList.setSelectedIndex(getIndexOf(fontFamily, DEFAULT_FONT) - 1);

		fontFamilyList.addActionListener(this);
		fontBar.add(fontFamilyList);

		textSize = new JComboBox<String>(fillTextSizeBox());
		textSize.setSelectedIndex(17); // Selected item at size[17] = 18
		textSize.addActionListener(this);
		fontBar.add(textSize);

		textOpaqueButton = new JButton();
		textOpaqueButton.setActionCommand("" + TEXT_OPAQUE);
		textOpaqueButton.setToolTipText("Set To Opaque");
		textOpaqueButton.setIcon(new ImageIcon("Images/opaque.gif", "Opaque"));
		textOpaqueButton.setSelectedIcon(new ImageIcon("Images/transparent.gif"));
		textOpaqueButton.addActionListener(this);
		fontBar.add(textOpaqueButton);
	}

	/**
	 * Returns a custom button with the passed properties
	 *
	 * @param imageURL A string representation of the path in the directory where the icon for the button exists
	 * @param actionCommand An integer representation of the command the button accepts
	 * @param toolTipText A hint for the buttons functionality
	 *
	 * @return Returns a JButton with the passed properties
	 */
	protected JButton makeToolbarButtons(String imageName, int actionCommand, String toolTipText) {
		// Create and initialize the button.
		JButton button = new JButton();

		if ((imageName != null) || toolTipText != null)
		{ //imageName is null
			// If imageName is any of the two Color identifiers.
			if (imageName == "defaultColor")
			{
				// Create a global reference for later call backs
				buttonColor1 = new ColorChooserButton(Color.BLACK);
				button = buttonColor1;
				// Set the newPrimColor to be defalted
				newPrimColor = buttonColor1.getSelectedColor();
				// Set the Background color to default
				button.setBackground(defaultColor);
				// Set the Size
				button.setMaximumSize(PreSize);
				button.setMinimumSize(PreSize);
				button.setPreferredSize(PreSize);
				button.setActionCommand("" + actionCommand);
				button.setToolTipText(toolTipText);
				button.addActionListener(this);
			}
			// Second Color
			else if (imageName == "defaultColor2")
			{
				// Create a global reference for later call backs
				buttonColor2 = new ColorChooserButton(Color.WHITE);
				button = buttonColor2;
				// Set the newSecColor to be defaulted.
				newSecColor = buttonColor2.getSelectedColor();
				// Set the Background color to White
				button.setBackground(defaultColor);
				// Set the Size.
				button.setMaximumSize(PreSize);
				button.setMinimumSize(PreSize);
				button.setPreferredSize(PreSize);
				button.setActionCommand("" + actionCommand);
				button.setToolTipText(toolTipText);
				button.addActionListener(this);
			}
			else
			{
				String path = "Images/" + imageName + ".gif";
				/*
				 * In this segment we are going to use actionCommand, the method
				 * setActionCommand takes a String value of the action the
				 * button has
				 */
				button.setActionCommand("" + actionCommand);
				button.setToolTipText(toolTipText);
				button.addActionListener(this);

				button.setIcon(new ImageIcon(path, toolTipText));
			}
		}
		else
		{ //imageName is not null
			button = new JButton();
			// no image found
			button.setText(toolTipText);
			System.err.println("Resource not found");
			return null;
		}

		return button;
	}

	// Stack operations and methods used to accomodate save and redos and undos
	// BILAL

	public void pushUndoStack() {
		StackNumberUnS++;
		UndoS.push(StackNumberUnS);

		while (!(RedoS.isEmpty())) {
			int a = (int) RedoS.pop();
			File file = new File(".\\BIN\\" + a + ".png");
			file.delete();
			// System.out.printf("Redo %d\n",a);
		}
		RedoS.clear();
		redo.setEnabled(false);
	}

	public void snapShot(ToDrawOrNotToDraw d) {
		BufferedImage BI = new BufferedImage(d.getWidth(), d.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		d.paint(BI.createGraphics());
		File imageFile = new File(".\\BIN\\" + Integer.toString(StackNumberUnS)
				+ ".png");
		try {
			imageFile.createNewFile();
			ImageIO.write(BI, "png", imageFile);
		} catch (Exception ex) {
		}
	}

	public void snapShot(ToDrawOrNotToDraw d, int i) {
		BufferedImage BI = new BufferedImage(d.getWidth(), d.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		d.paint(BI.createGraphics());
		File imageFile = new File(".\\BIN\\" + Integer.toString(i) + ".png");
		try {
			imageFile.createNewFile();
			ImageIO.write(BI, "png", imageFile);
		} catch (Exception ex) {
		}
	}

	public Image RetrieveSnapShot(int filename) {
		return Toolkit.getDefaultToolkit().getImage(
				".\\BIN\\" + Integer.toString(filename) + ".png");
	}

	public Stack<Integer> getUndoS() {
		return UndoS;
	}

	public Stack<Integer> getRedoS() {
		return RedoS;
	}
	
	public int StatusFlaggerator(){
		return Flaggerator;
	}

	/**
	 * Set the value of madeChanges to the passed value
	 *
	 * @param change <code>true</code> if changes were made, otherwise <code>false</code>
	 */
	public void setMadeChanges(boolean change)
	{
		madeChanges = change;
		
		save.setEnabled(change);
		cut.setEnabled(change);
		copy.setEnabled(change);
		delete.setEnabled(change);
		selectAll.setEnabled(change);
	}

	/**
	 * Returns the index at which the passed key is stored in the passed array
	 *
	 * @param array The array to search the key in
	 * @param key The key to search
	 *
	 * @return Returns the index of the next string after the key, otherwise returns zero
	 */
	public int getIndexOf(String[] array, String key)
	{
		for (int index = 0; index < array.length; index++)
		{
			if (array[index].equals(key))
				return (index + 1);
		}

		return 0;
	}

	/**
	 * Returns an array of String of numbers for fomt siz
	 *
	 * @return Returns am array of string of font sizes
	 */
	public String[] fillTextSizeBox()
	{
		String[] fontSize = new String[40];
		for (int index = 0; index < 40; index++)
			fontSize[index] = "" + (index + 1);

		return fontSize;
	}

	/**
	 * Sets the dimension of the scrollPane to the passed Dimension
	 *
	 * @param dim
	 *            The new dimension to set the scrollPane
	 */
	public void setScrollPaneSize(Dimension dim)
	{
		if ((dim.getWidth() > screenSize.getWidth())
				|| (dim.getHeight() > screenSize.getHeight()))
			dim = screenSize;

		scrollPane.setSize(dim);
		// scrollPane.setBounds(35, 40, (int)dim.getWidth(), (int)dim.getHeight());
	}

	/**
	 * Checks whether the fileName passed is valid or not. Validation is
	 * determined by whether the fileName follows the format this application
	 * uses to save a file. A valid fileName follows the following regular
	 * expression "^[a-Z][0-9][.]{1}[EXTENSIONS]$" where the valid extensions
	 * are predefined by the application
	 *
	 * @param fileName The title and extension inputed by the user
	 *
	 * @return Returns <code>true</code> if passed fileName follows the valid format, otherwise returns <code>false</code>.
	 */
	public boolean isValid(String fileName)
	{
		int dot = fileName.indexOf('.');
		String extension = fileName.substring(dot);
		//System.out.println(fileName + "\t" + extension + "\t" + dot);
		int moreDots = extension.indexOf(1);
		if (dot < 0)
		{ // No '.' is present
			JOptionPane.showMessageDialog(this,
									"File name must have valid extension.\nCheck \"About\" to know how to save files.",
									"Extension Error",
									JOptionPane.ERROR_MESSAGE
									);
			return false;
		}
		if (dot == 0)
		{ // the fileName starts with a '.'
			JOptionPane.showMessageDialog(this,
									"Please enter name of the image.\nCheck \"About\" to know how to save files.",
									"File Name Error",
									JOptionPane.ERROR_MESSAGE
									);
			return false;
		}
		if (moreDots >= 0)
		{ // There are more than one '.'
			JOptionPane.showMessageDialog(this,
									"File Name cannot have more than one \'.\'\nCheck \"About\" to know how to save files.",
									"Error",
									JOptionPane.ERROR_MESSAGE
									);
			return false;
		}
		if ((dot + 1) == fileName.length())
		{ // The fileName ends with a '.'
			JOptionPane.showMessageDialog(this,
									"A valid extension must follow after a \'.\' to save the image in that format.\nCheck \"About\" to know how to save files.",
									"File Name Error",
									JOptionPane.ERROR_MESSAGE
									);
			return false;
		}
		
		for (int i = 0; i < CHRARS_NOT_SUPPORTED.length; i++)
		{
			if (fileName.indexOf(CHRARS_NOT_SUPPORTED[i]) >= 0)
			{
				//System.out.println(fileName.indexOf(CHRARS_NOT_SUPPORTED[i]));
				JOptionPane .showMessageDialog(this,
										"File Name cannot have the character \'" + CHRARS_NOT_SUPPORTED[i] + "\'\nCheck \"About\" to know how to save files.",
										"File Name Error",
										JOptionPane.ERROR_MESSAGE
										);
				return false;
			}
		}
		
		for (int i = 0; i < EXTENSIONS.length; i++)
		{
			if (extension.toLowerCase().equals(EXTENSIONS[i]))
				return true;
		}

		JOptionPane.showMessageDialog(this,
								"The extension \'" + extension + "\' is not a valid extension. Please enter a valid extension.\nCheck \"About\" to know how to save files.",
								"Extension Error",
								JOptionPane.ERROR_MESSAGE
								);
		return false;
	}

	/**
	 * Saves the passed Image in the passed path with the passed name and format.
	 *
	 * @param bImage The BufferedImage to save
	 * @param filePath The path at which to save the BufferedImage. <b>Note</b>: The
	 *            path include the filename and extension e.g. /.../fileName.png
	 * @param fileName The title and extension of the Image to be saved
	 */
	public void saveImage(ToDrawOrNotToDraw image, String filePath, String fileName)
	{
		BufferedImage bImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		image.paint(bImage.createGraphics());
		String extension = fileName.substring(fileName.indexOf('.') + 1).toLowerCase();

		try {
			ImageOutputStream  imageOutStream =  ImageIO.createImageOutputStream(new File(filePath));
			Iterator<ImageWriter> imageIterator = ImageIO.getImageWritersByFormatName(extension);
			ImageWriter imageWriter = imageIterator.next();

			imageWriter.setOutput(imageOutStream);
			imageWriter.write(null, new IIOImage(bImage, null, null), null);
			imageWriter.dispose();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------
	// Override Methods
	// -----------------

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		Object source = ae.getSource();

		// For File Menu Listeners
		if (source == newFile)
		{
			this.setTitle(default_fileName + " - " + TITLE);

			// create the drawing panel
			drawMaybe = new ToDrawOrNotToDraw(this);
			setScrollPaneSize(new Dimension(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT));
			scrollPane.setViewportView(drawMaybe);
			drawMaybe.requestFocus();
			drawMaybe.setState(CURSOR);
			
			saveAs.setEnabled(true);
			close.setEnabled(true);
			paste.setEnabled(true);

			newButtonPressed = true;
			openButtonPressed = false;
		}
		
		else if (source == open)
		{
			// show the file chooser 'open' dialog box and get user response
			if (openFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				f = openFile.getSelectedFile();

				imageFilePath = f.toString().replace('\\', '/');
				default_fileName = new String(imageFilePath.substring(imageFilePath.lastIndexOf('/') + 1));
				this.setTitle(default_fileName + " (" + imageFilePath + ")" + " - " + TITLE);

				Image image = Toolkit.getDefaultToolkit().getImage(imageFilePath);

				drawMaybe = new ToDrawOrNotToDraw(this, image);
				while ((drawMaybe.getImageFileWidth() < 0) || (drawMaybe.getImageFileHeight() < 0))
					setScrollPaneSize(drawMaybe.getImageDimension());

				// Set the Panel to scroll when resized.
				drawMaybe.setPreferredSize(new Dimension(drawMaybe.getImageFileWidth() - 39, drawMaybe.getImageFileHeight() - 123));
				Flaggerator = 666;
				
				drawMaybe.scrollRectToVisible(new Rectangle(50, 50, 600, 600));
				drawMaybe.setAutoscrolls(true);
				scrollPane.setViewportView(drawMaybe);
                
				drawMaybe.requestFocus();
				drawMaybe.setState(CURSOR);
                	
				saveAs.setEnabled(true);
				close.setEnabled(true);
				paste.setEnabled(true);

				openButtonPressed = true;
				newButtonPressed = false;
			}
		}

		else if (source == save)
		{
			if (!savedOnce && !openButtonPressed)
			{ // First time saving and image has not been loaded from some directory
				String saveFilePath = default_path;
				fileSaver.setSelectedFile(new File(default_fileName));

				while (true)
				{ // Until a valid fileName is inserted or till the cancel option is selected
					int savePops = fileSaver.showSaveDialog(this);
					if (savePops == JFileChooser.APPROVE_OPTION)
					{ // In the file Chooser the save Option is selected
						default_fileName = fileSaver.getSelectedFile().getName().trim();
						saveFilePath = fileSaver.getSelectedFile().getAbsolutePath();
					}
					else if (savePops == JFileChooser.CANCEL_OPTION)
					{ // In the file Chooser the cancel Option is selected
						savedOnce = false;
						break;
					}

					if (isValid(default_fileName))
					{ // user's fileName is in a valid format
						File toCheck = new File(saveFilePath);
						//System.out.println(saveFilePath);
						if (toCheck.exists() && !toCheck.isDirectory())
						{ // User defined fileName already exists in the defined directory

							int overwrite = JOptionPane.showConfirmDialog(this, "File Aldready!\nWould you like to overwrite the existing file?", "File Exists", JOptionPane.OK_CANCEL_OPTION);
							if (overwrite == JOptionPane.OK_OPTION)
							{ // User decides to overwrite the exixting file with the current one
								try {
									Path path = Paths.get(saveFilePath);
									Files.deleteIfExists(path);
									saveImage(drawMaybe, saveFilePath, default_fileName);
									default_path = saveFilePath;
									savedOnce = true;
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							}

						}
						else
						{ // user defined fileName does not exist in the defined directory
							saveImage(drawMaybe, saveFilePath, default_fileName);
							default_path = saveFilePath;
							savedOnce = true;
							//System.out.println(saveFilePath + "\t" + default_fileName);
							break;
						}
					}
				} // loop - End
			} // First time saving - End
			else 
			{ //file has already been saved once
				try {
					Path path = Paths.get(default_path);
					Files.delete(path);
					saveImage(drawMaybe, default_path, default_fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		
		else if (source == saveAs)
		{
			String saveFilePath = default_path;
			fileSaver.setSelectedFile(new File(default_fileName));

			while (true)
			{ // Until a valid fileName is inserted or till the cancel option is selected
				int savePops = fileSaver.showSaveDialog(this);
				if (savePops == JFileChooser.APPROVE_OPTION)
				{ // In the file Chooser the save Option is selected
					default_fileName = fileSaver.getSelectedFile().getName().trim();
					saveFilePath = fileSaver.getSelectedFile().getAbsolutePath();
				}
				
				else if (savePops == JFileChooser.CANCEL_OPTION)
				{ // In the file Chooser the cancel Option is selected
					break;
				}

				if (isValid(default_fileName)) 
				{ // user's fileName is in a valid format
					File toCheck = new File(saveFilePath);
					//System.out.println(saveFilePath);
					
					if (toCheck.exists() && !toCheck.isDirectory())
					{ // User defined fileName already exists in the defined directory
						int overwrite = JOptionPane.showConfirmDialog(this, "File Aldready!\nWould you like to overwrite the existing file?", "File Exists", JOptionPane.OK_CANCEL_OPTION);
						if (overwrite == JOptionPane.OK_OPTION)
						{ // User decides to overwrite the exixting file with the current one
							try {
								Path path = Paths.get(saveFilePath);
								Files.deleteIfExists(path);
								saveImage(drawMaybe, saveFilePath, default_fileName);
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						}
					}
					else
					{ // user defined fileName does not exist in the defined directory
						saveImage(drawMaybe, saveFilePath, default_fileName);
						//System.out.println(saveFilePath + "\t" + default_fileName);
						break;
					}
				}
			} // Loop - End
		}
		
		else if (source == close)
		{
			this.setTitle(TITLE);

			setScrollPaneSize(new Dimension(this.getWidth(), this.getHeight()));
			blank.setSize(this.getWidth(), this.getHeight());
			scrollPane.setViewportView(blank);

			StackNumberUnS = 0;

			while (!(RedoS.isEmpty()))
			{
				int a = (int) RedoS.pop();
				File file = new File(".\\BIN\\" + a + ".png");
				file.delete();
				// System.out.printf("Redo %d\n",a);
			}

			File initial_file = new File(".\\BIN\\" + Integer.toString(-1) + ".png");
			initial_file.delete();

			while (!(UndoS.isEmpty()))
			{
				int a = (int) UndoS.pop();
				File file = new File(".\\BIN\\" + a + ".png");
				file.delete();
				System.out.printf("Redo %d\n", a);
			}

			default_fileName = new String("Do'Noh.png");

			close.setEnabled(false);
			saveAs.setEnabled(false);
			undo.setEnabled(false);
			redo.setEnabled(false);
			paste.setEnabled(false);
			setMadeChanges(false);

			newButtonPressed = false;
			openButtonPressed = false;
			savedOnce = false;

			boldCount = 0;
			italicCount = 0;
			underlineCount = 0;
			textOpaqueCount = 0;
			rectCount = -1;
			roundRectCount = -1;
			ovalCount = -1;

			setScrollPaneSize(new Dimension(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT));
		}

		// For Edit Menu Listeners
		else if (source == undo)
		{
			try {
				int now = (int) UndoS.pop();
				RedoS.push(now);
				redo.setEnabled(true);
				int prev = (int) UndoS.peek();
				Image temp = this.RetrieveSnapShot(prev);

				drawMaybe = new ToDrawOrNotToDraw(this, temp);
				while ((drawMaybe.getImageFileWidth() < 0) || (drawMaybe.getImageFileHeight() < 0))
					setScrollPaneSize(drawMaybe.getImageDimension());
				scrollPane.setViewportView(drawMaybe);
				
			} catch(EmptyStackException e) {
				undo.setEnabled(false);
				System.out.println("PEEKER");
				Image temp = this.RetrieveSnapShot(-1);

				drawMaybe = new ToDrawOrNotToDraw(this, temp);
				while ((drawMaybe.getImageFileWidth() < 0) || (drawMaybe.getImageFileHeight() < 0))
					setScrollPaneSize(drawMaybe.getImageDimension());

				scrollPane.setViewportView(drawMaybe);		
			}
		}
		
		else if (source == redo)
		{
			try {
                	int prev = (int) RedoS.peek();
				int now = (int) RedoS.pop();
			     UndoS.push(now);
			     undo.setEnabled(true);
			     Image temp = this.RetrieveSnapShot(prev);

				drawMaybe = new ToDrawOrNotToDraw(this, temp);
				while ((drawMaybe.getImageFileWidth() < 0) || (drawMaybe.getImageFileHeight() < 0))
					setScrollPaneSize(drawMaybe.getImageDimension());
					
				scrollPane.setViewportView(drawMaybe);
			} catch(EmptyStackException e){
				System.out.println("SSSEEKER");
				redo.setEnabled(false);
			}
		}
		
		else if (source == cut)
		{
		}
		
		else if (source == copy)
		{
		}
		
		else if (source == paste)
		{
		}
		
		else if (source == delete)
		{
			drawMaybe.delete();
		}
		
		else if (source == selectAll)
		{
			drawMaybe.selectAll();
		}

		/* For View Menu Listeners
		else if (source == zoomIn)
		{

		}
		
		else if (source == zoomOut)
		{

		}*/

		// For Help Menu Listeners
		else if (source == about)
		{
			// Creat a sumple dialog for the About message.
			//JOptionPane.showMessageDialog(this, ABOUTUS);
			JavaPaintAboutFrame frame = new JavaPaintAboutFrame(this);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(dim.width / 4 - frame.getSize().width / 4, dim.height / 4 - frame.getSize().height / 4);

			frame.setPreferredSize(new Dimension(600, 200));
			frame.pack();
			//frame.setResizable(false);
			frame.setVisible(true);
		}

		// For Font Toolbar Button Listeners
		else if (source == fontFamilyList)
		{
			if (save.isEnabled())
				drawMaybe.setFontType(fontFamilyList.getSelectedItem().toString());
		}
		else if (source == textSize)
		{
			if (save.isEnabled())
			{
				drawMaybe.setFontSize(Integer.parseInt(textSize.getSelectedItem().toString()));
			}
		}
		else if (source == exit) 
		{
			System.exit(0);
		}

		// else From toolbar
		else
		{
			try {
				if (ae.getActionCommand() instanceof String)
				{
					// In case any one button listeners are absent
					int command = Integer.parseInt(ae.getActionCommand());
					if (command == CURSOR)
						drawMaybe.setState(CURSOR);

					else if (command == PENCIL)
						drawMaybe.setState(PENCIL);

					else if (command == ERASER)
						drawMaybe.setState(ERASER);

					else if (command == LINE)
						drawMaybe.setState(LINE);

					else if (command == RECTANGLE)
					{
						rectCount++;
						if ((rectCount % 2) == 1)
						{
							drawMaybe.setState(SQUARE);
							rectangle.setSelected(true);
							rectangle.setToolTipText("Change To Rectangle");
						}
						else
						{
							drawMaybe.setState(RECTANGLE);
							rectangle.setSelected(false);
							rectangle.setToolTipText("Change To Square");
						}
					}

					else if (command == ROUND_RECTANGLE)
					{
						roundRectCount++;
						if ((roundRectCount % 2) == 1)
						{
							drawMaybe.setState(ROUND_SQUARE);
							round_rectangle.setSelected(true);
							round_rectangle.setToolTipText("Change To Round Rectangle");
						}
						else
						{
							drawMaybe.setState(ROUND_RECTANGLE);
							round_rectangle.setSelected(false);
							round_rectangle.setToolTipText("Change To Round Square");
						}
					}

					else if (command == OVAL)
					{
						ovalCount++;
						if ((ovalCount % 2) == 1)
						{
							drawMaybe.setState(CIRCLE);
							oval.setSelected(true);
							oval.setToolTipText("Change To Oval");
						}
						else
						{
							drawMaybe.setState(OVAL);
							oval.setSelected(false);
							oval.setToolTipText("Change To Circle");
						}
					}

					else if (command == TEXT)
						drawMaybe.setState(TEXT);
					
					else if (command == TEXT_BOLD)
					{
						boldCount++;
						if (boldCount % 2 == 1)
						{
							drawMaybe.setBold(true);
							bold.setSelected(true);
						}
						else
						{
							drawMaybe.setBold(false);
							bold.setSelected(false);
						}
					}
					else if (command == TEXT_ITALIC)
					{
						italicCount++;
						if (italicCount % 2 == 1)
						{
							drawMaybe.setItalic(true);
							italic.setSelected(true);
						}
						else
						{
							drawMaybe.setItalic(false);
							italic.setSelected(false);
						}
					}
					else if (command == TEXT_UNDERLINE)
					{
						underlineCount++;
						if (underlineCount % 2 == 1)
						{
							drawMaybe.setUnderline(true);
							underline.setSelected(true);
						} else {
							drawMaybe.setUnderline(false);
							underline.setSelected(false);
						}
					}
					else if (command == TEXT_OPAQUE)
					{
						textOpaqueCount++;
						if (textOpaqueCount % 2 == 1)
						{
							drawMaybe.setTextOpaque(true);
							textOpaqueButton.setSelected(true);
							textOpaqueButton.setToolTipText("Set To Transparent");
						}
						else
						{
							drawMaybe.setTextOpaque(false);
							textOpaqueButton.setSelected(false);
							textOpaqueButton.setToolTipText("Set To Opaque");
						}
					}
					else if (command == PRIMARY_COLOR)
					{ // The primaryColor button has been selected.
						newPrimColor = buttonColor1.getSelectedColor();
						drawMaybe.setPrimaryColor(buttonColor1
								.getSelectedColor());
					}
					else if (command == SECOND_COLOR)
					{ // The secondaryCOlor button has been selected
						newSecColor = buttonColor2.getSelectedColor();
						drawMaybe.setSecondaryColor(buttonColor2.getSelectedColor());
					}
					
					else if (command == SELECTOR)
						drawMaybe.setState(SELECTOR);
				}
			} catch (NullPointerException e) {
				// e.printStackTrace();
			}
			drawMaybe.setPrimaryColor(newPrimColor);
			drawMaybe.setSecondaryColor(newSecColor);
		}
	}

	@Override
	public void keyPressed(KeyEvent ke) {
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	// The following is only needed for the first editing keystroke.
	// Set the 'madeChanges' flag and then remove the KeyListener.
	// This avoids an endless stream of calls to the keyTyped
	// method during editing.

	@Override
	public void keyTyped(KeyEvent ke) {

	}

	// -------------
	// inner classes
	// -------------

	/**
	 * A class to extend the FileFilter class (which is abstract) and implement
	 * the 'accept' and 'getDescription' methods.
	 */
	class MyFileFilter extends FileFilter {
		private String[] s;

		MyFileFilter(String[] sArg) {
			s = sArg;
		}

		// determine which files to display in the chooser
		@Override
		public boolean accept(File fArg) {
			// if the file is a directory, show it
			if (fArg.isDirectory())
				return true;

			// if the filename contains the extension, show it
			for (int i = 0; i < s.length; ++i) {
				if (fArg.getName().toLowerCase().indexOf(s[i].toLowerCase()) > 0)
					return true;
			}

			// filter out everything else
			return false;
		}

		@Override
		public String getDescription() {
			String tmp = "";
			for (int i = 0; i < s.length; ++i)
				tmp += "*" + s[i] + " ";

			return tmp;
		}
	}
}
