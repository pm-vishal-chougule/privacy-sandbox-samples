package com.example.advertiserapp

import android.adservices.common.AdData
import android.adservices.common.AdSelectionSignals
import android.adservices.common.AdTechIdentifier
import android.adservices.customaudience.CustomAudience
import android.adservices.customaudience.CustomAudienceManager
import android.adservices.customaudience.JoinCustomAudienceRequest
import android.adservices.customaudience.TrustedBiddingData
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.advertiserapp.model.CustomAudienceModel
import com.example.advertiserapp.model.CustomAudienceRepository
import com.example.advertiserapp.ui.theme.FledgeSampleTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.stream.Collectors

class MainActivity : ComponentActivity() {


    companion object{
        const val TAG = "MainActivity"
        const val BIDDING_LOGIC_FILE = "BiddingLogic.js"
        const val BIDDING_LOGIC_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/BiddingLogic.js"
        const val DAILY_UPDATE_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/DailyUpdateResponse.json"
        const val BIDDING_SIGNAL_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/BiddingSignals.json"
        const val RENDER_URL = "https://advertiser-1.glitch.me/advertiser/shopping-ad.html"
    }
    private val TRUSTED_SCORING_SIGNALS = AdSelectionSignals.fromString("{\n"
            + "\t\"render_uri_1\": \"signals_for_1\",\n"
            + "\t\"render_uri_2\": \"signals_for_2\"\n"
            + "}")

    private var customAudienceManager: CustomAudienceManager? = null
    private val EXECUTOR: Executor = Executors.newCachedThreadPool()

    private val customAudienceViewModel: CustomAudienceViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FledgeSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (horizontalAlignment = Alignment.CenterHorizontally){
                        Text(text = "Create Custom Audience")
                        var buyerName =  remember { TextFieldState() }
                        buyerName.text = "advertiser-1"
                        CustomEditText(placeholder = "Buyer Name", buyerName)
                        var audienceName = remember { TextFieldState() }
                        audienceName.text = "shoes_ca"
                        CustomEditText(placeholder = "Custom Audience Name", audienceName)
                        var biddingLogicUrl = remember { TextFieldState() }
                        biddingLogicUrl.text = BIDDING_LOGIC_URL
                        CustomEditText(placeholder = "BiddingLogic Url", biddingLogicUrl)

                        var dailyUpdateUrl = remember { TextFieldState() }
                        dailyUpdateUrl.text = DAILY_UPDATE_URL
                        CustomEditText(placeholder = "Daily Update Url", dailyUpdateUrl)

                        Button(onClick = {
                            joinCustomAudience(buyerName.text,
                            audienceName.text, biddingLogicUrl.text,
                                dailyUpdateUrl.text)

                        }) {
                            Text(text = "Add Custom Audience")
                        }
                        CustomAudienceList(customAudienceViewModel = customAudienceViewModel)
                    }

                }
            }

        }

        customAudienceViewModel.customAudienceRepository =  CustomAudienceRepository.getInstance(this)
        if (SdkExtensions.getExtensionVersion(
                SdkExtensions.AD_SERVICES) >= 4) {
            customAudienceManager = getSystemService(CustomAudienceManager::class.java)
        } else {
            Log.e(TAG, "SdkExtensions.getExtensionVersion(AD_SERVICES) < 4")
        }

        // Enable WebView Debugging
        WebView.setWebContentsDebuggingEnabled(true)
    }


    private fun joinCustomAudience(buyerName: String, audienceName: String, biddingLogicUrl: String,
                                   dailyUpdateUrl: String){
        Log.d(TAG, "join custom audience with buyer: $buyerName, audienceName: $audienceName, biddingLogicUrl: $biddingLogicUrl")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                SdkExtensions.AD_SERVICES) >= 4){


            val adData: AdData = AdData.Builder()
                .setRenderUri(Uri.parse("https://advertiser-1.glitch.me/advertiser/shopping-ad.html"))
                .setMetadata("")
                .build()

            val adDataList: MutableList<AdData> = ArrayList()
            adDataList.add(adData)

            val trustedBiddingKeys: MutableList<String> = ArrayList()
            trustedBiddingKeys.add("key1")
            trustedBiddingKeys.add("key2")
            val trustedBiddingData: TrustedBiddingData = TrustedBiddingData.Builder()
                .setTrustedBiddingUri(Uri.parse("https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/BiddingSignals.json"))
                .setTrustedBiddingKeys(trustedBiddingKeys)
                .build()

            val audience = CustomAudience.Builder()
                .setBuyer(AdTechIdentifier.fromString(buyerName))
                .setName(audienceName)
                .setBiddingLogicUri(Uri.parse(biddingLogicUrl))
                .setDailyUpdateUri(Uri.parse(dailyUpdateUrl))
                .setUserBiddingSignals(TRUSTED_SCORING_SIGNALS)
                .setTrustedBiddingData(trustedBiddingData)
                .setAds(adDataList)
                .build()

            val joinCustomAudienceRequest: JoinCustomAudienceRequest =
                JoinCustomAudienceRequest.Builder().setCustomAudience(audience).build()

            var callback: OutcomeReceiver<Any, Exception> =
                @RequiresApi(Build.VERSION_CODES.S)
                object : OutcomeReceiver<Any, Exception> {
                    override fun onResult(result: Any) {
                        Log.i("CustomAudience", "Completed joinCustomAudience ${result}")
                        val customAudienceModel = CustomAudienceModel(audienceName, buyerName,
                            biddingLogicUrl, dailyUpdateUrl, RENDER_URL, "")
                        customAudienceViewModel.saveCustomAudience(customAudienceModel)
                    }

                    override fun onError(error: Exception) {
                        // Handle error
                        Log.e("CustomAudience", "Error executing joinCustomAudience", error)
                    }
                }

            customAudienceManager?.joinCustomAudience(joinCustomAudienceRequest,
                EXECUTOR,
                callback)

        }else {
            Log.e(TAG, "SdkExtensions.getExtensionVersion(AD_SERVICES) < 4")
        }

    }

    /**
     * Reads a file into a string, to be used to read the .js files into a string.
     */
    @Throws(IOException::class)
    private fun assetFileToString(location: String): String {
        return BufferedReader(InputStreamReader(applicationContext.assets.open(location)))
            .lines().collect(Collectors.joining("\n"))
    }

}


@Composable
fun CustomAudienceList(customAudienceViewModel: CustomAudienceViewModel){
    val customAudiences by customAudienceViewModel.customAudienceModelList.observeAsState(initial = emptyList())
    LaunchedEffect(Unit){
        customAudienceViewModel.fetchCustomAudience()
    }

    Column {
        if(customAudiences.isEmpty()){
            Text(text = "No CustomAudience available.")
        }else{
            LazyColumn{
                items(customAudiences){
                    customAudience ->
                    Text(text = customAudience.audienceName)
                    Text(text = customAudience.buyerName)
                    Text(text = customAudience.biddingLogicUrl)
                    Text(text = customAudience.dailyUpdateUrl)
                    Text(text = customAudience.renderUrl)
                }
            }
        }
    }
}

class TextFieldState(){
    var text: String by mutableStateOf("")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEditText(placeholder: String, value: TextFieldState = remember { TextFieldState() }, modifier: Modifier = Modifier){
    OutlinedTextField(value = value.text, onValueChange = { value.text = it }, label = { Text(placeholder) })
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview
@Composable
fun CustomEditTextPreview(){
    CustomEditText("Buyer Name")
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FledgeSampleTheme {
        Greeting("Android")
    }
}