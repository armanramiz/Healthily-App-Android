package com.cs506.healthily.view.activities

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class ProfileEditorActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_profile_editor)
//    }
//}



//package com.cs506.healthily.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.cs506.healthily.R
import com.cs506.healthily.viewModel.AboutYou
import com.cs506.healthily.viewModel.goalViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.widget.ArrayAdapter
import com.cs506.healthily.view.fragments.mGoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

lateinit var mGoogleSignInClient: GoogleSignInClient

class ProfileEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_editor)
        setUpSpinners()

        val home : Button = findViewById<Button>(R.id.btn_home)
        home.setOnClickListener {
            startActivity(
                Intent(
                    this, MainActivity
                    ::class.java
                )
            )
            finish()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        var currActivity: AppCompatActivity = this
        mGoogleSignInClient = GoogleSignIn.getClient(currActivity, gso)
        val logout: Button = findViewById<Button>(R.id.btn_log_out)
        logout.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(currActivity, SignInActivity::class.java)
                Toast.makeText(currActivity, "Logging Out", Toast.LENGTH_SHORT).show()
                startActivity(intent)

            }
        }
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_HEART_POINTS, FitnessOptions.ACCESS_READ)
            .build()
        val disable: Button = findViewById<Button>(R.id.btn_disable_fit_permissions)
        disable.setOnClickListener {
            val fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                .build()
            val signInOptions = GoogleSignInOptions.Builder().addExtension(fitnessOptions).build()
            val client = GoogleSignIn.getClient(currActivity, signInOptions)
            client.revokeAccess()
        }

    }


    private fun setUpSpinners() {
        val genders = resources.getStringArray(R.array.Genders)
        val genderSpinner = findViewById<Spinner>(R.id.sp_gender)
        if (genderSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, genders
            )
            genderSpinner.adapter = adapter

            // Added code that should set the spinner to whatever the user's current settings are
            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currGender = goals.gender.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var genderIndex = 0
                for (elem in genders.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (genders[elem].toString().contentEquals(currGender)) {
                        genderIndex = elem
                        genderSpinner.setSelection(genderIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

//            Toast.makeText(this, currGender, Toast.LENGTH_SHORT).show()



            genderSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val gender = genders[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindGender(user, gender)
                        }
                    }
                    selectionCount++
                }

            }

        }












        val ages = resources.getStringArray(R.array.Ages)
        val ageSpinner = findViewById<Spinner>(R.id.sp_age)
        if (ageSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, ages
            )
            ageSpinner.adapter = adapter

            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currAge = goals.age.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var ageIndex = 0
                for (elem in ages.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (ages[elem].toString().contentEquals(currAge)) {
                        ageIndex = elem
                        ageSpinner.setSelection(ageIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val age = ages[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindAge(user, age)
                        }
                    }
                    selectionCount++
                }

            }
        }


        val weights = resources.getStringArray(R.array.Weights)
        val weightSpinner = findViewById<Spinner>(R.id.sp_weight)
        if (weightSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, weights
            )
            weightSpinner.adapter = adapter

            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currWeight = goals.weight.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var weightIndex = 0
                for (elem in weights.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (weights[elem].toString().contentEquals(currWeight)) {
                        weightIndex = elem
                        weightSpinner.setSelection(weightIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            weightSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val weight = weights[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindWeight(user, weight)
                        }
                    }
                    selectionCount++
                }

            }
        }

        val heights  = arrayOf("Height:", "5'1\"",
            "5'2\"",
            "5'3\"",
            "5'4\"",
            "5'5\"",
            "5'6\"",
            "5'7\"",
            "5'8\"",
            "5'9\"",
            "5'10\"",
            "5'11\"",
            "6'0\"",
            "6'1\"",
            "6'2\"",
            "6'3\"",
            "6'4\"",
            "6'5\""
        )



        val heightSpinner = findViewById<Spinner>(R.id.sp_height)
        if (heightSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, heights
            )
            heightSpinner.adapter = adapter

            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currHeight = goals.height.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var heightIndex = 0
                for (elem in heights.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (heights[elem].toString().contentEquals(currHeight)) {
                        heightIndex = elem
                        heightSpinner.setSelection(heightIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            heightSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val height = heights[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindHeight(user, height)
                        }
                    }
                    selectionCount++
                }

            }
        }

        val times  = arrayOf("12am",
            "1am",
            "2am",
            "3am",
            "4am",
            "5am",
            "6am",
            "7am",
            "8am",
            "9am",
            "10am",
            "11am",
            "12pm",
            "1pm",
            "2pm",
            "3pm",
            "4pm",
            "5pm",
            "6pm",
            "7pm",
            "8pm",
            "9pm",
            "10pm",
            "11pm"
        )



        val availabilityStartSpinner = findViewById<Spinner>(R.id.sp_availability_start)
        if (availabilityStartSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, times
            )
            availabilityStartSpinner.adapter = adapter

            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currStart = goals.availabilityStart.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var startIndex = 0
                for (elem in times.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (times[elem].toString().contentEquals(currStart)) {
                        startIndex = elem
                        availabilityStartSpinner.setSelection(startIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            availabilityStartSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val time = times[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindAvailabilityStart(user, time)
                        }
                    }
                    selectionCount++
                }

            }
        }

        val availabilityEndSpinner = findViewById<Spinner>(R.id.sp_availability_end)
        if (availabilityEndSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, times
            )
            availabilityEndSpinner.adapter = adapter

            val goalViewModel: goalViewModel =
                ViewModelProviders.of(this).get(goalViewModel::class.java)
            goalViewModel.getUserSettings()?.observe(this) { goals ->
                var currEnd = goals.availabilityEnd.toString()
//                Toast.makeText(this, gender, Toast.LENGTH_SHORT).show()
                var endIndex = 0
                for (elem in times.indices) {
//                Toast.makeText(this, elem, Toast.LENGTH_SHORT).show()
                    if (times[elem].toString().contentEquals(currEnd)) {
                        endIndex = elem
                        availabilityEndSpinner.setSelection(endIndex)
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            availabilityEndSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                var selectionCount = 0
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing Selected")
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (selectionCount > 0) {
                        val time = times[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindAvailabilityEnd(user, time)
                        }
                    }
                    selectionCount++
                }

            }
        }



    }

    private fun bindGender(userId: String, gender: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setGenderFromRepo(userId, gender)

    }

    private fun bindAge(userId: String, age: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setAgeFromRepo(userId, age)

    }

    private fun bindHeight(userId: String, height: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setHeightFromRepo(userId, height)

    }

    private fun bindWeight(userId: String, weight: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setWeightFromRepo(userId, weight)

    }

    private fun bindAvailabilityStart(userId: String, start: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setAvailabilityStartFromRepo(userId, start)

    }

    private fun bindAvailabilityEnd(userId: String, end: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setAvailabilityEndFromRepo(userId, end)

    }








}