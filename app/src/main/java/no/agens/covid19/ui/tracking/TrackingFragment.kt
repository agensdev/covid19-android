package no.agens.covid19.ui.tracking

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tracking.*
import no.agens.covid19.*

class TrackingFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonActivateTracking.setOnClickListener {
            buttonActivateTracking.isEnabled = false
            MessageBus.publish(RequestLocationPermissions)
        }
    }

    override fun onResume() {
        buttonActivateTracking.isEnabled = true
        super.onResume()

        val permissionAccessFineLocationApproved = ActivityCompat
            .checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        val permissionAccessBackgroundApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ActivityCompat
                .checkSelfPermission(context!!,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED else true

        if (permissionAccessBackgroundApproved && permissionAccessFineLocationApproved) {
            locationPermissionsGranted()
        }

        MessageBus.onBusChanged(this) {
            when (it) {
                is LocationAccessGranted -> {
                    locationPermissionsGranted()
                }
            }
        }
    }

    private fun locationPermissionsGranted() {

        buttonActivateTracking.setText(R.string.tracking_button_is_active)
        buttonActivateTracking.setBackgroundColor(
            resources.getColor(R.color.tracking_active, context!!.theme))
        buttonActivateTracking.setOnClickListener {
            AlertDialog.Builder(context!!).setMessage("Stop location tracking").show()
        }
    }
}
