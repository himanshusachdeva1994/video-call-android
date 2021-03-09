package com.himanshu.videocallsdkvendors.view.custom.twilio

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.annotations.twilio.NO_VIDEO
import com.himanshu.videocallsdkvendors.annotations.twilio.SELECTED
import com.himanshu.videocallsdkvendors.annotations.twilio.VIDEO
import com.himanshu.videocallsdkvendors.databinding.ParticipantViewThumbBinding
import com.twilio.video.VideoScaleType
import com.twilio.video.VideoTextureView

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 * This class has been picked from Twilio SDK sample app with some changes according to requirement
 */
class ParticipantThumbView : ParticipantView {
    var binding: ParticipantViewThumbBinding? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        binding = ParticipantViewThumbBinding.inflate(LayoutInflater.from(context), this, true)
        setParticipantIdentity(identity)
        setParticipantState(state)
        setParticipantMirror(mirror)
        setParticipantScaleType(scaleType)
    }

    override fun setParticipantState(state: Int) {
        super.setParticipantState(state)
        when (state) {
            VIDEO -> {
                binding?.apply {
                    participantSelectedLayout.visibility = View.GONE
                    participantStubImage.visibility = View.GONE
                    participantSelectedIdentity.visibility = View.GONE
                    participantVideoLayout.visibility = View.VISIBLE
                    participantVideoIdentity.visibility = View.VISIBLE
                    participantVideo.visibility = View.VISIBLE
                }
            }
            NO_VIDEO, SELECTED -> {
                binding?.apply {
                    participantVideoLayout.visibility = View.GONE
                    participantVideoIdentity.visibility = View.GONE
                    participantVideo.visibility = View.GONE
                    participantSelectedLayout.visibility = View.VISIBLE
                    participantStubImage.visibility = View.VISIBLE
                    participantSelectedIdentity.visibility = View.VISIBLE
                }
            }
            else -> {
            }
        }
        val resId: Int = if (state == SELECTED) R.drawable.participant_selected_background else R.drawable.participant_background
        binding?.participantSelectedLayout?.background = ContextCompat.getDrawable(context, resId)
    }

    override fun setParticipantIdentity(identity: String) {
        super.setParticipantIdentity(identity)
        binding?.apply {
            participantVideoIdentity.text = identity
            participantSelectedIdentity.text = identity
        }
    }

    override fun setParticipantMirror(mirror: Boolean) {
        super.setParticipantMirror(mirror)
        binding?.participantVideo?.mirror = this.mirror
    }

    override fun setParticipantScaleType(scaleType: Int) {
        super.setParticipantScaleType(scaleType)
        binding?.participantVideo?.videoScaleType = VideoScaleType.values()[this.scaleType]
    }

    override fun setMuted(muted: Boolean) {
        binding?.participantNoAudio?.visibility = if (muted) View.VISIBLE else View.GONE
    }

    override fun setSpeakerIconVisibility(isVisible: Boolean) {
        binding?.dominantSpeakerImg?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override val networkQualityImage: ImageView?
        get() = binding?.networkQualityLevelImg

    override val videoView: VideoTextureView
        get() = binding!!.participantVideo
}