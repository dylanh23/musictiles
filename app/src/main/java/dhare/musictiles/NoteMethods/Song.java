package dhare.musictiles.NoteMethods;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{
	
	private long id;
	private String title;
	private String artist;
	
	public Song(long songID, String songTitle, String songArtist){
		id=songID;
		title=songTitle;
		artist=songArtist;
	}
	
	public long getID(){return id;}
	public String getTitle(){return title;}
	public String getArtist(){return artist;}

    private Song(Parcel in) {
        // This order must match the order in writeToParcel()
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        // Continue doing this for the rest of your member data
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(artist);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

}
