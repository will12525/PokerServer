import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class DataHolder extends Thread {
    List<String> messages = new ArrayList<String>();
    List<ClientThread> clients = new ArrayList<ClientThread>();
    List<ClientThread> clientsToAdd = new ArrayList<>();
    List<ClientThread> clientsToRemove = new ArrayList<ClientThread>();
    List<ClientThread> clientMessageTrack = new ArrayList<ClientThread>();
    List<ClientThread> bettingOrder = new ArrayList<>();
    List<Card> dealerCards = new ArrayList<>();
    private boolean serverRunning = false;
    private Deck deck;
    private int startPlayer = 0;
    private boolean gameRunning = false;
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
            if(clientsToRemove.size()>0)
            {
                removeClients();
            }
            if (messages.size()>0) {

                String message = messages.get(0);
                for (ClientThread client : clients) {
                    if (!checkSimilar(client)) {
                        client.write(message);
                    }

                }
               messages.remove(0);
                if (clientMessageTrack.size() > 0) {
                    clientMessageTrack.remove(0);
                }
            }



            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /*
            c1 get there cards
            c2 get dealer cards
            c3 know high bet
            c4 know winning hand
            c5 get winnings
            c6 bet
            c7 who is blind
            c8 anti

            c9 total bet
            c10 current bet
            c11 totalmoney
            c12 buyInMin
            c13 buyInMax

            c14 pass buyinmoney
     */
    public void broadCast()
    {
        for(ClientThread client : clients)
        {
            client.write("03"+highBet);
            client.write("09"+client.getTotalBet());
            client.write("10"+client.getCurrentBet());
            client.write("11"+client.getTotalMoney());
            client.write("14"+pot);
        }
    }
    public void blind(int min)
    {

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
        client.write("07");
        client.setCurrentBet(min/2);

        client=clients.get(bigBlind);
        highBet=min;
        client.write("07");
        client.setCurrentBet(min);

        for(int x=bigBlind+1;x<clients.size();x++)
        {
            bettingOrder.add(clients.get(x));
        }
        for(int x=0;x<bigBlind+1;x++)
        {
            bettingOrder.add(clients.get(x));
        }
    }
    public void bets(boolean raised)
    {
        broadCast();
        if(raised)
        {
            addMessage("The pot has been raised");
            for(ClientThread client: clients)
            {
                client.addToTotalBet();
                client.setCurrentBet(0);
            }
        }
        for(int x=0;x<bettingOrder.size();x++)
        {
            broadCast();
            ClientThread client = bettingOrder.get(x);
            client.write("06");
            while (!client.checkRecievedBet())
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(client.getCurrentBet()>highBet)
            {
                highBet=client.getCurrentBet();
                if(x==bettingOrder.size()-1)
                {
                    bets(true);
                }
            }
        }
        if(!raised) {
            for (ClientThread client : clients) {
                pot = pot+ client.getTotalBet();
                client.bettingOver();

            }
        }
        broadCast();
    }
    public void anti(int min)
    {
        broadCast();
        for(ClientThread client: bettingOrder)
        {
            broadCast();
            client.write("08");
            while (!client.checkRecievedBet())
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        broadCast();
    }
    public boolean checkSimilar(ClientThread client)
    {
        return clientMessageTrack.size() > 0 && clientMessageTrack.get(0) == client;
    }
    public void distributeDealerCards(int amount)
    {
        for(int x=0;x<amount;x++)
        {
            Card card = deck.getTopCard();
            dealerCards.add(card);
            messages.add("02"+card.getType());

        }

    }
    public void distributePlayerCards()
    {
        for(ClientThread client : clients)
        {
            for(int x=0;x<2;x++)
            {
                client.addCard(deck.checkCard(0));
                client.write("01"+(deck.getTopCard().getType()));
            }
        }
    }

    public void passDeck(Deck deck)
    {
        this.deck=deck;
    }

    public void addToRemoveClients(ClientThread client)
    {
        clientsToRemove.add(client);
    }
    public void removeClients()
    {
        for(ClientThread client : clientsToRemove)
        {
            addMessage(client.getUsername()+" has left the channel");
            System.out.println(client.getUsername() + " has left the channel");
            client.close();
        }
        clients.removeAll(clientsToRemove);
        clientsToRemove.clear();
    }
    public void addClient(ClientThread client)
    {
        addMessage("User " + client.getUsername() + " has joined");
        System.out.println("User " + client.getUsername() + " has joined");
        if(!gameRunning)
        {
            clients.add(client);
        }
        else
        {
            clientsToAdd.add(client);
        }
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

    //usually from server
    public void addMessage(String theMessage)
    {
        messages.add("00Server: "+theMessage);
    }


    public void newGame()
    {
        for(ClientThread client: clients)
        {
            client.clearCards();
            client.bettingOver();
        }
        clients.addAll(clientsToAdd);
        gameRunning=false;
    }
    public void distributeWinnings()
    {

    }
    public void setGameRunning(boolean running)
    {
        gameRunning=running;
    }
}