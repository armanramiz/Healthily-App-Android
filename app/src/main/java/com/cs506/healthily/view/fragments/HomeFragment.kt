package com.cs506.healthily.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.cs506.healthily.R
import com.cs506.healthily.adapter.GooglePlaceAdapter
import com.cs506.healthily.adapter.InfoWindowAdapter
import com.cs506.healthily.constant.AppConstant
import com.cs506.healthily.databinding.FragmentHomeBinding
import com.cs506.healthily.interfaces.NearLocationInterface
import com.cs506.healthily.models.googlePlaceModel.GooglePlaceModel
import com.cs506.healthily.models.googlePlaceModel.GoogleResponseModel
//import com.cs506.healthily.permissions.AppPermissions
import com.cs506.healthily.utility.LoadingDialog
import com.cs506.healthily.utility.State
import com.cs506.healthily.view.activities.DirectionActivity
import com.cs506.healthily.viewModel.LocationViewModel
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.flow.collect

//new stuff
import com.cs506.healthily.data.model.DaySteps
import com.cs506.healthily.data.model.Goals
import com.cs506.healthily.data.repository.GoalsRepository
import com.cs506.healthily.view.adapter.DayStepAdapter
import com.cs506.healthily.viewModel.DayStepsViewModel
import com.cs506.healthily.view.fragments.StepsFragment

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import com.jjoe64.graphview.series.DataPoint
import kotlin.math.roundToInt


