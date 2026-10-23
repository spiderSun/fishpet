package com.example.fishpet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.animation.ValueAnimator
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class FishView(context: Context) : View(context) {

    var windowManager: WindowManager? = null
    var layoutParams: WindowManager.LayoutParams? = null
    var onClicked: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            40f, 32f, 176f, 144f,
            Color.parseColor("#9AE7FF"),
            Color.parseColor("#37A2E3"),
            Shader.TileMode.CLAMP
        )
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f
        color = Color.parseColor("#3C4658")
    }

    private var t = 0f
    private var lastFrameTime = SystemClock.uptimeMillis()
    private var nextBlinkIn = 2.0f
    private var blinkStart = -10f
    private var blink = 0f
    private var popScale = 1f

    private var bubbleText = ""
    private var bubbleVisible = false
    private var bubbleAlpha = 0f
    private val hideBubble = Runnable { bubbleVisible = false }

    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0
    private var startY = 0
    private var dragging = false

    private val frameLoop = object : Runnable {
        override fun run() {
            val now = SystemClock.uptimeMillis()
            val dt = ((now - lastFrameTime) / 1000f).coerceIn(0f, 0.1f)
            lastFrameTime = now
            update(dt)
            invalidate()
            postOnAnimationDelayed(this, 16L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lastFrameTime = SystemClock.uptimeMillis()
        post(frameLoop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(frameLoop)
        removeCallbacks(hideBubble)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = resources.displayMetrics.density
        setMeasuredDimension((200 * d).toInt(), (200 * d).toInt())
    }

    private fun update(dt: Float) {
        t += dt

        nextBlinkIn -= dt
        if (nextBlinkIn <= 0f) {
            blinkStart = t
            nextBlinkIn = 2.5f + Random.nextFloat() * 3.5f
        }
        val bp = (t - blinkStart) / 0.16f
        blink = if (bp in 0f..1f) 1f - abs(bp * 2f - 1f) else 0f

        popScale += (1f - popScale) * 0.15f

        val target = if (bubbleVisible) 1f else 0f
        bubbleAlpha += (target - bubbleAlpha) * 0.18f
    }

    fun say(text: String, durationMs: Long = 2600L) {
        bubbleText = if (text.length > 12) text.take(12) + "…" else text
        bubbleVisible = true
        removeCallbacks(hideBubble)
        postDelayed(hideBubble, durationMs)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.scale(width / 200f, height / 200f)

        val bob = sin(t * 2.2f) * 3.5f
        val sway = sin(t * 2.6f) * 9f

        canvas.save()
        canvas.scale(popScale, popScale, 100f, 110f)
        canvas.translate(0f, bob)

        drawTail(canvas, sway)
        drawDorsalFin(canvas, sway)
        drawBellyFin(canvas, sway)
        drawBody(canvas)
        drawSideFin(canvas)
        drawFace(canvas)

        canvas.restore()
        drawBubble(canvas)

        canvas.restore()
    }

    private fun drawTail(canvas: Canvas, sway: Float) {
        paint.color = Color.parseColor("#2E93D4")
        val p = Path().apply {
            moveTo(52f, 74f)
            cubicTo(26f, 46f + sway, -6f, 54f + sway, 10f, 88f)
            cubicTo(-6f, 122f + sway, 26f, 130f + sway, 52f, 102f)
            close()
        }
        canvas.drawPath(p, paint)
    }

    private fun drawDorsalFin(canvas: Canvas, sway: Float) {
        paint.color = Color.parseColor("#2E93D4")
        val p = Path().apply {
            moveTo(78f, 42f)
            quadTo(88f, 6f + sway * 0.4f, 126f, 36f)
            close()
        }
        canvas.drawPath(p, paint)
    }

    private fun drawBellyFin(canvas: Canvas, sway: Float) {
        paint.color = Color.parseColor("#2E93D4")
        val p = Path().apply {
            moveTo(92f, 138f)
            quadTo(102f, 166f - sway * 0.4f, 126f, 136f)
            close()
        }
        canvas.drawPath(p, paint)
    }

    private fun drawBody(canvas: Canvas) {
        canvas.drawOval(RectF(40f, 32f, 176f, 14
                                      paint.color = Color.argb(60, 255, 255, 255)
        canvas.drawOval(RectF(56f, 82f, 168f, 142f), paint)
    }

    private fun drawSideFin(canvas: Canvas) {
        paint.color = Color.parseColor("#4AAEE8")
        canvas.save()
        canvas.rotate(-18f, 94f, 122f)
        canvas.drawOval(RectF(78f, 112f, 110f, 132f), paint)
        canvas.restore()
    }

    private fun drawFace(canvas: Canvas) {
        val eyeScale = (1f - blink).coerceAtLeast(0.06f)

        paint.color = Color.argb(90, 255, 120, 140)
        canvas.drawOval(RectF(54f, 92f, 78f, 108f), paint)
        canvas.drawOval(RectF(138f, 92f, 162f, 108f), paint)

        drawEye(canvas, 86f, 76f, eyeScale)
        drawEye(canvas, 130f, 76f, eyeScale)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.6f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.parseColor("#2B3440")
        canvas.drawArc(RectF(100f, 92f, 116f, 104f), 10f, 160f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawEye(canvas: Canvas, cx: Float, cy: Float, scaleY: Float) {
        canvas.save()
        canvas.scale(1f, scaleY, cx, cy)
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, 12f, paint)
        paint.color = Color.parseColor("#2B3440")
        canvas.drawCircle(cx + 2f, cy + 1f, 6.2f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(cx + 4.5f, cy - 2f, 2.4f, paint)
        canvas.restore()
    }

    private fun drawBubble(canvas: Canvas) {
        if (bubbleAlpha < 0.02f || bubbleText.isEmpty()) return

        val tw = textPaint.measureText(bubbleText)
        val padH = 12f
        val w = (tw + padH * 2).coerceAtMost(172f)
        val h = 30f
        val right = 196f
        val left = right - w
        val top = 4f

        val a = (240 * bubbleAlpha).toInt()
        paint.color = Color.argb(a, 255, 255, 255)
        canvas.drawRoundRect(RectF(left, top, right, top + h), h / 2, h / 2, paint)
        canvas.drawCircle(left + 30f, top + h + 6f, 4.5f, paint)
        canvas.drawCircle(left + 22f, top + h + 15f, 2.5f, paint)

        textPaint.color = Color.argb((255 * bubbleAlpha).toInt(), 60, 70, 90)
        canvas.drawText(bubbleText, left + padH, top + h / 2 + 4.5f, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val wm = windowManager ?: return false
        val lp = layoutParams ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                startX = lp.x
                startY = lp.y
                dragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && hypot(dx.toDouble(), dy.toDouble()) > 16.0) dragging = true
                if (dragging) {
                    lp.x = startX + dx.toInt()
                    lp.y = startY + dy.toInt()
                    runCatching { wm.updateViewLayout(this, lp) }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    snapToEdge(wm, lp)
                } else {
                    popScale = 1.22f
                    onClicked?.invoke()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToEdge(wm: WindowManager, lp: WindowManager.LayoutParams) {
        val screenW = resources.displayMetrics.widthPixels
        val targetX = if (lp.x + width / 2 < screenW / 2) 0 else screenW - width
        ValueAnimator.ofInt(lp.x, targetX).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                lp.x = it.animatedValue as Int
                runCatching { wm.updateViewLayout(this@FishView, lp) }
            }
            start()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
                              }
