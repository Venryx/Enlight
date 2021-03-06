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
import wo.lf.lifx.api.Location

class LightLocationLiveData(context: Context) : AbstractAndroidLightServiceLiveData<List<Location>>(context), ILightsChangedDispatcher {
    override fun groupsLocationChanged() {
        postValue(service?.locations)
    }

    override fun lightChanged(light: Light, property: LightProperty, oldValue: Any?, newValue: Any?) {
    }

    override fun lightsChanged(value: List<Light>) {
    }

    override fun unbindService(service: IAndroidLightService?) {
    }

    override fun bindService(service: IAndroidLightService) {
        service.addChangeListener(this)
        value = service.locations
    }
}