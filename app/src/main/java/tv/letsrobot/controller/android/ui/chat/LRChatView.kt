package tv.letsrobot.controller.android.ui.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tv.letsrobot.android.api.components.ChatSocketComponent
import tv.letsrobot.android.api.components.tts.TTSBaseComponent
import tv.letsrobot.controller.android.robot.RobotSettingsObject

/**
 * Created by Brendon on 2/7/2019.
 */
class LRChatView : RecyclerView{
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
    val robotId = RobotSettingsObject.load(context).robotId

    private val lrAdapter: LRChatAdapter?
        get() {return adapter as? LRChatAdapter}

    private val onMessageReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { it.hasExtra("json") }?.let {
                lrAdapter?.addMessage(it.getSerializableExtra("json") as TTSBaseComponent.TTSObject)
            }
        }
    }

    private val onChatRemovedReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { it.hasExtra("message_id") }?.let {
                lrAdapter?.removeMessage(it.getStringExtra("message_id"))
            }
        }
    }

    private val onUserRemovedReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf {
                it.hasExtra("username")
            }?.let {
                lrAdapter?.removeUser(it.getStringExtra("username"))
            }
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
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(
                        onMessageReceiver,
                        IntentFilter(ChatSocketComponent.LR_CHAT_MESSAGE_WITH_NAME_BROADCAST)
                )
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(
                        onChatRemovedReceiver,
                        IntentFilter(ChatSocketComponent.LR_CHAT_MESSAGE_REMOVED_BROADCAST)
                )
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(
                        onUserRemovedReceiver,
                        IntentFilter(ChatSocketComponent.LR_CHAT_USER_REMOVED_BROADCAST)
                )
    }
}