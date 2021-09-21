package com.example.splash_screen_sample

import android.os.Build
import androidx.core.view.WindowCompat
import io.flutter.embedding.android.FlutterActivity
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

class MainActivity : FlutterActivity() {
//  private var appReady = false

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

//    // Aligns the Flutter view vertically with the window.
//    WindowCompat.setDecorFitsSystemWindows(getWindow(), false)

    // The content view needs to set before calling setOnExitAnimationListener
    // to ensure that the SplashScreenView is attach to the right view root.
     setContentView(R.layout.main_activity_2)

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, i ->
      val insets = i.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
      i.inset(insets)
    }

    // (Optional) We can keep the splash screen visible until our app is ready.
//    splashScreen.setKeepVisibleCondition { !appReady }

//    // (Optional) Setting an OnExitAnimationListener on the SplashScreen indicates
    // to the system that the application will handle the exit animation.
//    // The listener will be called once the app is ready.
    splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
      onSplashScreenExit(splashScreenViewProvider)
    }

//    Handler(Looper.getMainLooper())
//      .postDelayed({ appReady = true }, splashScreen.)
//
////
////    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////      // Disable the Android splash screen fade out animation to avoid
////      // a flicker before the similar frame is drawn in Flutter.
////      splashScreen.setOnExitAnimationListener { splashScreenView -> splashScreenView.remove() }
////    }
//
//    super.onCreate(savedInstanceState)
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

    // To get fancy, we'll also animate our content
    //val marginAnimator = createContentAnimation()

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

    val function: (ValueAnimator) -> Unit = { i ->
      if (!transitionStarted && i.animatedFraction > 0.5) {
        transitionStarted = true

        TransitionManager.beginDelayedTransition(root, autoTransition)
        iconView.visibility = View.GONE
        set2.applyTo(root)
      }
      splashScreenView.background.alpha = i.animatedValue as Int
    }
    alpha.addUpdateListener(function)

    // Once the application is finished, we remove the splash screen from our view
    // hierarchy.
    animatorSet.doOnEnd { splashScreenViewProvider.remove() }

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
    const val MOCK_DELAY = 200
    const val MARGIN_ANIMATION_DURATION = 800
    const val SPLASHSCREEN_ALPHA_ANIMATION_DURATION = 500
    const val SPLASHSCREEN_TY_ANIMATION_DURATION = 500
    const val WAIT_FOR_AVD_TO_FINISH = false
  }
}