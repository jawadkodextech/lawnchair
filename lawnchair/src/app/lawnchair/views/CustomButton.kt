package app.lawnchair.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import app.lawnchair.font.FontManager

@SuppressLint("AppCompatCustomView")
class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : android.widget.Button(context, attrs) {
//AppCompat
    init {
        FontManager.INSTANCE.get(context).overrideFont(this, attrs)
    }
}
