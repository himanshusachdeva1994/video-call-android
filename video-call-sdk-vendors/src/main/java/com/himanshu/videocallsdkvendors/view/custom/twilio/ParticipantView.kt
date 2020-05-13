package com.himanshu.videocallsdkvendors.view.custom.twilio

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.annotations.twilio.NO_VIDEO
import com.twilio.video.I420Frame
import com.twilio.video.VideoRenderer
import com.twilio.video.VideoScaleType
import com.twilio.video.VideoTextureView

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 * This class has been picked from Twilio SDK sample app with some changes according to requirement
 */
abstract class ParticipantView : FrameLayout, VideoRenderer {
    var identity = ""
    @JvmField
    var state = NO_VIDEO
    @JvmField
    var mirror = false
    @JvmField
    var scaleType = VideoScaleType.ASPECT_BALANCED.ordinal

    abstract val networkQualityImage: ImageView?
    protected abstract val videoView: VideoTextureView

    constructor(context: Context) : super(context) {
        initParams(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initParams(context, attrs)
    }

    open fun setParticipantIdentity(identity: String) {
        this.identity = identity
    }

    open fun setParticipantState(state: Int) {
        this.state = state
    }

    open fun setParticipantMirror(mirror: Boolean) {
        this.mirror = mirror
    }

    open fun setParticipantScaleType(scaleType: Int) {
        this.scaleType = scaleType
    }

    override fun renderFrame(frame: I420Frame) {
        videoView.renderFrame(frame)
    }

    fun initParams(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val stylables = context.theme.obtainStyledAttributes(attrs, R.styleable.ParticipantView, 0, 0)

            // obtain identity
            val identityResId = stylables.getResourceId(R.styleable.ParticipantView_identity, -1)
            identity = if (identityResId != -1) context.getString(identityResId) else ""

            // obtain state
            state = stylables.getInt(R.styleable.ParticipantView_state, NO_VIDEO)

            // obtain mirror
            mirror = stylables.getBoolean(R.styleable.ParticipantView_mirror, false)

            // obtain scale type
            scaleType = stylables.getInt(R.styleable.ParticipantView_type, VideoScaleType.ASPECT_BALANCED.ordinal)
            stylables.recycle()
        }
    }

    abstract fun setMuted(muted: Boolean)
    abstract fun setSpeakerIconVisibility(isVisible: Boolean)
}