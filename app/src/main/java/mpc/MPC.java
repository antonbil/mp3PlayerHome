package mpc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.SpotifyActivity;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * This class manages connection with the MPD server and all connections
 * should go through it.
 * 
 * @author thelollies
 */

public class MPC {

	private String address;
	private int port;
	public final int timeout; // connection timeout (ms)
	private MusicDatabase database;
	private MPCListener mpcListener;
	private MPCDatabaseListener mpcDatabaseListener;

	/**
	 * This constructor accepts the instance MPC is instantiated from and sets
	 * up required connection with the MPD server.
	 *
	 */
	public MPC(String address, int port, int timeout, MusicDatabase database){
		this.address = address;
		this.port = port;
		this.timeout = timeout;
		this.database = database;
	}

	/**
	 * Clears the database of songs on the device before asking MPD to renew its
	 * database updates with the new one.
	 */
	public void renewDatabase(){
		new DatabaseThread(this).start();
	}

	/**
	 * Sends an instruction to MPD to play the song at specified index
	 * 
	 * @param index position in the playlist of the song to play
	 */
	public void play(int index){
		sendSingleMessage("play " + index);
		checkIfPlaying(true);
	}

	/**
	 * Sends a request to continue playback from where it was paused
	 */
	public void play(){
		sendSingleMessage("play");
		checkIfPlaying(true);
	}

	public void play(boolean first){
		sendSingleMessage("play");
		checkIfPlaying(first);
	}

	private void checkIfPlaying(final boolean first) {
		Log.v("samba","not playing before");
		final MainActivity mainObject = MainActivity.getThis;
		final Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			private void delay12() {
				final ProgressDialog loadingdialog;
				loadingdialog = ProgressDialog.show(mainObject,
						"", "Stop playing, please wait", true);
				SpotifyActivity.stopSpotifyPlaying(SpotifyActivity.checkAddress());
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						play(false);
						loadingdialog.dismiss();
					}
				}, 12000);
			}
			@Override
			public void run() {
				MPCStatus s=mainObject.getLogic().getMpc().getStatusSynch();
				if (!s.playing) {
					Log.v("samba","not playing 2");
					if (!first) {
						try {
							mainObject.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// remove the retrieving of data from this method and let it just build the views
									Log.v("samba", "not playing");
									new AlertDialog.Builder(mainObject)
											.setMessage("Not playing. End spotify playing?")
											.setCancelable(false)
											.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int id) {
													delay12();

												}
											})
											.setNegativeButton("No", null)
											.show();
								}

							});
						} catch (Exception e) {
							Log.v("samba", Log.getStackTraceString(e));
						}
					} else {
						delay12();

					}

				} else Log.v("samba","playing after");
				//Do something after 100ms
			}
		}, 2000);
	}

	public void clearPlaylist(){
		sendSingleMessage("clear");
	}

	/**
	 * Sends a request to the MPD server to pause playback
	 */
	public void pause(){
		sendSingleMessage("pause");
	}
	public void stop(){
		sendSingleMessage("stop");
	}

	public ArrayList<Mp3File> playlist(){
		/*PlaylistThread thread = new PlaylistThread(this);
		thread.start();

		try{
			thread.join();
		} catch(Exception e){
			e.printStackTrace();
		}
		return thread.getPlaylist();*/return null;
	}

	/**
	 * Sends a request to the MPD server to move playback to the previous song
	 * in the current playlist
	 */
	public void previous(){
		sendSingleMessage("previous");
	}

	/**
	 * Sends a request to the MPD server to move playback to the next song in
	 * the current playlist
	 */
	public void next(){
		sendSingleMessage("next");
	}

	/**
	 * Requests a status update from MPD server
	 * 
	 * @return true if a songs is playing on the MPD server
	 */
	public void requestStatus(){
		new StatusThread(this, true).start();
	}

	/**
	 * Queries the MPD server's status to determine if a song is playing
	 * 
	 * @return true if a songs is playing on the MPD server
	 */
	public MPCStatus getStatusAsynch(){
		StatusThread thread = new StatusThread(this, false);
		thread.start();
		try{
			thread.join();
			return thread.status;
		} catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	public MPCStatus getStatusSynch(){
		StatusThread thread = new StatusThread(this, true);
		thread.start();
		try{
			thread.join();
			return thread.status;
		} catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Enques the specified list of songs on the MPD server in the order
	 * they are passed, ready for playback.
	 * 
	 * @param songs a list of MPCSongs to enque on MPD server
	 */
	public void enqueSongs(List<MPCSong> songs, boolean clear) {
		EnqueThread thread = new EnqueThread(this, songs,clear);
		thread.start();

		try{
			thread.join();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void enqueCommands(ArrayList<String> songs) {
		EnqueueMessageThread thread = new EnqueueMessageThread(this, songs);
		thread.start();

		try{
			thread.join();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Send the specified message to the MPD server, use newline char to seperate lines
	 * @param message String to send to the MPD server
	 */
	public void sendSingleMessage(String message){
		MessageThread thread = new MessageThread(this, message);
		thread.start();

		// Check if the connection succeeded
		try{
			thread.join();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Enables/disables the shuffle feature on the MPDServer
	 * 
	 * @param shuffle true to turn on, false to turn off
	 */
	public void shuffle(boolean shuffle) {
		if(shuffle){
			sendSingleMessage("random 1");
		}
		else{
			sendSingleMessage("random 0");
		}
	}

	public void setVolume(int volume){
		volume = Math.min(volume, 100);
		volume = Math.max(volume, 0);
		sendSingleMessage("setvol " + volume);
	}

	public void changeVolume(int change) {
		// Get initial volume
		MPCStatus status = getStatusAsynch();
		Integer currentVol = status != null ? status.volume : null;

		// Set the volume or return null if volume couldn't be found
		if(currentVol != null)	setVolume(currentVol + change);
	}

	// Listener Triggers
	
	public void connectionFailed(String message){
		if(mpcListener != null) mpcListener.connectionFailed(message);
		if(mpcDatabaseListener != null) mpcDatabaseListener.connectionFailed(message);
	}
	
	protected void databaseUpdated(){
		if(mpcListener != null) mpcListener.databaseUpdated();
		if(mpcDatabaseListener != null) mpcDatabaseListener.databaseUpdated();
	}
	
	protected void statusUpdate(MPCStatus newStatus){
		if(mpcListener != null) mpcListener.statusUpdate(newStatus);
	}
	
	protected void databaseUpdateProgressChanged(int progress){
		if(mpcDatabaseListener != null) mpcDatabaseListener.databaseUpdateProgressChanged(progress);
	}
	
	// Getters and setters

	protected MusicDatabase getDatabase(){
		return database;
	}
	
	public String getAddress(){
		return address;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setMPCListener(MPCListener mpcListener){
		this.mpcListener = mpcListener;
	}
	
	public void setMPCDatabaseListener(MPCDatabaseListener mpcDatabaseListener){
		this.mpcDatabaseListener = mpcDatabaseListener;
	}

	public void updateSettings(int port, String address) {
		this.port = port;
		this.address = address;
	}
}