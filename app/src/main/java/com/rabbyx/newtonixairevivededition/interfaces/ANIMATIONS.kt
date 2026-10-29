package com.rabbyx.newtonixairevivededition.interfaces

import android.view.View
import androidx.cardview.widget.CardView

class ANIMATIONS(private val topBarCard: CardView, private val bottomCard: CardView) {
    internal fun hideCardsWithAnimation() {
        if (topBarCard.visibility == View.VISIBLE || bottomCard.visibility == View.VISIBLE) {
            fadeOut(topBarCard)
            fadeOut(bottomCard)
        }
    }

    internal fun showCardsWithAnimation() {
        if (topBarCard.visibility == View.GONE || bottomCard.visibility == View.GONE) {
            fadeIn(topBarCard)
            fadeIn(bottomCard)
        }
    }

    private fun fadeOut(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    view.visibility = View.GONE
                }
            })
    }

    private fun fadeIn(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    }

}