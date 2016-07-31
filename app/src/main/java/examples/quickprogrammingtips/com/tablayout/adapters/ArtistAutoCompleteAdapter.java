package examples.quickprogrammingtips.com.tablayout.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.SpotifyActivity;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * Created by anton on 31-7-16.
 */
public class ArtistAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;
    ArrayList<String> temp=new ArrayList<>();
    private char c=' ';

        public ArtistAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return temp.size();
        }

        @Override
        public String getItem(int index) {
            return temp.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override

                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    Log.v("samba","constraint:"+constraint);
                    if ((constraint != null)&&(constraint.length()>0)) {
                        String cs=constraint.toString();
                        if (constraint.charAt(0)!=c) {
                            Log.v("samba","get array"+constraint.charAt(0));
                            // Retrieve the autocomplete results.
                            resultList = autocomplete(cs);
                            c=constraint.charAt(0);
                        }

                        // Assign the data to the FilterResults
                        temp=new ArrayList<>();
                        for (String s:resultList){
                            if (s.toLowerCase().contains(cs))temp.add(s);
                        }
                        filterResults.values = temp;
                        filterResults.count = temp.size();
                    }
                    else {
                        Log.v("samba","default:");
                        temp= SpotifyActivity.searchArtistString;
                        if (temp.size()>0)Log.v("samba","default:"+temp.get(0));
                        filterResults.values = temp;
                        filterResults.count = temp.size();

                    }
                    //searchArtistString
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
            return filter;
        }

    ArrayList<String> autocomplete(String s){
        NtlmPasswordAuthentication auth = NetworkShare.getNtlmPasswordAuthentication();
        ArrayList<String>ar=new ArrayList<>();
        try {
            SmbFile dir = new SmbFile("smb://192.168.2.8/FamilyLibrary/FamilyMusic/"+s.toLowerCase().substring(0,1)+"/", auth);
            for (SmbFile f : dir.listFiles()) {
                String h=f.getName().split("-")[0];
                h=h.replace("/","");
                boolean add=true;
                for (String s1:ar)
                if (s1.equals(h)) add=false;
                if (add)
                    ar.add(h);
            }

            } catch (Exception e) {
            e.printStackTrace();
        }
        return ar;
        /*return new ArrayList<String>(
                Arrays.asList(
                "Abbea", "King Crimson", "Beatles", "Rolling Stones", "Radiohead"));*/
    }

}