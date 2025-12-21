package shared;

import java.io.Serializable;

public class SimpleGameState implements Serializable {
    private SerializableBoard board;
    private char currentPlayer;
    private boolean gameOver;
    private String winner;

    public SimpleGameState() {
        this.board = new SerializableBoard();
        this.currentPlayer = 'X';
        this.gameOver = false;
        this.winner = null;
    }

    public SimpleGameState(String[][] boardState, char currentPlayer, boolean gameOver, String winner) {
        this.board = new SerializableBoard(boardState);
        this.currentPlayer = currentPlayer;
        this.gameOver = gameOver;
        this.winner = winner;
    }

    // Геттеры и сеттеры
    public SerializableBoard getBoard() { return board; }
    public void setBoard(SerializableBoard board) { this.board = board; }

    public char getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(char currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    // Конвертеры
    public static SimpleGameState fromGameState(GameState gameState) {
        if (gameState == null) return null;

        SimpleGameState simpleState = new SimpleGameState();
        simpleState.setBoard(new SerializableBoard(gameState.getBoard()));
        simpleState.setCurrentPlayer(gameState.getCurrentPlayer());
        simpleState.setGameOver(gameState.isGameOver());
        simpleState.setWinner(gameState.getWinner());
        return simpleState;
    }

    public GameState toGameState() {
        GameState gameState = new GameState();
        gameState.setBoard(this.board.getBoard());
        gameState.setCurrentPlayer(this.currentPlayer);
        gameState.setGameOver(this.gameOver);
        gameState.setWinner(this.winner);
        return gameState;
    }

    public void print() {
        System.out.println("SimpleGameState:");
        System.out.println("Current player: " + currentPlayer);
        System.out.println("Game over: " + gameOver);
        System.out.println("Winner: " + winner);
        board.print();
    }
}