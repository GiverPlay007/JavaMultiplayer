package me.giverplay.fascinante.server.player;

import me.giverplay.fascinante.protocol.Protocol;
import me.giverplay.fascinante.protocol.packet.Packet;
import me.giverplay.fascinante.server.Server;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

public class PlayerHandle implements Runnable
{
  private Thread thread;
  private Socket socket;
  private Server server;

  private BufferedReader input;
  private BufferedWriter output;

  private boolean authenticated = false;
  private boolean isConnected = true;

  public PlayerHandle(Server server, Socket conn)
  {
    this.server = server;
    this.socket = conn;

    try
    {
      setup();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    thread = new Thread(this);
    thread.start();
  }

  public void disconnect(String reason)
  {
    isConnected = false;
    thread.interrupt();

    try
    {
      sendMessage(reason);
      socket.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void setup() throws IOException
  {
    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  private void processEntry(String entry)
  {
    System.out.println("Dados: " + entry);
    JSONObject json;

    try
    {
      json = new JSONObject(entry);
    }
    catch (JSONException e)
    {
      badPacketDisconnet();
      return;
    }

    Packet packet = Protocol.parsePacket(json);

    if(packet == null)
    {
      badPacketDisconnet();
      return;
    }


  }

  public void sendPacket(Packet packet)
  {

  }

  public void badPacketDisconnet()
  {
    isConnected = false;
    thread.interrupt();

    try
    {
      System.out.println("Disconnecting for bad packet");
      socket.close();
      server.removeUnauth(this);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void sendMessage(String message)
  {
    try
    {
      output.write(message);
      output.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void run()
  {
    String line;

    while (isConnected)
    {
      try
      {
        if ((line = input.readLine()) != null)
        {
          processEntry(line);
        }
      }
      catch (IOException sss)
      {
        sss.printStackTrace();
      }

      try
      {
        Thread.sleep(1000 / 30);
      }
      catch (InterruptedException ignored) { }
    }
  }
}
