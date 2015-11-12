package com.codeprototype.kevin.foolaroundmaterialdesign.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeprototype.kevin.foolaroundmaterialdesign.ParseConstants;
import com.codeprototype.kevin.foolaroundmaterialdesign.R;
import com.codeprototype.kevin.foolaroundmaterialdesign.view.RoundedTransformation;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by kevin on 9/13/15.
 */
public class MessagesFragment extends Fragment {
    RecyclerView mRecyclerView;
    ContentAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView)inflater.inflate(R.layout.recycler_view, container, false);


        // Inflate the layout for this fragment
        return mRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                if (e == null) {
                    mAdapter.setDataSource(messages);
                    mAdapter.notifyDataSetChanged();
                } else {

                }
            }
        });


        mAdapter = new ContentAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public ImageView mThumbnail;
        public TextView mTitle;
        private ParseObject mMessage;
        private Context mContext;
        private ProgressBar mProgressBar = null;

        public ViewHolder(View view, Context context) {
            super(view);

            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            mProgressBar.setVisibility(View.VISIBLE);

            mThumbnail = (ImageView) view.findViewById(R.id.imageThumbnail);
            mTitle = (TextView) view.findViewById(R.id.imageTitle);
            mContext = context;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            String messageType = mMessage.getString(ParseConstants.KEY_FILE_TYPE);
            ParseFile file = mMessage.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
                Intent intent = new Intent(mContext, ViewImageActivity.class);
                intent.setData(fileUri);
                mContext.startActivity(intent);
            } else {

            }
        }

        public void setMessage(ParseObject message) {
            mMessage = message;
            String messageType = mMessage.getString(ParseConstants.KEY_FILE_TYPE);
            ParseFile file = mMessage.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
                Picasso.with(mContext).load(fileUri)
                        //22143157/android-picasso-placeholder-and-error-image-styling
                        .transform(new RoundedTransformation(50, 4))
                        .resizeDimen(R.dimen.avator_size, R.dimen.avator_size)
                        .centerCrop()
                        .into(mThumbnail,
                                new ImageLoadedCallback(mProgressBar) {
                                    @Override
                                    public void onSuccess() {
                                        if (mProgressBar != null) {
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
            }
        }

        private class ImageLoadedCallback implements Callback {
            ProgressBar _progressBar;
            public  ImageLoadedCallback(ProgressBar progressBar){
                _progressBar = progressBar;
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private static final int LENGTH = 18;
        private Context mContext;
        protected List<ParseObject> mMessages;

        public ContentAdapter(Context context) {
            mContext = context;
        }

        public void setDataSource(List<ParseObject> messages) {
            mMessages = messages;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.message_list, parent, false);

            ViewHolder viewHolder = new ViewHolder(v, mContext);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ParseObject message = mMessages.get(position);
            holder.mTitle.setText(message.getString(ParseConstants.KEY_SENDER_NAME));
            holder.setMessage(message);
        }

        @Override
        public int getItemCount() {
            if (mMessages == null)
                return 0;
            else
                return mMessages.size();
        }
    }
}
