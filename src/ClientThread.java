import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class ClientThread extends Thread{

    /*
            c0 chat
            c1 receive player cards
            c2 get dealer cards
            c3 get high bet
            c4 request bet
            c5 tells player if blind
            c6 requests anti
            c7 get total bet for the round
            c8 get current bet for the betting instance
            c9 get total player money
            c10 gets buyInMin
            c11 gets buyInMax

            c12 request buy in money
            c13 gets pot
            c14 gets winning hand
            c15 get winnings
            c16 request name
            c17 check connection
            c18 new game
            c19 fold
    */
    private List<Card> playerCards = new ArrayList<>();
    private boolean requestBet = false;
    private boolean blind = false;
    private boolean requestAnti = false;
    private int totalBet = 0;
    private int currentBet = 0;
    private int totalMoney = 0;
    private boolean requestBuyIn=true;
    private boolean requestName = true;
    private String username ="";
    private boolean requestConnection = false;
    private long checkConnection;
    private boolean fold = false;

    private boolean firstRun=true;
    private boolean ready = false;
    private boolean removeClient = false;

    private int min,max;

    private PrintWriter pWriter;
    private DataHolder data;

    public ClientThread(final Socket socket, final DataHolder data,int min,int max)
    {
        checkConnection=System.currentTimeMillis();
        this.min=min;
        this.max=max;
        this.data=data;
        try{
            pWriter = new PrintWriter(socket.getOutputStream(),true);
        }catch(IOException e)
        {
            e.printStackTrace();
        }

        new MessageReceiver(this,socket).start();
        write("12");
        write("13");
        this.start();
    }

    public void run()
    {
        long broadcastTime = System.currentTimeMillis();
        while(true)
        {
            if(!requestName&&!requestBuyIn)
            {
                ready = true;
            }
            if((System.currentTimeMillis()-broadcastTime>(3000)||firstRun)&&ready)
            {
                broadCast();
                broadcastTime=System.currentTimeMillis();
            }
            if(System.currentTimeMillis()-checkConnection>1000*30)
            {
                if(requestConnection)
                {
                    //player dissconected
                    fold();
                    removeClient();
                }
                else {
                    write("17");
                    checkConnection = System.currentTimeMillis();
                    requestConnection = true;
                }
            }
        }
    }
    public void broadCast()
    {
        write("03"+data.getHighBet());
        write("07"+totalBet);
        write("08"+currentBet);
        write("09"+totalMoney);
        write("10"+min);
        write("11"+max);
        write("13"+data.getPot());
        firstRun=false;
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

    public boolean checkRecievedBet()
    {
        return requestBet;
    }
    public boolean checkRecievedAnti(){
        return requestAnti;
    }
    public boolean checkFold()
    {
        return fold;
    }
    public boolean checkReady()
    {
        return ready;
    }
    public boolean removeClient()
    {
        return removeClient;
    }
    public String getUsername()
    {
        return username;
    }

    public void write(String message)
    {
        if(message.substring(0,2).equals("04"))
        {
            requestBet=true;
        }
        if(message.substring(0,2).equals("06"))
        {
            requestAnti=true;
        }
        if(message.substring(0,2).equals("12"))
        {
            requestBuyIn=true;
        }
        if(message.substring(0,2).equals("16"))
        {
            requestName=true;
        }
        pWriter.println(message);
    }

    /*
            c0 chat
            c4 request bet
            c6 requests anti
            c12 request buy in money
            c16 request name
            c17 check connection
            c19 fold
    */
    public void setRemoveClient()
    {
        removeClient=true;
    }
    public void addMessage(String message)
    {
        data.addMessage(message,this);
    }
    public void gotBet(int cBet)
    {
        requestBet=false;
        currentBet=currentBet+cBet;
        totalMoney=totalMoney-cBet;
    }
    public void gotAnti(int cAnti)
    {
        requestAnti=false;
        currentBet=currentBet+cAnti;
        totalMoney=totalMoney-cAnti;
    }
    public void gotTotalMoney(int tMoney)
    {
        requestBuyIn=false;
        totalMoney=tMoney;
    }
    public void setUsername(String name)
    {
        username=name;
    }
    public void connected()
    {
        requestConnection=false;
    }
    public void fold()
    {
        fold = true;
    }
}
