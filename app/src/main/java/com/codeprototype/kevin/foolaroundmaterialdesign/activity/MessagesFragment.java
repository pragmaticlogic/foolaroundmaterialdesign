package com.codeprototype.kevin.foolaroundmaterialdesign.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeprototype.kevin.foolaroundmaterialdesign.ParseConstants;
import com.codeprototype.kevin.foolaroundmaterialdesign.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
                //getActivity().setProgressBarIndeterminateVisibility(false);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView title;

        /*
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.message_list, parent, false));
        }
        */
        public ViewHolder(View view) {
            super(view);

            this.thumbnail = (ImageView) view.findViewById(R.id.list_avatar);
            this.title = (TextView) view.findViewById(R.id.list_title);
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
            //\\return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.message_list, parent, false);

            ViewHolder viewHolder = new ViewHolder(v);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ParseObject message = mMessages.get(position);
            holder.title.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

            //Handle click event on both title and image click
            holder.title.setOnClickListener(clickListener);
            holder.thumbnail.setOnClickListener(clickListener);
        }

        @Override
        public int getItemCount() {
            if (mMessages == null)
                return 0;
            else
                return mMessages.size();
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ParseObject message = mMessages.get();
                ImageView imageView = (ImageView) view.findViewById(R.id.list_avatar);
                Intent intent = new Intent(mContext, ViewImageActivity.class);
                mContext.startActivity(intent);
            }
        };
    }
    /*
    public MessagesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    ListView listView = getListView();
                    mMessages = messages;

                    String[] userNames = new String[messages.size()];
                    int i = 0;
                    for (ParseObject message : messages) {
                        userNames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            listView.getContext(),
                            android.R.layout.simple_list_item_1,
                            userNames
                    );
                    setListAdapter(arrayAdapter);

                } else {

                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);


        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    */
}
