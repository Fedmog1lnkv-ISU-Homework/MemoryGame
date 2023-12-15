package com.fedmog1lnkv.memorygame.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.fedmog1lnkv.memorygame.R
import com.fedmog1lnkv.memorygame.model.CardModel
import kotlin.math.min

class FieldView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCardClickListener: (CardModel) -> Unit = {}

    var field = listOf<CardModel>()
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var rowCount = 4
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    var columnCount = 4
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private val colorList = listOf(
        android.graphics.Color.RED,
        android.graphics.Color.GREEN,
        android.graphics.Color.BLUE,
        android.graphics.Color.YELLOW,
        android.graphics.Color.MAGENTA,
        android.graphics.Color.CYAN,
        android.graphics.Color.DKGRAY,
        android.graphics.Color.GRAY,
    )

    private val colorPaints = colorList.map {
        Paint().apply {
            color = it
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private val backImageResId = R.drawable.card_back

    private val typeImageResIds = listOf(
        R.drawable.card1,
        R.drawable.card2,
        R.drawable.card3,
        R.drawable.card4,
        R.drawable.card5,
        R.drawable.card6,
        R.drawable.card7,
        R.drawable.card8,
    )

    private val gestureDetector = GestureDetector(context, FieldGestureListener())

    private var cardWidth: Float = 0f

    private var cardHeight: Float = 0f

    private var imageSize = 0

    private var backImage = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, backImageResId), 48, 48, true
    )
    private var typeImages = typeImageResIds.map {
        Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, it), 48, 48, true
        )
    }

    private val cardViews = mutableListOf<CardViewModel>()

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val newCardWidth = width.toFloat() / columnCount
        val newCardHeight = newCardWidth

        if (newCardWidth == cardWidth || newCardHeight == cardHeight) {
            imageSize = min(newCardWidth, newCardHeight).toInt()

            backImage = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, backImageResId), imageSize, imageSize, true
            )
            typeImages = typeImageResIds.map {
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, it), imageSize, imageSize, true
                )
            }
        }

        cardHeight = newCardHeight
        cardWidth = newCardWidth

        setMeasuredDimension(width, width)

        cardViews.clear()
    }

    override fun onDraw(canvas: Canvas) {
        for (row in 0 until rowCount) {
            for (column in 0 until columnCount) {
                val index = row * columnCount + column
                if (index >= field.size) break

                canvas.drawBitmap(
                    if (field[index].isOpen) typeImages[field[index].type] else backImage,
                    column * cardWidth + (cardWidth - imageSize) / 2,
                    row * cardHeight + (cardHeight - imageSize) / 2,
                    null
                )
            }
        }

        super.onDraw(canvas)
    }

    inner class FieldGestureListener : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y

            val index = (y / cardHeight).toInt() * columnCount + (x / cardWidth).toInt()

            if (index >= field.size) return false

            onCardClickListener(field[index])

            return true
        }

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean = false

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean = false

        override fun onLongPress(e: MotionEvent) = Unit

        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean = false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        return gestureDetector.onTouchEvent(event)
    }

    data class CardViewModel(
        val id: Int,
        val type: Int,
        val isOpen: Boolean,
        val rect: Rect,
    )
}