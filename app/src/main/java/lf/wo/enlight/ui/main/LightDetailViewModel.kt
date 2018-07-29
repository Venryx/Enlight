package lf.wo.enlight.ui.main

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import lf.wo.enlight.lifx.AndroidLightService
import lf.wo.enlight.lifx.IAndroidLightService
import lf.wo.enlight.lifx.ILightsChangedDispatcher
import wo.lf.lifx.api.ILightChangeDispatcher
import wo.lf.lifx.api.Light
import wo.lf.lifx.api.LightProperty
import javax.inject.Inject

class LightDetailViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    val light = LightLiveData(application.applicationContext)

    private val mutableSettings = MutableLiveData<LightDetailViewSettings>().apply { value = LightDetailViewSettings(ZoneSelectionMode.ALL, setOf()) }
    val settings = mutableSettings

    fun initialize(lightId: Long) {
        light.id = lightId
    }

    fun setZoneSelectionMode(mode: ZoneSelectionMode) {
        mutableSettings.value = settings.value?.copy(selectionMode = mode)
    }

    fun setSelection(zone: Int, selected: Boolean) {
        mutableSettings.value = settings.value?.let { it.copy(selectedZones = if (selected) it.selectedZones.plus(zone) else it.selectedZones.minus(zone)) }
    }
}

enum class ZoneSelectionMode {
    ALL,
    INDIVIDUAL
}

data class LightDetailViewSettings(val selectionMode: ZoneSelectionMode, val selectedZones: Set<Int>)

class LightLiveData(private val context: Context) : LiveData<Light>(), ILightChangeDispatcher {

    inner class AwaitLight : ILightsChangedDispatcher {
        override fun lightChanged(light: Light, property: LightProperty, oldValue: Any?, newValue: Any?) {

        }

        override fun lightsChanged(value: List<Light>) {
            value.firstOrNull { it.id == id }?.let { light ->
                service?.removeChangeListener(this)
                bindLight(light)
                postValue(light)
            }
        }
    }

    val awaitLightListener by lazy {
        AwaitLight()
    }


    override fun onLightChange(light: Light, property: LightProperty, oldValue: Any?, newValue: Any?) {
        postValue(light)
    }

    private var service: IAndroidLightService? = null
    private var bound = false

    var id: Long? = null
        set(value) {
            if (bound && id != value) {
                throw IllegalAccessError("can only set before the livedata is bound")
            }
            field = value
        }

    override fun onActive() {
        if (!bound) {
            context.bindService(Intent(context, AndroidLightService::class.java), connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onInactive() {
        if (bound) {
            val light = value
            if (light != null) {
                unbindLight(light)
            } else {
                service?.removeChangeListener(awaitLightListener)
            }
            context.unbindService(connection)
            bound = false
        }
    }

    private fun unbindLight(light: Light) {
        light.removeChangeDispatcher(this)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        serviceBinder: IBinder) {
            val binder = serviceBinder as AndroidLightService.AndroidLightServiceBinder
            this@LightLiveData.service = binder.service.apply {
                id?.let {
                    val light = getLight(it)
                    if (light != null) {
                        bindLight(light)
                        postValue(light)
                    } else {
                        postValue(null)
                        addChangeListener(awaitLightListener)
                    }
                }
            }
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    private fun bindLight(light: Light) {
        light.addChangeDispatcher(this)
    }
}