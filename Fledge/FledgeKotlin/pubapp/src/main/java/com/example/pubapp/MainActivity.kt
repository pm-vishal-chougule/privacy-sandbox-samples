package com.example.pubapp

import android.adservices.adselection.AdSelectionConfig
import android.adservices.adselection.AdSelectionManager
import android.adservices.adselection.AdSelectionOutcome
import android.adservices.adselection.ReportImpressionRequest
import android.adservices.common.AdSelectionSignals
import android.adservices.common.AdTechIdentifier
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pubapp.model.AdSelectionOutcomeModel
import com.example.pubapp.ui.theme.FledgeSampleTheme
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.stream.Collectors

@RequiresApi(api = 34)
@SuppressLint("NewApi")
class MainActivity : ComponentActivity() {

    companion object{
        const val TAG = "MainActivity"
        const val SCORING_LOGIC_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/api/ScoringLogic.js"
        const val SCORING_SIGNAL_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/api/ScoringSignals.json"
    }

    private var adSelectionManager: AdSelectionManager? = null
    private val EXECUTOR: Executor = Executors.newCachedThreadPool()
    private val TRUSTED_SCORING_SIGNALS = AdSelectionSignals.fromString("{\n"
            + "\t\"render_uri_1\": \"signals_for_1\",\n"
            + "\t\"render_uri_2\": \"signals_for_2\"\n"
            + "}")

    private val TRUSTED_BIDDING_SIGNALS = AdSelectionSignals.fromString("{\n"
            + "\t\"example\": \"example\",\n"
            + "\t\"valid\": \"Also valid\",\n"
            + "\t\"list\": \"list\",\n"
            + "\t\"of\": \"of\",\n"
            + "\t\"keys\": \"trusted bidding signal Values\"\n"
            + "}")

