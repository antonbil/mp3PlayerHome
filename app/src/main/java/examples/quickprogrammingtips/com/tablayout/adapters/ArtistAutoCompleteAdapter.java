package examples.quickprogrammingtips.com.tablayout.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;

import examples.quickprogrammingtips.com.tablayout.SpotifyActivity;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * ArtistAutoCompleteAdapter returns artist-list retrieved from samba-share
 * Created by anton on 31-7-16.
 */
public class ArtistAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    public static final String FAMILYMUSIC = "smb://192.168.2.8/FamilyLibrary/FamilyMusic/";
    private ArrayList<String> artistList;
    private ArrayList<String> suggestions =new ArrayList<>();
    private char firstLetter =' ';

        public ArtistAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return suggestions.size();
        }

        @Override
        public String getItem(int index) {
            return suggestions.get(index);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override

                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if ((constraint != null)&&(constraint.length()>0)) {
                        String firstLetterString=constraint.toString().toLowerCase();
                        //if first letter changes, retrieve results
                        if (constraint.charAt(0)!= firstLetter) {
                            // Retrieve the  results.
                            artistList = retrieveFromSambaShare(firstLetterString);
                            firstLetter =constraint.charAt(0);
                        }

                        // Assign the data to the FilterResults
                        suggestions =new ArrayList<>();
                        for (String s: artistList){
                            if (s.toLowerCase().contains(firstLetterString)) suggestions.add(s);
                        }
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                    }
                    else {
                        suggestions = SpotifyActivity.searchArtistString;
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();

                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
        }

    ArrayList<String> retrieveFromSambaShare(String firstLetter){
        firstLetter=firstLetter.toLowerCase().substring(0,1);
        //authentication
        NtlmPasswordAuthentication auth = NetworkShare.getNtlmPasswordAuthentication();
        ArrayList<String>artists=new ArrayList<>();
        try {
            //retrieve artist-list from samba-share
            SmbFile dir = new SmbFile(FAMILYMUSIC +firstLetter+"/", auth);
            for (SmbFile f : dir.listFiles()) {
                String h=f.getName().split("-")[0];//if album: artist is first part before -
                h=h.replace("/","");//remove trailing /
                //only add if not already in list
                boolean add=true;
                for (String s1:artists)
                    if (s1.equals(h) || h.startsWith(".")) add=false;
                if (add)
                    artists.add(h);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //sort the artist-list
        String start=firstLetter;
        Collections.sort(artists, (s1, s2) -> {
            s1=s1.toLowerCase();s2=s2.toLowerCase();
            try {//if does not start with letter, remove first name
                if (!s1.startsWith(start))
                    s1 = s1.split(" ")[1];
                if (!s2.startsWith(start))
                    s2 = s2.split(" ")[1];
                return s1.compareTo(s2);
            } catch (Exception e){
                return s1.compareTo(s2);
            }
        });
        return artists;
    }

}