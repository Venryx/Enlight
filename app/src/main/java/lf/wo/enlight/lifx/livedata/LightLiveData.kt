package lf.wo.enlight.lifx.livedata

import android.content.Context
import lf.wo.enlight.lifx.IAndroidLightService
import lf.wo.enlight.lifx.ILightsChangedDispatcher
import wo.lf.lifx.api.ILightChangeDispatcher
import wo.lf.lifx.api.Light
import wo.lf.lifx.api.LightProperty

class LightLiveData(context: Context) : AbstractAndroidLightServiceLiveData<Light>(context), ILightChangeDispatcher {

    inner class AwaitLight : ILightsChangedDispatcher {
        override fun groupsLocationChanged() {

        }

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

    override fun unbindService(service: IAndroidLightService?) {
        val light = value
        if (light != null) {
            unbindLight(light)
        } else {
            service?.removeChangeListener(awaitLightListener)
        }
    }

    override fun bindService(service: IAndroidLightService) {
        id?.let {
            val light = service.getLight(it)
            if (light != null) {
                bindLight(light)
                postValue(light)
            } else {
                postValue(null)
                service.addChangeListener(awaitLightListener)
            }
        }
    }

    var id: Long? = null
        set(value) {
            if (bound && id != value) {
                throw IllegalAccessError("can only set before the livedata is bound")
            }
            field = value
        }

    private fun unbindLight(light: Light) {
        light.removeChangeDispatcher(this)
    }

    private fun bindLight(light: Light) {
        light.addChangeDispatcher(this)
    }
}