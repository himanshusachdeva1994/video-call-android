package com.himanshu.videocallsdkvendors.model.twilio

import android.view.View
import android.view.ViewGroup
import com.himanshu.videocallsdkvendors.annotations.twilio.NO_VIDEO
import com.himanshu.videocallsdkvendors.annotations.twilio.SELECTED
import com.himanshu.videocallsdkvendors.annotations.twilio.State
import com.himanshu.videocallsdkvendors.annotations.twilio.VIDEO
import com.himanshu.videocallsdkvendors.view.custom.twilio.ParticipantPrimaryView
import com.himanshu.videocallsdkvendors.view.custom.twilio.ParticipantThumbView
import com.himanshu.videocallsdkvendors.view.custom.twilio.ParticipantView
import com.twilio.video.VideoTrack
import java.util.*

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 * ParticipantController is main controlling party for rendering participants.
 */
class ParticipantController(
        /**
         * RemoteParticipant thumb view group, where participants are added or removed from.
         */
        private val thumbsViewContainer: ViewGroup,
        /**
         * Primary video track.
         */
        val primaryView: ParticipantPrimaryView) {
    /**
     * Get data about primary participant.
     *
     * @return participant item data.
     */
    /**
     * Data container about primary participant - sid, identity, video track, audio state and
     * mirroring state.
     */
    var primaryItem: Item? = null
        private set

    /**
     * Get primary participant view.
     *
     * @return primary participant view instance.
     */

    /**
     * Relationship collection - item (data) -> thumb.
     */
    private val thumbs: MutableMap<Item?, ParticipantView> = HashMap()

    /**
     * Each participant thumb click listener.
     */
    private var listener: ItemClickListener? = null
    private fun addThumb(sid: String, identity: String) {
        addThumb(sid, identity, null, true, false)
    }

    fun addThumb(item: Item) {
        addThumb(item.sid, item.identity, item.videoTrack, item.isMuted, item.isMirror)
    }

    private fun addThumb(sid: String, identity: String, videoTrack: VideoTrack?) {
        addThumb(sid, identity, videoTrack, true, false)
    }

    /**
     * Create new participant thumb from data.
     *
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted      participant audio state.
     */
    fun addThumb(sid: String, identity: String?, videoTrack: VideoTrack?, muted: Boolean, mirror: Boolean) {
        val item = Item(sid, identity, videoTrack, muted, mirror)
        val view = createThumb(item)
        thumbs[item] = view
        thumbsViewContainer.addView(view)
    }

    /**
     * Update primary participant thumb with mirroring.
     *
     * @param mirror enable/disable video track mirroring.
     */
    fun updatePrimaryThumb(mirror: Boolean) {
        val target = primaryItem
        if (target != null) {
            val view: ParticipantView = primaryView
            target.isMirror = mirror
            view.setParticipantMirror(target.isMirror)
        }
    }

    /**
     * Update participant thumb with video track.
     *
     * @param sid      unique participant identifier.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    fun updateThumb(sid: String, oldVideo: VideoTrack?, newVideo: VideoTrack?) {
        val target = findItem(sid, oldVideo)
        if (target != null) {
            val view = getThumb(sid, oldVideo)
            removeRender(target.videoTrack, view)
            target.videoTrack = newVideo
            if (target.videoTrack != null) {
                view!!.setParticipantState(VIDEO)
                target.videoTrack!!.addRenderer(view)
            } else {
                view!!.setParticipantState(NO_VIDEO)
            }
        }
    }

    /**
     * Update participant video track thumb with state.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @param state      new thumb state.
     */
    fun updateThumb(sid: String, videoTrack: VideoTrack?, @State state: Int) {
        val target = findItem(sid, videoTrack)
        if (target != null) {
            val view = getThumb(sid, videoTrack) as ParticipantThumbView?
            view!!.setParticipantState(state)
            when (state) {
                NO_VIDEO, SELECTED -> removeRender(target.videoTrack, view)
                VIDEO -> target.videoTrack!!.addRenderer(view)
            }
        }
    }

    /**
     * Update participant video track thumb with mirroring.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @param mirror     enable/disable mirror.
     */
    fun updateThumb(sid: String, videoTrack: VideoTrack?, mirror: Boolean) {
        val target = findItem(sid, videoTrack)
        if (target != null) {
            val view = getThumb(sid, videoTrack) as ParticipantThumbView?
            target.isMirror = mirror
            view!!.setParticipantMirror(target.isMirror)
        }
    }

    /**
     * Update all participant thumbs with audio state.
     *
     * @param sid   unique participant identifier.
     * @param muted new audio state.
     */
    fun updateThumbs(sid: String, muted: Boolean) {
        for ((key, value) in thumbs) {
            if (key!!.sid == sid) {
                key.isMuted = muted
                value.setMuted(muted)
            }
        }
    }

    /**
     * Add new participant thumb or update old instance.
     *
     * @param sid      unique participant identifier.
     * @param identity participant name to display.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    fun addOrUpdateThumb(sid: String, identity: String, oldVideo: VideoTrack?, newVideo: VideoTrack?) {
        if (hasThumb(sid, oldVideo)) {
            updateThumb(sid, oldVideo, newVideo)
        } else {
            addThumb(sid, identity, newVideo)
        }
    }

    fun removeThumb(item: Item) {
        removeThumb(item.sid, item.videoTrack)
    }

    /**
     * Remove participant video track thumb.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     */
    fun removeThumb(sid: String, videoTrack: VideoTrack?) {
        val target = findItem(sid, videoTrack)
        if (target != null) {
            val view = getThumb(sid, videoTrack)
            removeRender(target.videoTrack, view)
            thumbsViewContainer.removeView(view)
            thumbs.remove(target)
        }
    }

    /**
     * Remove all participant thumbs.
     *
     * @param sid unique participant identifier.
     */
    fun removeThumbs(sid: String) {
        val deleteKeys = ArrayList<Item?>()
        for ((key, value) in thumbs) {
            if (key!!.sid == sid) {
                deleteKeys.add(key)
                thumbsViewContainer.removeView(value)
                val remoteVideoTrack = key.videoTrack
                remoteVideoTrack?.removeRenderer(value)
            }
        }
        for (deleteKey in deleteKeys) {
            thumbs.remove(deleteKey)
        }
    }

    /**
     * Remove participant thumb or leave empty (no video) thumb if nothing left.
     *
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack target video track.
     */
    fun removeOrEmptyThumb(sid: String, identity: String, videoTrack: VideoTrack?) {
        val thumbsCount = getThumbs(sid).size
        if (thumbsCount > 1 || thumbsCount == 1 && primaryItem!!.sid == sid) {
            removeThumb(sid, videoTrack)
        } else if (thumbsCount == 0) {
            addThumb(sid, identity)
        } else {
            updateThumb(sid, videoTrack, null)
        }
    }

    /**
     * Get participant video track thumb instance.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @return participant thumb instance.
     */
    fun getThumb(sid: String, videoTrack: VideoTrack?): ParticipantView? {
        for ((key, value) in thumbs) {
            if (key != null && key.sid == sid && key.videoTrack === videoTrack) {
                return value
            }
        }
        return null
    }

    /**
     * Remove all thumbs for all participants.
     */
    fun removeAllThumbs() {
        for ((key, value) in thumbs) {
            thumbsViewContainer.removeView(value)
            if (key != null) {
                removeRender(key.videoTrack, value)
            }
        }
        thumbs.clear()
    }

    fun renderAsPrimary(item: Item) {
        renderAsPrimary(item.sid, item.identity, item.videoTrack, item.isMuted, item.isMirror)
    }

    /**
     * Render participant as primary participant from data.
     *
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted      participant audio state.
     * @param mirror     enable/disable mirroring for video track.
     */
    fun renderAsPrimary(sid: String, identity: String?, videoTrack: VideoTrack?, muted: Boolean, mirror: Boolean) {
        val old = primaryItem
        val newItem = Item(sid, identity, videoTrack, muted, mirror)

        // clean old primary video renderings
        if (old != null) {
            removeRender(old.videoTrack, primaryView)
        }
        primaryItem = newItem
        primaryView.identity = primaryItem!!.identity!!
        primaryView.showIdentityBadge(true)
        primaryView.setMuted(primaryItem!!.isMuted)
        primaryView.setParticipantMirror(mirror)
        if (primaryItem!!.videoTrack != null) {
            removeRender(primaryItem!!.videoTrack, primaryView)
            primaryView.setParticipantState(VIDEO)
            primaryItem!!.videoTrack!!.addRenderer(primaryView)
        } else {
            primaryView.setParticipantState(NO_VIDEO)
        }
    }

    /**
     * Remove primary participant.
     */
    fun removePrimary() {
        removeRender(primaryItem!!.videoTrack, primaryView)
        // TODO: temp state
        primaryView.setParticipantState(NO_VIDEO)
        primaryItem = null
    }

    fun setListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun setDominantSpeaker(participantView: ParticipantView?) {
        clearDominantSpeaker()
        participantView?.setSpeakerIconVisibility(true)
    }

    private fun clearDominantSpeaker() {
        primaryView.setSpeakerIconVisibility(false)
        for ((_, value) in thumbs) {
            value.setSpeakerIconVisibility(false)
        }
    }

    private fun hasThumb(sid: String, videoTrack: VideoTrack?): Boolean {
        return getThumb(sid, videoTrack) != null
    }

    private fun findItem(sid: String, videoTrack: VideoTrack?): Item? {
        for (item in thumbs.keys) {
            if (item!!.sid == sid && item.videoTrack === videoTrack) {
                return item
            }
        }
        return null
    }

    private fun createThumb(item: Item): ParticipantView {
        val view: ParticipantView = ParticipantThumbView(thumbsViewContainer.context)
        view.identity = item.identity!!
        view.setMuted(item.isMuted)
        view.setParticipantMirror(item.isMirror)
        view.setOnClickListener { _: View? ->
            if (listener != null) {
                listener!!.onThumbClick(item)
            }
        }
        if (item.videoTrack != null) {
            item.videoTrack!!.addRenderer(view)
            view.setParticipantState(VIDEO)
        } else {
            view.setParticipantState(NO_VIDEO)
        }
        return view
    }

    private fun getThumbs(sid: String): ArrayList<ParticipantView> {
        val views = ArrayList<ParticipantView>()
        for ((key, value) in thumbs) {
            if (key!!.sid == sid) {
                views.add(value)
            }
        }
        return views
    }

    private fun removeRender(videoTrack: VideoTrack?, view: ParticipantView?) {
        if (videoTrack == null || !videoTrack.renderers.contains(view)) {
            return
        }
        videoTrack.removeRenderer(view!!)
    }

    /**
     * RemoteParticipant information data holder.
     */
    class Item(
            /**
             * RemoteParticipant unique identifier.
             */
            var sid: String,
            /**
             * RemoteParticipant name.
             */
            var identity: String?,
            /**
             * RemoteParticipant video track.
             */
            var videoTrack: VideoTrack?,
            /**
             * RemoteParticipant audio state.
             */
            var isMuted: Boolean,
            /**
             * Video track mirroring enabled/disabled.
             */
            var isMirror: Boolean)

    interface ItemClickListener {
        fun onThumbClick(item: Item?)
    }
}