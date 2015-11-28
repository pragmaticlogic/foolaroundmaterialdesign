package com.codeprototype.kevin.foolaroundmaterialdesign.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codeprototype.kevin.foolaroundmaterialdesign.ParseConstants;
import com.codeprototype.kevin.foolaroundmaterialdesign.R;
import com.codeprototype.kevin.foolaroundmaterialdesign.helper.FileHelper;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecepientsActivity extends AppCompatActivity {

    public static final String TAG = RecepientsActivity.class.getSimpleName();
    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected Uri mMediaUri;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepients);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
                progressView.startAnimation();

                ArrayList<String> recipientIds = getRecipientIds();
                if (recipientIds.size() <= 0) {
                    Snackbar.make(view, "You must select at least one recipient to send the message", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    createAndSendMessage(view, recipientIds);
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void createAndSendMessage(final View view, final ArrayList<String> recipientIds) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ParseObject message = createMessage(mCurrentUser.getObjectId(), mCurrentUser.getUsername(), recipientIds, mFileType);
                if (message == null) {
                    Snackbar.make(view, "There was a problem with the file selected", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    send(message);
                }
            }
        };
        new Thread(runnable).start();
    }

    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    mFriends = friends;
                    String[] friendNames = new String[friends.size()];
                    int i = 0;
                    for (ParseUser friend : friends) {
                        friendNames[i] = friend.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            RecepientsActivity.this,
                            android.R.layout.simple_list_item_checked, friendNames);
                    ListView listView = (ListView) findViewById(android.R.id.list);
                    listView.setAdapter(adapter);
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }


    private void addFriendCheckmarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    ListView listView = (ListView) findViewById(android.R.id.list);

                    for (int i = 0; i < mFriends.size(); i++) {
                        ParseUser user = mFriends.get(i);
                        for (ParseUser friend : friends) {
                            if (user.getObjectId().equals(friend.getObjectId())) {
                                listView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    protected ParseObject createMessage(String senderId, String senderName,
                                        ArrayList<String> recipientIds,
                                        String fileType) {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, senderId);
        message.put(ParseConstants.KEY_SENDER_NAME, senderName);
        message.put(ParseConstants.KEY_RECIPIENTS_IDS, recipientIds);
        message.put(ParseConstants.KEY_FILE_TYPE, fileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        if (fileBytes == null) {
            return null;
        } else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }
    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        ListView listView = (ListView) findViewById(android.R.id.list);
        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.isItemChecked(i)) {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                        .coordinatorLayout);
                if (e != null) {
                    Log.i(TAG, e.getMessage());
                    Snackbar.make(coordinatorLayout, "Error " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else { //success
                    Snackbar.make(coordinatorLayout, "Your message was sent successfully", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    finish();
                }
            }
        });
    }
}
