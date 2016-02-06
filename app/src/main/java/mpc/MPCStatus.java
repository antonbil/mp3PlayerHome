package mpc;

/**
 * Represents the current status of the MPClient. This includes whether a song is playing,
 * whether shuffle is enabled and the current volume.
 * @author Rory Stephenson
 *
 */
public class MPCStatus {
	
	public final boolean playing;
	public final boolean shuffling;
	public final Integer volume;
	public final Integer song;
	public final Integer time;

	public MPCStatus(boolean playing, boolean shuffling, Integer volume, Integer song, Integer time){
		this.playing = playing;
		this.shuffling = shuffling;
		this.volume = volume;
		this.time=time;
		this.song=song;
	}
}
