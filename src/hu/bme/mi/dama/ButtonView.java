package hu.bme.mi.dama;

import hu.bme.mi.utils.GameEvent.GameEvents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ButtonView extends JFrame {
	protected Board board;
	protected BoardButton[][] buttonArray;
	protected JPanel grid;
	protected JPanel bottomPanel;
	protected JLabel colorLabel;
	protected JLabel errorLabel;
	protected Color defaultBgColor;
	protected String previousSource;
	private ButtonController controller;

	public void highlightCell(Cell aCell) {
		buttonArray[aCell.getRow()][aCell.getColumn()].setBackground(new Color(
				0xFFFFFF));
	}

	public void setDefaultPaint(Cell aCell) {
		Color bgColor;
		if ((aCell.getRow() + aCell.getColumn()) % 2 != 0)
			bgColor = new Color(0x8DB3F1);
		else
			bgColor = new Color(0x518091);

		buttonArray[aCell.getRow()][aCell.getColumn()].setBackground(bgColor);
	}
	
	public void setDefaultPaintAll(){
		for (int j = 0; j < board.dimension; j++) {
			for (int i = 0; i < board.dimension; i++) {
				setDefaultPaint(new Cell(i, j));
			}
		}
	}

	public void init() {
		this.setLayout(new BorderLayout());
		grid.setLayout(new GridLayout(8, 8));
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				String bTxt;
				Cell aCell = new Cell(i, j);
				if (board.getFigure(aCell) != null)
					bTxt = board.getFigure(aCell).toString();
				else
					bTxt = "";

				buttonArray[i][j] = new BoardButton(bTxt);
				buttonArray[i][j].cell = aCell;
				buttonArray[i][j].setPreferredSize(new Dimension(50, 50));
				buttonArray[i][j].setMargin(new Insets(0, 0, 0, 0));
				setDefaultPaint(aCell);

				grid.add(buttonArray[i][j]);
			}
		}
		this.add(grid, BorderLayout.CENTER);

		// Az alsó státusz jelző beállítása
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
		bottomPanel.add(colorLabel);
		;
		colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottomPanel.add(errorLabel);
		errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		defaultBgColor = bottomPanel.getBackground();
		bottomPanel.setPreferredSize(new Dimension(300, 40));
		this.add(bottomPanel, BorderLayout.SOUTH);

		// Menüsáv hozzáadása
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		JMenu fileMenu = new JMenu("File");
		menubar.add(fileMenu);
		JMenuItem newAction = new JMenuItem("Új játék");
		//JMenuItem saveAction = new JMenuItem("Mentés");
		//JMenuItem loadAction = new JMenuItem("Betöltés");
		fileMenu.add(newAction);
		//fileMenu.addSeparator();
		//fileMenu.add(saveAction);
		//fileMenu.add(loadAction);

		// Menü eseménykezelője
		newAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object[] possibilities = { "Világos", "Sötét" };
				String s = (String) JOptionPane.showInputDialog(grid,
						"A kezdő szín beállítása:", "Beállítások",
						JOptionPane.PLAIN_MESSAGE, null, possibilities, 0);
				if (s != null) {
					boolean starter;
					board.reset();
					controller.status = GameEvents.KEEPGOING;
					if (s.equals("Világos"))
						starter = true;
					else
						starter = false;
					board.setWhiteOnTurn(starter);
					
					reset();
					
					board.start();
				}
			}
		});
	}

	public void reset() {
		refreshAll();
		delErrorLabel();
		refreshColorLabel();
	}
	
	public void refreshAll() {
		for (int j = 0; j < board.dimension; j++) {
			for (int i = 0; i < board.dimension; i++) {
				refresh(new Cell(i, j));
			}
		}
	}

	public void refresh(Cell aCell) {

		String bTxt = "";
		if (board.getFigure(aCell) != null)
			bTxt = board.getFigure(aCell).toString();
		else
			bTxt = "";
		buttonArray[aCell.getRow()][aCell.getColumn()].setText(bTxt);
	}

	public void setColorLabel(String s) {
		colorLabel.setText(s);
	}

	public void setErrorLabel(String s) {
		errorLabel.setText(s);
		bottomPanel.setBackground(new Color(0xFF0000));

	}
	
	public void setWinnerLabel(GameEvents status) {
		switch (status) {
		case WINNERBLACK:
			errorLabel.setText("A nyertes a Sötét!");
			bottomPanel.setBackground(new Color(0x0000FF));
			break;
			
		case WINNERWHITE:
			errorLabel.setText("A nyertes a Világos!");
			bottomPanel.setBackground(new Color(0x0000FF));
			break;
			
		case TIE:
			errorLabel.setText("Döntetlen!");
			bottomPanel.setBackground(new Color(0x0000FF));
			break;
		
		default:
			break;
		}
	}

	public void delErrorLabel() {
		errorLabel.setText("");
		bottomPanel.setBackground(defaultBgColor);
	}

	public Component[] getButtons() {
		return grid.getComponents();
	}

	public void refreshColorLabel() {
		colorLabel.setText(board.getWhiteOnTurnLabel());
	}

	public ButtonView(Board aBoard, ButtonController controller) {
		super("Checker Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.controller = controller;
		board = aBoard;
		buttonArray = new BoardButton[8][8];
		grid = new JPanel();
		bottomPanel = new JPanel();
		colorLabel = new JLabel("");
		refreshColorLabel();
		errorLabel = new JLabel("");
		previousSource = "";

		setMinimumSize(new Dimension(400, 300));
		setResizable(false);
		init();
		pack();
	}

}
