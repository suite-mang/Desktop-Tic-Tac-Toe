package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToe extends JFrame implements ActionListener {
    private final RoundedButton[][] buttons = new RoundedButton[3][3];
    private boolean xTurn = true;
    private int moveCount = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private final JLabel LabelStatus;
    private final JButton ButtonPlayer1;
    private final JButton ButtonPlayer2;
    private final JButton ButtonStartReset;

    private Player player1;
    private Player player2;
    private boolean player1IsHuman = true;
    private boolean player2IsHuman = false;

    private final Random random = new Random();
    private Timer computerMoveTimer;

    // Abstract Player class - Template Method Pattern
    abstract class   Player {
        protected String symbol;

        public Player(String symbol) {
            this.symbol = symbol;
        }

        // Template method - defines the algorithm structure
        public final void makeMove() {
            if (gameOver || !gameStarted) {
                return;
            }

            RoundedButton move = selectMove();
            if (move != null) {
                executeMove(move);
            }
        }

        // Abstract method - subclasses must implement
        protected abstract RoundedButton selectMove();

        // Concrete method - shared by all players
        protected void executeMove(RoundedButton button) {
            button.setText(symbol);
            button.setForeground(symbol.equals("X") ? Color.BLUE : Color.RED);

            moveCount++;

            if (checkWinner()) {
                LabelStatus.setText("The " + symbol + " Player (" + getPlayerType() + ") wins");
                gameOver = true;
                disableAllButtons();
            } else if (moveCount == 9) {
                LabelStatus.setText("Draw");
                gameOver = true;
                disableAllButtons();
            } else {
                xTurn = !xTurn;
                scheduleComputerMove();
            }
        }

        protected abstract String getPlayerType();
    }

    // Concrete implementation - Human Player
    class HumanPlayer extends Player {
        public HumanPlayer(String symbol) {
            super(symbol);
        }

        @Override
        protected RoundedButton selectMove() {
            // Human selection is handled by button clicks
            // This method won't be called directly for humans
            return null;
        }

        @Override
        protected String getPlayerType() {
            return "Human";
        }
    }

    // Concrete implementation - Robot Player
    class RobotPlayer extends Player {
        public RobotPlayer(String symbol) {
            super(symbol);
        }

        @Override
        protected RoundedButton selectMove() {
            return findBestMove();
        }

        @Override
        protected String getPlayerType() {
            return "Robot";
        }

        private RoundedButton findBestMove() {
            // Strategy implementation (in order of priority):
            // 1. Win if possible
            // 2. Block opponent from winning
            // 3. Take center if available
            // 4. Take a corner if available
            // 5. Take any available space

            String opponent = symbol.equals("X") ? "O" : "X";

            // 1. Try to win
            RoundedButton winMove = findWinningMove(symbol);
            if (winMove != null) return winMove;

            // 2. Block opponent from winning
            RoundedButton blockMove = findWinningMove(opponent);
            if (blockMove != null) return blockMove;

            // 3. Take center (position [1][1])
            if (isCellEmpty(buttons[1][1])) {
                return buttons[1][1];
            }

            // 4. Take a corner
            int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
            List<RoundedButton> availableCorners = new ArrayList<>();
            for (int[] corner : corners) {
                if (isCellEmpty(buttons[corner[0]][corner[1]])) {
                    availableCorners.add(buttons[corner[0]][corner[1]]);
                }
            }
            if (!availableCorners.isEmpty()) {
                return availableCorners.get(random.nextInt(availableCorners.size()));
            }

            // 5. Take any available space
            List<RoundedButton> availableMoves = new ArrayList<>();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (isCellEmpty(buttons[row][col])) {
                        availableMoves.add(buttons[row][col]);
                    }
                }
            }

            if (!availableMoves.isEmpty()) {
                return availableMoves.get(random.nextInt(availableMoves.size()));
            }

            return null;
        }

        private RoundedButton findWinningMove(String playerSymbol) {
            // Check all rows, columns, and diagonals for winning move

            // Check rows
            for (int i = 0; i < 3; i++) {
                if (countInLine(buttons[i][0], buttons[i][1], buttons[i][2], playerSymbol) == 2) {
                    return getEmptyButton(buttons[i][0], buttons[i][1], buttons[i][2]);
                }
            }

            // Check columns
            for (int i = 0; i < 3; i++) {
                if (countInLine(buttons[0][i], buttons[1][i], buttons[2][i], playerSymbol) == 2) {
                    return getEmptyButton(buttons[0][i], buttons[1][i], buttons[2][i]);
                }
            }

            // Check diagonals
            if (countInLine(buttons[0][0], buttons[1][1], buttons[2][2], playerSymbol) == 2) {
                return getEmptyButton(buttons[0][0], buttons[1][1], buttons[2][2]);
            }

            if (countInLine(buttons[0][2], buttons[1][1], buttons[2][0], playerSymbol) == 2) {
                return getEmptyButton(buttons[0][2], buttons[1][1], buttons[2][0]);
            }

            return null;
        }

        private int countInLine(RoundedButton b1, RoundedButton b2, RoundedButton b3, String playerSymbol) {
            int count = 0;
            if (b1.getText().equals(playerSymbol)) count++;
            if (b2.getText().equals(playerSymbol)) count++;
            if (b3.getText().equals(playerSymbol)) count++;
            return count;
        }

        private RoundedButton getEmptyButton(RoundedButton b1, RoundedButton b2, RoundedButton b3) {
            if (isCellEmpty(b1)) return b1;
            if (isCellEmpty(b2)) return b2;
            if (isCellEmpty(b3)) return b3;
            return null;
        }
    }

    // Inner class for rounded buttons
    static class RoundedButton extends JButton {
        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Button background
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            // Border
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);

            // Text
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Initialize players
        player1 = new HumanPlayer("X");
        player2 = new HumanPlayer("O");

        // Top panel with player controls
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        ButtonPlayer1 =  new RoundedButton("Human", 35);
        ButtonPlayer1.setFont(new Font("Arial", Font.BOLD, 12));
        ButtonPlayer1.setPreferredSize(new Dimension(100, 35));
        ButtonPlayer1.addActionListener(e -> togglePlayer1());

        ButtonPlayer2 = new RoundedButton("Robot", 35);
        ButtonPlayer2.setFont(new Font("Arial", Font.BOLD, 12));
        ButtonPlayer2.setPreferredSize(new Dimension(100, 35));
        ButtonPlayer2.addActionListener(e -> togglePlayer2());

        ButtonStartReset =new RoundedButton("Start", 35);
        ButtonStartReset.setFont(new Font("Arial", Font.BOLD, 12));
        ButtonStartReset.setPreferredSize(new Dimension(100, 35));
        ButtonStartReset.setBackground(new Color(70, 130, 180));
        ButtonStartReset.setForeground(Color.WHITE);
        ButtonStartReset.addActionListener(e -> startResetGame());

        //topPanel.add(new JLabel("Player 1:"));
        topPanel.add(ButtonPlayer1);
        topPanel.add(ButtonStartReset);
        //topPanel.add(new JLabel("Player 2:"));
        topPanel.add(ButtonPlayer2);


        // Game panel (3x3 grid)
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(3, 3, 5, 5));
        gamePanel.setBackground(new Color(240, 240, 240));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons with grid labels and names

        String[][] names = {
                {"ButtonA3", "ButtonB3", "ButtonC3"},
                {"ButtonA2", "ButtonB2", "ButtonC2"},
                {"ButtonA1", "ButtonB1", "ButtonC1"}
        };

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new RoundedButton(" ", 40);
                buttons[row][col].setName(names[row][col]);
                buttons[row][col].setFont(new Font("Arial", Font.BOLD, 40));
                buttons[row][col].setBackground(new Color(255, 200, 0));
                buttons[row][col].setForeground(new Color(200, 200, 200));
                buttons[row][col].setEnabled(false);
                buttons[row][col].addActionListener(this);
                gamePanel.add(buttons[row][col]);
            }
        }

        // Bottom panel with status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LabelStatus = new JLabel("Game is not started", SwingConstants.CENTER);
        LabelStatus.setName("LabelStatus");
        LabelStatus.setFont(new Font("Arial", Font.BOLD, 16));
        LabelStatus.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        bottomPanel.add(LabelStatus, BorderLayout.WEST);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void togglePlayer1() {
        if (!gameStarted) {
            if (player1 instanceof HumanPlayer) {
                player1IsHuman = !player1IsHuman;
                player1 = new RobotPlayer("X");
                ButtonPlayer1.setText("Robot");
            } else {
                player1 = new HumanPlayer("X");
                ButtonPlayer1.setText("Human");
            }
        }
    }

    private void togglePlayer2() {
        if (!gameStarted) {
            if (player2 instanceof RobotPlayer) {
                player2 = new RobotPlayer("O");
                ButtonPlayer2.setText("Human");
            } else {
                player2IsHuman = !player2IsHuman;
                player2 = new HumanPlayer("O");
                ButtonPlayer2.setText("Human");
            }
        }
    }

    private void startResetGame() {
        if (!gameStarted) {
            // Start the game
            gameStarted = true;
            gameOver = false;
            ButtonStartReset.setText("Reset");
            ButtonPlayer1.setEnabled(false);
            ButtonPlayer2.setEnabled(false);

            // Enable all buttons and set yellow
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    buttons[row][col].setEnabled(true);
                    //buttons[row][col].setBackground(new Color(255, 200, 0));
                }
            }

            LabelStatus.setText("Game is in progress");
            xTurn = true;
            moveCount = 0;

            // If X is computer, make first move
            if (player1 instanceof RobotPlayer) {
                scheduleComputerMove();

            }
        } else {
            // Reset the game
            resetGame();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RoundedButton clickedButton = (RoundedButton) e.getSource();

        // Ignore clicks if game is over or not started
        if (gameOver || !gameStarted) {
            return;
        }

        // Check if it's a human player's turn
        boolean currentPlayerIsHuman = (xTurn && player1IsHuman) || (!xTurn && player2IsHuman);
        if (!currentPlayerIsHuman) {
            return; // Computer's turn, ignore human clicks
        }

        // Only allow move if button hasn't been played
        if (clickedButton.getText().length() <= 2) {
            makeMove(clickedButton);
        }
    }

    private void makeMove(RoundedButton button) {
        if (xTurn) {
            button.setText("X");
            button.setForeground(Color.BLUE);
        } else {
            button.setText("O");
            button.setForeground(Color.RED);
        }

        moveCount++;


        if (checkWinner()) {
            String winner = xTurn ? "X" : "O";
            String playerType = (xTurn ? player1IsHuman : player2IsHuman) ? "Human" : "Robot";
            LabelStatus.setText("The " + winner + " Player (" + playerType + ") wins");
            gameOver = true;
            disableAllButtons();
        } else if (moveCount == 9) {
            LabelStatus.setText("Draw");
            gameOver = true;
            disableAllButtons();
        } else {
            // Switch turn
            xTurn = !xTurn;

            // If next player is computer, schedule their move
            boolean nextPlayerIsComputer = (xTurn && !player1IsHuman) || (!xTurn && !player2IsHuman);
            if (nextPlayerIsComputer) {
                scheduleComputerMove();
            }
        }
    }

    private void scheduleComputerMove() {
        // Add 1-second delay for computer move
        if (computerMoveTimer != null && computerMoveTimer.isRunning()) {
            computerMoveTimer.stop();
        }

        computerMoveTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeComputerMove();
            }
        });
        computerMoveTimer.setRepeats(false);
        computerMoveTimer.start();
    }

    private void makeComputerMove() {
        if (gameOver || !gameStarted) {
            return;
        }

        RoundedButton move = findBestMove();
        if (move != null) {
            makeMove(move);
        }
    }

    private RoundedButton findBestMove() {
        // Strategy implementation (in order of priority):
        // 1. Win if possible
        // 2. Block opponent from winning
        // 3. Take center if available
        // 4. Take a corner if available
        // 5. Take any available space

        String currentPlayer = xTurn ? "X" : "O";
        String opponent = xTurn ? "O" : "X";

        // 1. Try to win
        RoundedButton winMove = findWinningMove(currentPlayer);
        if (winMove != null) return winMove;

        // 2. Block opponent from winning
        RoundedButton blockMove = findWinningMove(opponent);
        if (blockMove != null) return blockMove;

        // 3. Take center (position [1][1]) - check if it's actually empty
        if (isCellEmpty(buttons[1][1])) {
            return buttons[1][1];
        }

        // 4. Take a corner
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        List<RoundedButton> availableCorners = new ArrayList<>();
        for (int[] corner : corners) {
            if (isCellEmpty(buttons[corner[0]][corner[1]])) {
                availableCorners.add(buttons[corner[0]][corner[1]]);
            }
        }
        if (!availableCorners.isEmpty()) {
            return availableCorners.get(random.nextInt(availableCorners.size()));
        }

        // 5. Take any available space
        List<RoundedButton> availableMoves = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (isCellEmpty(buttons[row][col])) {
                    availableMoves.add(buttons[row][col]);
                }
            }
        }

        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        return null;
    }

    private boolean isCellEmpty(RoundedButton button) {
        String text = button.getText();
        return text.length() <= 2 && !text.equals("X") && !text.equals("O");
    }

    private RoundedButton findWinningMove(String player) {
        // Check all rows, columns, and diagonals for winning move

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (countInLine(buttons[i][0], buttons[i][1], buttons[i][2], player) == 2) {
                return getEmptyButton(buttons[i][0], buttons[i][1], buttons[i][2]);
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (countInLine(buttons[0][i], buttons[1][i], buttons[2][i], player) == 2) {
                return getEmptyButton(buttons[0][i], buttons[1][i], buttons[2][i]);
            }
        }

        // Check diagonals
        if (countInLine(buttons[0][0], buttons[1][1], buttons[2][2], player) == 2) {
            return getEmptyButton(buttons[0][0], buttons[1][1], buttons[2][2]);
        }

        if (countInLine(buttons[0][2], buttons[1][1], buttons[2][0], player) == 2) {
            return getEmptyButton(buttons[0][2], buttons[1][1], buttons[2][0]);
        }

        return null;
    }

    private int countInLine(RoundedButton b1, RoundedButton b2, RoundedButton b3, String player) {
        int count = 0;
        if (b1.getText().equals(player)) count++;
        if (b2.getText().equals(player)) count++;
        if (b3.getText().equals(player)) count++;
        return count;
    }

    private RoundedButton getEmptyButton(RoundedButton b1, RoundedButton b2, RoundedButton b3) {
        if (isCellEmpty(b1)) return b1;
        if (isCellEmpty(b2)) return b2;
        if (isCellEmpty(b3)) return b3;
        return null;
    }

    private boolean checkWinner() {
        String symbol = xTurn ? "X" : "O";

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(symbol) &&
                    buttons[i][1].getText().equals(symbol) &&
                    buttons[i][2].getText().equals(symbol)) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (buttons[0][i].getText().equals(symbol) &&
                    buttons[1][i].getText().equals(symbol) &&
                    buttons[2][i].getText().equals(symbol)) {
                return true;
            }
        }

        // Check diagonals
        if (buttons[0][0].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][2].getText().equals(symbol)) {
            return true;
        }

        return buttons[0][2].getText().equals(symbol) &&
                buttons[1][1].getText().equals(symbol) &&
                buttons[2][0].getText().equals(symbol);
    }

    private void disableAllButtons() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col].setEnabled(false);
                buttons[row][col].setForeground(Color.BLACK);
                buttons[row][col].setBackground(new Color(200, 200, 200));
            }
        }
    }

    private void resetGame() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col].setText(" ");
                buttons[row][col].setForeground(Color.BLACK);
                buttons[row][col].setEnabled(false);
                buttons[row][col].setBackground(new Color(200, 200, 200));
            }
        }

        xTurn = true;
        moveCount = 0;
        gameOver = false;
        gameStarted = false;

        ButtonStartReset.setText("Start");
        ButtonPlayer1.setEnabled(true);
        ButtonPlayer2.setEnabled(true);
        LabelStatus.setText("Game is not started");

        if (computerMoveTimer != null) {
            computerMoveTimer.stop();
        }
    }


}