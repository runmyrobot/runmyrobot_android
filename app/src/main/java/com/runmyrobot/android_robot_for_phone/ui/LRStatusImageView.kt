package com.runmyrobot.android_robot_for_phone.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import com.runmyrobot.android_robot_for_phone.R
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus

/**
 * Status view that will communicate directly with a chosen component
 */
class LRStatusImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr), Runnable {
    private var uiHandler : Handler = Handler(Looper.getMainLooper())
    private var component: String? = null
    private var status : ComponentStatus? = null
    val values = ComponentStatus.values()
    init{
        uiHandler.post(this)
    }

    @Suppress("DEPRECATION")
    fun setDrawableColor(drawable: Drawable, id : Int) : Drawable{
        val color : Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(id)
        }
        else{
            context.resources.getColor(id)
        }
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        return drawable
    }

    fun setStatus(componentStatus: ComponentStatus){
        background = when(componentStatus){
            ComponentStatus.DISABLED_FROM_SETTINGS -> setDrawableColor(background, R.color.colorIndicatorDisabledFromSettings)
            ComponentStatus.DISABLED -> setDrawableColor(background, R.color.colorIndicatorDisabled)
            ComponentStatus.CONNECTING -> setDrawableColor(background, R.color.colorIndicatorConnecting)
            ComponentStatus.STABLE -> setDrawableColor(background, R.color.colorIndicatorStable)
            ComponentStatus.INTERMITTENT -> setDrawableColor(background, R.color.colorIndicatorUnstable)
            ComponentStatus.ERROR -> setDrawableColor(background, R.color.colorIndicatorError)
        }
    }

    override fun run() {
        status?.let {
            setStatus(it)
            uiHandler.postDelayed(this, 100)
        } ?: run{
            setStatus(loopStatus())
            uiHandler.postDelayed(this, 1000)
        }
    }

    var i = 0
    private fun loopStatus(): ComponentStatus {
        var status : ComponentStatus = ComponentStatus.DISABLED_FROM_SETTINGS
        values.forEachIndexed { index, componentStatus ->
            if (index == i) {
                status = componentStatus
            }
        }
        i++
        if(i > values.size)
            i = 0
        return status
    }

    private val onStatus: (Any?) -> Unit = {
        it?.takeIf { it is ComponentStatus }?.let{
            status = it as ComponentStatus
        }
    }

    fun setComponentInterface(component: String) {
        this.component?.let { EventManager.unsubscribe(it, onStatus) }
        this.component = component
        EventManager.subscribe(component, onStatus)
    }
}