    private val adSelectionOutcomeViewModel: AdSelectionOutcomeViewModel by  viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FledgeSampleTheme {

                var renderUrl by remember { mutableStateOf("") }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally){
                        Text("Test Publisher Application")
                        Text("Run ad selection")

                        var sellerName =  remember { TextFieldState() }
                        sellerName.text = Uri.parse(SCORING_LOGIC_URL).host.toString()
                        CustomEditText(placeholder = "Seller Name", sellerName)

                        var decisionLogicUrl = remember { TextFieldState() }
                        decisionLogicUrl.text = SCORING_LOGIC_URL
                        CustomEditText(placeholder = "Decision Logic Url", decisionLogicUrl)

                        var buyerName = remember { TextFieldState() }
                        buyerName.text = Uri.parse(SCORING_LOGIC_URL).host.toString()
                        CustomEditText(placeholder = "Audience Buyer", buyerName)

                        val renderUri = remember {
                            mutableStateOf("")
                        }
                        Button(onClick = {
                            adSelectionOutcomeViewModel.updateUrl("", 0, 0)
                            runAdSelection(sellerName.text, decisionLogicUrl.text, buyerName.text)
                        }) {
                            Text(text = "Run Ad Selection")
                        }

                        Column(
                            Modifier
                                .width(320.dp)
                                .height(50.dp),
                            verticalArrangement = Arrangement.Bottom) {
                            Banner(adSelectionOutcomeViewModel)
                        }
                    }

                }
            }
        }

        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4) {
            adSelectionManager = getSystemService(AdSelectionManager::class.java)
        }else{
            Log.e(TAG, "SdkExtensions.getExtensionVersion(AD_SERVICES) < 4")
        }
    }


    private fun runAdSelection(sellerName: String, decisionLogicUrl: String, audiences: String){
        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4) {

            var customAudienceBuyers = ArrayList<AdTechIdentifier>()
            customAudienceBuyers.add(AdTechIdentifier.fromString(audiences))

            Log.d(TAG, "Running Ad selection with sellerName: $sellerName, decisionLogicUrl: $decisionLogicUrl," +
                    "audienceBuyer: $customAudienceBuyers")

//            var adSelectionSignals = AdSelectionSignals.fromString("the publisher sets these")

//            var perbuyerSingnal : MutableMap<AdTechIdentifier, AdSelectionSignals> = HashMap()
//            perbuyerSingnal[AdTechIdentifier.fromString(audiences)] = adSelectionSignals

//            Log.d(TAG, "Running Ad selection with adSelectionSignals: $adSelectionSignals, perbuyerSingnal: $perbuyerSingnal")

            val adSelectionConfig: AdSelectionConfig = AdSelectionConfig.Builder()
                .setSeller(AdTechIdentifier.fromString(sellerName))
                .setDecisionLogicUri(Uri.parse(decisionLogicUrl))
                .setCustomAudienceBuyers(customAudienceBuyers)
                .setAdSelectionSignals(AdSelectionSignals.EMPTY)
                .setSellerSignals(AdSelectionSignals.EMPTY)
                .setPerBuyerSignals(customAudienceBuyers.stream().collect(
                    Collectors.toMap(
                    { buyer: AdTechIdentifier -> buyer },
                    { AdSelectionSignals.EMPTY })))
                .setTrustedScoringSignalsUri(Uri.parse(""))
                .build()

            var callback: OutcomeReceiver<AdSelectionOutcome, Exception> =
                @RequiresApi(Build.VERSION_CODES.S)
                object : OutcomeReceiver<AdSelectionOutcome, Exception> {
                    override fun onResult(result: AdSelectionOutcome) {
                        Log.i("Ad Selection", "Completed running ad selection renderUrl: ${result.renderUri}, adSelectionId: ${result.adSelectionId}")
                        this@MainActivity.runOnUiThread {
                            // Update View Model
                            adSelectionOutcomeViewModel.updateUrl(
                                result.renderUri.toString(),
                                320,
                                50
                            )
                            // Report impression
                            reportImpression(result.adSelectionId, adSelectionConfig)
                        }


                    }

                    override fun onError(error: Exception) {
                        // Handle error
                        Log.e("Ad Selection", "Error executing joinCustomAudience", error)
                    }
                };

            adSelectionManager?.selectAds(adSelectionConfig, EXECUTOR, callback)
        } else {
            Log.e(TAG, "SdkExtensions.getExtensionVersion(AD_SERVICES) < 4 while running ad selection.")
        }

    }

    // Reports an impression, this method should be called after the ad is rendered.
    fun reportImpression(adSelectionId: Long, adSelectionConfig: AdSelectionConfig){
        val reportImpressionRequest = ReportImpressionRequest(adSelectionId, adSelectionConfig)
        adSelectionManager?.reportImpression(reportImpressionRequest, EXECUTOR, object : OutcomeReceiver<Any, java.lang.Exception>{

            override fun onResult(result: Any) {
                Log.e(TAG, "onResult : ${result}")
                this@MainActivity.runOnUiThread{
                    Toast.makeText(this@MainActivity, "Impression executed for $adSelectionId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: java.lang.Exception) {
                Log.e(TAG, "Error while executing impresion: ${error}")
            }

        })

    }
}

class TextFieldState(){
    var text: String by mutableStateOf("")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Banner(outcomeViewModel: AdSelectionOutcomeViewModel, modifier: Modifier = Modifier){
    val adSelectionOutcome by outcomeViewModel.adSelectionViewModel.observeAsState(initial = AdSelectionOutcomeModel("", 0,0,))
    val renderUrl = adSelectionOutcome.renderURL
    if(renderUrl.isEmpty()){
        // Empty text view
        Text(text = "")
    }else{
        AndroidView(factory = {
            WebView(it).apply {
                loadUrl(renderUrl)
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEditText(placeholder: String, value: TextFieldState = remember { TextFieldState() }, modifier: Modifier = Modifier){
    OutlinedTextField(value = value.text, onValueChange = { value.text = it }, label = { Text(placeholder) })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FledgeSampleTheme {
        Greeting("Android")
    }
}