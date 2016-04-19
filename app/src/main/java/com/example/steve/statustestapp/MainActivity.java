package com.example.steve.statustestapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*; //file input/output
import java.net.*; //socket input/output
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView out;
    EditText server;
    EditText status;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws NullPointerException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        out = (TextView) findViewById(R.id.out);
        server = (EditText) findViewById(R.id.server);
        server.setText("192.168.0.101:23000"); //default server
        status = (EditText) findViewById(R.id.status);
        status.setText("Player,stefan@gamemail.com,SomebodyStopMe\n" +
            "Achievement,Tutorial,6,6\n" +
            "Achievement,Noob missions,3,10\n" +
            "Achievement,Pleb missions,6,10\n" +
            "Achievement,Hero missions,1,10\n" +
            "Achievement,Boss fight,0,1\n"); //default status
    }

    public void onUpdate(View v) {
        out.setText("updating status...");
        try {
            StatusTask statusTask = new StatusTask();
            statusTask.setContext(this); //tell asynchronous task where callback can be found
            statusTask.execute(server.getText().toString(), status.getText().toString()); //run asynchronous task
        } catch (RuntimeException re) { //already running
            out.setText("update " + re.getMessage());
        }
    }

    public void showResult(String result) { //callback from asynchronous task
        out.setText(result);
    }
}

class StatusTask extends AsyncTask<String, Void, String> {

    private MainActivity mainActivity;

    protected void setContext(MainActivity context) { //remember calling context: main task
        mainActivity = context;
    }

    protected String doInBackground(String... args){ //send updated status
        String strOut = "";
        String server = args[0];
        String addr = server.split(":")[0];
        String port = server.split(":")[1];
        Socket clientSocket = null;
        strOut += String.format("connecting to %s:%s...\n", addr, port);
        try {
            clientSocket = new Socket(addr, Integer.valueOf(port)); //connect to server
        } catch (IOException ioe) {
            strOut += "\nconnecting " + ioe.getMessage();
        }
        if (clientSocket != null) {
            DataOutputStream dos = null;
            DataInputStream dis = null;
            try {
                dos = new DataOutputStream(clientSocket.getOutputStream());
                dis = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException ioe) {
                strOut += "\ndatastreaming " + ioe.getMessage();
            }
            String send = args[1];
            //strOut += String.format("sending to %s:%s\n%s...", addr, port, send);
            try {
                dos.writeUTF(send); //send text
                dos.flush(); //force data out the stream
                String strRcv = dis.readUTF();
                if (strRcv.indexOf("OK") != -1) { //did server respond with update okay?
                    strOut += String.format("\nstatus updated %s\n", new Date().toString()); //display success
                } else {
                    strOut += String.format("\nreceived:\n%s\n%s", strRcv, new Date().toString()); //display success
                }
                clientSocket.close(); //close server connection
            } catch (IOException ioe) {
                strOut += "\nsending " + ioe.getMessage();
            }
        } else {
            strOut += "\nno server found";
        }
        return strOut;
    }

    protected void onPostExecute(String result) { //give result back to calling context: main task
        mainActivity.showResult(result);
    }

}