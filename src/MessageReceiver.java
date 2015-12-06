import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by lawrencew on 12/3/2015.
 */
public class MessageReceiver extends Thread {
    ClientThread client;
    BufferedReader bReader;
    public MessageReceiver(ClientThread client, Socket socket)
    {
        this.client=client;
        try {
            bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException e)
        {

        }
        start();
    }
    public void run()
    {
        String message;
        while(client.isConnected())
        {
            try {
                if((message=bReader.readLine())!=null)
                {
                    System.out.println("recieved response: "+message+" from "+client.getUsername());
                    if(message.equals("exit"))
                    {
                        client.disconnect();
                        return;
                    }
                    else
                    {
                        System.out.println(message);
                        if(message.substring(0,2).equals("00"))
                        {
                            client.addMessage(message.substring(2));
                        }
                        if(message.substring(0,2).equals("04"))
                        {
                            client.gotBet(Integer.parseInt(message.substring(2)));
                        }
                        if(message.substring(0,2).equals("06"))
                        {
                            client.gotAnti(Integer.parseInt(message.substring(2)));
                        }
                        if(message.substring(0,2).equals("12"))
                        {
                            client.gotTotalMoney(Integer.parseInt(message.substring(2)));
                        }
                        if(message.substring(0,2).equals("16"))
                        {
                            client.setUsername(message.substring(2));
                        }
                        if(message.substring(0,2).equals("17"))
                        {
                            client.connected();
                        }
                        if(message.substring(0,2).equals("19"))
                        {
                            client.fold();
                        }
                    }
                }
            }
            catch(IOException e)
            {

            }
        }
    }
}
