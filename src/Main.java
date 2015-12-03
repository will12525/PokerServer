import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class Main extends Thread{

    private final int port = 5000;
    private boolean running = true;
    final DataHolder data;
    private int buyInMin=10,buyInMax=200;

    public Main() throws Exception
    {
        data = new DataHolder(true,buyInMin,buyInMax);
        data.start();

        ServerSocket sSocket = new ServerSocket(port);
        System.out.println("Server ready on port " + port);

        this.start();

        while(running&&data.clientSize()<23) {
            Socket socket = sSocket.accept();
            ClientThread clientThread = new ClientThread(socket,data,buyInMin,buyInMax);

            if(!data.checkForClient(clientThread))
            {
                data.addClient(clientThread);
            }
        }

    }
    public void run()
    {
        boolean gameOn = true;
        boolean startGame = false;
        int timeDelay=30;
        long activate = System.currentTimeMillis();
        Deck deck;
        while(gameOn)
        {
            if(((System.currentTimeMillis()-activate)>(1000*timeDelay)))
            {
                if(data.clientSize()>1)
                {
                    startGame = true;
                }
                else
                {
                    activate=System.currentTimeMillis();
                }
            }
            else {
                String time = timeDelay-((System.currentTimeMillis()-activate)/1000)+"";
                System.out.println(time);
                data.addMessage("Seconds until next game: "+time);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(startGame)
            {
                data.setGameRunning(true);
                deck = new Deck(1);
                data.passDeck(deck);
                data.blind(buyInMin);
                data.distributePlayerCards();
                data.anti(buyInMin);


                data.distributeDealerCards(3);
                data.bets(false);


                data.distributeDealerCards(1);
                data.bets(false);


                data.distributeDealerCards(1);
                data.bets(false);


                //check winnings and distribute money
                data.newGame();
                startGame=false;

                activate=System.currentTimeMillis();
            }



        }
    }

    public static void main(String[] args) throws Exception{
        new Main();
    }
}