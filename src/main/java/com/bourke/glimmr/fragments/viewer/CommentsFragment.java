package com.bourke.glimmrpro.fragments.viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.activities.ProfileViewerActivity;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.tasks.LoadCommentsTask;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.TextUtils;
import com.bourke.glimmrpro.event.Events.ICommentAddedListener;
import com.bourke.glimmrpro.event.Events.ICommentsReadyListener;
import com.bourke.glimmrpro.event.Events.IUserReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseDialogFragment;
import com.bourke.glimmrpro.tasks.AddCommentTask;
import com.bourke.glimmrpro.tasks.LoadUserTask;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.activities.ProfileViewerActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.common.TextUtils;
import com.bourke.glimmrpro.event.Events.ICommentAddedListener;
import com.bourke.glimmrpro.event.Events.ICommentsReadyListener;
import com.bourke.glimmrpro.event.Events.IUserReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseDialogFragment;
import com.bourke.glimmrpro.tasks.AddCommentTask;
import com.bourke.glimmrpro.tasks.LoadCommentsTask;
import com.bourke.glimmrpro.tasks.LoadUserTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.*;

public final class CommentsFragment extends BaseDialogFragment
        implements ICommentsReadyListener, ICommentAddedListener,
                   IUserReadyListener {

    private static final String TAG = "Glimmr/CommentsFragment";

    private static final String KEY_PHOTO =
            "com.bourke.glimmr.CommentsFragment.KEY_PHOTO";

    private LoadCommentsTask mTask;
    private ArrayAdapter<Comment> mAdapter;
    private final Map<String, UserItem> mUsers = Collections.synchronizedMap(
            new HashMap<String, UserItem>());
    private final List<LoadUserTask> mLoadUserTasks = new ArrayList<LoadUserTask>();
    private PrettyTime mPrettyTime;
    private ProgressBar mProgressBar;
    private ListView mListView;

    private Photo mPhoto;

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(mPhoto, outState, KEY_PHOTO);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mPhoto == null) {
            String json = savedInstanceState.getString(KEY_PHOTO);
            if (json != null) {
                mPhoto = new Gson().fromJson(json, Photo.class);
            } else {
                Log.e(TAG, "No stored photo found in savedInstanceState");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.comments_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);

        ImageButton submitButton = (ImageButton)
                mLayout.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
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
        titleText.setText(title.toUpperCase(Locale.getDefault()));

        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadCommentsTask(this, mPhoto);
        mTask.execute(mOAuth);
    }

    public void submitButtonClicked() {
        if (mOAuth == null || mOAuth.getUser() == null) {
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
    public void onUserReady(User user, Exception e) {
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
        if (user != null) {
            mUsers.put(user.getId(), new UserItem(user, false));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCommentAdded(String commentId, Exception e) {
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "Sucessfully added comment with id: " +
                    commentId);
        }
        startTask();
    }

    @Override
    public void onCommentsReady(List<Comment> comments, Exception e) {
        mProgressBar.setVisibility(View.GONE);

        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
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
                                Intent profileViewer = new Intent(mActivity,
                                        ProfileViewerActivity.class);
                                profileViewer.putExtra(
                                        ProfileViewerActivity.KEY_PROFILE_ID,
                                        author.user.getId());
                                profileViewer.setAction(
                                        ProfileViewerActivity.ACTION_VIEW_USER_BY_ID);
                                startActivity(profileViewer);
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
        public final User user;
        public boolean isLoading = true;

        public UserItem(User user, boolean isLoading) {
            this.user = user;
            this.isLoading = isLoading;
        }
    }
}
