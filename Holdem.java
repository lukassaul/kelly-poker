
//import java.util.*;
//import java.io.*;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import javax.swing.*;
import java.awt.*;
//import java.awt.print.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.*;

/**
* A gui to control poker simulations
* l. saul dec 06
*/
class Holdem extends JFrame implements ActionListener{
	public String user_dir = System.getProperty("user.dir");
	public String file_sep = System.getProperty("file.separator");
	public String save_dir = user_dir; // for now...
	public String CRLF = System.getProperty("line.separator");
	public String backupFileName = ".ctofSettings";
	public String outputFileName = "";

	// GUI Objects...
	private Container contentPane;
	private JButton runButton, stopButton, clearButton, showInfoButton;
	private JPanel eastPanel, northPanel, westPanel, buttonPanel;
	private JLabel oddsLabel, betLabel;
	private JTextField pocketField, flopField, turnField, riverField, bankField;
	private JTextField tableSizeField, opponentsInField, numSimulationsField;

	// poker objects
	private Table table;

	/**
	* the constructor for the GUI.  Constructing this object starts the program.
	*/
	public Holdem () {
		if (user_dir == null) user_dir = "";
		if (save_dir == null) save_dir = "";

		table = new Table();

		// Setting up GUI...
		setTitle("Holdem Helper");
		contentPane = getContentPane();
		// our window listener...
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				//saveSettings();
				System.exit(0);
			}
		});

		// GUI Components
		oddsLabel = new JLabel("This hand wins: 0%");
		betLabel = new JLabel("Kelly Bet: 0$");
		pocketField = new JTextField();
		flopField = new JTextField();
		turnField = new JTextField();
		riverField = new JTextField();
		bankField = new JTextField("1000");
		tableSizeField = new JTextField("10");
		opponentsInField = new JTextField();
		numSimulationsField = new JTextField("1000");
		runButton = new JButton("Run");
		runButton.addActionListener(this);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		showInfoButton = new JButton("Show Info");
		showInfoButton.addActionListener(this);
		westPanel = new JPanel();
		northPanel = new JPanel();
		eastPanel = new JPanel(); // empty for now...
		buttonPanel = new JPanel();

		// layouts
		westPanel.setLayout(new GridLayout(8,2));
		westPanel.add(new JLabel("# to simulate: "));
		westPanel.add(numSimulationsField);
		westPanel.add(new JLabel("Bankroll: "));
		westPanel.add(bankField);
		westPanel.add(new JLabel("Table Size: "));
		westPanel.add(tableSizeField);
		westPanel.add(new JLabel("Opponents In: "));
		westPanel.add(opponentsInField);
		westPanel.add(new JLabel("Pocket: "));
		westPanel.add(pocketField);
		westPanel.add(new JLabel("Flop: "));
		westPanel.add(flopField);
		westPanel.add(new JLabel("Turn: "));
		westPanel.add(turnField);
		westPanel.add(new JLabel("River: "));
		westPanel.add(riverField);

		northPanel.add(oddsLabel);
		northPanel.add(betLabel);
		buttonPanel.add(runButton);
		buttonPanel.add(clearButton);
		//buttonPanel.add(clearButton);
		buttonPanel.add(showInfoButton);
		//buttonPanel.add(nextIntervalButton);

		contentPane.add(buttonPanel, "South");
		contentPane.add(northPanel, "North");
		contentPane.add(westPanel, "West");
		contentPane.add(eastPanel, "East");

		//initialize time segments:
		pack();
		show();

		//file f = new file(user_dir + file_sep + backupFileName);
		//if (f.exists()) {
		//	loadSettings();
		//}
	}

	/** Here's where all the button presses and actions go...
	*
	*/
	public void actionPerformed(ActionEvent e) {
		Object source = e.getActionCommand();
		if (source == "Clear") {
			flopField.setText("");
			riverField.setText("");
			pocketField.setText("");
			turnField.setText("");
			pocketField.requestFocus(true);
			show();
		}

		if (source == "Run") {
			float percent = 0.0f;
			int kellyBet = 0;

			// first read fields
			int numSimulations = 1000;
			int bank = 0;
			int tableSize = 0;
			int opponentsIn = 0;
			String pocket = "";
			String flop = "";
			String turn = "";
			String river = "";
			try {
				numSimulations = Integer.parseInt(numSimulationsField.getText());
				pocket = pocketField.getText();
				flop = flopField.getText();
				turn = turnField.getText();
				bank = Integer.parseInt(bankField.getText());
				tableSize = Integer.parseInt(tableSizeField.getText());
				opponentsIn = Integer.parseInt(opponentsInField.getText());
			} catch (Exception ee) { ee.printStackTrace(); }

			// run some simulations
			if (river.length()==0 & turn.length()==0 & flop.length()==0) {
				// simulate only pocket

				table = new Table(tableSize);
				Card c1 = new Card(pocket.substring(0,2));
				Card c2 = new Card(pocket.substring(2,4));
				percent = table.simulate(numSimulations,c1,c2);
				updatePercent(percent);
				float kelly = (percent*opponentsIn+percent-1.0f)/(float)opponentsIn;
				updateKelly( (int)(kelly*bank));
				System.out.println("kelly: " + kelly);
				System.out.println("c1: " + c1 + " answer: " + percent);
			}

			else if (river.length()==0 & turn.length()==0 & flop.length()==6) {

				table = new Table(tableSize);
				Card c1 = new Card(pocket.substring(0,2));
				Card c2 = new Card(pocket.substring(2,4));
				Card c3 = new Card(flop.substring(0,2));
				Card c4 = new Card(flop.substring(2,4));
				Card c5 = new Card(flop.substring(4,6));
				percent = table.simulate(numSimulations,c1,c2,c3,c4,c5);
				float kelly = (percent*opponentsIn+percent-1.0f)/(float)opponentsIn;
				updateKelly( (int)(kelly*bank));
				System.out.println("kelly: " + kelly);
				updatePercent(percent);
				System.out.println("c1: " + c1 + " answer: " + percent);
			}

			else if (river.length()==0 & turn.length()==2 & flop.length()==6) {

				table = new Table(tableSize);
				Card c1 = new Card(pocket.substring(0,2));
				Card c2 = new Card(pocket.substring(2,4));
				Card c3 = new Card(flop.substring(0,2));
				Card c4 = new Card(flop.substring(2,4));
				Card c5 = new Card(flop.substring(4,6));
				Card c6 = new Card(turn.substring(0,2));
				percent = table.simulate(numSimulations,c1,c2,c3,c4,c5,c6);
				float kelly = (percent*opponentsIn+percent-1.0f)/(float)opponentsIn;
				updateKelly( (int)(kelly*bank));
				System.out.println("kelly: " + kelly);
				updatePercent(percent);
				System.out.println("c1: " + c1 + " answer: " + percent);
			}

			else if (river.length()==2 & turn.length()==2 & flop.length()==6) {

				table = new Table(tableSize);
				Card c1 = new Card(pocket.substring(0,2));
				Card c2 = new Card(pocket.substring(2,4));
				Card c3 = new Card(flop.substring(0,2));
				Card c4 = new Card(flop.substring(2,4));
				Card c5 = new Card(flop.substring(4,6));
				Card c6 = new Card(turn.substring(0,2));
				Card c7 = new Card(river.substring(0,2));
				percent = table.simulate(numSimulations,c1,c2,c3,c4,c5,c6,c7);
				float kelly = (percent*opponentsIn+percent-1.0f)/(float)opponentsIn;
				updateKelly( (int)(kelly*bank));
				System.out.println("kelly: " + kelly);
				updatePercent(percent);
				System.out.println("c1: " + c1 + " answer: " + percent);
			}


		}
		//System.out.println(e+"");
	}

	private void updatePercent(float f) {
		oddsLabel.setText("This hand wins: " + f);
		show();
	}

	private void updateKelly(int bet) {
		betLabel.setText("Kelly Bet: " + bet);
		show();
	}


	/**
	*  use this method to run this shit
	*/
	public static void main(String[] args) {
		Holdem pr = new Holdem();
	}

}
