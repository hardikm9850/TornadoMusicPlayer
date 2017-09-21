package com.example.thenotoriousrog.tornadomusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.thenotoriousrog.tornadomusicplayer.R;;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by thenotoriousrog on 6/27/17.
 * This class allows for user to add their songs into their playlists.
 */

public class ModifyAllPlaylistsFragmentActivity extends FragmentActivity {

    private ArrayList<String> songs; // list of songs paths.
    private ArrayList<String> playlistNames; // list of all the playlists names.
    private ArrayList<SongInfo> songInfos; // list of song info to display on our list view instead of the actual file name.
    private ArrayList<String> folders; // holds all of the folders.
    private Vector<View> playlistPages = new Vector<>(); // this vector will hold all of the list views that the user wants to have.
    private Vector<View> songlistPages = new Vector<>(); // this vector will hold all of the songs and folders list view.
    private Vector<ListView> playlistViews = new Vector<>(); // this vector will hold all of the playlist listViews.
    private Vector<ListView> songlistViews = new Vector<>(); // holds all song list views.
    private TextView playListSeparator; // this controls the blue separator for the playlist. This will change when we swipe to the next playlist.
    private TextView songListSeparator; // this controls the text of the song list separator.
    private SongInfo selectedSong; // grabs the song that the user has selected to drag.
    private String selectedFolderName; // holds the name of the folder that was selected by the user.
    private String selectedFolderPath; // holds the path of the folder itself.

    private ViewPager playlistPager; // view pager for the playlists.
    private View songItemView; // holds the view of the item selected in the song list.
    Vector<ItemDragListener> dragListeners = new Vector<>(); // create a new vector that will hold the different drag listeners for each of the playlistViews.
    private ViewPager songListPager; // view pager for the playlist to hold both the song list and the folders list.
    private View clickedItem; // holds the item clicked in the list view so that it can be modified after a drag event has completed.
    private String folderSeperatorText = ""; // shows the folderSongSeperator text depending on what it is that the user is doing at the current moment.
    //private ArrayList<Pair<String, ArrayList<SongInfo>>> Playlists = new ArrayList<>(); // This arraylist of pairs will allow us to add playlists and their names to the playlists themselves. Which is crucial. old version
    private ArrayList<Playlist> Playlists = new ArrayList<>(); // holds all of the playlists that are being used.
    public final int CREATE_PLAYLIST_INTENT = 200; // result code we want to send to the open playlist intent.


    // sets the text of the folderSeperator.
    public void setFolderSeperatorText(String text)
    {
        folderSeperatorText = text;
    }

    // returns the seperator text to be used
    public String getFolderSeperatorText()
    {
        return folderSeperatorText;
    }

    // sets the selected song from the player itself
    public void setSongItemView(View view)
    {
        songItemView = view;
    }

    // gets the song item view that will allow for the correct shadow figure to be built.
    public View getSelectedSongItemView()
    {
        return songItemView;
    }

    // sets the song info that the user has chosen to drag.
    public void setSelectedSong(SongInfo info)
    {
        selectedSong = info;
    }

    // gets the selected song by the user.
    public SongInfo getSelectedSong()
    {
        return selectedSong;
    }

    // returns a playlist name based on the value that you enter in.
    public String getPlaylistName(int pos)
    {
        return playlistNames.get(pos); // return that playlist nane.
    }

