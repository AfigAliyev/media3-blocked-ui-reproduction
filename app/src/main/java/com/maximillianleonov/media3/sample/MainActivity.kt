package com.maximillianleonov.media3.sample

import android.content.ComponentName
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.maximillianleonov.media3.sample.ui.theme.Media3SampleTheme

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var player: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Media3SampleTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxSize(),
                        strokeWidth = 24.dp
                    )

                    Button(
                        modifier = Modifier
                            .weight(0.1f)
                            .fillMaxWidth(),
                        onClick = {
                            val uri = buildUriFromRawResource(R.raw.valesco_cloud_9)
                            val items = List(10000) { buildMediaItemFromUri(uri) }

                            player?.run {
                                setMediaItems(items)
                                prepare()
                                play()
                            }
                        }
                    ) {
                        Text(text = "Play")
                    }
                }
            }
        }
    }

    private fun buildUriFromRawResource(@RawRes rawRes: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(packageName)
        .appendPath(rawRes.toString())
        .build()

    private fun buildMediaItemFromUri(uri: Uri) = MediaItem.Builder()
        .setRequestMetadata(
            RequestMetadata.Builder()
                .setMediaUri(uri)
                .build()
        )
        .build()

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync().apply {
            addListener(
                { player = get() },
                MoreExecutors.directExecutor()
            )
        }
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let(MediaController::releaseFuture)
        player = null
    }
}
