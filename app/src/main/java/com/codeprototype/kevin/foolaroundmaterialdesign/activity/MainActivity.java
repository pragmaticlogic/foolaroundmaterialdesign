package com.codeprototype.kevin.foolaroundmaterialdesign.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codeprototype.kevin.foolaroundmaterialdesign.R;
import com.codeprototype.kevin.foolaroundmaterialdesign.dbmodel.Token;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    public static final String TAG  = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; //10 MB limit

    protected Uri mMediaUri;

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        Token token = Token.getToken();

        if (ParseUser.getCurrentUser() == null && token == null) {
            navigateToLogin();
        } else if (ParseUser.getCurrentUser() == null && token != null) {
            String sessionToken = token.sessionToken;
            ParseUser.becomeInBackground(sessionToken, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        navigateToLogin();
                    }
                }
            });
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        displayView(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, "OError!!!!", Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                }

                if (requestCode == PICK_VIDEO_REQUEST) {
                    int fileSize = 0;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException fnf) {
                        Toast.makeText(this, "Error opening file", Toast.LENGTH_LONG).show();
                        return;
                    } catch (IOException ioe) {
                        Toast.makeText(this, "Error opening file", Toast.LENGTH_LONG).show();
                        return;
                    } finally {
                        try {
                         inputStream.close();
                        } catch (Exception e) {
                            //Intentionally left blank
                        }
                    }

                    if (fileSize >= FILE_SIZE_LIMIT) {
                        Toast.makeText(MainActivity.this, "The selected video must be less than 10MB",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent intent = new Intent(this, RecepientsActivity.class);
            intent.setData(mMediaUri);
            startActivity(intent);

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "OError!!!!", Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "User Canceled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        else if (id == R.id.action_search) {
            Toast.makeText(getApplicationContext(), "Search action is selected!", Toast.LENGTH_SHORT).show();
            return true;
        }

        else if (id == R.id.action_camera) {
            new MaterialDialog.Builder(this)
                    .title(R.string.action_camera_choices)
                    .items(R.array.camera_choices)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/
                            switch (which)
                            {
                                case TAKE_PHOTO_REQUEST:
                                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                                    if (mMediaUri == null) {
                                        //snackbar here
                                        Toast.makeText(MainActivity.this, "Error external storage",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                                    }
                                    break;
                                case TAKE_VIDEO_REQUEST:
                                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                                    if (mMediaUri == null) {
                                        //snackbar here
                                        Toast.makeText(MainActivity.this, "Error external storage",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                                    }
                                    break;
                                case PICK_PHOTO_REQUEST:
                                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                    choosePhotoIntent.setType("image/*");
                                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                                    break;
                                case PICK_VIDEO_REQUEST:
                                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                    chooseVideoIntent.setType("video/*");
                                    Toast.makeText(MainActivity.this, "The selected video must be less than 10MB",
                                            Toast.LENGTH_LONG).show();
                                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                                    break;
                            }
                            return true;
                        }

                        private Uri getOutputMediaFileUri(int mediaType) {
                            //Make sure external stogae is mounted using Environment.getExternalStorageState()
                            if (isExternalStorageAvailable()) {
                                String appName = MainActivity.this.getString(R.string.app_name);
                                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                        appName);

                                if (!mediaStorageDir.exists()) {
                                    if (!mediaStorageDir.mkdirs()) {
                                        Log.e(TAG, "Failed to create directory");
                                        return null;
                                    }
                                }

                                File mediaFile;
                                Date now = new Date();
                                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                                        .format(now);

                                String path = mediaStorageDir + File.separator;
                                if (mediaType == MEDIA_TYPE_IMAGE) {
                                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                                } else if (mediaType == MEDIA_TYPE_VIDEO) {
                                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                                } else {
                                    return null;
                                }

                                Log.d(TAG, Uri.fromFile(mediaFile).toString());

                                return Uri.fromFile(mediaFile);
                            } else {
                                return null;
                            }
                        }

                        private boolean isExternalStorageAvailable() {
                            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                        }
                    })
                    .positiveText(R.string.choose_camera_choices)
                    .show();
        }

        else if (id == R.id.action_logout) {
            ParseUser.logOut();
            Token token = Token.getToken();
            new Delete().from(Token.class).where("SessionToken = ?", token.sessionToken).execute();
            Token token2 = Token.getToken();
            navigateToLogin();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case 1:
                fragment = new FriendsFragment();
                title = getString(R.string.title_friends);
                break;
            case 2:
                fragment = new MessagesFragment();
                title = getString(R.string.title_messages);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }
}
