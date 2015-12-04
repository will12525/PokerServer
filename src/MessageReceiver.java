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
    }
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
                        client.setRemoveClient();
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
