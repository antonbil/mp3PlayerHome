package mpc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Handles interaction with the MPD server database. Used to fetch all song
 * information as well as to instruct the database on the server to update.
 * 
 * @author thelollies
 *
 */

public class PlaylistsCommand extends Thread{

	private final MpcPlaylistListener mpcPlaylistListener;
	private  boolean fold=false;
	private String command;
	private MPC mpc;
	private String previousAlbum="";

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	/**
	 * Creates an instance of database thread with the specified settings
	 *  @param mpc
	 * @param command
	 * @param mpcPlaylistListener activity the database is being accessed from
	 * @param fold
	 */
	public PlaylistsCommand(MPC mpc, String command, MpcPlaylistListener mpcPlaylistListener, boolean fold){
		this.mpcPlaylistListener=mpcPlaylistListener;
		this.mpc = mpc;
		this.command=command;
		this.fold=fold;
	}
	public PlaylistsCommand(MPC mpc, String command, MpcPlaylistListener mpcPlaylistListener){
		this(mpc, command, mpcPlaylistListener, false);
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
			//Log.v("samba", command);
			String line;

			ArrayList<String> files=new ArrayList<String>();
			//MPCSong mpcSong=null;
			ArrayList<String>list=new ArrayList<String>();
			String path="";
			while((line = in.readLine()) != null){
				//Log.v("samba", line);
				//Log.v("samba",line);
				if (line.startsWith("OK"))break;
				//Log.v("samba", line);
				//directory:
				//Last-Modified:
				if (line.startsWith("Last-Modified:")) {
					//do nothing
				} else
				if (line.startsWith("playlist:")) {
					files.add(line.split("playlist:")[1].trim());


				}
			}


			mpcPlaylistListener.databaseCallCompleted(files);

			throw new IOException("error executing mpd-command "+command);
		} catch(IOException e){
			mpc.connectionFailed("Connection failed, check settings");
		}
		try{
			mpc.getDatabase().endTransaction();
			sock.close();
			if(in != null) in.close();
			if(out != null) out.close();
		} catch(Exception e){}
	}

}
