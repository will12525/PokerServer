import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class DataHolder extends Thread {
    private List<String> messages = new ArrayList<String>();
    private List<ClientThread> clients = new ArrayList<ClientThread>();
    private List<ClientThread> clientsToAdd = new ArrayList<>();
    private List<ClientThread> clientsToRemove = new ArrayList<ClientThread>();
    private List<ClientThread> clientMessageTrack = new ArrayList<ClientThread>();
    private List<ClientThread> bettingOrder = new ArrayList<>();
    private List<Card> dealerCards = new ArrayList<>();

    private boolean serverRunning = false;
    private Deck deck;
    private int startPlayer = 0;
    private boolean gameRunning = false;
    private boolean firstWave=false;
    private int highBet=0;
    private int min,max,pot;

    DataHolder(boolean serverRunning,int min,int max)
    {
        this.min=min;
        this.max=max;
        this.serverRunning=serverRunning;
    }

    public void run()
    {
        while(serverRunning) {

            if (messages.size()>0) {

                String message = messages.get(0);
                for (ClientThread client : clients) {
                    if (!checkSimilar(client)) {
                     //   client.write(message);
                    }

                }
                messages.remove(0);
                if (clientMessageTrack.size() > 0) {
                    clientMessageTrack.remove(0);
                }
            }
        }
    }
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


    public void blind()
    {
        firstWave=true;
        if(startPlayer>clients.size())
        {
            startPlayer=0;
        }
        int bigBlind = startPlayer+1;
        if(bigBlind>clients.size())
        {
            bigBlind=0;
        }

        ClientThread client = clients.get(startPlayer);
        client.write("05");
        client.isBlind(min/2);

        client=clients.get(bigBlind);
        highBet=min;
        client.write("05");
        client.isBlind(min);
        for(int x=bigBlind+1;x<clients.size();x++)
        {
            System.out.println("first loop: "+x);
            client = clients.get(x);
            if(client.checkReady())
            {
                bettingOrder.add(client);
            }
        }
        for(int x=0;x<bigBlind+1;x++)
        {
            System.out.println("second loop: "+x);
            client=clients.get(x);
            if(client.checkReady())
            {
                bettingOrder.add(client);
            }
        }
    }
    public void bets(boolean raised)
    {

        if(!raised)
        {
            highBet=0;
        }
        if(raised)
        {
            addMessage("The pot has been raised");
            raised=false;
        }

        for(int x=0;x<bettingOrder.size();x++)
        {
            ClientThread client = bettingOrder.get(x);
            client.broadCast();
            if(client.checkFold())
            {
                continue;
            }
            client.write("04");
            while (client.checkRecievedBet())
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

          //  client.addToTotalBet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(firstWave)
            {
                highBet=client.getTotalBet();
                firstWave=false;
            }
            if(client.getTotalBet()>highBet)
            {
                highBet=client.getTotalBet();
                raised=true;
            }
        }

        if(raised)
        {
            bets(true);
        }
        else {
            for (ClientThread client : clients) {
                pot = pot + client.getTotalBet();
                client.bettingOver();
            }
        }
    }
    public void anti()
    {
        for(ClientThread client: bettingOrder)
        {
            if(client.checkFold())
            {
                continue;
            }
            client.broadCast();
            client.write("06");
            while (client.checkRecievedAnti())
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        for (ClientThread client : clients) {
            pot = pot + client.getTotalBet();
            client.bettingOver();
        }

    }
    public void totalPot()
    {
        for(ClientThread client:clients)
        {
            pot =pot+client.getTotalBet();
            client.bettingOver();
        }
    }
    public void distributeDealerCards(int amount)
    {
        for(int x=0;x<amount;x++)
        {
            System.out.println("sending dealer cards");
            Card card = deck.getTopCard();
            dealerCards.add(card);
            for (ClientThread client : clients)
            {
                client.write("02"+card.getType());
            }

        }

    }
    public void distributePlayerCards()
    {
        for(ClientThread client : bettingOrder)
        {
            for(int x=0;x<2;x++)
            {
                Card card = deck.getTopCard();
                client.addCard(card);
                client.write("01"+(card.getType()));
            }
        }
    }

    public void passDeck(Deck deck)
    {
        this.deck=deck;
    }
    public int getPot()
    {
        return pot;
    }
    public int getHighBet()
    {
        return highBet;
    }

    public void newGame()
    {
        for(ClientThread client: clients)
        {
            client.newGame();
        }
        gameRunning=false;
    }
    public void distributeWinnings()
    {
        DetermineRank rank = new DetermineRank();
        int folded=0;
        for(ClientThread client : clients)
        {
            if(client.checkFold())
            {
                folded++;
                if(folded==clientSize())
                {
                    return;
                }
                continue;
            }
            List<Card> finalCards = new ArrayList<>();
            finalCards.addAll(dealerCards);
            finalCards.addAll(client.getCards());
            client.setHandRank(rank.determineRank(finalCards));
        }

        int lastRank =0;
        int highCard=0;
        int secondHigh=0;
        ClientThread winningClient=null;
        for(ClientThread client:clients)
        {
            if(client.checkFold())
            {
                continue;
            }
            String theRank = client.getHandRank();
            int currentRank = Integer.parseInt(theRank.substring(0,2));
            System.out.println("Rank: "+currentRank+", "+lastRank);
            if(currentRank>lastRank)
            {
                lastRank=currentRank;
                winningClient=client;
                if(theRank.length()>2)
                {
                    highCard=Integer.parseInt(theRank.substring(2,4));
                }
                if(theRank.length()>4)
                {
                    secondHigh=Integer.parseInt(theRank.substring(4,6));
                }
            }
            else if (currentRank == lastRank) {
                if (theRank.length() > 2) {
                    int currentHighCard = Integer.parseInt(theRank.substring(2, 4));
                    System.out.println("HighCard: " + currentHighCard + ", " + highCard);
                    if (currentHighCard > highCard)
                    {
                        highCard = currentHighCard;
                        winningClient = client;
                    }
                    else if(currentHighCard==highCard)
                    {
                        if(theRank.length()>4)
                        {
                            int currentSecondHigh = Integer.parseInt(theRank.substring(4,6));
                            if(currentSecondHigh>secondHigh)
                            {
                                winningClient=client;
                                highCard=currentHighCard;
                                secondHigh=currentSecondHigh;
                            }
                        }
                    }

                }
            }
        }

        String winner;
        for(ClientThread client: clients)
        {
            if(client==winningClient)
            {
                winner="t";
                client.write("15"+pot);
            }
            else
            {
                winner="f";
            }
            client.write("14"+winner+winningClient.getUsername()+","+rank.decodeRank(winningClient.getHandRank()));
        }
        pot=0;
    }
    public void removeClients()
    {
        for(ClientThread client : clients)
        {
            if(!client.isConnected())
            {
                addMessage(client.getUsername()+" has left the channel");
                System.out.println(client.getUsername() + " has left the channel");
                clientsToRemove.add(client);
            }

        }
        for(ClientThread client : clientsToAdd)
        {
            if(!client.isConnected())
            {
                clientsToRemove.add(client);
            }
        }
        clientsToAdd.removeAll(clientsToRemove);
        clients.removeAll(clientsToRemove);
        clientsToRemove.clear();
    }
    public void addClient(ClientThread client)
    {
        clientsToAdd.add(client);
    }
    public void addClients()
    {
        for(ClientThread client:clientsToAdd)
        {
            if(client.checkReady())
            {
                addMessage("User " + client.getUsername() + " has joined");
                System.out.println("User " + client.getUsername() + " has joined");
                clients.add(client);
            }
        }
        clientsToAdd.removeAll(clients);

    }

    public boolean checkForClient(ClientThread client)
    {
        return clients.contains(client);
    }
    public int clientSize()
    {
        return clients.size();
    }
    public void addMessage(String theMessage,ClientThread client)
    {
        clientMessageTrack.add(client);
        messages.add("00"+client.getUsername()+": "+theMessage);
        System.out.println(client.getUsername()+": "+theMessage);
    }
    public boolean checkSimilar(ClientThread client)
    {
        return clientMessageTrack.size() > 0 && clientMessageTrack.get(0) == client;
    }
    //usually from server
    public void addMessage(String theMessage)
    {
        messages.add("00Server: "+theMessage);
    }


    public void setGameRunning(boolean running)
    {
        gameRunning=running;
    }
}