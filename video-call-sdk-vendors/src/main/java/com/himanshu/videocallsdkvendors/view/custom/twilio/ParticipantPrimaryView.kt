package com.himanshu.videocallsdkvendors.view.custom.twilio

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.himanshu.videocallsdkvendors.annotations.twilio.NO_VIDEO
import com.himanshu.videocallsdkvendors.annotations.twilio.SELECTED
import com.himanshu.videocallsdkvendors.annotations.twilio.VIDEO
import com.himanshu.videocallsdkvendors.databinding.ParticipantViewPrimaryBinding
import com.twilio.video.VideoScaleType
import com.twilio.video.VideoTextureView

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 * This class has been picked from Twilio SDK sample app with some changes according to requirement
 */
class ParticipantPrimaryView : ParticipantView {
    var binding: ParticipantViewPrimaryBinding? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun showIdentityBadge(show: Boolean) {
        binding!!.participantBadge.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun init(context: Context) {
        binding = ParticipantViewPrimaryBinding.inflate(LayoutInflater.from(context), this, true)
        setParticipantIdentity(identity)
        setParticipantState(state)
        setParticipantMirror(mirror)
        setParticipantScaleType(scaleType)
    }

    override fun setParticipantState(state: Int) {
        super.setParticipantState(state)
        when (state) {
            VIDEO -> {
                binding!!.participantSelectedLayout.visibility = View.GONE
                binding!!.participantStubImage.visibility = View.GONE
                binding!!.participantSelectedIdentity.visibility = View.GONE
                binding!!.participantVideoLayout.visibility = View.VISIBLE
                binding!!.participantVideoIdentity.visibility = View.VISIBLE
                binding!!.participantVideo.visibility = View.VISIBLE
            }
            NO_VIDEO, SELECTED -> {
                binding!!.participantVideoLayout.visibility = View.GONE
                binding!!.participantVideoIdentity.visibility = View.GONE
                binding!!.participantVideo.visibility = View.GONE
                binding!!.participantSelectedLayout.visibility = View.VISIBLE
                binding!!.participantStubImage.visibility = View.VISIBLE
                binding!!.participantSelectedIdentity.visibility = View.VISIBLE
            }
            else -> {
            }
        }
    }

    override fun setParticipantIdentity(identity: String) {
        super.setParticipantIdentity(identity)
        binding!!.participantVideoIdentity.text = identity
        binding!!.participantSelectedIdentity.text = identity
    }

    override fun setParticipantMirror(mirror: Boolean) {
        super.setParticipantMirror(mirror)
        binding!!.participantVideo.mirror = this.mirror
    }

    override fun setParticipantScaleType(scaleType: Int) {
        super.setParticipantScaleType(scaleType)
        binding!!.participantVideo.videoScaleType = VideoScaleType.values()[scaleType]
    }

    override fun setMuted(muted: Boolean) {
        binding!!.participantNoAudio.visibility = if (muted) View.VISIBLE else View.GONE
    }

    override fun setSpeakerIconVisibility(isVisible: Boolean) {
        binding!!.dominantSpeakerImg.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override val networkQualityImage: ImageView
        get() = binding!!.networkQualityLevelImg

    override val videoView: VideoTextureView
        get() = binding!!.participantVideo
}