package examples.quickprogrammingtips.com.tablayout.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.SpotifyFragment;

/**
 * Created by anton on 11-8-16.
 */
public class RelatedArtistAdapter<String> extends ArrayAdapter {
    public static boolean longclicked=false;

    ArrayList<java.lang.String> objects;

        public RelatedArtistAdapter(Context context, int resource, ArrayList<java.lang.String> objects) {
            super(context, resource, objects);
            this.objects=objects;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
// This a new view we inflate the new layout
                LayoutInflater inflater = (LayoutInflater) SpotifyFragment.getInstance().getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_file1, parent, false);
            }
            final View convertView2=convertView;
            TextView tv = (TextView) convertView.findViewById(R.id.textViewPerformer1);
            tv.setText(objects.get(position));
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final java.lang.String selectedItem = SpotifyFragment.getInstance().artistList.get(position);

                    //Log.v("long clicked", "pos: " + pos + "artist: " + selectedItem);
                    PopupMenu menu = new PopupMenu(convertView2.getContext(), convertView2);
                    menu.getMenu().add("search");
                    menu.getMenu().add("wikipedia");
                    menu.show();
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try{

                                java.lang.String title = item.getTitle().toString();
                                if ((title.equals("search"))) {
                                    /*MainActivity.getThis.selectTab(2);
                                    try{ Thread.sleep(1000); MainActivity.getThis.searchTerm(selectedItem);}catch(InterruptedException e){ }
                                    */
                                    final Intent intent = SpotifyFragment.getInstance().getActivity().getIntent();
                                    intent.putExtra("artist", selectedItem);
                                    SpotifyFragment.getInstance().getActivity().setResult(Activity.RESULT_OK, intent);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                                    //SpotifyActivity.getThis.finish(); //finish the activity




                                }
                                if ((title.equals("wikipedia"))) {
                                    MainActivity.getInstance().startWikipediaPage(selectedItem);
                                }
                            } catch (Exception e) {
                                Log.v("samba", Log.getStackTraceString(e));
                            }

                            return true;
                        }

                    });
                    return false;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        java.lang.String s = SpotifyFragment.getInstance().artistList.get(position);
                        Log.v("samba","get "+s);
                        longclicked=true;
                        SpotifyFragment.getInstance().setVisibility(View.VISIBLE);
                        SpotifyFragment.getInstance().listAlbumsForArtist(s);
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

                }
            });
            /*OnFlingGestureListener flingListener;
            flingListener = new OnFlingGestureListener() {
                @Override
                public void onRightToLeft() {
                    //Log.v("samba","righttoleft");
                    //Toast toast = Toast.makeText(SpotifyActivity.getThis, "righttoleft", Toast.LENGTH_SHORT);
                    //toast.show();
                    SpotifyActivity.getThis.previousList();

                }

                @Override
                public void onLeftToRight() {
                    //Log.v("samba","lefttoright");
                    //Toast toast = Toast.makeText(SpotifyActivity.getThis, "lefttoright", Toast.LENGTH_SHORT);
                    //toast.show();
                    SpotifyActivity.getThis.nextList();


                }


                @Override
                public void onTapUp() {
                    ;
                }

                @Override
                public void onLongTapUp() {
                    longclicked=true;


                }

            };
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (flingListener.onTouch(v, event)) {
                        // if gesture detected, ignore other touch events
                        return true;
                    } //else {
                    //Log.v("samba","nofling");

                    int action = MotionEventCompat.getActionMasked(event);
                    if (action == MotionEvent.ACTION_DOWN) {
                        // normal touch events
                        //onClickFunc(position);
                        return false;
                    }
                    return true;
                }


            });*/

            return super.getView(position, convertView, parent);
        }
    }
