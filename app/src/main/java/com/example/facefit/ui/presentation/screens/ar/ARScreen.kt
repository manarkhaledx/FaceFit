//package com.example.facefit.ui.presentation.screens.ar
//
//import android.content.Context
//import android.opengl.GLSurfaceView
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.facefit.AR.augmentedfaces.AugmentedFacesActivity
//
//@Composable
//fun ARScreen(context: Context) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        // GLSurfaceView for AR rendering
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { ctx ->
//                GLSurfaceView(ctx).apply {
//                    // Configure GLSurfaceView as in AugmentedFacesActivity
//                    preserveEGLContextOnPause = true
//                    setEGLContextClientVersion(2)
//                    setEGLConfigChooser(8, 8, 8, 8, 16, 0)
//                    setRenderer(AugmentedFacesActivity().apply { this.onCreate(null) })
//                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//                }
//            },
//            update = { view ->
//                // Update logic if needed
//            }
//        )
//
//        // Close button
//        IconButton(
//            onClick = { /* Handle back navigation */ },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Close, contentDescription = "Close AR")
//        }
//    }
//}