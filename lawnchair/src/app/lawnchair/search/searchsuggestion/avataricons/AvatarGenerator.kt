package app.lawnchair.search.searchsuggestion.avataricons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import java.util.Locale

/**
 * Created by Korir on 1/21/20.
 */
class AvatarGenerator(private val builder: AvatarBuilder) {

    class AvatarBuilder(private val context: Context) {

        private var textSize = 100
        private var size = 14
        private var name = " "
        private var backgroundColor: Int? = null
        private var shapeType = AvatarConstants.CIRCLE


        fun setTextSize(textSize: Int) = apply {
            this.textSize = textSize
        }

        fun setAvatarSize(int: Int) = apply {
            this.size = int
        }

        fun setLabel(label: String) = apply {
            this.name = label
        }

        fun setBackgroundColor(color: Int) = apply {
            this.backgroundColor = color
        }

        fun toSquare() = apply {
            this.shapeType = AvatarConstants.RECTANGLE
        }

        fun toCircle() = apply {
            this.shapeType = AvatarConstants.CIRCLE
        }


        fun build(): BitmapDrawable {
            return avatarImageGenerate(
                context,
                size,
                shapeType,
                name,
                textSize,
                AvatarConstants.COLOR700,
                5
            )
        }


        private fun avatarImageGenerate(
            context: Context,
            size: Int,
            shape: Int,
            name: String,
            textSize: Int,
            colorModel: Int,
        ): BitmapDrawable {
            uiContext = context

            texSize = calTextSize(textSize)
            val label = firstCharacter(name)
            val textPaint = textPainter()
            val painter = painter()
            painter.isAntiAlias = true

            // Adjust the areaRect to account for the padding
            val padding = 5;
            val paddedSize = size - (2 * padding) // Subtract padding from all sides
            val areaRect = Rect(padding, padding, size - padding, size - padding)


//            val areaRect = Rect(0, 0, size, size)

            if (shape == 0) {
                painter.color = backgroundColor ?: RandomColors(colorModel).getColor()
            } else {
                painter.color = Color.TRANSPARENT
            }

            val bitmap = Bitmap.createBitmap(size, size, ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawRect(areaRect, painter)

            //reset painter
            if (shape == 0) {
                painter.color = Color.TRANSPARENT
            } else {
                painter.color = backgroundColor ?: RandomColors(colorModel).getColor()
            }
            painter.strokeWidth = 2.0f
//            painter.str
            val bounds = RectF(areaRect)
            bounds.right = textPaint.measureText(label, 0, 1)
            bounds.bottom = textPaint.descent() - textPaint.ascent()

            bounds.left += (areaRect.width() - bounds.right) / 2.0f
            bounds.top += (areaRect.height() - bounds.bottom) / 2.0f

            canvas.drawCircle(size.toFloat() / 2, size.toFloat() / 2, size.toFloat() / 2, painter)
            canvas.drawText(label, bounds.left, bounds.top - textPaint.ascent(), textPaint)
            return BitmapDrawable(uiContext.resources, bitmap)

        }


        private fun avatarImageGenerate(
            context: Context,
            size: Int,
            shape: Int,
            name: String,
            textSize: Int,
            colorModel: Int,
            padding: Int // New parameter for padding
        ): BitmapDrawable {
            uiContext = context

            texSize = calTextSize(textSize)
            val label = firstCharacter(name)
            val textPaint = textPainter()
            val painter = painter()
            painter.isAntiAlias = true

            // Adjust the areaRect to account for the padding
            val paddedSize = size - (2 * padding) // Subtract padding from all sides
            val areaRect = Rect(padding, padding, size - padding, size - padding)

            if (shape == 0) {
                painter.color = backgroundColor ?: RandomColors(colorModel).getColor()
            } else {
                painter.color = Color.TRANSPARENT
            }

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawRect(areaRect, painter)

            // Reset painter for the circle or shape
            if (shape == 0) {
                painter.color = Color.TRANSPARENT
            } else {
                painter.color = backgroundColor ?: RandomColors(colorModel).getColor()
            }
            painter.strokeWidth = 2.0f

            val bounds = RectF(areaRect)
            bounds.right = textPaint.measureText(label, 0, 1)
            bounds.bottom = textPaint.descent() - textPaint.ascent()

            // Adjust bounds for padding
            bounds.left += (areaRect.width() - bounds.right) / 2.0f
            bounds.top += (areaRect.height() - bounds.bottom) / 2.0f

            // Draw the circle (inside the padded area)
            canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (paddedSize / 2).toFloat(), painter)
            // Draw the text centered
            canvas.drawText(label, bounds.left, bounds.top - textPaint.ascent(), textPaint)

            return BitmapDrawable(uiContext.resources, bitmap)
        }

