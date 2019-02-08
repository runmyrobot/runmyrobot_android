package tv.letsrobot.controller.android.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import tv.letsrobot.android.api.components.tts.TTSBaseComponent
import tv.letsrobot.controller.android.R

/**
 * A Chat adapter for LetsRobot chat that converts a List of TTSBaseComponent.TTSObject into UI
 */
class LRChatAdapter(internal var mCtx: Context, internal var chatMessages: ArrayList<TTSBaseComponent.TTSObject>) : RecyclerView.Adapter<LRChatAdapter.LRChatViewHolder>() {
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): LRChatViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.lr_chat_item_layout, parent, false)
        return LRChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: LRChatViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        //holder.userField.text = chatMessage.user
        //holder.messageField.text = chatMessage.text
        //TODO icons if mod or owner? For now, just a simple UI
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    fun addMessage(obj : TTSBaseComponent.TTSObject) {
        chatMessages.add(obj)
    }

    inner class LRChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userField = itemView.findViewById(R.id.lrChatUserField) as TextView
        internal var messageField = itemView.findViewById(R.id.lrChatMessageField) as TextView
    }
}