package mpc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.MpdInterface;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Handles playlist manipulation on the MPD server. Allows songs to be enqued on
 * the MPD server's playlist.
 * 
 * @author thelollies
 */

public class PlaylistThread extends Thread {

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	private List<MPCSong> songs;
	private MPC mpc;
	private static ArrayList<Mp3File>playlist;
	private MpdInterface mpdInterface;

	/**
	 * Creates an instance of PlaylistThread with the specified settings
	 */
	public PlaylistThread(MpdInterface mpdInterface, MPC mpc){
		this.mpc = mpc;
		this.mpdInterface=mpdInterface;
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

			generatePlaylist();
		} catch(IOException e){
			mpc.connectionFailed("Connection failed, check settings");
		}
		
		try{
			sock.close();
			if(in != null) in.close();
			if(out != null) out.close();
		} catch(Exception e){}
	}

	/**
	 */
	
	private void generatePlaylist() throws IOException{

		out.println("playlistinfo");


		playlist=new ArrayList<Mp3File>();
		String response;
		ArrayList<String> list=new ArrayList<String>();
		while(((response = in.readLine()) != null)){
			if (response.startsWith("OK"))break;
			list.add(response);
			if (response.startsWith("Id:")){
				getPlaylist().add(new Mp3File("",list));
			}
		}
		int i=0;
		mpdInterface.playlistCall(playlist,false);
	}

	public static ArrayList<Mp3File> getPlaylist() {
		return playlist;
	}

}
