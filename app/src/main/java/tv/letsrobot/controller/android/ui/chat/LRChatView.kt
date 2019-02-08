package tv.letsrobot.controller.android.ui.chat

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tv.letsrobot.android.api.components.tts.TTSBaseComponent

/**
 * Created by Brendon on 2/7/2019.
 */
class LRChatView : RecyclerView{
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val lrAdapter: LRChatAdapter?
        get() {return adapter as? LRChatAdapter}

    init {
        adapter = LRChatAdapter(context, ArrayList())
        layoutManager = LinearLayoutManager(context)
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter!!.addMessage(TTSBaseComponent.TTSObject("Test", 1.0f, "banana"))
        lrAdapter?.notifyDataSetChanged()
    }
}