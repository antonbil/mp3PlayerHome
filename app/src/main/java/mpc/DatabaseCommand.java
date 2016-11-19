package mpc;

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

public class DatabaseCommand extends Thread{

	private final MPCDatabaseListener mpcDatabaseListener;
	private boolean findit=false;
	private  boolean fold=false;
	private String command;
	private MPC mpc;
	private String previousAlbum="";

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	/**
	 * Creates an instance of database thread with the specified settings
	 *  @param mpc the caller
	 * @param command command to be executed
	 * @param mpcDatabaseListener activity the database is being accessed from
	 * @param fold fold yes/no
	 */
	public DatabaseCommand(MPC mpc, String command, MPCDatabaseListener mpcDatabaseListener, boolean fold, boolean findit){
		this.mpcDatabaseListener=mpcDatabaseListener;
		this.mpc = mpc;
		this.command=command;
		this.fold=fold;
		this.findit=findit;
	}
	public DatabaseCommand(MPC mpc, String command, MPCDatabaseListener mpcDatabaseListener, boolean fold){
		this.mpcDatabaseListener=mpcDatabaseListener;
		this.mpc = mpc;
		this.command=command;
		this.fold=fold;
	}
	/*public DatabaseCommand(MPC mpc, String command, MPCDatabaseListener mpcDatabaseListener){
		this(mpc, command, mpcDatabaseListener, false);
	}*/

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

			out.println(command.trim());
			String line;

			ArrayList<File> files=new ArrayList<>();
			ArrayList<String>list=new ArrayList<>();
			String path="";
			while((line = in.readLine()) != null){
				if (line.startsWith("OK"))break;

				/*if (line.startsWith("Last-Modified:")) {
					//do nothing
				} else*/
				if (line.startsWith("directory:")) {
					path=line.split("directory:")[1].trim();
					GetPathAndFile getPathAndFile = new GetPathAndFile(path).invoke();
					String f = getPathAndFile.getF();
					path = getPathAndFile.getPath();
					files.add(new File(f,path));

				} else

				if (line.startsWith("file:")){

					addMp3FileToList(files, list, path);
					path=line.split("file:")[1].trim();
					list.add(line);
				}else
				list.add(line);
			}
			addMp3FileToList(files, list, path);

			if (findit)
			mpcDatabaseListener.databaseFindCompleted(files);
			else
				mpcDatabaseListener.databaseCallCompleted(files);

			throw new IOException("error executing mpd-command "+command);
		} catch(IOException e){
			mpc.connectionFailed("Connection failed, check settings");
		}
		try{
			mpc.getDatabase().endTransaction();
			sock.close();
			if(in != null) in.close();
			if(out != null) out.close();
		} catch(Exception e){/**/}
	}

	private void addMp3FileToList(ArrayList<File> files, ArrayList<String> list, String path) {
		if (list.size()>0) {
			Mp3File mp3File = new Mp3File(path, list);
			String album = mp3File.niceAlbum();
			if (fold && (previousAlbum.equals(album))) {
				if (files.get(files.size()-1) instanceof Mp3File) {
					files.remove(files.size()-1);
					GetPathAndFile getPathAndFile = new GetPathAndFile(path).invoke();
					path = getPathAndFile.getPath();
					if (path.endsWith("/"))path=path.substring(0,path.length()-1);
					getPathAndFile = new GetPathAndFile(path).invoke();
					String f = getPathAndFile.getF();
					path = getPathAndFile.getPath();
					files.add(new File(f, path));
				}
			} else {if (mp3File.getTime() > 0)
				files.add(mp3File);
			}
			previousAlbum=album;
		}
	}


	private class GetPathAndFile {
		private String path;
		private String f;

		GetPathAndFile(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		public String getF() {
			return f;
		}

		GetPathAndFile invoke() {
			String[] pathLines=path.split("/");
			f = pathLines[pathLines.length - 1];
			path=path.substring(0, path.length() - f.length());
			return this;
		}
	}
}