    // Sets the layout and sets up the creation so users can immediately create their Playlists
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drag_song_list); // set up the main view to begin creating the view.

        // grab the intent so we can begin saving items into the playlist.
        Intent playlistIntent = getIntent();
        Bundle args = playlistIntent.getBundleExtra("args"); // get the bundle of arguments from the intent itself.
        songs = args.getStringArrayList("songs");
        playlistNames = args.getStringArrayList("playlistNames");
        songInfos = args.getParcelableArrayList("songInfos");
        folders = args.getStringArrayList("folders");

        Playlists = args.getParcelableArrayList("playlists"); // attempt to extract the playlists that were sent in to the this activity.
        System.out.println("The playlist that we received from the MainUIFragment is: " + Playlists);

        playListSeparator = (TextView) findViewById(R.id.playlistTextSeparator); // get the playlist separator.
        playListSeparator.setText(getPlaylistName(0)); // change the playlist separator to show the name of the first playlist that we create!

        songListSeparator = (TextView) findViewById(R.id.songTextSeparator); // get our song text separator.
        songListSeparator.setText("Songs"); // this is okay since is it just the default.

        // Create listviews for every playlist that a user has created. todo: we will also need to keep track of the songs that are already populated in each playlist so users can see those song!
        for(int i = 0; i < playlistNames.size(); i++)
        {
            ListView newListView = new ListView(getApplicationContext()); // make a new list view to be used by the view pager.
            playlistPages.add(newListView); // add the new listview into the list of listViews.
            playlistViews.add(newListView); // add the playlist simply to just have a copy of it.
        }


        ListView songsListView = new ListView(getApplicationContext()); // I think this is breaking my list right here for whatever reason. I cannot get it fixed for whatever reason.
        ListView foldersListView = new ListView(getApplicationContext());
        songlistPages.add(songsListView); // add to the page viewer
        songlistPages.add(foldersListView); // add to the page viewer.
        songlistViews.add(songsListView); // add to vector of listviews for songlist.
        songlistViews.add(foldersListView); // add to vector of listviews for songlist.

        // TODO: use the current playlists to fill this data since there is stuff in it already. Basically we need to get the playlists to show the data that already exists in the playlists.
        // set and create the ViewPager
        playlistPager = (ViewPager) findViewById(R.id.playListPager); // set the playlist pager.
        songListPager = (ViewPager) findViewById(R.id.songListPager); // set the songList pager.

        createListenersAndAdapters(); // create our listeners and adapters.
    }

    // sets the item that was clicked by the user.
    private void setClickedItem(View item)
    {
        clickedItem = item;
    }

    // gets the item clicked by the user.
    public View getClickedItem()
    {
        return clickedItem;
    }

    // this method will find songs within the directory passed into it and return with a File[] full of .mp3 files.
    protected File[] getSongsInDirectory(File dir)
    {
        // filteredSongFiles holds all of the mp3 files that are found
        File[] filteredSongFiles = dir.listFiles(new FilenameFilter() {

            // This will search for songs and ensure that whatever is retrieved is an mp3 and nothing else.
            @Override
            public boolean accept(File dir, String songName) {
                return songName.contains(".mp3");
            }
        });

        return filteredSongFiles; // returns the File[] of filtered songs. It can return 0 if no songs were found.
    }

    // WARNING: this method is extremely inefficient in terms of memory.
    public ArrayList<SongInfo> convertToSongInfoList(ArrayList<String> songsList)
    {
        //System.out.println("Am I failing here?");
        ArrayList<SongInfo> songsInfo = new ArrayList<SongInfo>(); // holds the songs and their info into this list.

        // iterate through the arraylist of song paths and convert each one into a SongInfo, and add to list of SongInfos.
        for(int i = 0; i < songsList.size(); i++)
        {

            SongInfo newSong = new SongInfo(); // create a new SongInfo object.
            newSong.getandSetSongInfo(songsList.get(i)); // get the song path and send it to SongInfo to be parsed into it's song details.
            songsInfo.add(newSong); // add the new SongInfo into list of SongInfos.
        }

        // System.out.println("Did I finish grabbing the info?");
        return songsInfo; // return this list back to caller. All song information has been parsed successfully.
    }


    // simply changes the pager back to the folders after the user is finished with whatever it is that it is doing.
    private void revertPagerToFolders(final ItemTouchListener touchListener)
    {
        FolderAdapter folderAdapter = new FolderAdapter(getApplicationContext(), R.layout.folderlist, folders); // create the adapter needed to set up this list.
        songlistViews.get(1).setAdapter(folderAdapter); // set the view for the folder list that the users can switch between.
        folderAdapter.notifyDataSetChanged(); // update the list in the view

        setFolderSeperatorText("Folders"); // set the text of the folder seperator text.
        songListSeparator.setText(getFolderSeperatorText()); // show all of the folders again.

        // todo: reset the item click listener here.

        // When the user clicks a folder it will open the folder and allow the user to pick songs from that folder.
        songlistViews.get(1).setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Change the current view of all the folders to be that of the songs that are inside the folder.
                String folderPath = folders.get(position); // gets the path of the selected folder itself.
                File folderDir = new File(folderPath);
                File[] songs; // songs within the file.

                ArrayList<String> rawSongs = new ArrayList<String>();
                if(folderDir.isDirectory()) // if it is a directory then we need to extract all of the songs in it and create a new arraylist of songInfos
                {
                    songs = getSongsInDirectory(folderDir); // get all the songs in the folder directory

                    for(int i = 0; i < songs.length; i++)
                    {
                        rawSongs.add(songs[i].toString()); // add the songs into the arrayList.
                    }
                }

                ArrayList<SongInfo> folderSongs = new ArrayList<SongInfo>();
                folderSongs = convertToSongInfoList(rawSongs); // convert all of the raw songs to the folders songs to SongInfos

                FloatingActionButton backToFoldersButton = (FloatingActionButton) findViewById(R.id.backToFoldersButton); // get the back to folders button here.
                backToFoldersButton.setVisibility(View.VISIBLE); // make the button visible again.

                openFolderInPager(folderSongs, touchListener); // open the folder and display the songs in it and allow the user to drag and drop.

                String temp = folders.get(position); // get the folder path that the user has chosen.
                String[] arr = temp.split("/"); // split the string by /'s

                int length = arr.length; // get the length of the split string.
                String folderName = arr[length -1]; // get the last split which should hold our folder name.

                setFolderSeperatorText(folderName); // get the folder seperator text and display it.

                songListSeparator.setText(getFolderSeperatorText()); // update the songListSeperator text to show the name of the folder that the user has selected.
            }
        });
    }

    // This method will update the pager to show the songs in the folder. This will also open up to show all of the songs in the folder itself.
    private void openFolderInPager(final ArrayList<SongInfo> folderSongs, final ItemTouchListener touchListener)
    {
        FolderSongAdapter folderSongAdapter = new FolderSongAdapter(getApplicationContext(), R.layout.playlist_songlist, folderSongs);
        songlistViews.get(1).setAdapter(folderSongAdapter); // set the folder song adapter overwriting the playlist songs.
        folderSongAdapter.notifyDataSetChanged(); // tell the adapter that we have changed the list and show the songs in the list itself.

        System.out.println("A regular click event was triggered, this should not be happening!!");

        // Now we need to show the button here.
        final FloatingActionButton backToFoldersButton = (FloatingActionButton) findViewById(R.id.backToFoldersButton); // get the back to folders button here.
        backToFoldersButton.setOnClickListener(new View.OnClickListener() {

            // behavior for when this button is clicked.
            @Override
            public void onClick(View v)
            {
                revertPagerToFolders(touchListener); // revert back to the folders
                backToFoldersButton.setVisibility(View.INVISIBLE); // make the button invisibile again.
            }
        });

        // Allow the pager to be able to drag and drop in the new list now.
        songlistViews.get(1).setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // The onItemClick listener here will behave same and the click listener for the songs.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                setSelectedSong(folderSongs.get(position)); // grabs the song out of the folderSong list to allow for drag and drop.
                setSongItemView(view);

                parent.setOnTouchListener(touchListener); // set the touch listener that was created in the CreateListenersAndAdapter method.

                int currentPlaylistPage = playlistPager.getCurrentItem();
                parent.setOnDragListener(dragListeners.get(currentPlaylistPage));

                ImageView touchIcon = (ImageView) view.findViewById(R.id.touchIcon);
                ImageView dragIcon = (ImageView) view.findViewById(R.id.dragIcon);

                touchIcon.setVisibility(View.INVISIBLE); // make the touch icon invisible
                dragIcon.setVisibility(View.VISIBLE); // make the drag icon visible now.

                setClickedItem(view); // set the item that was clicked by the user.
            }
        });

        // TODO: add the code so that when users hit "BACK" it will revert the songs to go back to just the folders, that is very important.
    }

    // this method will set up the adapters for the list views and create the draggable events.
    public void createListenersAndAdapters()
    {
        // todo: this whole thing needs to be redone in order to get the image itself to be listening for drag and drop nothing else.

        // First entry in the song list view is all the songs themselves.
        DragSongAdapter dsAdapter = new DragSongAdapter(getApplicationContext(), R.layout.playlist_songlist, songInfos); // create the custom adapter. Uses the same songlist design as the songListAdapter in the main view.
        songlistViews.get(0).setAdapter(dsAdapter); // set the adapter for the song list page.
        dsAdapter.notifyDataSetChanged(); // update the list to show the songs.

        // todo: add the functionality for users to add entire folders into the playlist.
        // Second entry in the song list view is the folders, if any, that the user has.
        final FolderAdapter folderAdapter = new FolderAdapter(getApplicationContext(), R.layout.folderlist, folders); // create the adapter needed to set up this list.
        songlistViews.get(1).setAdapter(folderAdapter); // set the view for the folder list that the users can switch between.
        folderAdapter.notifyDataSetChanged(); // update the list in the view

        // First playlistPagerAdapter is for the songs and folders viewpager
        PlaylistPagerAdapter songListPagerAdapter = new PlaylistPagerAdapter(getApplicationContext(), songlistPages); // create playlistPagerAdapter for songList
        songListPager.setAdapter(songListPagerAdapter); // set the adapter for songlist pager
        songListPagerAdapter.notifyDataSetChanged(); // let the adapter know that data has changed.

        // Second playlistPagerAdapter is for the playlists in the viewpager.
        PlaylistPagerAdapter playlistPagerAdapter = new PlaylistPagerAdapter(getApplicationContext(), playlistPages); // create playlistPagerAdapter for playlists
        playlistPager.setAdapter(playlistPagerAdapter); // set the playlist pager adapter.
        playlistPagerAdapter.notifyDataSetChanged(); // let the pagerAdapter know that there is a change in the list itself.


        final ItemTouchListener touchListener = new ItemTouchListener(ModifyAllPlaylistsFragmentActivity.this);

        // todo: implement this vector or delete. Hurry up and decide what is important and what is not.
        final Vector<ArrayList<SongInfo>> songsInPlaylists = new Vector<>(); // this vector holds the different list of playlists. This is what will need to be saved back to the main activity after the user saves.

        // give every listview a unique drag listener.
        for(int i = 0; i < playlistViews.size(); i++)
        {

            final ItemDragListener newDragListener = new ItemDragListener(playlistViews.get(i), ModifyAllPlaylistsFragmentActivity.this, Playlists.get(i)); // set every playlist in the order in which they were created.
            dragListeners.add(newDragListener); // add the dragListener to the list of drag listeners.


            ArrayList<SongInfo> newPlaylistSongList = new ArrayList<>(); // create a new playlist to hold the songs within that playlist.


            // TODO: we are working here trying to get the existing songs to show up in the playlists!!
            // ** Important Notes below be sure to read them! **
            // we want to use the adapter if we have songs already inside the playlist this is very important.
            // Since there is a new playlist here it will be added to the last of the items in the playlist, so we will have to make sure that the last item gets a brand new list.
            if(i < Playlists.size()) // if less than the number of playlists available fill the playlists with songs, else generate new lists to fill.
            {
                System.out.println("i = " + i);
                System.out.println("Playlist we are currently on is: " + playlistNames.get(i));
               // System.out.println("Is the playlist songs null? " + Playlists.get(i).getSongs()); // The problem is here. We are getting null items in our songs, this is no good. old way
                System.out.println("Is the playlist songs null? " + Playlists.get(i).songs()); // The problem is here. We are getting null items in our songs, this is no good.


                //final DragSongAdapter newDSAdapter = new DragSongAdapter(getApplicationContext(), R.layout.playlist_songlist, Playlists.get(i).getSongs()); // at this point, each playlist is empty. old way
                final DragSongAdapter newDSAdapter = new DragSongAdapter(getApplicationContext(), R.layout.playlist_songlist, Playlists.get(i).songs()); // at this point, each playlist is empty.
                playlistViews.get(i).setAdapter(newDSAdapter); // set the adapter with the already populated songs in the playlist.
                playlistViews.get(i).setOnDragListener(newDragListener);
            }
            else // new playlist here.
            {
                final DragSongAdapter newDSAdapter = new DragSongAdapter(getApplicationContext(), R.layout.playlist_songlist, newPlaylistSongList); // at this point, each playlist is empty.
                songsInPlaylists.add(newPlaylistSongList); // add the new playlist song into the list of playlist songs.
                playlistViews.get(i).setAdapter(newDSAdapter); // set the same adapter for every list view. will need to change eventually.
                playlistViews.get(i).setOnDragListener(newDragListener); // set the drag listener for each item in the playlist. These are in order so it should not be incorrect.
            }
        }

        // Control whatever happens to the playlist whenever we change the playlist, we want to change the name of that playlist so the user knows what they are modifying.
        playlistPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // no need for this method.
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            // whenever a page is changed, change the name of the playlistTextSeparator to match the correct playlist name.
            @Override
            public void onPageSelected(int position)
            {
                // todo: this should work based on how I am implementing the arraylists/vectors, but could get problammatic, create a similar SongInfo class to handle the playlists many details.
                // todo: then I will only need a vector or arraylist of the class much like how SongInfo works. DO THIS!
                playListSeparator.setText(playlistNames.get(position)); // get the playlist name based on the position of the page we are looking at.
            }

            // no need for this method
            @Override
            public void onPageScrollStateChanged(int state) {}
        });


        // Control behavior for when user swipes left and/or right. Switches between song lists and folder lists.
        songListPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // not needed.
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            // set a behavior for when users swipe to change the look of the drag and drop behavior.
            @Override
            public void onPageSelected(int position)
            {
                if(position == 0)
                {
                    songListSeparator.setText("Songs"); // do not change the text on this one.
                }
                else // the only other position could be 1, thus our folders.
                {
                    if(getFolderSeperatorText().equalsIgnoreCase("")) {
                        songListSeparator.setText("Folders"); // default to showing this when the playlist function is created for the first time.
                    }
                    else {
                        songListSeparator.setText(getFolderSeperatorText()); // gets the text of the seperator at the current moment.
                    }

                    // todo: We need to figure out how to continue to show the folder name if the has chosen to show the name of the folder.
                }
            }

            // method not needed.
            @Override
            public void onPageScrollStateChanged(int state) {}
        });


        // Essentially this is the touch_icon and drag feature that we want to use. After a user touches it, then we want to drag the item thus, switch the image that we are using to show a drag option.
        // This will set an on touch_icon listener right away, but only after the listview item has been clicked.
        songlistViews.get(0).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSelectedSong(songInfos.get(position));
                setSongItemView(view);

                parent.setOnTouchListener(touchListener);

                int currentPlaylistPage = playlistPager.getCurrentItem();
                parent.setOnDragListener(dragListeners.get(currentPlaylistPage));

                ImageView touchIcon = (ImageView) view.findViewById(R.id.touchIcon);
                ImageView dragIcon = (ImageView) view.findViewById(R.id.dragIcon);

                touchIcon.setVisibility(View.INVISIBLE); // make the touch icon invisible
                dragIcon.setVisibility(View.VISIBLE); // make the drag icon visible now.

                setClickedItem(view); // set the item that was clicked by the user.
                //System.out.println("Is the touch icon we tried to get from the view null? " + touchIcon);

                // todo: need to figure out how to change the icon back to the touch icon after a drag was successfully make or not.
            }
        });

        // When the user clicks a folder it will open the folder and allow the user to pick songs from that folder.
        songlistViews.get(1).setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Change the current view of all the folders to be that of the songs that are inside the folder.
                String folderPath = folders.get(position); // gets the path of the selected folder itself.
                File folderDir = new File(folderPath);
                File[] songs; // songs within the file.

                ArrayList<String> rawSongs = new ArrayList<String>();
                if(folderDir.isDirectory()) // if it is a directory then we need to extract all of the songs in it and create a new arraylist of songInfos
                {
                    songs = getSongsInDirectory(folderDir); // get all the songs in the folder directory

                    for(int i = 0; i < songs.length; i++)
                    {
                        rawSongs.add(songs[i].toString()); // add the songs into the arrayList.
                    }
                }

                FloatingActionButton backToFoldersButton = (FloatingActionButton) findViewById(R.id.backToFoldersButton); // get the back to folders button here.
                backToFoldersButton.setVisibility(View.VISIBLE); // make the button visible again.

                ArrayList<SongInfo> folderSongs = new ArrayList<SongInfo>();
                folderSongs = convertToSongInfoList(rawSongs); // convert all of the raw songs to the folders songs to SongInfos

                openFolderInPager(folderSongs, touchListener); // open the folder and display the songs in it and allow the user to drag and drop.

                String temp = folders.get(position); // get the folder path that the user has chosen.
                String[] arr = temp.split("/"); // split the string by /'s

                int length = arr.length; // get the length of the split string.
                String folderName = arr[length -1]; // get the last split which should hold our folder name.

                setFolderSeperatorText(folderName); // get the folder seperator text and display it.

                songListSeparator.setText(getFolderSeperatorText()); // update the songListSeperator text to show the name of the folder that the user has selected.
            }
        });

         FloatingActionButton savePlaylistsButton = (FloatingActionButton) findViewById(R.id.savePlaylistButton); // button used to save the playlists.


        // When clicked, this will end the fragment and tell the MainUIFragment that the playlists have been which will end this fragment and will begin saving the playlists to main memory.
        savePlaylistsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                sendAndSavePlaylists();
                //onBackPressed(); // handle the situation for when the back button is pressed.
            }
        });
    }

    // Saves the playlists and sends them to the MainUIFragment and also makes sure that the list is sent to the MainUIFragment and also ends this activity.
    public void sendAndSavePlaylists()
    {
        // Begin saving the items in the playlist.
        System.out.println("User has chosen to save the playlists lets see what is actually in the playlists.");

        Intent resultIntent = new Intent(); // the result that we wish to send back to the MainUIFragment.
        Bundle resultBundle = new Bundle();

        System.out.println("The number of playlist names that we need to save is: " + playlistNames.size());

        resultBundle.putStringArrayList("playlistNames", playlistNames); // put the playlist names which is important for the MainUIFragment to get.
        ArrayList<Playlist> playlists = new ArrayList<>(); // the list of playlists that we are going to work with.


        // Create the playlist names again.
        // NOTE: this is the biggest time killer here. We may have to have a loading bar for when the app itself is loading.
        // loop through all of the playlist views.
        for(int i = 0; i < playlistViews.size(); i++)
        {
            ArrayList<SongInfo> songsInPlaylist = new ArrayList<>(); // holds the name of the songs in the playlist itself.

            // loop through all of the songs in the playlist
            for(int j = 0; j < playlistViews.get(i).getCount(); j++)
            {
                SongInfo song = (SongInfo) playlistViews.get(i).getItemAtPosition(j); // grab the jth song in the ith playlist.
                songsInPlaylist.add(song); // add the song to the playlist.
            }

            Playlist playlist = new Playlist(getPlaylistName(i), songsInPlaylist); // create a playlist again.
            playlists.add(playlist); // add playlist to list of playlist.

            //songsInPlaylist.clear(); // clear the songs in the playlist to ensure that the right songs get added into the playlist.
        }


        //resultBundle.putParcelableArrayList("playlists", playlists); // attempt to send this playlist to the MainUIFragment.

        // TODO: create a method that will save to main memory, I don't like having this in random locations within the code.
        // WRITE the playlist object to main memory.
        String ser = SerializeObject.objectToString(playlists); // Trying to serialize the entire Playlist Arraylist, Not sure if it is possible or not yet.
        if(ser != null && !ser.equalsIgnoreCase(""))
        {
            String savedPlaylistFileName = "playlists.dat"; // should be something like "Playlist 1.dat"
            SerializeObject.WriteSettings(getBaseContext(), ser, savedPlaylistFileName); // write the item to main memory.
        }
        else // Writing the obeject failed. Think of a better way to handle this if at all.
        {
            System.out.println("WE DID NOT WRITE THE LIST CORRECTLY, SOMETHING BAD HAPPENED.");
            SerializeObject.WriteSettings(getBaseContext(), "", "playlists.dat"); // we should be getting this list if we are something bad has happened.
        }

        resultIntent.putExtras(resultBundle); // set the result intent extras.
        setResult(CREATE_PLAYLIST_INTENT, resultIntent); // send in the result back to the MainUIFragment. Very important.

        finish(); // end the activity after the buttons are pushed.
    }

    // This will control the behavior after the back button is pressed.
    @Override
    public void onBackPressed()
    {
        finish(); // just close end the activity immediately.
        //super.onBackPressed();
        // TODO: tell the user that the information they have created will be lost if they choose to go back.
    }
}
