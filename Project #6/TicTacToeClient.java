
import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

/**
 * Created by xinyuan_wang on 5/26/17.
 */
public class TicTacToeClient {


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new TicTacToeClient().startNewGame();
    }

    private Socket socket;

    private Queue<Message> messageQueue;

    private Scanner scan;


    public TicTacToeClient() throws IOException {
        socket = new Socket("codebank.xyz",38006);
        scan = new Scanner(System.in);
        messageQueue = new ArrayDeque<>();
    }

    public void startNewGame() throws IOException, ClassNotFoundException {
        new Thread(new GamePlay(socket.getOutputStream())).start();
        receiveMsg();

    }

    private void receiveMsg() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        while(socket.isConnected())
        {
            Message incomingMsg;
            try {
                 incomingMsg= (Message) ois.readObject();
            }catch (EOFException e)
            {
                break;
            }
            synchronized (messageQueue) {
                messageQueue.add(incomingMsg);
                messageQueue.notify();
            }
        }
    }

    private class GamePlay implements Runnable
    {

        ObjectOutputStream oos;

        public GamePlay(OutputStream os) throws IOException {
            oos = new ObjectOutputStream(os);
        }

        @Override
        public void run() {
            try {
                connectToServer();
                gameloop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void connectToServer() throws IOException, ClassNotFoundException, InterruptedException {
            System.out.println("Please enter your user name:");
            oos.writeObject(new ConnectMessage(scan.nextLine()));
            oos.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));

        }

        private void gameloop() throws InterruptedException,IOException {
            boolean gameOver = false;
            Message msg;
            while(!gameOver)
            {
                synchronized (messageQueue)
                {
                    if(messageQueue.isEmpty())
                        messageQueue.wait();
                    msg =messageQueue.poll();
                }

                    if(msg instanceof BoardMessage)
                    {
                        displayBoard(((BoardMessage) msg).getBoard());
                        BoardMessage.Status status = ((BoardMessage) msg).getStatus();
                        switch (status)
                        {
                            case IN_PROGRESS:
                                putMarker();
                                break;
                            case PLAYER1_VICTORY:
                                System.out.println("You won!");
                                gameOver=true;
                                break;
                            case PLAYER2_VICTORY:
                                System.out.println("You lost!");
                                gameOver=true;
                                break;
                            case STALEMATE:
                                System.out.println("Stalemate!");
                                gameOver=true;
                                break;
                            case PLAYER1_SURRENDER:
                                System.out.println("You surrendered!");
                                gameOver=true;
                                break;
                            case PLAYER2_SURRENDER:
                                System.out.println("The other player surrendered!");
                                gameOver=true;
                                break;
                            case ERROR:
                                System.out.println("Error!");
                                break;
                        }
                    }

                    else if (msg instanceof ErrorMessage) {
                        System.out.println(((ErrorMessage) msg).getError());
                        if(((ErrorMessage) msg).getError().equals("Game stopping."))
                            gameOver=true;
                        else
                        putMarker();
                    }
                }
                oos.writeObject(new CommandMessage(CommandMessage.Command.EXIT));
        }

        private void putMarker() throws IOException {

            boolean inputCheck;
           do{
                System.out.println("X marker's turn. Enter row and column (from 1 to 3) separated by comma.");
                System.out.println("Or you can enter \"surrender\" to surrender.");
                System.out.print("Input: ");
                String input = scan.nextLine();
                if (input.equals("surrender")) {
                    inputCheck=true;
                    oos.writeObject(new CommandMessage(CommandMessage.Command.SURRENDER));
                }
                else {
                    String[] row_col = input.split(",");

                    try {
                        byte row = (byte) (Byte.parseByte(row_col[0])-1);
                        byte col = (byte) (Byte.parseByte(row_col[1])-1);
                        oos.writeObject(new MoveMessage(row, col));
                        inputCheck=true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                        inputCheck = false;
                    }
                }
            }while (!inputCheck);
        }

        private void displayBoard(byte[][] board)
        {
            for(int i=0;i<board.length;i++)
            {
                for(int j=0;j<board[i].length ;j++)
                {
                    String marker;
                    if(board[i][j]==1)
                        marker="X";
                    else if(board[i][j]==2)
                        marker="O";
                    else
                        marker=" ";
                    System.out.print("["+marker+"]");
                }
                System.out.println();
            }
        }
    }
}
