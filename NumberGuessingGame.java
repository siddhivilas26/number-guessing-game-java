import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Files;

public class NumberGuessingGame {
    private static int numberToGuess = new Random().nextInt(100) + 1;

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server running on http://localhost:" + port);

        while (true) {
            Socket socket = serverSocket.accept();
            handleClient(socket);
        }
    }

    private static void handleClient(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream out = socket.getOutputStream();

        String line = in.readLine();
        if (line == null) return;
        String request = line.split(" ")[1];

        if (request.startsWith("/guess")) {
            String[] parts = request.split("\\?");
            if (parts.length > 1 && parts[1].startsWith("number=")) {
                try {
                    int guess = Integer.parseInt(parts[1].split("=")[1]);
                    String message;

                    // ✅ Reset condition
                    if (guess == -1) {
                        numberToGuess = new Random().nextInt(100) + 1;
                        message = "Game reset!";
                    }
                    else if (guess < numberToGuess) {
                        message = "Too low!";
                    } else if (guess > numberToGuess) {
                        message = "Too high!";
                    } else {
                        message = "Correct! The number was " + numberToGuess;
                        numberToGuess = new Random().nextInt(100) + 1; // reset after win
                    }
                    sendResponse(out, message, "text/plain");
                } catch (NumberFormatException e) {
                    sendResponse(out, "Invalid number!", "text/plain");
                }
            }
        } else {
            File file = new File("web" + (request.equals("/") ? "/index.html" : request));
            if (file.exists()) {
                String contentType = request.endsWith(".css") ? "text/css" : "text/html";
                sendResponse(out, new String(Files.readAllBytes(file.toPath())), contentType);
            } else {
                sendResponse(out, "404 Not Found", "text/plain");
            }
        }

        socket.close();
    }

    private static void sendResponse(OutputStream out, String content, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" +
                content;
        out.write(response.getBytes());
        out.flush();
    }
}
