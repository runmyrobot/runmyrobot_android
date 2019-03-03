package tv.letsrobot.controller.android.ui.chat

import android.content.Context
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tv.letsrobot.android.api.components.ChatSocketComponent
import tv.letsrobot.android.api.components.tts.TTSBaseComponent
import tv.letsrobot.android.api.utils.LocalBroadcastReceiverExtended

/**
 * Created by Brendon on 2/7/2019.
 */
class LRChatView : RecyclerView{
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val lrAdapter: LRChatAdapter?
        get() {return adapter as? LRChatAdapter}

    private val onChatMessageReceiver= LocalBroadcastReceiverExtended(context,
            IntentFilter(ChatSocketComponent.LR_CHAT_MESSAGE_WITH_NAME_BROADCAST)){ _, intent ->
        intent?.extras?.getSerializable("json")?.let {
            lrAdapter?.addMessage(it as TTSBaseComponent.TTSObject)
        }
    }

    private val onChatMessageRemovedReceiver = LocalBroadcastReceiverExtended(
            context,
            IntentFilter(ChatSocketComponent.LR_CHAT_MESSAGE_REMOVED_BROADCAST)){ _, intent ->
        intent?.extras?.getString("message_id", null)?.let {
            lrAdapter?.removeMessage(it)
        }
    }

    private val onUserRemovedReceiver = LocalBroadcastReceiverExtended(context,
            IntentFilter(ChatSocketComponent.LR_CHAT_USER_REMOVED_BROADCAST
                    ,ChatSocketComponent.LR_CHAT_USER_REMOVED_BROADCAST)){ _, intent ->
        intent?.extras?.getString("username", null)?.let {
            lrAdapter?.removeUser(it)
        }
    }

    init {
        adapter = LRChatAdapter(context, LinkedHashMap())
        layoutManager = LinearLayoutManager(context)
        val recyclerView = this
        adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (layoutManager as LinearLayoutManager).smoothScrollToPosition(recyclerView, null, adapter!!.itemCount)
            }
        })
        onChatMessageRemovedReceiver.register()
        onUserRemovedReceiver.register()
        onChatMessageReceiver.register()
    }
}