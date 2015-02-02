


import java.awt.Robot;
import javax.swing.*;

class Weboggle  extends JFrame implements ActionListener{
	public String user_dir = System.getProperty("user.dir");
	public String file_sep = System.getProperty("file.separator");
	public String save_dir = user_dir; // for now...
	public String CRLF = System.getProperty("line.separator");
	public String backupFileName = ".ctofSettings";
	public String outputFileName = "";

	// GUI Objects...
	private Container contentPane;
	private JButton runButton, stopButton;
	private JPanel eastPanel, westPanel, buttonPanel;

	public JTextField lettersField;

	/**
	* the constructor for the GUI.  Constructing this object starts the program.
	*/
	public Weboggle () {
		if (user_dir == null) user_dir = "";
		if (save_dir == null) save_dir = "";
		doTI = false;

		// Setting up GUI...
		setTitle("Weboggle Cheater");
		contentPane = getContentPane();
		// our window listener...
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				//saveSettings();
				System.exit(0);
			}
		});

		lettersField = new JTextField();
		runButton = new JButton("Run");
		stopButton = new JButton("Stop");

		westPanel = new JPanel();
		westPanel.setLayout(new GridLayout(9,1));
		eastPanel = new JPanel(); // empty for now...

		buttonPanel = new JPanel();
		buttonPanel.add(statusLabel);
		buttonPanel.add(runButton);
		buttonPanel.add(stopButton);
		//buttonPanel.add(clearButton);
		buttonPanel.add(showInfoButton);
		//buttonPanel.add(nextIntervalButton);

		contentPane.add(buttonPanel, "South");
		contentPane.add(westPanel, "West");
		contentPane.add(eastPanel, "East");

		//initialize time segments:
		pack();
		show();

		file f = new file(user_dir + file_sep + backupFileName);
		if (f.exists()) {
			loadSettings();
		}
	}

	/** Here's where all the button presses and actions go...
	*
	*/
	public void actionPerformed(ActionEvent e) {


		Object source = e.getActionCommand();


		if (source == "Correlation Integral...") {

		//System.out.println(e+"");
	}



	/**
	*  use this method to run this shit
	*/
	public static void main(String[] args) {
		CTOFGui pr = new CTOFGui();
	}

	/*
	public void outputError() {
		System.out.println("Command line ASCII dump for Cluster Level 1 Data");
		System.out.println("Currently supports product 3 & product 28");
		System.out.println("Usage: java ASCIIDump fileToRead fileToWrite (options)\n");
		System.out.println("fileToRead must be in official format with official name");
		System.out.println("Warning: fileToWrite will be overwritten!");
		System.out.println("Options:  ");
		System.out.println("-o  :  (for product 3) - make ascii in original form");
		System.out.println("Without this option, the data will be presented like PHA data.");
		System.exit(0);
	}*/

}
