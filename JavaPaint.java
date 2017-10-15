import java.awt.*;
import java.io.File;

import javax.swing.*;

public class JavaPaint
{
	public static void main(String[] args)
	{
		File FD = new File(".\\BIN");
		File [] FA = FD.listFiles();
		
		if (FA.length != 0){
			int x =0;
			while (x < FA.length){
				FA[x].delete();
				x++;
			}
		}
		
		JavaPaintFrame frame = new JavaPaintFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set the location at which it opens at (CENTERED)
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		
		//Open the window in fullscreen 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 

		// Allow the fram to show up
		frame.setVisible(true);
	}
}



