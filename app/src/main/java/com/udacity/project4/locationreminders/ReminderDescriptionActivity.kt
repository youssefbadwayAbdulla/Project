package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class ReminderDescriptionActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var geofencingClientDescriptionLocation: GeofencingClient
    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
//        TODO: Add the implementation of the reminder details
        geofencingClientDescriptionLocation = LocationServices.getGeofencingClient(this)
        val reminderItemDescriptionLocation = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        removeGeofenceDescriptionLocation(reminderItemDescriptionLocation.id)
        binding.reminderDataItem = reminderItemDescriptionLocation
    }

    private fun removeGeofenceDescriptionLocation(geofenceId: String) {
        geofencingClientDescriptionLocation.removeGeofences(listOf(geofenceId)).run {
            addOnCompleteListener {tasks->
                if (tasks.isSuccessful){
                    Toast.makeText(applicationContext, getString(R.string.geofences_removed_description_location), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, getString(R.string.geofences_isNot_removed_description_location), Toast.LENGTH_SHORT).show()
                }
            }
            }
    }
}
