package com.example.guru2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var teamInput: EditText
    private lateinit var btnSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        teamInput = findViewById(R.id.editTextTeam)
        btnSearch = findViewById(R.id.btnSearch)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSearch.setOnClickListener {
            val teamName = teamInput.text.toString().trim()
            if (teamName.isNotEmpty()) {
                searchTeamLocation(teamName)
            } else {
                Toast.makeText(this, "팀 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun searchTeamLocation(teamName: String) {
        RetrofitClient.api.searchTeam(teamName).enqueue(object : Callback<TeamResponse> {
            override fun onResponse(call: Call<TeamResponse>, response: Response<TeamResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@PlayerActivity, "API 응답 실패", Toast.LENGTH_SHORT).show()
                    return
                }

                val team = response.body()?.teams?.firstOrNull()

                if (team != null) {
                    Log.d("API_DEBUG", "팀: ${team.strTeam}, 위도: ${team.strStadiumLatitude}, 경도: ${team.strStadiumLongitude}")

                    val latStr = team.strStadiumLatitude
                    val lngStr = team.strStadiumLongitude

                    if (!latStr.isNullOrBlank() && !lngStr.isNullOrBlank()) {
                        val lat = latStr.toDoubleOrNull()
                        val lng = lngStr.toDoubleOrNull()

                        if (lat != null && lng != null) {
                            val stadiumLocation = LatLng(lat, lng)
                            mMap.clear()
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(stadiumLocation)
                                    .title("${team.strTeam} 홈구장: ${team.strStadium}")
                            )
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stadiumLocation, 14f))
                        } else {
                            Toast.makeText(this@PlayerActivity, "위도/경도 형식 오류", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PlayerActivity, "위도/경도 정보가 없습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PlayerActivity, "팀 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TeamResponse>, t: Throwable) {
                Toast.makeText(this@PlayerActivity, "API 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "API 호출 실패", t)
            }
        })
    }
}
