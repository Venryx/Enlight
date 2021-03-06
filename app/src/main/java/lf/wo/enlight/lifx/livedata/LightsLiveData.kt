/*

  Copyright (c) 2018 Florian Sprenger

  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */

package lf.wo.enlight.lifx.livedata

import android.content.Context
import lf.wo.enlight.lifx.IAndroidLightService
import lf.wo.enlight.lifx.ILightsChangedDispatcher
import wo.lf.lifx.api.Light
import wo.lf.lifx.api.LightProperty

class LightsLiveData(context: Context) : AbstractAndroidLightServiceLiveData<List<Light>>(context), ILightsChangedDispatcher {
    override fun groupsLocationChanged() {
    }

    override fun lightChanged(light: Light, property: LightProperty, oldValue: Any?, newValue: Any?) {
        when (property) {
            LightProperty.Color, LightProperty.Power, LightProperty.Reachable -> postValue(value)
            else -> {
            }
        }
    }

    override fun lightsChanged(value: List<Light>) {
        postValue(value)
    }

    override fun unbindService(service: IAndroidLightService?) {
    }

    override fun bindService(service: IAndroidLightService) {
        service.addChangeListener(this)
        value = service.lights
    }
}