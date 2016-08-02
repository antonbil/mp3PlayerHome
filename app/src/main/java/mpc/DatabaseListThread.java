package mpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles interaction with the MPD server database. Used to fetch all song
 * information as well as to instruct the database on the server to update.
 * 
 * @author thelollies
 *
 */

public class DatabaseListThread extends Thread{

	private final String command;
	private final MPCDatabaseListListener mpcDatabaseListListener;
	private MPC mpc;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	/**
	 * Creates an instance of database thread with the specified settings
	 *
	 */
	public DatabaseListThread(MPC mpc, String command, MPCDatabaseListListener mpcDatabaseListListener){
		this.mpc = mpc;
		this.command=command;
		this.mpcDatabaseListListener=mpcDatabaseListListener;
	}

	@Override
	public void run(){
		// Establish socket connection
		try{
			sock = new Socket();
			sock.connect(new InetSocketAddress(mpc.getAddress(), mpc.getPort()), mpc.timeout);

			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Clear version number from buffer
			in.readLine();

			out.println(command);

			String line;
			ArrayList<String> result= new ArrayList<>();
			while((line = in.readLine()) != null){
				if(line.equals("OK")){break;}
				//Log.v("samba",line);
				result.add(line);
			}
			mpcDatabaseListListener.resultDbCall(result);

		} catch(IOException e){
			mpc.connectionFailed("Connection failed, check settings");
		}
		try{
			mpc.getDatabase().endTransaction();
			sock.close();
			if(in != null) in.close();
			if(out != null) out.close();
		} catch(Exception e){}
		mpc.databaseUpdated();
	}
}
