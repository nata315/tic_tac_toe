//Обработка подключения клиентов


package server;

import shared.GameMessage;
import shared.Move;
import shared.GameState;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler implements Runnable {
    private Socket clientSocket;
    private GameManager gameManager;
    private AuthManager authManager;

    public ConnectionHandler(Socket clientSocket, GameManager gameManager, AuthManager authManager) {
        this.clientSocket = clientSocket;
        this.gameManager = gameManager;
        this.authManager = authManager;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (true) {
                GameMessage message = (GameMessage) in.readObject();

                switch (message.getType()) {
                    case "LOGIN":
                        handleLogin(message, out);
                        break;
                    case "REGISTER":
                        handleRegister(message, out);
                        break;
                    case "MOVE":
                        handleMove(message, out);
                        break;
                    case "NEW_GAME":
                        handleNewGame(message, out);
                        break;
                    case "GET_GAME_STATE":
                        handleGetGameState(message, out);
                        break;
                    default:
                        System.out.println("Неизвестный тип сообщения: " + message.getType());
                }
            }
        } catch (EOFException e) {
            System.out.println("Клиент отключился корректно");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка соединения: " + e.getMessage());
        }
    }

    private void handleLogin(GameMessage message, ObjectOutputStream out) throws IOException {
        String username = (String) message.getData("username");
        String password = (String) message.getData("password");

        boolean success = authManager.authenticate(username, password);

        GameMessage response = new GameMessage("LOGIN_RESPONSE");
        response.addData("success", success);
        if (success) {
            response.addData("username", username);
        }

        out.writeObject(response);
        out.flush();
    }

    private void handleRegister(GameMessage message, ObjectOutputStream out) throws IOException {
        String username = (String) message.getData("username");
        String password = (String) message.getData("password");

        boolean success = authManager.register(username, password);

        GameMessage response = new GameMessage("REGISTER_RESPONSE");
        response.addData("success", success);
        response.addData("message", success ? "Регистрация успешна" : "Пользователь уже существует");

        out.writeObject(response);
        out.flush();
    }

    private void handleMove(GameMessage message, ObjectOutputStream out) throws IOException {
        System.out.println("\n=== СЕРВЕР: Получен MOVE ===");

        String gameId = (String) message.getData("gameId");
        String player = (String) message.getData("player");
        Object moveObj = message.getData("move");

        if (moveObj instanceof Move) {
            Move move = (Move) moveObj;

            try {
                GameState gameState = gameManager.processMove(gameId, move, player);

                // Отправляем доску как List<String> для надежности
                List<String> boardAsList = new ArrayList<>();
                String[][] board = gameState.getBoard();

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        String cell = board[i][j];
                        boardAsList.add(cell == null ? "" : cell);
                    }
                }

                GameMessage response = new GameMessage("MOVE_RESPONSE");
                response.addData("success", true);
                response.addData("board", boardAsList);  // Отправляем как список
                response.addData("currentPlayer", gameState.getCurrentPlayer());
                response.addData("gameOver", gameState.isGameOver());
                response.addData("winner", gameState.getWinner());

                out.writeObject(response);
                out.flush();

            } catch (Exception e) {
                System.err.println("СЕРВЕР: Ошибка обработки хода: " + e.getMessage());
                e.printStackTrace();

                GameMessage response = new GameMessage("ERROR");
                response.addData("message", e.getMessage());
                out.writeObject(response);
                out.flush();
            }
        }
    }

    private void handleNewGame(GameMessage message, ObjectOutputStream out) throws IOException {
        String player = (String) message.getData("player");
        boolean vsAI = (Boolean) message.getData("vsAI");

        GameManager.GameSession session;
        if (vsAI) {
            session = gameManager.createGameWithAI(player);
        } else {
            session = gameManager.createNewGame(player, "Waiting for opponent...");
        }

        GameMessage response = new GameMessage("NEW_GAME_RESPONSE");
        response.addData("gameId", session.getGameId());
        response.addData("gameState", session.getGameState());
        response.addData("player2", session.getPlayer2());

        out.writeObject(response);
        out.flush();
    }

    private void handleGetGameState(GameMessage message, ObjectOutputStream out) throws IOException {
        String gameId = (String) message.getData("gameId");

        GameManager.GameSession session = gameManager.getGameSession(gameId);
        if (session != null) {
            GameMessage response = new GameMessage("GAME_STATE_RESPONSE");
            response.addData("gameState", session.getGameState());
            response.addData("gameOver", session.getGameState().isGameOver());
            response.addData("winner", session.getGameState().getWinner());

            out.writeObject(response);
            out.flush();
        } else {
            GameMessage response = new GameMessage("ERROR");
            response.addData("message", "Game not found");

            out.writeObject(response);
            out.flush();
        }
    }
}