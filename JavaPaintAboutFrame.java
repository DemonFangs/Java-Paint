import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.event.*;
import java.lang.String;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class JavaPaintAboutFrame extends JDialog implements ActionListener
{
	static final long serialVersionUID = 5L;

	private JButton intro, save, hints, disclaimer, back, readme;
	private JTextArea introL, saveL, hintsL, disclaimerL, readmeL;
	private JScrollPane scrollPane;
	private BufferedReader in;
	private String line;

	private JavaPaintFrame parent;

	public JavaPaintAboutFrame(JavaPaintFrame parent)
	{
		this.parent = parent;
		this.setTitle("About");

		Image icon = Toolkit.getDefaultToolkit().getImage("Images/about.gif");
		this.setIconImage(icon);

		scrollPane = new JScrollPane();
		
		intro = new JButton("Introduction");
		intro.addActionListener(this);

		save = new JButton("How to Save");
		save.addActionListener(this);

		hints = new JButton("Some Hints");
		hints.addActionListener(this);

		disclaimer = new JButton("Disclaimer");
		disclaimer.addActionListener(this);

		readme = new JButton("readme");
		readme.addActionListener(this);

		back = new JButton("<< Back");
		back.addActionListener(this);

		try
		{
			introL = new JTextArea();
			in = new BufferedReader(new FileReader(new File("intro.txt")));
			line = in.readLine();
			while(line != null){
				introL.append(line + "\n");
				line = in.readLine();
			}
			introL.setEnabled(false);

			saveL = new JTextArea();
			in = new BufferedReader(new FileReader(new File("save.txt")));
			line = in.readLine();
			while(line != null){
				saveL.append(line + "\n");
				line = in.readLine();
			}
			saveL.setEnabled(false);

			hintsL = new JTextArea();
			in = new BufferedReader(new FileReader(new File("hints.txt")));
			line = in.readLine();
			while(line != null){
				hintsL.append(line + "\n");
				line = in.readLine();
			}
			hintsL.setEnabled(false);

			disclaimerL = new JTextArea();
			in = new BufferedReader(new FileReader(new File("disclaimer.txt")));
			line = in.readLine();
			while(line != null){
				disclaimerL.append(line + "\n");
				line = in.readLine();
			}
			disclaimerL.setEnabled(false);

			readmeL = new JTextArea();
			in = new BufferedReader(new FileReader(new File("readme.txt")));
			line = in.readLine();
			while(line != null){
				readmeL.append(line + "\n");
				line = in.readLine();
			}
			readmeL.setEnabled(false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(6, 1));
		buttons.add(intro);
		buttons.add(save);
		buttons.add(hints);
		buttons.add(disclaimer);
		buttons.add(readme);
		buttons.add(back);
		
		JPanel contentPane =  new JPanel(new BorderLayout());
		contentPane.add(buttons, BorderLayout.WEST);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		this.setContentPane(contentPane);
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source == intro)
		{
			scrollPane.setViewportView(introL);
		}
		else if (source == save)
		{
			scrollPane.setViewportView(saveL);
		}
		else if (source == hints)
		{
			scrollPane.setViewportView(hintsL);
		}
		else if (source == disclaimer)
		{
			scrollPane.setViewportView(disclaimerL);
		}
		else if (source == readme)
		{
			scrollPane.setViewportView(readmeL);
		}
		else if (source == back)
		{
			parent.setVisible(true);
			this.dispose();
		}
	}
}
