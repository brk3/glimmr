package com.bourke.glimmr.tape;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.TaskQueueDelegateFactory;
import com.bourke.glimmr.tasks.AddItemToGroupTask;
import com.squareup.tape.TaskQueue;

public class AddToGroupTaskQueueService extends AbstractTaskQueueService {

    private static final String TAG = "Glimmr/AddToGroupTaskQueueService";

    public static boolean IS_RUNNING;

    @Override
    public void onCreate() {
        super.onCreate();
        IS_RUNNING = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IS_RUNNING = false;
    }


    @Override
    protected void initTaskQueue() {
        TaskQueueDelegateFactory<AddItemToGroupTask> factory =
            new TaskQueueDelegateFactory<AddItemToGroupTask>(this);
        mQueue = new TaskQueue(factory.get(Constants.GROUP_QUEUE, AddItemToGroupTask.class));
    }
}
