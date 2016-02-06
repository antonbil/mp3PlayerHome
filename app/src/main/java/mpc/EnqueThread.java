package mpc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * Handles playlist manipulation on the MPD server. Allows songs to be enqued on
 * the MPD server's playlist.
 * 
 * @author thelollies
 */

public class EnqueThread extends Thread {

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	private List<MPCSong> songs;
	private MPC mpc;
	private boolean clear;

	/**
	 * Creates an instance of EnqueThread with the specified settings
	 *
	 * @param songs list of songs to enque in the order they will be enqued
	 */
	public EnqueThread(MPC mpc, List<MPCSong> songs, boolean clear){
		this.mpc = mpc;
		this.songs = songs;
		this.clear=clear;
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

			enqueSongs(clear);
		} catch(IOException e){
			mpc.connectionFailed("Connection failed, check settings");
		}
		
		try{
			sock.close();
			in.close();
			out.close();
		} catch(Exception e){}
	}

	/**
	 * 	Clears the MPD playlist before enqueing the list of songs in
	 *  the order they are passed.
	 * 
	 * @throws IOException
	 */
	
	private void enqueSongs(boolean clear) throws IOException{

		// Clear the playlist then request all song locations
		if (clear) {
			out.println("clear");
			//Log.v("samba", "clear");
			in.readLine(); // clear the "OK" response for clearing
		}

		out.println("command_list_begin"); // indicate to server to wait for multiple entries
		//Log.v("samba", "clear");

		boolean needsDir=false;
		String dir="";
		for(MPCSong song : songs)
			if (song.file.contains("'"))//%27
			{
				needsDir = true;
				String[]s1=song.file.split("/");
				String fname=s1[s1.length-1];
				dir=song.file.substring(0,song.file.length()-fname.length()-1);
			}
		if (needsDir){
			out.println("add \"" + dir + "\"");
			Log.v("samba", "add \"" + dir + "\"");
		} else
			for(MPCSong song : songs)
			{
				out.println("add \"" + song.file + "\"");
				Log.v("samba", "add \"" + song.file.replace("'","\'") + "\"");
			}
		out.println("command_list_end"); // indicate to server that it can process the entries
		in.readLine();

	}
	
}
