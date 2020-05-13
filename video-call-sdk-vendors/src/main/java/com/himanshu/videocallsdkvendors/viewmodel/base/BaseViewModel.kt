package com.himanshu.videocallsdkvendors.viewmodel.base

import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.himanshu.videocallsdkvendors.interfaces.DataBindingViewClickCallbacks

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val TAG = javaClass.name
    protected var appContext: Context
    protected var callback: DataBindingViewClickCallbacks? = null
    protected val toastMessage = MutableLiveData<String>()
    private val combinedToastMessages = MediatorLiveData<String>()

    fun attachCallback(callback: DataBindingViewClickCallbacks?) {
        this.callback = callback
    }

    open fun onClicked(view: View?) {
        if (callback != null) {
            callback!!.onClickEvent(view)
        }
    }

    private fun attachRepositoryToastMessageLiveData(toastMessage: LiveData<String>?) {
        if (toastMessage != null) {
            combinedToastMessages.addSource(toastMessage) { value: String -> combinedToastMessages.setValue(value) }
        }
    }

    /**
     * Override this method to return the repository live data for toastMessage to be attached to View model toastMessage
     *
     * @return LiveData<String>
     */
    protected abstract fun setRepositoryToastMessage(): LiveData<String>?

    val toastMessages: LiveData<String>
        get() = combinedToastMessages

    init {
        appContext = application.applicationContext
        combinedToastMessages.addSource(toastMessage) { value: String -> combinedToastMessages.setValue(value) }
        attachRepositoryToastMessageLiveData(setRepositoryToastMessage())
    }
}