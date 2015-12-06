import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class ClientThread extends Thread {

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
   // private int currentBet = 0;
    private int totalMoney = 0;
    private boolean requestBuyIn = true;
    private boolean requestName = true;
    private String username = "";
    private boolean requestConnection = false;
    private long checkConnection;
    private boolean fold = false;
    private boolean connected = true;
    private String handRank ="";
    private boolean firstRun = true;
    private boolean ready = false;
    private boolean removeClient = false;

    private int min, max;

    private PrintWriter pWriter;
    private DataHolder data;

    public ClientThread(final Socket socket, final DataHolder data, int min, int max) {
        checkConnection = System.currentTimeMillis();
        this.min = min;
        this.max = max;
        this.data = data;
        try {
            pWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new MessageReceiver(this, socket);
        write("10"+min);
        write("11"+max);
        write("12");
        write("16");
        this.start();
    }

    public void run() {
        long broadcastTime = System.currentTimeMillis();
        while (connected) {
            if (!requestName && !requestBuyIn) {
                ready = true;
                broadCast();
            }
            if ((System.currentTimeMillis() - broadcastTime > (1000*5) || firstRun) && ready) {
                broadCast();
                broadcastTime = System.currentTimeMillis();
                System.out.println("broadcasting");
            }
            if (System.currentTimeMillis() - checkConnection > 1000 * 10) {
                if (requestConnection) {
                    System.out.println("stopping");
                    connected=false;
                    fold();
                    interrupt();
                } else {
                    write("17");
                    checkConnection = System.currentTimeMillis();
                    requestConnection = true;
                }
            }
        }
    }

    public void broadCast() {
        write("03" + data.getHighBet());
     //   write("07" + totalBet);
        write("08" + totalBet);
        write("09" + totalMoney);
        write("10" + min);
        write("11" + max);
        write("13" + data.getPot());
        write("21"+data.clientSize());
        firstRun = false;
    }

    public void bettingOver() {
        totalBet = 0;
    }

    public void addCard(Card newCard) {
        playerCards.add(newCard);
    }

    public List<Card> getCards() {
        return playerCards;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public int getTotalBet() {
        return totalBet;
    }

    public boolean checkRecievedBet() {
        return requestBet;
    }

    public boolean checkRecievedAnti() {
        return requestAnti;
    }

    public boolean checkFold() {
        return fold;
    }

    public boolean checkReady() {
        return ready;
    }

    public void setHandRank(String rank)
    {
        handRank=rank;
    }
    public String getHandRank()
    {
        return handRank;
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void newGame()
    {
        totalBet=0;
        playerCards.clear();
        fold=false;
        write("20");
    }
    public void isBlind(int amount)
    {
        totalBet=amount;
        totalMoney=totalMoney-amount;
        blind=true;
    }

    public void write(String message)
    {
        if(message.substring(0,2).equals("04"))
        {
            requestBet=true;
        }
        if(message.substring(0,2).equals("05"))
        {
            blind=true;
        }
        if(message.substring(0,2).equals("06"))
        {
            requestAnti=true;
        }
        if(message.substring(0,2).equals("12"))
        {
            requestBuyIn=true;
        }
        if(message.substring(0,2).equals("15"))
        {
            totalMoney=totalMoney+data.getPot();
        }
        if(message.substring(0,2).equals("16"))
        {
            requestName=true;
        }
        if(connected) {
            pWriter.println(message);
            pWriter.flush();
        }
        else
        {
            fold = true;
        }
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
  /*  public void setRemoveClient()
    {
        removeClient=true;
    }*/
    public void addMessage(String message)
    {
        data.addMessage(message,this);
    }
    public void gotBet(int cBet)
    {
        requestBet=false;
        totalBet=totalBet+cBet;
        totalMoney=totalMoney-cBet;
    }
    public void gotAnti(int cAnti)
    {
        requestAnti=false;
        totalBet=totalBet+cAnti;
        totalMoney=totalMoney-cAnti;
    }
    public void gotTotalMoney(int tMoney)
    {
        System.out.println(tMoney);
        requestBuyIn=false;
        totalMoney=tMoney;
    }
    public void setUsername(String name)
    {
        username=name;
        requestName=false;
        System.out.println(username);
    }
    public void disconnect()
    {
        connected=false;
    }
    public void connected()
    {
        requestConnection=false;
    }
    public void fold()
    {
        requestAnti=false;
        requestBet=false;
        fold = true;
    }
    public void setPoints(int points)
    {

    }
}
