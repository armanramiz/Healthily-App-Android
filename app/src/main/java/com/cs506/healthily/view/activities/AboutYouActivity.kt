package com.cs506.healthily.view.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.cs506.healthily.R
import com.cs506.healthily.viewModel.AboutYou
import com.cs506.healthily.viewModel.goalViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class AboutYouActivity : AppCompatActivity() {

    var notificationTime = "11"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_you)
        setUpSpinners()



        val next : Button = findViewById<Button>(R.id.btn_next)

        next.setOnClickListener {
            val alarmManager =
                getSystemService(ALARM_SERVICE) as AlarmManager //we are using alarm manager for the notification


            val notificationIntent = Intent(
                this,
                AlarmReceiver::class.java
            ) //this intent will be called when taping the notification

            val broadcast = PendingIntent.getBroadcast(
                this,
                100,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            ) //this pendingIntent will be called by the broadcast receiver


            val cal = Calendar.getInstance() //getting calender instance


            cal.timeInMillis = System.currentTimeMillis() //setting the time from device

            cal[Calendar.HOUR_OF_DAY] = notificationTime.toInt()
            Log.d("LOG", notificationTime)
            // cal.set NOT cal.add

            cal[Calendar.MINUTE] = 11
            cal[Calendar.SECOND] = 0
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                broadcast
            ) //alarm manager will repeat the notification each day at the set time
            startActivity(
                Intent(
                    this, StepCountGoalActivity
                    ::class.java
                )
            )
            finish()
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
        val ageSpinner = findViewById<NumberPicker>(R.id.sp_age)
        if (ageSpinner != null) {
            ageSpinner.displayedValues = ages
            ageSpinner.minValue = 0
            ageSpinner.maxValue = ages.size - 1
            ageSpinner.wrapSelectorWheel = false
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
                        ageSpinner.value = ageIndex
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ageSpinner?.setOnValueChangedListener { picker, oldVal, newVal ->
                val age = ages[newVal]
                val user = Firebase.auth.currentUser?.uid
                if (user != null) {
                    bindAge(user, age)
                }
            }
        }


        val weights = resources.getStringArray(R.array.Weights)
        val weightSpinner = findViewById<NumberPicker>(R.id.sp_weight)
        if (weightSpinner != null) {
            weightSpinner.displayedValues = weights
            weightSpinner.minValue = 0
            weightSpinner.maxValue = weights.size - 1
            weightSpinner.wrapSelectorWheel = false
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
                        weightSpinner.value = weightIndex
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            weightSpinner?.setOnValueChangedListener { picker, oldVal, newVal ->
                val weight = weights[newVal]
                val user = Firebase.auth.currentUser?.uid
                if (user != null) {
                    bindWeight(user, weight)
                }
            }
        }

        val heights  = arrayOf(
            "4'0\"",
            "4'1\"",
            "4'2\"",
            "4'3\"",
            "4'4\"",
            "4'5\"",
            "4'6\"",
            "4'7\"",
            "4'8\"",
            "4'9\"",
            "4'10\"",
            "4'11\"",
            "5'0\"",
            "5'1\"",
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
            "6'5\"",
            "6'6\"",
            "6'7\"",
            "6'8\"",
            "6'9\"",
            "6'10\"",
            "6'11\"",

        )



        val heightSpinner = findViewById<NumberPicker>(R.id.sp_height)
        if (heightSpinner != null) {
            heightSpinner.displayedValues = heights
            heightSpinner.minValue = 0
            heightSpinner.maxValue = heights.size - 1
            heightSpinner.wrapSelectorWheel = false
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
                        heightSpinner.value = heightIndex
//                    Toast.makeText(this, "Gender found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            heightSpinner?.setOnValueChangedListener { picker, oldVal, newVal ->
                val height = heights[newVal]
                val user = Firebase.auth.currentUser?.uid
                if (user != null) {
                    bindHeight(user, height)
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
                        var time = times[position]
                        val user = Firebase.auth.currentUser?.uid
                        if (user != null) {
                            bindAvailabilityStart(user, time)

                            var hour = ""


                            if(time[1]== 'a' || time[1] == 'p'){
                                 hour = time[0].toString()
                                 time = time[1].toString() + time[2].toString()
                            }else{

                                 hour = time[0].toString() + time[1].toString()
                                 time = time[2].toString() + time[3].toString()
                            }

                            if (time == "pm"){
                                var hrInt = hour.toInt()
                                hrInt += 12
                                hour = hrInt.toString()
                            }

                            notificationTime = hour



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

     fun bindGender(userId: String, gender: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setGenderFromRepo(userId, gender)

    }

     fun bindAge(userId: String, age: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setAgeFromRepo(userId, age)

    }

     fun bindHeight(userId: String, height: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setHeightFromRepo(userId, height)

    }

    fun bindWeight(userId: String, weight: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setWeightFromRepo(userId, weight)

    }

     fun bindAvailabilityStart(userId: String, start: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)

        aboutYouViewModel.setAvailabilityStartFromRepo(userId, start)

    }

     fun bindAvailabilityEnd(userId: String, end: String) {

        val aboutYouViewModel: AboutYou = ViewModelProviders.of(this).get(AboutYou::class.java)
        aboutYouViewModel.setAvailabilityEndFromRepo(userId, end)

    }








}
