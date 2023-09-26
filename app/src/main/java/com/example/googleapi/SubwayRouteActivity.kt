package com.example.googleapi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.*
import com.google.maps.model.Unit

class SubwayRouteActivity : AppCompatActivity() {

    private lateinit var geoApiContext: GeoApiContext
    private lateinit var editTextStartStation: EditText
    private lateinit var editTextEndStation: EditText
    private lateinit var buttonFindRoute: Button
    private lateinit var textViewTransfer: TextView
    private lateinit var textViewDuration: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subway_route)

        editTextStartStation = findViewById(R.id.editTextStartStation)
        editTextEndStation = findViewById(R.id.editTextEndStation)
        buttonFindRoute = findViewById(R.id.buttonFindRoute)
        textViewTransfer = findViewById(R.id.textViewTransfer)
        textViewDuration = findViewById(R.id.textViewDuration)

        geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyAOsQfD2tdX0-rC27Aai9KSmMYtVYDF5qc")
            .build()

        buttonFindRoute.setOnClickListener {
            findSubwayRoute()
        }
    }

    private fun findSubwayRoute() {
        val startStationName = editTextStartStation.text.toString()
        val endStationName = editTextEndStation.text.toString()

        val startLocation = geocodeLocation(startStationName)
        val endLocation = geocodeLocation(endStationName)

        if (startLocation != null && endLocation != null) {

            val request = DirectionsApi.newRequest(geoApiContext)
                .origin(startLocation)
                .destination(endLocation)
                .mode(TravelMode.TRANSIT)
                .transitMode(TransitMode.SUBWAY)
                .units(Unit.METRIC)
                .language("ko")

            val result = request.await()

            if (result.routes.isNotEmpty()) {
                val route = result.routes[0]
                val leg = route.legs[0]

                val transferStations = mutableListOf<String>()

                var totalDurationSeconds: Long = 0 // Long으로 명시적으로 선언

                for (step in leg.steps) {
                    if (step.travelMode == TravelMode.TRANSIT && step.transitDetails.line.vehicle.type == VehicleType.SUBWAY) {
                        val stationName = step.transitDetails.departureStop.name
                        val stationNameWithoutLocation = stationName.substringBefore("역")
                        if (stationNameWithoutLocation != startStationName) {
                            transferStations.add(stationNameWithoutLocation)
                        }
                    }
                    totalDurationSeconds += step.duration.inSeconds.toLong() // Int를 Long으로 변환
                }

                val durationHours = totalDurationSeconds / 3600
                val durationMinutes = (totalDurationSeconds % 3600) / 60

                if (transferStations.isNotEmpty()) {
                    val transferStationsString = transferStations.joinToString(", ")
                    textViewTransfer.text = "환승역: $transferStationsString"
                } else {
                    textViewTransfer.text = "환승역이 없습니다."
                }

                textViewDuration.text = "소요 시간: ${durationHours}시간 ${durationMinutes}분"
            } else {
                textViewTransfer.text = "경로를 찾을 수 없습니다."
                textViewDuration.text = ""
            }
        } else {
            textViewTransfer.text = "출발 또는 도착 지역을 찾을 수 없습니다."
            textViewDuration.text = ""
        }
    }

    private fun geocodeLocation(locationName: String): LatLng? {
        val geocodingResult = GeocodingApi.geocode(geoApiContext, locationName).await()

        if (geocodingResult.isNotEmpty()) {
            val result = geocodingResult[0]
            return result.geometry.location
        }

        return null
    }
}


