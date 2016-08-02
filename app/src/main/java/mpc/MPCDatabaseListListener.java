package mpc;

import java.util.ArrayList;

/**
 * Interface for listening to MPClient updates.
 * @author Rory Stephenson
 *
 */
public interface MPCDatabaseListListener {
	
	/**
	 * result of dbcall
	 * @param dblist
	 */
	public void resultDbCall(ArrayList<String>dblist);
	
}
