package com.bourke.glimmrpro.fragments.viewer;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Html;

import android.util.Log;

import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.ProfileActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.TextUtils;
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

    public static final String TAG = "Glimmr/CommentsFragment";

    private LoadCommentsTask mTask;
    private Photo mPhoto;
    private ArrayAdapter<Comment> mAdapter;
    private Map<String, UserItem> mUsers = Collections.synchronizedMap(
            new HashMap<String, UserItem>());
    private List<LoadUserTask> mLoadUserTasks = new ArrayList<LoadUserTask>();
    private PrettyTime mPrettyTime;
    private ImageButton mSubmitButton;
    private ProgressBar mProgressBar;
    private ListView mListView;

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
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.comments_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);

        mSubmitButton = (ImageButton) mLayout.findViewById(R.id.submitButton);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentsFragment.this.submitButtonClicked();
            }
        });

        mProgressBar =
            (ProgressBar) mLayout.findViewById(R.id.progressIndicator);
        mProgressBar.setVisibility(View.VISIBLE);

        mListView = (ListView) mLayout.findViewById(R.id.list);

        /* Set title text to uppercase and roboto font */
        TextView titleText = (TextView) mLayout.findViewById(R.id.titleText);
        mTextUtils.setFont(titleText, TextUtils.FONT_ROBOTOREGULAR);
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

    public void submitButtonClicked() {
        if (mActivity.getUser() == null) {
            Toast.makeText(mActivity, getString(R.string.login_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        TextView editText = (TextView) mLayout.findViewById(R.id.editText);
        String commentText = editText.getText().toString();
        if ("".equals(commentText)) {
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
        View focusedView = mActivity.getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        Toast.makeText(mActivity, mActivity.getString(R.string.comment_sent),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserReady(User user) {
        if (user != null) {
            mUsers.put(user.getId(), new UserItem(user, false));
            mAdapter.notifyDataSetChanged();
        }
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
        mProgressBar.setVisibility(View.GONE);

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
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {
                ViewHolder holder;

                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.comment_list_row, null);
                    holder = new ViewHolder();
                    holder.textViewUsername = (TextView)
                        convertView.findViewById(R.id.userName);
                    holder.textViewCommentDate = (TextView)
                        convertView.findViewById(R.id.commentDate);
                    holder.textViewCommentText = (TextView)
                        convertView.findViewById(R.id.commentText);
                    holder.imageViewUserIcon = (ImageView)
                        convertView.findViewById(R.id.userIcon);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Comment comment = getItem(position);

                holder.textViewUsername.setText(comment.getAuthorName());

                String pTime = mPrettyTime.format(comment.getDateCreate());
                /* keep Oliver happy */
                if ("es".equals(Locale.getDefault().getLanguage())) {
                    pTime = capitaliseWord(pTime);
                }
                holder.textViewCommentDate.setText(pTime);

                holder.textViewCommentText.setText(
                    Html.fromHtml(comment.getText()));

                final UserItem author = mUsers.get(comment.getAuthor());
                if (author == null) {
                    mUsers.put(comment.getAuthor(), new UserItem(null, true));
                    LoadUserTask loadUserTask = new LoadUserTask(mActivity,
                            CommentsFragment.this, comment.getAuthor());
                    loadUserTask.execute(mOAuth);
                    mLoadUserTasks.add(loadUserTask);
                } else {
                    if (!author.isLoading) {
                        mAq.id(holder.imageViewUserIcon).image(
                                author.user.getBuddyIconUrl(),
                                Constants.USE_MEMORY_CACHE,
                                Constants.USE_FILE_CACHE, 0, 0, null,
                                AQuery.FADE_IN_NETWORK);
                        holder.imageViewUserIcon.setOnClickListener(
                                new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ProfileActivity.startProfileViewer(
                                    mActivity, author.user);
                            }
                        });
                    }
                }
                return convertView;
            }
        };
        mListView.setAdapter(mAdapter);
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

    class ViewHolder {
        TextView textViewUsername;
        TextView textViewCommentDate;
        TextView textViewCommentText;
        ImageView imageViewUserIcon;
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