class HomeFragment : Fragment(), OnMapReadyCallback, NearLocationInterface, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mGoogleMap: GoogleMap
    //private lateinit var appPermission: AppPermissions
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()
    private var isLocationPermissionOk = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var currentLocation: Location
    private var currentMarker: Marker? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private var isTrafficEnable: Boolean = false
   // private var radius = 1500
      private val locationViewModel: LocationViewModel by viewModels<LocationViewModel>()
     private lateinit var googlePlaceList: ArrayList<GooglePlaceModel>
    private lateinit var googlePlaceAdapter: GooglePlaceAdapter
    private var userSavedLocaitonId: ArrayList<String> = ArrayList()
     private var infoWindowAdapter: InfoWindowAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //appPermission = AppPermissions()
        loadingDialog = LoadingDialog(requireActivity())
        firebaseAuth = Firebase.auth
        googlePlaceList = ArrayList()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true

                if (isLocationPermissionOk)
                    setUpGoogleMap()
                else
                    Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

        for (placeModel in AppConstant.placesName) {
            val chip = Chip(requireContext())
            chip.text = placeModel.name
            chip.id = placeModel.id
            chip.setPadding(8, 8, 8, 8)
            chip.setTextColor(resources.getColor(R.color.white, null))
            chip.chipBackgroundColor = resources.getColorStateList(R.color.primaryColor, null)
            chip.chipIcon = ResourcesCompat.getDrawable(resources, placeModel.drawableId, null)
            chip.isCheckable = true
            chip.isCheckedIconVisible = false
            binding.placesGroup.addView(chip)
        }

        binding.currentLocation.setOnClickListener { getCurrentLocation() }

        binding.enableTraffic.setOnClickListener {

            if (isTrafficEnable) {
                mGoogleMap?.apply {
                    isTrafficEnabled = false
                    isTrafficEnable = false
                }
            } else {
                mGoogleMap?.apply {
                    isTrafficEnabled = true
                    isTrafficEnable = true
                }
            }
        }

        binding.btnMapType.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.apply {
                menuInflater.inflate(R.menu.map_type_menu, popupMenu.menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {

                        R.id.btnNormal -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        R.id.btnSatellite -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        R.id.btnTerrain -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }
                    true
                }

                show()
            }
        }

        binding.placesGroup.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId != -1) {
                val placeModel = AppConstant.placesName[checkedId - 1]
               // val viewModel: DayStepsViewModel
                binding.edtPlaceName.setText(placeModel.name)

                val viewModel: DayStepsViewModel =
                    ViewModelProviders.of(this).get(DayStepsViewModel::class.java)
                    viewModel.getAllDays()?.observe(viewLifecycleOwner) { mDays ->
                    getNearByPlace(placeModel.placeType,   mDays)

                }



            }
        }

        setUpRecyclerView()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Near me required location permission to access your location")
                    .setPositiveButton("Ok") { _, _ ->
                        requestLocation()
                    }.create().show()
            }

            else -> {
                requestLocation()
            }
        }

    }

    private fun requestLocation() {
        permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        mGoogleMap?.setOnMarkerClickListener(this)

        setUpLocationUpdate()
    }
    private fun setUpLocationUpdate() {

        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for (location in locationResult?.locations!!) {
                    Log.d("TAG", "onLocationResult: ${location.longitude} ${location.latitude}")
                }
            }
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                Toast.makeText(requireContext(), "Location update start", Toast.LENGTH_SHORT).show()
            }
        }

        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            currentLocation = it
            infoWindowAdapter=null
            infoWindowAdapter = InfoWindowAdapter(currentLocation!!,requireContext())
            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
            moveCameraToLocation(currentLocation)
        }
    }

    private fun moveCameraToLocation(location: Location) {

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), 17f
        )

        val markerOption = MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .snippet(firebaseAuth.currentUser?.displayName)

        currentMarker?.remove()
        currentMarker = mGoogleMap?.addMarker(markerOption)
        currentMarker?.tag = 703
        mGoogleMap?.animateCamera(cameraUpdate)

    }


    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        Log.d("TAG", "stopLocationUpdates: Location Update Stop")
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null) {
            startLocationUpdates()
            currentMarker?.remove()
        }
    }

    private fun activityRecommend(){
    //((step count goal - current step count) / (steps to meters conversion)) or maybe that divided by 2,
    // depending on whether we assume the user is walking there, or going there and back




    }

    private fun getNearByPlace(placeType: String, days: List<DaySteps>) {

// For example, 70 steps on a 50-meter course (5000 cm / 70 steps) would reveal a 71.43 cm step.

        //var steps = StepsFragment()
       // var currentStepCount = steps.totalSteps
        //var stepCountGoal = steps.stepGoal

        var radius = 0
        var totalSteps = 0
        totalSteps = days[0].steps?.toInt()!!
        val stepGoal = days[0].stepGoal?.toInt()!!
        radius = ((stepGoal - totalSteps) * 0.718).toInt()
        if(radius < 1500){//((step count goal - current step count) / (steps to meters conversion))
            radius = 1500
        }


        val url = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&radius=" + radius + "&type=" + placeType + "&key=" +
                resources.getString(R.string.API_KEY))



        lifecycleScope.launchWhenStarted {
            locationViewModel.getNearByPlace(url).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Success -> {
                        loadingDialog.stopLoading()
                        val googleResponseModel: GoogleResponseModel =
                            it.data as GoogleResponseModel

                        if (googleResponseModel.googlePlaceModelList !== null &&
                            googleResponseModel.googlePlaceModelList.isNotEmpty()
                        ) {
                            googlePlaceList.clear()
                            mGoogleMap?.clear()

                            for (i in googleResponseModel.googlePlaceModelList.indices) {
                                googlePlaceList.add(googleResponseModel.googlePlaceModelList[i])
                                addMarker(googleResponseModel.googlePlaceModelList[i], i)
                            }
                            googlePlaceAdapter.setGooglePlaces(googlePlaceList)
                        } else {
                            mGoogleMap?.clear()
                            googlePlaceList.clear()

                        }

                    }
                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make( binding.root, "There are no nearby places of that kind!", Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

            }


        }
    }

    private fun addMarker(googlePlaceModel: GooglePlaceModel, position: Int) {
        val markerOptions = MarkerOptions()
            .position(
                LatLng(
                    googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!
                )
            )
            .title(googlePlaceModel.name)
            .snippet(googlePlaceModel.vicinity)

        markerOptions.icon(getCustomIcon())
        mGoogleMap?.addMarker(markerOptions)?.tag = position

    }

    private fun getCustomIcon(): BitmapDescriptor {

        val background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
        background?.setTint(resources.getColor(R.color.quantum_googred900, null))
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            background?.intrinsicWidth!!, background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    private fun setUpRecyclerView() {
        val snapHelper: SnapHelper = PagerSnapHelper()
        googlePlaceAdapter = GooglePlaceAdapter(this)

        binding.placesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            setHasFixedSize(false)
            adapter = googlePlaceAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val linearManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = linearManager.findFirstCompletelyVisibleItemPosition()
                    if (position > -1) {
                        val googlePlaceModel: GooglePlaceModel = googlePlaceList[position]
                        mGoogleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    googlePlaceModel.geometry?.location?.lat!!,
                                    googlePlaceModel.geometry.location.lng!!
                                ), 20f
                            )
                        )
                    }
                }
            })
        }

        snapHelper.attachToRecyclerView(binding.placesRecyclerView)
    }

    override fun onDirectionClick(googlePlaceModel: GooglePlaceModel) {
        val placeId = googlePlaceModel.placeId
        val lat = googlePlaceModel.geometry?.location?.lat
        val lng = googlePlaceModel.geometry?.location?.lng
        val intent = Intent(requireContext(), DirectionActivity::class.java)
        intent.putExtra("placeId", placeId)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)

        startActivity(intent)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val markerTag = marker.tag as Int
        binding.placesRecyclerView.scrollToPosition(markerTag)
        return false
    }



}