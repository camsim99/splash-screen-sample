package com.example.splash_screen_sample
import android.util.Log

import android.os.Build
import androidx.core.view.WindowCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.android.FlutterTextureView
import io.flutter.embedding.android.FlutterSurfaceView
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.ViewCompat
//import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.flutter.util.ViewUtils
import android.view.ViewGroup
import android.view.SurfaceView
import android.view.TextureView
import android.transition.Transition


class MainActivity : FlutterActivity() {

  var flutterUiReady : Boolean = false
  var animationFinished : Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)

    // This activity will be handling the splash screen transition
    val splashScreen = installSplashScreen()

    // The splashscreen goes edge to edge, so for a smooth transition to our app, we also
    // want to draw edge to edge.
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController?.isAppearanceLightNavigationBars = true
    insetsController?.isAppearanceLightStatusBars = true


    // The content view needs to set before calling setOnExitAnimationListener
    // to ensure that the SplashScreenView is attach to the right view root.
    //    setContentView(R.layout.main_activity_2)

    // Add FlutterView
    val view = getWindow().getDecorView() as ViewGroup
    val rootLayout= findViewById(android.R.id.content) as FrameLayout
    //TODO: get first child, SurfaceView; set visibility GONE
    val flutterView = rootLayout.getChildAt(0) as FlutterView
//    val flutterSurfaceView = flutterView.getChildAt(0) as FlutterSurfaceView
    View.inflate(this, R.layout.main_activity_2, rootLayout)
    flutterView.setVisibility(View.INVISIBLE)
//    rootLayout.setVisibility(View.GONE)



    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, i ->
      val insets = i.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
      i.inset(insets)
    }

    // (Optional) Setting an OnExitAnimationListener on the SplashScreen indicates
    // to the system that the application will handle the exit animation.
    // The listener will be called once the app is ready.
    splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
      onSplashScreenExit(splashScreenViewProvider)
    }

  }

  override fun onFlutterUiDisplayed(){
    flutterUiReady = true
  }

  override fun onFlutterUiNoLongerDisplayed(){
    flutterUiReady = false
  }

  /**
   * Handles the transition from the splash screen to the application
   */
  private fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
    val accelerateInterpolator = FastOutLinearInInterpolator()
    val splashScreenView = splashScreenViewProvider.view
    val iconView = splashScreenViewProvider.iconView

    // We'll change the alpha of the main view
    val alpha = ValueAnimator.ofInt(255, 0)
    alpha.duration = SPLASHSCREEN_ALPHA_ANIMATION_DURATION.toLong()
    alpha.interpolator = accelerateInterpolator

    // And we translate the icon down
    val translationY = ObjectAnimator.ofFloat(
      iconView,
      View.TRANSLATION_Y,
      iconView.translationY,
      splashScreenView.height.toFloat()
    )
    translationY.duration = SPLASHSCREEN_TY_ANIMATION_DURATION.toLong()
    translationY.interpolator = accelerateInterpolator

    // And we play all of the animation together
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(alpha)


    val root = findViewById<ConstraintLayout>(R.id.container)
    val set1 = ConstraintSet().apply {
      clone(this@MainActivity, R.layout.main_activity)
    }
    set1.applyTo(root)
    val set2 = ConstraintSet().apply {
      clone(this@MainActivity, R.layout.main_activity_2)
    }

    var transitionStarted = false
    val autoTransition = AutoTransition().apply {
      interpolator = AccelerateDecelerateInterpolator()
    }
    autoTransition.addListener(object: Transition.TransitionListener {
      override fun onTransitionEnd(transition: Transition) {
        val rootLayout = findViewById(android.R.id.content) as FrameLayout
        val flutterView = rootLayout.getChildAt(0) as FlutterView
        rootLayout.removeView(findViewById(R.id.container))
        if (flutterUiReady) {
          flutterView.setVisibility(View.VISIBLE)
        }
        animationFinished = true
    }
      override fun onTransitionCancel(transition: Transition){}
      override fun onTransitionPause(transition: Transition) {}
      override fun onTransitionResume(transition: Transition) {}
      override fun onTransitionStart(transition: Transition) {}
    })

    val function: (ValueAnimator) -> Unit = { i ->
      if (!transitionStarted && i.animatedFraction > 0.5) {
        transitionStarted = true

        TransitionManager.beginDelayedTransition(root, autoTransition)
        iconView.visibility = View.GONE
        set2.applyTo(root)
      }
//      else if (transitionStarted && i.animatedFraction == 1F) {
//        val rootLayout = findViewById(android.R.id.content) as FrameLayout
////        Log.d("REMOVE_ANIMATION_TAG", "animation removed")
//      rootLayout.removeView(findViewById(R.id.container))
//
//      }
      splashScreenView.background.alpha = i.animatedValue as Int
    }
    alpha.addUpdateListener(function)

    // Once the application is finished, we remove the splash screen from our view
    // hierarchy.
    animatorSet.doOnEnd {
      splashScreenViewProvider.remove()
//      val rootLayout = findViewById(android.R.id.content) as FrameLayout
//      val flutterView = rootLayout.getChildAt(0) as FlutterView
//      rootLayout.removeView(findViewById(R.id.container))
//      if (flutterUiReady) {
//        flutterView.setVisibility(View.VISIBLE)
//      }
//      animationFinished = true
    }

    waitForAnimatedIconToFinish(
      splashScreenViewProvider,
      splashScreenView
    ) { animatorSet.start() }

  }

  /**
   * Wait until the AVD animation is finished before starting the splash screen dismiss animation
   */
  private fun waitForAnimatedIconToFinish(
    splashScreenViewProvider: SplashScreenViewProvider,
    view: View,
    onAnimationFinished: () -> Unit
  ) {
    // If we want to wait for our Animated Vector Drawable to finish animating, we can compute
    // the remaining time to delay the start of the exit animation
    val delayMillis: Long =
      if (WAIT_FOR_AVD_TO_FINISH) splashScreenViewProvider.remainingAnimationDuration() else 0
    view.postDelayed(delayMillis, onAnimationFinished)
  }

  private fun SplashScreenViewProvider.remainingAnimationDuration() = iconAnimationStartMillis +
    iconAnimationDurationMillis - System.currentTimeMillis()

  private companion object {
    const val SPLASHSCREEN_ALPHA_ANIMATION_DURATION = 500
    const val SPLASHSCREEN_TY_ANIMATION_DURATION = 500
    const val WAIT_FOR_AVD_TO_FINISH = false
  }
}