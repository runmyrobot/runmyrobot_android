package tv.letsrobot.android.api.viewModels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import tv.letsrobot.android.api.interfaces.ILetsRobotControl
import tv.letsrobot.android.api.services.LetsRobotControlApi

/**
 * ViewModel to handle LetsRobot controller logic,
 * and automatically destroy listeners when activity is killed
 */
class LetsRobotViewModel : ViewModel(){

    lateinit var api : ILetsRobotControl
        private set

    /**
     * Calls api.getServiceStateObserver().observe(activity, observer) in ILetsRobotControl
     * @see ILetsRobotControl.getServiceStateObserver(activity, observer)
     */
    fun setStatusObserver(activity: FragmentActivity, observer : Observer<Int>){
        api.getServiceStateObserver().observe(activity, observer)
    }

    /**
     * Calls api.getServiceConnectionStatusObserver().observe(activity, observer) in ILetsRobotControl
     * @see ILetsRobotControl.getServiceConnectionStatusObserver(activity, observer)
     */
    fun setServiceConnectedObserver(activity: FragmentActivity, observer : Observer<Int>){
        api.getServiceConnectionStatusObserver().observe(activity, observer)
    }

    override fun onCleared() {
        super.onCleared()
        api.disconnectFromService()
    }

    companion object {
        fun getObject(activity: FragmentActivity) : LetsRobotViewModel {
            return ViewModelProviders.of(activity).get(LetsRobotViewModel::class.java).also {
                it.api = LetsRobotControlApi.getNewInstance(activity).also { api ->
                    api.connectToService()
                }
            }
        }
    }
}