        private fun firstCharacter(name: String): String {
            if (name.isEmpty()) {
                return "-"
            }
            return name.first().toString()
        }

        private fun textPainter(): TextPaint {
            val textPaint = TextPaint()
            textPaint.isAntiAlias = true
            textPaint.textSize = texSize * uiContext.resources.displayMetrics.scaledDensity
            textPaint.color = Color.BLACK//Color.WHITE
            return textPaint
        }

        private fun painter(): Paint {
            return Paint()
        }

        private fun calTextSize(size: Int): Float {
            return (size).toFloat()
        }

        private fun getDominantColor(hexColor: String): String {
            // Remove the hash at the start of the string if it's there
            val color = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor

            // Convert hex to RGB values
            val r = Integer.valueOf(color.substring(0, 2), 16)
            val g = Integer.valueOf(color.substring(2, 4), 16)
            val b = Integer.valueOf(color.substring(4, 6), 16)

            // Determine the dominant color by comparing R, G, B values
            return when {
                r >= g && r >= b -> "Red (R)"
                g >= r && g >= b -> "Green (G)"
                else -> "Blue (B)"
            }
        }
    }


    /**
     * Deprecate and will be removed
     */
    companion object {
        private lateinit var uiContext: Context
        private var texSize = 0F

        @Deprecated("Switch to using builder method")
        fun avatarImage(context: Context, size: Int, shape: Int, name: String): BitmapDrawable {
            return avatarImageGenerate(context, size, shape, name, AvatarConstants.COLOR700)
        }


        fun avatarImage(
            context: Context,
            size: Int,
            shape: Int,
            name: String,
            colorModel: Int,
        ): BitmapDrawable {
            return avatarImageGenerate(context, size, shape, name, colorModel)
        }

        private fun avatarImageGenerate(
            context: Context,
            size: Int,
            shape: Int,
            name: String,
            colorModel: Int,
        ): BitmapDrawable {
            uiContext = context

            texSize = calTextSize(size)
            val label = firstCharacter(name)
            val textPaint = textPainter()
            val painter = painter()
            painter.isAntiAlias = true
            val areaRect = Rect(0, 0, size, size)

            if (shape == 0) {
                val firstLetter = firstCharacter(name)
                val r = firstLetter[0]
                painter.color = RandomColors(colorModel).getColor()
            } else {
                painter.color = Color.TRANSPARENT
            }

            val bitmap = Bitmap.createBitmap(size, size, ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawRect(areaRect, painter)

            //reset painter
            if (shape == 0) {
                painter.color = Color.TRANSPARENT
            } else {
                val firstLetter = firstCharacter(name)
                val r = firstLetter[0]
                painter.color = RandomColors(colorModel).getColor()
            }

            val bounds = RectF(areaRect)
            bounds.right = textPaint.measureText(label, 0, 1)
            bounds.bottom = textPaint.descent() - textPaint.ascent()

            bounds.left += (areaRect.width() - bounds.right) / 2.0f
            bounds.top += (areaRect.height() - bounds.bottom) / 2.0f

            canvas.drawCircle(size.toFloat() / 2, size.toFloat() / 2, size.toFloat() / 2, painter)
            canvas.drawText(label, bounds.left, bounds.top - textPaint.ascent(), textPaint)
            return BitmapDrawable(uiContext.resources, bitmap)

        }


        private fun firstCharacter(name: String): String {
            return name.first().toString().toUpperCase(Locale.ROOT)
        }

        private fun textPainter(): TextPaint {
            val textPaint = TextPaint()
            textPaint.isAntiAlias = true
            textPaint.textSize = texSize * uiContext.resources.displayMetrics.scaledDensity
            textPaint.color = Color.WHITE
            return textPaint
        }

        private fun painter(): Paint {
            return Paint()
        }

        private fun calTextSize(size: Int): Float {
            return (size).toFloat()
        }
    }
}
