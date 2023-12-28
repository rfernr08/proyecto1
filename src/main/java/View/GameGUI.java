package View;
import javax.swing.*;
import javax.swing.undo.UndoManager;

import Control.ButtonControl;
import Control.FileHandler;
import Control.SolutionFinder;
import Model.Token;
import Model.Result;
import Model.GenerateMoves;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class GameGUI {
    private JFrame frame;
    static private JPanel boardPanel;
    private JMenu mainMenu;
    private JMenuBar menuBar;
    private JPanel buttonPanel;
    private JButton newGameButton;
    private String[] colors = {"Red", "Green", "Blue"};
    private JButton saveGameButton;
    private JButton loadGameButton;
    private JMenuItem findSolutionItem;
    private JMenu editMenu;
    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private UndoManager undoManager;
    private static JButton[][] boardButtons; // Modified line
    private static JTextArea infoArea;
    public static int movimiento = 1;

    private JTextField movesField;
    private JTextField scoreField;
    private JTextField tokensField;

    private GameState gameState = GameState.SETTING_UP;


    public enum GameState {
        SETTING_UP,
        PLAYING
    }

    public GameGUI() {
        frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        //boardPanel = new JPanel();
        //boardPanel.setLayout(new GridLayout(rows, cols)); // Modified line

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        mainMenu = new JMenu("Main Menu");
        menuBar.add(mainMenu);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frame.add(buttonPanel, BorderLayout.NORTH);

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                play();
            }
        });
        buttonPanel.add(newGameButton);

        // // Initialize the boardButtons array.
        // boardButtons = new JButton[rows][cols];

        // // Create a button for each cell in the grid.
        // for (int i = 0; i < rows; i++) {
        //     for (int j = 0; j < cols; j++) {
        //         boardButtons[i][j] = new JButton();
        //         boardButtons[i][j].addActionListener(new ActionListener() {
        //             public void actionPerformed(ActionEvent e) {
        //                 // Handle the button click.
        //                 TokenButton.handleButtonClick(getCurrentBoard(), rows, cols);
        //             }
        //         });
        //         boardPanel.add(boardButtons[i][j]);
        //     }
        // }
        frame.setJMenuBar(menuBar);
        //frame.add(boardPanel);
        frame.setVisible(true);

        saveGameButton = new JButton("Save Game");
        saveGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    FileHandler.saveGameToFile(boardPanel, file);
                }
            }
        });
        buttonPanel.add(saveGameButton);

        loadGameButton = new JButton("Load Game");
        loadGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    FileHandler.loadGameFromFile(boardPanel, file, colors);
                }
            }
        });
        buttonPanel.add(loadGameButton);

        frame.add(buttonPanel, BorderLayout.NORTH);

        infoArea = new JTextArea();
        infoArea.setEditable(false); 
        frame.add(new JScrollPane(infoArea), BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GameGUI();
            }
        });
    }

    private void play() {
        String rowsInput = JOptionPane.showInputDialog(frame, "Enter number of rows:");
        String colsInput = JOptionPane.showInputDialog(frame, "Enter number of columns:");
        int rows = Integer.parseInt(rowsInput);
        int cols = Integer.parseInt(colsInput);

        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(rows, cols));

        boardButtons = new JButton[rows][cols]; // Added line

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                boardButtons[i][j] = new JButton(); // Added line
                final int row = i;
                final int col = j;
                boardButtons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (gameState == GameState.SETTING_UP) {
                            // Ask for the color and set it
                            Color chosenColor = colorChooser(frame);
                            boardButtons[row][col].setBackground(chosenColor);
                        } else if (gameState == GameState.PLAYING) {
                            // Handle the button click.
                            ButtonControl.handleButtonClick(getCurrentBoard(), row, col);
                        }
                    }
                });
                boardPanel.add(boardButtons[i][j]);
            }
        }
        frame.add(boardPanel, BorderLayout.CENTER);
        // boardPanel.revalidate();
        // boardPanel.repaint();

        findSolutionItem = new JMenuItem("Find Solution");
        findSolutionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Llama al método findSolution en un hilo de trabajador en segundo plano
                        SolutionFinder.findSolution(getCurrentBoard());
                        return null;
                    }
                };
        
                worker.execute();
            }
        });
        menuBar.add(findSolutionItem);

        undoManager = new UndoManager();

        editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
    
        undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });
        editMenu.add(undoItem);

        redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });
        editMenu.add(redoItem);

        findSolutionItem.setEnabled(false);
        undoItem.setEnabled(false);
        redoItem.setEnabled(false);

        JButton startPlayingButton = new JButton("Start Playing");
        startPlayingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Check if the board is fully complete
                if(checkBoard(rows, cols)){
                    gameState = GameState.PLAYING;
                    // If the board is fully complete, change the game state to PLAYING
                    findSolutionItem.setEnabled(true);
                    undoItem.setEnabled(true);
                    redoItem.setEnabled(true);
                }   
            }
        });
        // Add the startPlayingButton to the boardPanel
        boardPanel.add(startPlayingButton);

        // Create the panel for displaying the result of a move
        JTextArea moveResultArea = new JTextArea();
        JScrollPane moveResultPanel = new JScrollPane(moveResultArea);

        // Create the panel for displaying the game stats
        JPanel gameStatsPanel = new JPanel(new GridLayout(3, 2));
        gameStatsPanel.add(new JLabel("Number of moves:"));
        movesField = new JTextField();
        gameStatsPanel.add(movesField);
        gameStatsPanel.add(new JLabel("Current total score:"));
        scoreField = new JTextField();
        gameStatsPanel.add(scoreField);
        gameStatsPanel.add(new JLabel("Remaining tokens on board:"));
        tokensField = new JTextField();
        gameStatsPanel.add(tokensField);

        // Create a split pane and add the panels to it
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, moveResultPanel, gameStatsPanel);
        frame.add(splitPane, BorderLayout.SOUTH);
    }

    public static Token[][] getCurrentBoard() {
        int rows = boardButtons.length;
        int cols = boardButtons[0].length;
        Token[][] currentBoard = new Token[rows][cols];
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Get the color of the button
                Color buttonColor = boardButtons[i][j].getBackground();
                // Create a new Token with the color of the button
                currentBoard[i][j] = new Token(getCharColor(buttonColor), i, j);
            }
        }
        return currentBoard;
    }

    // public void actionPerformed(ActionEvent e) {
    //     JButton clickedButton = (JButton) e.getSource();
    //     // Handle the button click.
    // }

    public static void updateBoard(Token[][] fixedBoard) {
        // Remove all existing buttons from the boardPanel
        boardPanel.removeAll();
        // Create new buttons that reflect the current state of the fixedBoard
        for (int i = 0; i < fixedBoard.length; i++) {
            for (int j = 0; j < fixedBoard[i].length; j++) {
                JButton button = new JButton();
                button.setBackground(getVisualColor(fixedBoard[i][j].getColor()));
                final int row = i;
                final int col = j;
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Handle the button click.
                        ButtonControl.handleButtonClick(fixedBoard, row, col);
                    }
                });
                // Add the button to the boardPanel
                boardPanel.add(button);
            }
        }
        // Revalidate and repaint the boardPanel to reflect the new buttons
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    public boolean checkBoard(int rows, int cols){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (boardButtons[i][j].getBackground() == null) {
                    JOptionPane.showMessageDialog(frame, "The board is not fully complete.");
                    return false;
                }
            }
        }
        return true;
    }

    public static void showResult(Result data){
        if(data.getPoints() == 1){
            infoArea.append("Movimiento "+(movimiento)+" en ("+data.getXPosition()+", "+data.getYPosition()+"): eliminó "+data.getGroupLength()+" fichas de color "+data.getGroupColor()+" y obtuvo "+data.getPoints()+" punto.");  
        }else{
            infoArea.append("Movimiento "+(movimiento)+" en ("+data.getXPosition()+", "+data.getYPosition()+"): eliminó "+data.getGroupLength()+" fichas de color "+data.getGroupColor()+" y obtuvo "+data.getPoints()+" puntos.");  
        }
        movimiento++;
        if(checkEnd()){
            if(remainingTokens == 1){
                infoArea.append("Puntuación final: "+finalScore+", quedando "+remainingTokens+" ficha.");
            }else{
                infoArea.append("Puntuación final: "+finalScore+", quedando "+remainingTokens+" fichas.");            
            }
            int option = JOptionPane.showConfirmDialog(null, "¿Quieres guardar el resultado del juego en un archivo de texto?", "Guardar resultado", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                // El jugador quiere guardar el resultado del juego.
                // Aquí puedes llamar a tu método para guardar el resultado en un archivo de texto.
            } else {
                // El jugador no quiere guardar el resultado del juego.
                // No hagas nada o maneja esta situación como prefieras.
            }
        }
    }

    public static boolean checkEnd(){
        Token[][] board = getCurrentBoard();
        ArrayList<LinkedList<Token>> groups = new ArrayList<>();
        boolean[][] visited = new boolean[board.length][board[0].length];
        for (boolean[] fila : visited)
            Arrays.fill(fila, false);
        for (int i = board.length - 1; i >= 0; i--) {
            for (int j = 0; j < board[0].length; j++) {
                if (visited[i][j] == false && board[i][j].valid())
                    groups.add(GenerateMoves.formGroup(board, visited, i, j, board.length - 1, board[0].length - 1));
            }
        }
        Iterator<LinkedList<Token>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            LinkedList<Token> currentGroup = groupIterator.next();
            int groupLength = currentGroup.size();
            if (groupLength >= 2) {
                return false;
            }
        }
        return true;
    }

    public static Color getVisualColor(char boardColor){
        if(boardColor == 'R'){
            return Color.RED;
        }else if(boardColor == 'V'){
            return Color.GREEN;
        }else if(boardColor == 'A'){
            return Color.BLUE;
        }else {
            return Color.WHITE;

        }
    }

    public static char getCharColor(Color boardColor){
        if(boardColor == Color.RED){
            return 'R';
        }else if(boardColor == Color.GREEN){
            return 'V';
        }else if(boardColor == Color.BLUE){
            return 'A';
        }else {
            return '_';
        }
    }

    public static Color colorChooser(JFrame frame){
        Object[] options = {"Blue", "Red", "Green", "Blank"};
        int n = JOptionPane.showOptionDialog(frame,
            "Choose a color for this button:",
            "Choose Color",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        switch (n) {
            case 0: return Color.BLUE;
            case 1: return Color.RED;
            case 2: return Color.GREEN;
            case 3: return Color.WHITE;
            default: return null; // This will be returned if the user closes the dialog without choosing an option
        }
    }

    public void updateMovesField(int numberOfMoves) {
        movesField.setText(String.valueOf(numberOfMoves));
    }
    
    public void updateScoreField(int totalScore) {
        scoreField.setText(String.valueOf(totalScore));
    }
    
    public void updateTokensField(int remainingTokens) {
        tokensField.setText(String.valueOf(remainingTokens));
    }
}
