package com.bourke.glimmrpro.fragments.viewer;

import android.content.Context;

import android.graphics.Typeface;

import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Html;

import android.util.Log;

import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.ProfileActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.ICommentAddedListener;
import com.bourke.glimmrpro.event.Events.ICommentsReadyListener;
import com.bourke.glimmrpro.event.Events.IUserReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseDialogFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.AddCommentTask;
import com.bourke.glimmrpro.tasks.LoadCommentsTask;
import com.bourke.glimmrpro.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ocpsoft.pretty.time.PrettyTime;

public final class CommentsFragment extends BaseDialogFragment
        implements ICommentsReadyListener, ICommentAddedListener,
                   IUserReadyListener {

    protected String TAG = "Glimmr/CommentsFragment";

    private LoadCommentsTask mTask;
    private Photo mPhoto;
    private ArrayAdapter<Comment> mAdapter;
    private Map<String, UserItem> mUsers = Collections.synchronizedMap(
            new HashMap<String, UserItem>());
    private List<LoadUserTask> mLoadUserTasks = new ArrayList<LoadUserTask>();
    private PrettyTime mPrettyTime;

    public static CommentsFragment newInstance(Photo p) {
        CommentsFragment f = new CommentsFragment();
        f.mPhoto = p;
        f.mPrettyTime = new PrettyTime(Locale.getDefault());
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.comments_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        mAq.id(R.id.submitButton).clicked(this, "submitButtonClicked");
        mAq.id(R.id.progressIndicator).visible();

        /* Set title text to uppercase and roboto font */
        Typeface robotoRegular = Typeface.createFromAsset(
                mActivity.getAssets(), Constants.FONT_ROBOTOREGULAR);
        TextView titleText = (TextView) mLayout.findViewById(R.id.titleText);
        titleText.setTypeface(robotoRegular);
        String title = mActivity.getString(R.string.menu_view_comments);
        titleText.setText(title.toUpperCase());

        return mLayout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Constants.DEBUG) Log.d(TAG, "onPause");

        if (mTask != null) {
            mTask.cancel(true);
        }

        /* Also stop any remaining LoadUserTasks */
        for (AsyncTask loadUserTask : mLoadUserTasks) {
            loadUserTask.cancel(true);
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        if (mPhoto != null) {
            if (Constants.DEBUG) Log.d(getLogTag(), "startTask()");
            mTask = new LoadCommentsTask(this, mPhoto);
            mTask.execute(mOAuth);
        } else {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "startTask: mPhoto is null");
            }
        }
    }

    public void submitButtonClicked(View view) {
        if (mActivity.getUser() == null) {
            Toast.makeText(mActivity, getString(R.string.login_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        TextView editText = (TextView) mLayout.findViewById(R.id.editText);
        String commentText = editText.getText().toString();
        if (commentText.equals("")) {
            // TODO: alert user
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Comment text empty, do nothing");
            }
            return;
        }

        if (Constants.DEBUG) {
            Log.d(getLogTag(), "Starting AddCommentTask: " + commentText);
        }
        new AddCommentTask(this, mPhoto, commentText).execute(mOAuth);

        /* Clear the editText and hide keyboard */
        editText.setText("");
        InputMethodManager inputManager = (InputMethodManager)
            mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus()
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        Toast.makeText(mActivity, mActivity.getString(R.string.comment_sent),
                Toast.LENGTH_SHORT).show();
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO
    }

    @Override
    public void onUserReady(User user) {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onUserReady: " + user.getId());
        mUsers.put(user.getId(), new UserItem(user, false));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCommentAdded(String commentId) {
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "Sucessfully added comment with id: " +
                    commentId);
        }
        startTask();
    }

    @Override
    public void onCommentsReady(List<Comment> comments) {
        mAq.id(R.id.progressIndicator).gone();

        if (comments == null) {
            Log.e(TAG, "onCommentsReady: comments are null");
            return;
        }
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "onCommentsReady, comments.size(): "
                + comments.size());
        }

        mAdapter = new ArrayAdapter<Comment>(mActivity,
                R.layout.comment_list_row, (ArrayList<Comment>) comments) {
            // TODO: implement ViewHolder pattern
            // TODO: add aquery delay loading for fling scrolling
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {

                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.comment_list_row, null);
                }

                final Comment comment = getItem(position);
                AQuery aq = mAq.recycle(convertView);

                // TODO: if your username replace with "You"
                aq.id(R.id.userName).text(comment.getAuthorName());

                String pTime = mPrettyTime.format(comment.getDateCreate());
                /* keep Oliver happy */
                if (Locale.getDefault().getLanguage().equals("es")) {
                    pTime = capitaliseWord(pTime);
                }
                aq.id(R.id.commentDate).text(pTime);

                aq.id(R.id.commentText).text(Html.fromHtml(comment.getText()));

                final UserItem author = mUsers.get(comment.getAuthor());
                if (author == null) {
                    mUsers.put(comment.getAuthor(), new UserItem(null, true));
                    LoadUserTask loadUserTask = new LoadUserTask(mActivity,
                            CommentsFragment.this, comment.getAuthor());
                    loadUserTask.execute(mOAuth);
                    mLoadUserTasks.add(loadUserTask);
                } else {
                    if (!author.isLoading) {
                        aq.id(R.id.userIcon).image(
                                author.user.getBuddyIconUrl(),
                                Constants.USE_MEMORY_CACHE,
                                Constants.USE_FILE_CACHE, 0, 0, null,
                                AQuery.FADE_IN_NETWORK);
                        aq.id(R.id.userIcon).clicked(
                                new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ProfileActivity.startProfileViewer(
                                    author.user, mActivity);
                            }
                        });
                    }
                }
                return convertView;
            }
        };

        mAq.id(R.id.list).adapter(mAdapter).itemClicked(this,
                "itemClicked");
    }

    private String capitaliseWord(String word) {
        if (word == null || word.length() == 0) {
            return word;
        }
        StringBuilder sb = new StringBuilder(word);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    class UserItem {
        public User user;
        public boolean isLoading = true;

        public UserItem(User user, boolean isLoading) {
            this.user = user;
            this.isLoading = isLoading;
        }
    }
}
