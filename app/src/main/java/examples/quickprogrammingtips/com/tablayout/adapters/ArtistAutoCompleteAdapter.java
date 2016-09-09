package examples.quickprogrammingtips.com.tablayout.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;

import examples.quickprogrammingtips.com.tablayout.SpotifyFragment;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

//import org.apache.commons.lang3.StringUtils;

/**
 * ArtistAutoCompleteAdapter returns artist-list retrieved from samba-share
 * Created by anton on 31-7-16.
 */
public class ArtistAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    public static final String FAMILYMUSIC = "smb://192.168.2.8/FamilyLibrary/FamilyMusic/";
    private static ArrayList<String> artistList=new ArrayList();
    private ArrayList<String> suggestions =new ArrayList<>();
    private char firstLetter =' ';
    public static ArrayList<Character> letters=new ArrayList();

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
                        char c = constraint.charAt(0);
                        if (c != firstLetter) {
                            // Retrieve the  results.
                            if (notpartOf(c)) {
                                artistList.addAll(retrieveFromSambaShare(firstLetterString));
                                letters.add(c);
                                getAllFilenames();
                            }
                            firstLetter = c;
                        }

                        // Assign the data to the FilterResults
                        suggestions =new ArrayList<>();
                        for (String s: artistList){
                            if (s.toLowerCase().contains(firstLetterString)) suggestions.add(s);
                            if (suggestions.size()>10)  break;
                        }
                        if (suggestions.size()==0){
                            int n=10;
                            String suit="";
                            for (String s: artistList){
                                int levenshteinDistance = levenshtein(s.toLowerCase(), firstLetterString);
                                if (levenshteinDistance <=n) {
                                    suit=s;
                                    suggestions.add(0,suit);
                                    n= levenshteinDistance;
                                }
                                //if (s.toLowerCase().contains(firstLetterString)) suggestions.add(s);
                                //if (suggestions.size()>10)  break;
                            }

                        }
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                    }
                    else {
                        suggestions = SpotifyFragment.searchArtistString;
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

    public static int levenshtein(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public static void getAllFilenames() {
        new Thread()
        {
            public void run() {
                for (char c1 = 'a'; c1 <= 'z'; c1++) {
                    if (notpartOf(c1)) {
                        letters.add(c1);
                        ArrayList<String> artistList1 = retrieveFromSambaShare(c1 + "");
                        artistList.addAll(artistList1);
                    }
                }
            }
        }.run();
    }

    private static boolean notpartOf(char c) {
        for (char c1:letters){
            if (c==c1)return false;
        }
        return true;
    }

    static ArrayList<String> retrieveFromSambaShare(String firstLetter){
        ArrayList<String>artists=new ArrayList<>();
        firstLetter=firstLetter.toLowerCase().substring(0,1);
        //authentication
        try {
        NtlmPasswordAuthentication auth = NetworkShare.getNtlmPasswordAuthentication();
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
        } catch (Exception e) {
            Log.v("samba","error samba");
        }
        return artists;
    }

}