package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var mapPoi: PointOfInterest? = null
    private var isLocationAddressSelected = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var poiName: String? = null
    private var markerLocation: Marker? = null

    companion object {
        private const val PERMISSION_CODE_LOCATION_REQUEST = 1
        private const val REQUEST_LOCATION_PERMISSION=1
    }


    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        onLocationSelected()
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapLocation) as SupportMapFragment
        mapFragment.getMapAsync(this)
        onLocationSelected()

        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val latitude = 30.0434574
        val longitude = 31.2765762
        val zoomLevel = 17f
        val overlaySize = 100f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))
        val androidOverlay =
            GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.map))
                .position(homeLatLng, overlaySize)

        map.addGroundOverlay(androidOverlay)
        onMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()

        if (isPermissionGranted()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

    }

    private fun onMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLong ->
            map.clear()
            val currentLocation = getAddress(latLong.latitude, latLong.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            isLocationAddressSelected = true
            latitude = latLong.latitude
            longitude = latLong.longitude
            poiName = currentLocation
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f ,Long: %2$.5f",
                latLong.latitude,
                latLong.longitude
            )
            markerLocation = map.addMarker(
                MarkerOptions()
                    .position(latLong)
                    .title(currentLocation)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            markerLocation?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLong))


        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        var stringAddAddress = ""
        val geocoderAddress = Geocoder(context, Locale.getDefault())
        try {
            val addAddresses: List<Address>? =
                geocoderAddress.getFromLocation(latitude, longitude, 1)
            if (addAddresses != null) {
                val returnedAddAddress: Address = addAddresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddAddress.getAddressLine(i)).append("\n")
                }
                stringAddAddress = strReturnedAddress.toString()

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return stringAddAddress

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { Poi ->
            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Poi.latLng, 15f))
            val poiMarker = map.addMarker(
                MarkerOptions().position(Poi.latLng)
                    .title(Poi.name)
            )
            poiMarker?.showInfoWindow()
            mapPoi = Poi
            isLocationAddressSelected = true
            latitude = Poi.latLng.latitude
            longitude = Poi.latLng.longitude
            poiName = Poi.name

        }

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e(AuthenticationActivity.TAG, "Style failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(AuthenticationActivity.TAG, " style. Error: ", e)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

        } else {
           requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_CODE_LOCATION_REQUEST
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            enableMyLocation()
        } else {
            showRationaleAlertDialog()
        }
    }

    private fun onLocationSelected() {

            markerLocation?.let { marker ->
                _viewModel.latitude.value = marker.position.latitude
                _viewModel.longitude.value = marker.position.longitude
                _viewModel.reminderSelectedLocationStr.value = marker.title
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }



    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { currentLocation: Location? ->
                    currentLocation?.let {
                        val userLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        markerLocation = map.addMarker(
                            MarkerOptions().position(userLocation)
                                .title(getString(R.string.my_current_location))
                        )
                        markerLocation?.showInfoWindow()
                    }
                }
        }

        else{
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showRationaleAlertDialog() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_select_permission)
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _ ->
                    requestLocationPermission()
                }
                .create()
                .show()

        } else {
            requestLocationPermission()
        }
    }
}
