package com.alpha.extractfeature

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alpha.extractfeature.databinding.ActivityMainBinding
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mapview: MapView by lazy { binding.map }

    private var defaultViewPoint: Viewpoint = Viewpoint(30.052697, 31.198192, 72000.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupMap()
    }

    private fun setupMap(){
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)
        mapview.map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
        mapview.setViewpoint(defaultViewPoint)
    }

    override fun onPause() {
        mapview.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapview.resume()
    }

    override fun onDestroy() {
        mapview.dispose()
        super.onDestroy()
    }
}