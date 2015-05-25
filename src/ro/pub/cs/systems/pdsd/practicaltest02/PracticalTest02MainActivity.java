package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

class Utilities {
	 
    public static BufferedReader getReader(Socket socket) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        return null;
    }
 
    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }
 
}

public class PracticalTest02MainActivity extends Activity {
	
	ServerThread serverThread;
    Button startButton, request;
    EditText port;
    WebView infoText;
	
   class StartServerButtonListener implements Button.OnClickListener {
	   
        @Override
        public void onClick(View v) {
            Log.d("CLICK", "STARTING SERVER");
            
            int p = Integer.parseInt(port.getText().toString());
 
            serverThread = new ServerThread();
            serverThread.startServer(p);
        }
 
    }
   
   class SendButtonListener implements Button.OnClickListener {
	   
       @Override
       public void onClick(View v) {
	   	   ClientThread clientThread = new ClientThread();
           String url = ((EditText) findViewById(R.id.website)).getText().toString();
           int port = Integer.parseInt(((EditText) findViewById(R.id.clientaddress)).getText().toString());
           clientThread.startClient(port, url);
       }

   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);
        
        port = (EditText) findViewById(R.id.serverport);
        startButton = (Button) findViewById(R.id.startserver);
        request = (Button) findViewById(R.id.request);
        infoText = (WebView) findViewById(R.id.webView1);
        
        startButton.setOnClickListener(new StartServerButtonListener());
        request.setOnClickListener(new SendButtonListener());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.practical_test02_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class ClientThread extends Thread {
    	 
        private Socket socket = null;
        String info;
        int port;
        String url;
 
        public void startClient(int p, String url) {
            start();
            info = "";
            this.port = p;
            this.url = url;
        }
 
        @Override
        public void run() {
            try {
                Socket socket = new Socket("127.0.0.1", this.port);
 
                PrintWriter writer = Utilities.getWriter(socket);
 
                writer.println(url);
                writer.flush();
 
                BufferedReader reader = Utilities.getReader(socket);
                String content = "";
 
                while(true) {
                    String line = reader.readLine();
                    if (line.equals("end") || line == null)
                        break;
 
                    content += line;
                }
                final String c = content;
                
                Log.d("CLIENT", content);
                
                infoText.post(new Runnable() {
                	 
                    @Override
                    public void run() {
                        infoText.loadData(c, "text/html", "utf-8");
                    }

                });
                
                socket.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    
    
    private class ServerThread extends Thread {
    	 
        private boolean isRunning = false;
        int port;
        private ServerSocket serverSocket;
 
        public void startServer(int p) {
            this.port = p;
            isRunning = true;
            start();
        }
 
        public void stopServer() {
            isRunning = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch(IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }).start();
        }
 
        public void run() {
            try {
            	Log.d("SERVER", "Starting on port " + this.port);
                serverSocket = new ServerSocket(this.port);
 
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    
                    Log.d("SERVER", "Got a socket connection");
 
                    BufferedReader reader = Utilities.getReader(socket);
                    
                    String option = reader.readLine();
                    
                    Log.d("SERVER", "Fetch url " + option);
                    
                    PrintWriter writer = Utilities.getWriter(socket);
                    
                    
                    URL obj = new URL(option);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + option);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    
                    writer.println(response.toString());
                    
                    writer.println("end");
                    writer.println(option);
                    
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SERVER", e.getMessage());
            }
        }
 
    }
    
}
