package dhare.musictiles.Main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dhare.musictiles.NoteMethods.Song;
import dhare.musictiles.R;
import dhare.musictiles.Game.GameActivity;

public class MainList extends Activity {

    //song list variables
    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView songView;
        songView = (ListView) findViewById(R.id.song_list);
        if (songView.getCount() == 0) {
        TextView textView = (TextView) findViewById(R.id.empty_view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        lp.setMargins(0, (size.y / 2) - (textView.getLineHeight() / 2), 0, 0);
        textView.setLayoutParams(lp);
        songView.setEmptyView(textView);

        //instantiate list
        songList = new ArrayList<Song>();
        //get songs from device
        getSongList();
        //sort alphabetically by title
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });


        //create and set adapter
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);}

        //setup controller
        //setController();
    }

    //user song select
    public void songPicked(View view) {
        Intent i = new Intent(this, GameActivity.class);
        i.putParcelableArrayListExtra("songList", songList);
        i.putExtra("index", Integer.parseInt(view.getTag().toString()));
        startActivity(i);

        //controller.show(0);
    }

    //method to retrieve song info from device
    public void getSongList() {
        //query external audio
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int musicColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.IS_MUSIC);
            //add songs to list
            do {
                if (musicCursor.getInt(musicColumn) != 0) {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                }
            }
            while (musicCursor.moveToNext());
        }
    }

}
