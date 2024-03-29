package max.hej;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Communicator extends AsyncTask<Void, Void, String> {
    protected String URL = "cheapdrink.nl";
    protected int port = 8000;

    protected SSLSocketFactory socketFactory;
    protected SSLSocket socket;
    protected InputStream IN;
    protected BufferedReader BR;
    protected OutputStream OUT;
    protected Handler handler;
    protected PrintWriter PW;
    private max.hej.Message message;
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";

    public Communicator(max.hej.Message msg, Handler handler){
        this.message = msg;
        this.handler = handler;
        socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public String doInBackground(Void...params){
        if(message.getIntent().equals(max.hej.Message.NEW_ACCOUNT)) {
            return createAccount();
        }
        else if(message.getIntent().equals(max.hej.Message.VALIDATE_USER_NAME)) {
            return validateUsername();
        }
        else if (message.getIntent().equals(max.hej.Message.CHECK_FOR_USERNAME)) {
            return checkForUsername();
        }
        else if(message.getIntent().equals(max.hej.Message.SEND_HEJ)) {
            sendHej();
        }
        else if(message.getIntent().equals(max.hej.Message.UPDATE_REGID)) {
            updateGCMID();
        }

        try {
            this.socket.close();
        }
        catch(Exception e){e.printStackTrace();}

        return "";

    }

    @Override
    protected void onPostExecute(String results) {
                Message msg = this.handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("0",results);
                msg.setData(bundle);
                handler.sendMessage(msg);
    }


    private String connectToServer(){
        try{
            System.out.println("Trying to connect to Hej server");
            InetAddress serverAddr = InetAddress.getByName(URL);
            this.socket = (SSLSocket) socketFactory.createSocket(serverAddr.getHostAddress(),port);
            final String[] enabledCipherSuites = this.socket.getSupportedCipherSuites();
            this.socket.setEnabledCipherSuites(enabledCipherSuites);

            //this.socket = new Socket(serverAddr,port);
            this.IN = socket.getInputStream();
            this.BR = new BufferedReader(new InputStreamReader(IN));
            this.OUT = socket.getOutputStream();
            PW = new PrintWriter(socket.getOutputStream(),true);
        }
        catch(Exception e){e.printStackTrace(); return this.NETWORK_ERROR;}

        return this.SUCCESS;
    }

    public String createAccount(){
        if(this.connectToServer().equals(NETWORK_ERROR)){
            return NETWORK_ERROR;
        }
        try {
            PW.write(message.asJSONString() + "\n");
            PW.flush();
            //System.out.println("Waiting for response");
            String response = BR.readLine();
            if(response.equals(SUCCESS)){
                //success
                return SUCCESS;
            }
            else{
                return FAIL;
            }

        }
        catch(Exception e){e.printStackTrace();}
        return FAIL;
    }


    public void updateGCMID(){
        connectToServer();
        try{
            PW.write(message.asJSONString() + "\n");
            PW.flush();
        }
        catch(Exception e){e.printStackTrace();}
    }
    public String validateUsername(){
        if(this.connectToServer().equals(NETWORK_ERROR)){
            return NETWORK_ERROR;
        }
        try{
            PW.write(message.asJSONString() + "\n");
            PW.flush();
            String response =  BR.readLine(); //expecting string "valid" or "invalid"
            if(response.equals(SUCCESS)){
                //success
                return SUCCESS;
            }
            else{
                return FAIL;
            }
        }
        catch(Exception e){e.printStackTrace();}
        return FAIL;//nothing or a problem with connection.
    }

    public String checkForUsername(){
        if(this.connectToServer().equals(NETWORK_ERROR)){
            return NETWORK_ERROR;
        }        try{
            PW.write(message.asJSONString() + "\n");
            PW.flush();
            return BR.readLine(); //expecting string "valid" or "invalid"
        }
        catch(Exception e){e.printStackTrace();}
        return FAIL;//nothing or a problem with connection.
    }

    public void sendHej(){
        connectToServer();
        try{
            PW.write(message.asJSONString() + "\n");
            PW.flush();
        }
        catch(Exception e){e.printStackTrace();}
    }
}
