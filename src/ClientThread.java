import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class ClientThread extends Thread{

    private String username ="";
    private BufferedReader bReader;
    private PrintWriter pWriter;
    private Socket socket;
    private DataHolder data;
    private List<Card> playerCards = new ArrayList<>();
    private int totalBet = 0;
    private int currentBet = 0;
    private int totalMoney = 0;
    private boolean recievedBet = false;

    public ClientThread(final Socket socket, final DataHolder data,int min,int max)
    {
        this.socket=socket;
        this.data=data;
        try{
            bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pWriter = new PrintWriter(socket.getOutputStream(),true);
            username = bReader.readLine();
            write("12"+min);
            write("13"+max);
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        this.start();
    }

    public void bettingOver()
    {
        totalBet=0;
        currentBet=0;
    }
    public void addCard(Card newCard)
    {
        playerCards.add(newCard);
    }
    public List<Card> getCards()
    {
        return playerCards;
    }
    public void clearCards()
    {
        playerCards.clear();
    }

    public int getTotalMoney()
    {
        return totalMoney;
    }
    public void setTotalMoney(int newMoney)
    {
        totalMoney=totalMoney+newMoney;
    }
    public int getTotalBet()
    {
        return totalBet;
    }
    public void addToTotalBet()
    {
        totalBet=totalBet+currentBet;
    }
    public int getCurrentBet()
    {
        return currentBet;
    }
    public void setCurrentBet(int bet)
    {
        currentBet=currentBet+bet;
        totalMoney=totalMoney-bet;
    }
    public boolean checkRecievedBet()
    {
        return recievedBet;
    }
    public void write(String message)
    {
        if(message.substring(0,2).equals("06")||message.substring(0,2).equals("08"))
        {
            recievedBet=false;
        }
        pWriter.println(message);
    }

    /* players need to:

            code 1 See there cards
            code 2 See dealer cards
            code 3 know high bet
            code 4 know winning hand
            code 5 getWinnings
            code 6 bet

            FROM THE SERVER
            c1 get there cards
            c2 get dealer cards
            c3 know high bet
            c4 know winning hand
            c5 get winnings
            c7 who is blind
            c8 anti

            c9 total bet
            c10 current bet
            c11 totalmoney



     */
    public void run()
    {
        String message;

        while(true)
        {
            try {
                if((message=bReader.readLine())!=null)
                {
                    if(message.equals("exit"))
                    {
                        data.addToRemoveClients(this);
                    }
                    else
                    {
                        System.out.println(message);
                        if(message.substring(0,2).equals("06")||message.substring(0,2).equals("08"))
                        {
                            System.out.println("a bet was recieved");
                            currentBet=Integer.parseInt(message.substring(2));
                            recievedBet=true;
                        }
                        if(message.substring(0,2).equals("11"))
                        {
                            totalMoney=Integer.parseInt(message.substring(2));
                        }
                        //data.addMessage(message,this);





                    }
                }
            }
            catch(IOException e)
            {

            }
        }
    }

    public void close()
    {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername()
    {
        return username;
    }

}
