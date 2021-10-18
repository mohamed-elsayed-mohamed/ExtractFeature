package com.alpha.extractfeature

import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alpha.extractfeature.databinding.ActivityMainBinding
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Ref1: https://developers.arcgis.com/android/kotlin/sample-code/feature-layer-selection/
// Ref2: https://developers.arcgis.com/android/java/sample-code/feature-layer-show-attributes/

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mapview: MapView by lazy { binding.map }
    private val callout: Callout by lazy { mapview.callout }

    private val calloutContent: TextView by lazy {
        TextView(this@MainActivity).apply {
            setTextColor(Color.BLACK)
            isSingleLine = false
            isVerticalScrollBarEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            movementMethod = ScrollingMovementMethod()
            setLines(8)
        }
    }

    private lateinit var featureLayer: FeatureLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupMap()

        addListener()
    }

    private fun setupMap(){
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable("https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Trailheads/FeatureServer/0")

        // create the feature layer using the service feature table
        featureLayer = FeatureLayer(serviceFeatureTable)
        map.operationalLayers.add(featureLayer)

        mapview.map = map

        mapview.setViewpoint(Viewpoint(34.0270, -118.8050, 200000.0))
    }

    private fun addListener() {

        mapview.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapview) {
            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {

                featureLayer.clearSelection()
                if (callout.isShowing)
                    callout.dismiss()

                val  tappedPoint = Point(event.x.roundToInt(), event.y.roundToInt())

                // set a tolerance for accuracy of returned selections from point tapped
                val tolerance = 0.0

                // identify nearby features at the clicked location
                val identifyLayerResultFuture = mapview.identifyLayerAsync(featureLayer, tappedPoint, tolerance, false, -1)

                identifyLayerResultFuture.addDoneListener {
                    try {
                        val identifyLayerResult = identifyLayerResultFuture.get()

                        // get first element in the selection
                        val selectedElement = identifyLayerResult.elements.first()

                        if(selectedElement != null){
                            val feature = selectedElement as Feature
                            featureLayer.selectFeature(selectedElement)

                            // loop on all available attributes

                            for((key, value) in feature.attributes){
                                if (value is GregorianCalendar) {
                                    val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                                    calloutContent.append("$key | ${simpleDateFormat.format(value.time)}\n")
                                } else {
                                    calloutContent.append("$key | $value\n")
                                }
                            }

                            // center the mapview on selected feature
                            val envelope = feature.geometry.extent
                            mapview.setViewpointGeometryAsync(envelope, 200.0)
                            // show callout
                            callout.location = envelope.center
                            callout.content = calloutContent
                            callout.show()
                        }

                    } catch (e: Exception) {
                        val errorMessage = "Select feature failed: " + e.message
                        Log.e(TAG, errorMessage)
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                return super.onSingleTapConfirmed(event)
            }
        }
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