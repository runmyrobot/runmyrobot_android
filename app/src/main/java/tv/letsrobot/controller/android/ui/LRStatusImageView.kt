package tv.letsrobot.controller.android.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseIntArray
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.controller.android.R

/**
 * Status view that will communicate directly with a chosen component
 *
 * FIXME Leaks memory since it uses Context
 * TODO replace with ViewModel
 */
class LRStatusImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr), Runnable {
    private var colorLookup = SparseIntArray().also{
        appendColor(context, it, R.color.colorIndicatorDisabledFromSettings)
        appendColor(context, it, R.color.colorIndicatorDisabled)
        appendColor(context, it, R.color.colorIndicatorConnecting)
        appendColor(context, it, R.color.colorIndicatorStable)
        appendColor(context, it, R.color.colorIndicatorUnstable)
        appendColor(context, it, R.color.colorIndicatorError)
    }

    @Suppress("DEPRECATION")
    private fun appendColor(context: Context, it: SparseIntArray, resId: Int) {
        val color : Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(resId)
        }
        else{
            context.resources.getColor(resId)
        }
        it.append(resId, color)
    }

    private var uiHandler : Handler = Handler(Looper.getMainLooper())
    private var component: String? = null
    private var status : ComponentStatus? = null
    val values = ComponentStatus.values()
    init{
        uiHandler.post(this)
    }

    fun setDrawableColor(drawable: Drawable, id : Int) : Drawable{
        val color : Int = colorLookup.get(id, Color.BLACK)
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

    fun onDestroy(){
        this.component?.let { EventManager.unsubscribe(it, onStatus) }
        uiHandler.removeCallbacksAndMessages(null)
    }
}
