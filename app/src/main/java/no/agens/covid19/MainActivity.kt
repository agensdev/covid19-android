package no.agens.covid19

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import no.agens.covid19.messages.LocationAccessGranted
import no.agens.covid19.messages.RequestLocationPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), CheckLocationPermissionsListener,
    Observer<MessageBus.Message> {

    companion object {
        const val ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.fragment_tracking, R.id.fragment_information))
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        MessageBus.observeBus(this, this)
    }

    override fun checkLocationPermissions() {
        val permissionAccessFineLocationApproved = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        Timber.d("")

        if (permissionAccessFineLocationApproved) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundLocationPermissionApproved = ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED

                if (backgroundLocationPermissionApproved) {
                    MessageBus.publish(LocationAccessGranted())
                } else {
                    // App can only access location in the foreground. Display a dialog
                    // warning the user that your app must have all-the-time access to
                    // location in order to function properly. Then, request background
                    // location.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        ACCESS_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                MessageBus.publish(LocationAccessGranted())
            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACCESS_LOCATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                checkLocationPermissions()
//                Timber.d("We have location access!")
//                MessageBus.publish(LocationAccessGranted())
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_require_location_title)
                    .setMessage(R.string.dialog_require_location_message)
                    .setNegativeButton(R.string.generic_no) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.generic_yes) { dialog, _ ->
                        dialog.dismiss()
                        checkLocationPermissions()
                    }.show()
            }
        }
    }

    override fun onChanged(t: MessageBus.Message?) {

        when (t) {
            is RequestLocationPermissions -> {
                checkLocationPermissions()
            }
        }
    }

}
