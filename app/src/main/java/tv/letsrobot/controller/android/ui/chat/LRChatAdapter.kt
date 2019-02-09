package tv.letsrobot.controller.android.ui.chat

import android.content.Context
import android.graphics.Color
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
class LRChatAdapter(
        internal var mCtx: Context,
        internal var chatMessages: LinkedHashMap<String, TTSBaseComponent.TTSObject>
) : RecyclerView.Adapter<LRChatAdapter.LRChatViewHolder>() {
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): LRChatViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.lr_chat_item_layout, parent, false)
        return LRChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: LRChatViewHolder, position: Int) {
        val chatMessage = chatMessages.values.elementAt(position)
        holder.userField.text = chatMessage.user
        holder.userField.setTextColor(Color.parseColor(chatMessage.color))
        holder.messageField.text = chatMessage.text
        //TODO icons if mod or owner? For now, just a simple UI
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    fun addMessage(obj : TTSBaseComponent.TTSObject) {
        chatMessages[obj.message_id] = obj
        val index = chatMessages.keys.indexOf(obj.message_id)
        //notifyItemInserted(index)
        notifyDataSetChanged()
    }

    fun removeMessage(id : String){
        val index = chatMessages.keys.indexOf(id)
        if(index != -1){
            chatMessages.remove(id)
//            notifyItemRemoved(index)
        }
        notifyDataSetChanged()
    }

    inner class LRChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userField = itemView.findViewById(R.id.lrChatUserField) as TextView
        internal var messageField = itemView.findViewById(R.id.lrChatMessageField) as TextView
    }
}