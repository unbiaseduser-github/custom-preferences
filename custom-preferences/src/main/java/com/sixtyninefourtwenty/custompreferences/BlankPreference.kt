package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.core.view.get
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import java.util.Objects

/**
 * Preference that hosts your own content view instead of the default one.
 *
 * Supports associating your own views to standard preference ids, like `android.R.id.title`.
 *
 * The root layout's background is cleared if [getOnPreferenceClickListener] is `null` -
 * this behavior is so that clicks not handled on your view have no visual effect. Handling clicks on the
 * preference itself through [setOnPreferenceClickListener] is still supported.
 *
 * Style attribute: [R.attr.blankPreferenceStyle], default [R.style.Preference_BlankPreference]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class BlankPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.blankPreferenceStyle, 0),
    defStyleRes: Int = R.style.Preference_BlankPreference
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var _contentView: View? = null

    /**
     * Setting this externally is discouraged since this view's parent isn't known until the UI is updated -
     * any layout parameters set on the root view is ignored, see [LayoutInflater.inflate].
     *
     * Instead, use [setContentView].
     */
    var contentView: View?
        get() = _contentView
        set(value) = setContentViewInternal(value, true)

    private fun setContentViewInternal(contentView: View?, notifyChanged: Boolean) {
        this._contentView = contentView
        if (notifyChanged) {
            notifyChanged()
        }
    }

    private var nextContentViewCreation: ((parent: ViewGroup, attachToRoot: Boolean) -> View?)? = null

    /**
     * Set a lambda expression that will be called the next time this preference's UI is updated.
     * After that happens, [contentView] will hold its result.
     *
     * Note that due to the invocation being deferred, if the preference is not yet displayed on screen
     * (e.g. on [PreferenceFragmentCompat.onCreatePreferences]), after this is called, [contentView]
     * will still be `null`.
     *
     * @see LayoutInflater.inflate
     */
    fun setContentView(viewCreation: ((parent: ViewGroup, attachToRoot: Boolean) -> View?)) {
        nextContentViewCreation = viewCreation
        notifyChanged()
    }

    /**
     * Convenience for
     * ```
     * setContentView { parent, attachToRoot ->
     *     LayoutInflater.from(parent.context).inflate(layoutRes, parent, attachToRoot)
     * }
     * ```
     *
     * @see setContentView
     */
    fun setContentLayoutResource(@LayoutRes layoutRes: Int) {
        setContentView { parent, attachToRoot ->
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, attachToRoot)
        }
    }

    /**
     * Returns the content view.
     *
     * @throws IllegalStateException if the view has not been set
     */
    fun requireContentView() = contentView ?: error("Preference ${key ?: "has no key and"} doesn't have a view set")

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BlankPreference, defStyleAttr, defStyleRes)
        val layoutRes = typedArray.getResourceId(R.styleable.BlankPreference_bp_contentLayout, 0)
        if (layoutRes != 0) {
            setContentLayoutResource(layoutRes)
        }
        typedArray.recycle()
    }

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        super.setOnPreferenceClickListener(onPreferenceClickListener)
        notifyChanged()
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        val content = holder.findViewById(R.id.content) as ViewGroup
        if (nextContentViewCreation != null) {
            setContentViewInternal(
                contentView = nextContentViewCreation!!(content, false),
                notifyChanged = false
            )
            nextContentViewCreation = null
        }
        with(content) {
            if (!Objects.equals(getChildAt(0), contentView) /* Optimization for when this preference needs to redisplay properties unrelated to its content view. */) {
                removeAllViews()
                val v = contentView
                if (v != null) {
                    addView(v)
                }
            }
        }

        super.onBindViewHolder(holder)

        holder.itemView.background = onPreferenceClickListener?.let { _ ->
            val tv = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
            ContextCompat.getDrawable(context, tv.resourceId)
        }

        holder.itemView.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN || content.getChildAt(0) == null) {
                return@setOnKeyListener false
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                val hasChildTakenFocus = content[0].requestFocus()
                /*
                Successfully requesting focus on child breaks click functionality; manually perform a click
                */
                if (hasChildTakenFocus) {
                    performClick()
                }
                return@setOnKeyListener hasChildTakenFocus // Prevent ripple (if present) from being retained after click when successfully requesting focus on child
            }
            false
        }
    }

}