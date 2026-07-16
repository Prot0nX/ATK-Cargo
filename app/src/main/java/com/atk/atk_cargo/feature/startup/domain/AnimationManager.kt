package com.atk.atk_cargo.feature.startup.domain

object AnimationManager {
    private var performanceScore: Int = 50
    private var animationsEnabled: Boolean = true

    fun setPerformanceScore(score: Int) {
        performanceScore = score
        animationsEnabled = score >= 70
    }

    fun areAnimationsEnabled(): Boolean {
        return animationsEnabled
    }
}
