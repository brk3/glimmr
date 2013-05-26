package com.bourke.glimmrpro.tape;

import com.bourke.glimmrpro.common.TaskQueueDelegateFactory;
import com.bourke.glimmrpro.fragments.group.AddToGroupDialogFragment;
import com.bourke.glimmrpro.tape.AbstractTaskQueueService;
import com.bourke.glimmrpro.tasks.AddItemToGroupTask;
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
        mQueue = new TaskQueue(factory.get(AddToGroupDialogFragment.QUEUE_FILE,
                AddItemToGroupTask.class));
    }
}
