package com.himanshu.videocallsdkvendors.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.himanshu.videocallsdkvendors.R;
import com.himanshu.videocallsdkvendors.constants.IntentKeyConstants;
import com.himanshu.videocallsdkvendors.constants.PhoneCallStateConstants;
import com.himanshu.videocallsdkvendors.databinding.ActivityTwilioVideoCallBinding;
import com.himanshu.videocallsdkvendors.helper.twilio.CameraCaptureHelper;
import com.himanshu.videocallsdkvendors.helper.twilio.RoomManager;
import com.himanshu.videocallsdkvendors.model.twilio.ParticipantController;
import com.himanshu.videocallsdkvendors.model.twilio.RoomEvent;
import com.himanshu.videocallsdkvendors.util.GlobalUtil;
import com.himanshu.videocallsdkvendors.view.activity.base.BaseActivity;
import com.himanshu.videocallsdkvendors.view.custom.twilio.ParticipantPrimaryView;
import com.himanshu.videocallsdkvendors.view.custom.twilio.ParticipantView;
import com.himanshu.videocallsdkvendors.viewmodel.TwilioVideoCallViewModel;
import com.himanshu.videocallsdkvendors.viewmodel.factory.RoomViewModelFactory;
import com.twilio.video.AspectRatio;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalDataTrackPublication;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.TwilioException;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoTrack;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static com.himanshu.videocallsdkvendors.annotations.twilio.StateKt.NO_VIDEO;
import static com.himanshu.videocallsdkvendors.annotations.twilio.StateKt.SELECTED;
import static com.himanshu.videocallsdkvendors.annotations.twilio.StateKt.VIDEO;
import static com.himanshu.videocallsdkvendors.constants.IntentKeyConstants.PHONE_CALL_STATE;
import static com.himanshu.videocallsdkvendors.constants.LocalBroadcastKeyConstants.BROADCAST_PHONE_CALL_STATE;
import static com.twilio.video.AspectRatio.ASPECT_RATIO_16_9;
import static com.twilio.video.Room.State.CONNECTED;

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public class TwilioVideoCallActivity extends BaseActivity {

    private ActivityTwilioVideoCallBinding binding;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;
    private static final String MICROPHONE_TRACK_NAME = "microphone";
    private static final String CAMERA_TRACK_NAME = "camera";
    private static final String SCREEN_TRACK_NAME = "screen";
    private static final String IS_AUDIO_MUTED = "IS_AUDIO_MUTED";
    private static final String IS_VIDEO_MUTED = "IS_VIDEO_MUTED";
    private static final String AUTH_TOKEN = "AUTH_TOKEN";

    // This will be used instead of real local participant sid,
    // because that information is unknown until room connection is fully established
    private static final String LOCAL_PARTICIPANT_STUB_SID = "";

    private final AspectRatio aspectRatio = ASPECT_RATIO_16_9;
    private final VideoDimensions videoDimensionMin = VideoDimensions.CIF_VIDEO_DIMENSIONS;
    private final VideoDimensions videoDimensionMax = VideoDimensions.HD_720P_VIDEO_DIMENSIONS;
    private int fpsMin = 0;
    private int fpsMax = 30;

    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private int savedVolumeControlStream;
    private boolean savedIsMicrophoneMute = false;
    private boolean savedIsSpeakerPhoneOn = false;

    private LocalParticipant localParticipant;
    private String localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
    private Room room;
    private VideoConstraints videoConstraints;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack cameraVideoTrack;
    private boolean restoreLocalVideoCameraTrack = false;
    private LocalVideoTrack screenVideoTrack;

    private final String[] requiredPermissionsForVideoCall = GlobalUtil.getPermissionListForVideoCall();
    private boolean isVideoCallStarted;

    /**
     * Coordinates participant thumbs and primary participant rendering.
     */
    private ParticipantController participantController;

    private boolean isAudioMuted;
    private boolean isVideoMuted;

    private CameraCaptureHelper cameraCapturer;
    private ScreenCapturer screenCapturer;

    private final Map<String, String> localVideoTrackNames = new HashMap<>();
    private final Map<String, NetworkQualityLevel> networkQualityLevels = new HashMap<>();

    private final ScreenCapturer.Listener screenCapturerListener =
            new ScreenCapturer.Listener() {
                @Override
                public void onScreenCaptureError(@NonNull String errorDescription) {
                    Timber.e("Screen capturer error: %s", errorDescription);
                    stopScreenCapture();
                    Snackbar.make(
                            binding.layoutContentRoom.primaryVideo,
                            R.string.screen_capture_error,
                            Snackbar.LENGTH_LONG)
                            .show();
                }

                @Override
                public void onFirstFrameAvailable() {
                    Timber.d("First frame from screen capturer available");
                }
            };
    private TwilioVideoCallViewModel viewModel;

    @Inject
    RoomManager roomManager;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isAudioMuted = savedInstanceState.getBoolean(IS_AUDIO_MUTED);
            isVideoMuted = savedInstanceState.getBoolean(IS_VIDEO_MUTED);
            authToken = savedInstanceState.getString(AUTH_TOKEN);
        } else if (getIntent() != null && getIntent().hasExtra(IntentKeyConstants.AUTH_TOKEN)) {
            authToken = getIntent().getStringExtra(IntentKeyConstants.AUTH_TOKEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_twilio_video_call);

        initViewModel();

        requestPermissions();
        viewModel.getRoomEvents().observe(this, this::bindRoomEvents);
    }

    private void initiateVideoCall() {
        // Setup Audio
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(true);
        }
        savedVolumeControlStream = getVolumeControlStream();

        // setup participant controller
        participantController = new ParticipantController(binding.layoutContentRoom.remoteVideoThumbnails,
                binding.layoutContentRoom.primaryVideo);
        participantController.setListener(participantClickListener());

        setupLocalMedia();

        obtainVideoConstraints();

        viewModel.connectToRoom(authToken);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(IS_AUDIO_MUTED, isAudioMuted);
        outState.putBoolean(IS_VIDEO_MUTED, isVideoMuted);
        outState.putString(AUTH_TOKEN, authToken);
        super.onSaveInstanceState(outState);
    }

    private void initViewModel() {
        RoomViewModelFactory factory = new RoomViewModelFactory(getApplication(), roomManager);
        viewModel = new ViewModelProvider(this, factory).get(TwilioVideoCallViewModel.class);
        viewModel.attachCallback(this);
        binding.setViewModel(viewModel);
        binding.setHandler(viewModel);
    }

    private void obtainVideoConstraints() {
        VideoConstraints.Builder builder = new VideoConstraints.Builder();
        builder.aspectRatio(aspectRatio);
        builder.minVideoDimensions(videoDimensionMin);
        builder.maxVideoDimensions(videoDimensionMax);
        builder.minFps(fpsMin);
        builder.maxFps(fpsMax);
        videoConstraints = builder.build();
    }

    private ParticipantController.ItemClickListener participantClickListener() {
        return this::renderItemAsPrimary;
    }

    private @Nullable
    RemoteParticipant getRemoteParticipant(ParticipantController.Item item) {
        RemoteParticipant remoteParticipant = null;

        for (RemoteParticipant temp : room.getRemoteParticipants()) {
            if (temp.getSid().equals(item.getSid())) {
                remoteParticipant = temp;
            }
        }
        return remoteParticipant;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {

                setMediaControlOptionState(binding.ivMediaControlScreenShare, false);
                Snackbar.make(binding.layoutContentRoom.primaryVideo,
                        R.string.screen_capture_permission_not_granted,
                        Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
            screenCapturer = new ScreenCapturer(this, resultCode, data, screenCapturerListener);
            startScreenCapture();
        }
    }

    /**
     * Sets new item to render as primary view and moves existing primary view item to thumbs view.
     *
     * @param item New item to be rendered in primary view
     */
    private void renderItemAsPrimary(ParticipantController.Item item) {
        // nothing to click while not in room
        if (room == null) {
            return;
        }

        // no need to render if same item clicked
        ParticipantController.Item old = participantController.getPrimaryItem();
        if (old != null && item.getSid().equals(old.getSid()) && item.getVideoTrack() == old.getVideoTrack()) {
            return;
        }

        // add back old participant to thumbs
        if (old != null) {
            if (old.getSid().equals(localParticipantSid)) {
                // toggle local participant state
                int state = old.getVideoTrack() == null ? NO_VIDEO : VIDEO;
                participantController.updateThumb(old.getSid(), old.getVideoTrack(), state);
                participantController.updateThumb(old.getSid(), old.getVideoTrack(), old.isMirror());
            } else {
                // add thumb for remote participant
                RemoteParticipant remoteParticipant = getRemoteParticipant(old);
                if (remoteParticipant != null) {
                    participantController.addThumb(old.getSid(),
                            old.getIdentity(),
                            old.getVideoTrack(),
                            old.isMuted(),
                            old.isMirror());
                    RemoteParticipantListener listener = new RemoteParticipantListener(
                            participantController.getThumb(old.getSid(), old.getVideoTrack()),
                            remoteParticipant.getSid());
                    remoteParticipant.setListener(listener);
                }
            }
        }

        // handle new primary participant click
        participantController.renderAsPrimary(item);

        RemoteParticipant remoteParticipant = getRemoteParticipant(item);
        if (remoteParticipant != null) {
            ParticipantPrimaryView primaryView = participantController.getPrimaryView();
            RemoteParticipantListener listener = new RemoteParticipantListener(primaryView,
                    remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        }

        if (item.getSid().equals(localParticipantSid)) {

            // toggle local participant state and hide his badge
            participantController.updateThumb(item.getSid(),
                    item.getVideoTrack(),
                    SELECTED);
            participantController.getPrimaryView().showIdentityBadge(false);
        } else {

            // remove remote participant thumb
            participantController.removeThumb(item);
        }
    }

    private void initializeRoom() {
        if (room != null) {
            localParticipant = room.getLocalParticipant();
            publishLocalTracks();
            setAudioFocus(true);
            addParticipantViews();
        }
    }

    /**
     * Sets the microphone mute state.
     */
    private void setMicrophoneMute() {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (!wasMuted) {
            return;
        }
        audioManager.setMicrophoneMute(false);
    }

    private void setAudioFocus(boolean setFocus) {
        if (setFocus) {
            savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
            savedIsMicrophoneMute = audioManager.isMicrophoneMute();
            setMicrophoneMute();
            savedAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            requestAudioFocus();
            /*
             * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
             * required to be in this mode when playout and/or recording starts for
             * best possible VoIP performance.
             * Some devices have difficulties with speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            setVolumeControl(true);
        } else {
            audioManager.setMode(savedAudioMode);
            audioManager.abandonAudioFocus(null);
            audioManager.setMicrophoneMute(savedIsMicrophoneMute);
            audioManager.setSpeakerphoneOn(savedIsSpeakerPhoneOn);
            setVolumeControl(false);
        }
    }

    private void setVolumeControl(boolean setVolumeControl) {
        if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        } else {
            setVolumeControlStream(savedVolumeControlStream);
        }
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(i -> {
                    })
                    .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                    null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    private void bindRoomEvents(RoomEvent roomEvent) {
        try {
            if (roomEvent != null) {
                this.room = roomEvent.getRoom();
                if (room != null) {
                    if (roomEvent instanceof RoomEvent.RoomState) {
                        Room.State state = room.getState();
                        switch (state) {
                            case CONNECTED:
                                isVideoCallStarted = true;
                                initializeRoom();
                                break;

                            case DISCONNECTED:
                                if (isVideoCallStarted) {
                                    removeAllParticipants();
                                    localParticipant = null;
                                    room = null;
                                    localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
                                    setAudioFocus(false);
                                    networkQualityLevels.clear();
                                    finish();
                                }
                                break;
                        }
                    }
                    if (roomEvent instanceof RoomEvent.ConnectFailure) {
                        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                                .setTitle(getString(R.string.room_screen_connection_failure_title))
                                .setMessage(getString(R.string.room_screen_connection_failure_message))
                                .setNeutralButton("OK", (dialog, which) -> finish())
                                .show();
                        removeAllParticipants();
                        setAudioFocus(false);
                    }
                    if (roomEvent instanceof RoomEvent.ParticipantConnected) {
                        boolean renderAsPrimary = room.getRemoteParticipants().size() == 1;
                        setRemoteThumbVisibility();
                        addParticipant(((RoomEvent.ParticipantConnected) roomEvent).getRemoteParticipant(), renderAsPrimary);
                    }
                    if (roomEvent instanceof RoomEvent.ParticipantDisconnected) {
                        RemoteParticipant remoteParticipant = ((RoomEvent.ParticipantDisconnected) roomEvent).getRemoteParticipant();
                        networkQualityLevels.remove(remoteParticipant.getSid());
                        setRemoteThumbVisibility();
                        removeParticipant(remoteParticipant);
                    }
                    if (roomEvent instanceof RoomEvent.DominantSpeakerChanged) {
                        RemoteParticipant remoteParticipant = ((RoomEvent.DominantSpeakerChanged) roomEvent).getRemoteParticipant();

                        if (remoteParticipant == null) {
                            participantController.setDominantSpeaker(null);
                            return;
                        }

                        VideoTrack videoTrack = (remoteParticipant.getRemoteVideoTracks().size() > 0)
                                ? remoteParticipant
                                .getRemoteVideoTracks()
                                .get(0)
                                .getRemoteVideoTrack()
                                : null;

                        if (videoTrack != null) {
                            ParticipantView participantView = participantController.getThumb(remoteParticipant.getSid(), videoTrack);
                            if (participantView != null) {
                                participantController.setDominantSpeaker(participantView);
                            } else {
                                remoteParticipant.getIdentity();
                                ParticipantPrimaryView primaryParticipantView = participantController.getPrimaryView();
                                if (primaryParticipantView.getIdentity().equals(remoteParticipant.getIdentity())) {
                                    participantController.setDominantSpeaker(participantController.getPrimaryView());
                                } else {
                                    participantController.setDominantSpeaker(null);
                                }
                            }
                        }
                    }
                } else {
                    Timber.i("bindRoomEvents");
                }
                updateUi(room, roomEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUi(Room room, RoomEvent roomEvent) {
        int disconnectButtonState = View.GONE;

        if (roomEvent instanceof RoomEvent.Connecting) {
            disconnectButtonState = View.VISIBLE;
        }

        if (room != null && room.getState() == CONNECTED) {
            disconnectButtonState = View.VISIBLE;
        }

        // Check mute state
        setMediaControlOptionState(binding.ivMediaControlAudio, !isAudioMuted);
        setMediaControlOptionState(binding.ivMediaControlVideo, !isVideoMuted);

        binding.ivMediaControlDisconnect.setVisibility(disconnectButtonState);
    }

    /**
     * Remove single remoteParticipant thumbs and all it associated thumbs. If rendered as primary
     * remoteParticipant, primary view switches to local video track.
     *
     * @param remoteParticipant recently disconnected remoteParticipant.¬
     */
    private void removeParticipant(RemoteParticipant remoteParticipant) {
        if (participantController.getPrimaryItem().getSid().equals(remoteParticipant.getSid())) {
            // render local video if primary remoteParticipant has gone
            participantController.getThumb(localParticipantSid, cameraVideoTrack).callOnClick();
        }
        participantController.removeThumbs(remoteParticipant.getSid());
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(requiredPermissionsForVideoCall,
                        PERMISSIONS_REQUEST_CODE);
            } else if (!isVideoCallStarted) {
                initiateVideoCall();
            } else {
                setupLocalMedia();
            }
        } else if (!isVideoCallStarted) {
            initiateVideoCall();
        } else {
            initiateVideoCall();
        }
    }

    private boolean permissionsGranted() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED)
                && (resultMic == PackageManager.PERMISSION_GRANTED)
                && (resultStorage == PackageManager.PERMISSION_GRANTED)
                && (resultPhoneState == PackageManager.PERMISSION_GRANTED));
    }

    /**
     * Initialize local media and provide stub participant for primary view.
     */
    private void setupLocalMedia() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME);
            if (room != null && localParticipant != null && localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
        }
        if (cameraVideoTrack == null && !isVideoMuted) {
            setupLocalVideoTrack();
            renderLocalParticipantStub();
            if (room != null && localParticipant != null) {
                localParticipant.publishTrack(cameraVideoTrack);
            }
        }
    }

    /**
     * Create local video track
     */
    private void setupLocalVideoTrack() {
        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer = new CameraCaptureHelper(this, CameraCapturer.CameraSource.FRONT_CAMERA);
        }

        cameraVideoTrack = LocalVideoTrack.create(
                this,
                true,
                cameraCapturer.getVideoCapturer(),
                videoConstraints,
                CAMERA_TRACK_NAME);
        if (cameraVideoTrack != null) {
            localVideoTrackNames.put(cameraVideoTrack.getName(), getString(R.string.camera_video_track));
        } else {
            Snackbar.make(binding.layoutContentRoom.primaryVideo,
                    R.string.failed_to_add_camera_video_track,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void stopScreenCapture() {
        if (screenVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.unpublishTrack(screenVideoTrack);
            }
            screenVideoTrack.release();
            localVideoTrackNames.remove(screenVideoTrack.getName());
            screenVideoTrack = null;
            binding.ivMediaControlScreenShare.setImageResource(R.drawable.ic_screen_share_white);
            binding.ivMediaControlScreenShare.setTag(getString(R.string.share_screen));
        }
    }

    /**
     * Render local video track.
     *
     * <p>NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private void renderLocalParticipantStub() {
        participantController.renderAsPrimary(
                localParticipantSid,
                getString(R.string.you),
                cameraVideoTrack,
                localAudioTrack == null,
                cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

        binding.layoutContentRoom.primaryVideo.showIdentityBadge(false);
    }

    private void setNetworkQualityLevelImage(ImageView networkQualityImage, NetworkQualityLevel networkQualityLevel, String sid) {
        networkQualityLevels.put(sid, networkQualityLevel);
        if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN) {
            networkQualityImage.setVisibility(View.GONE);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_0);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_1);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_2);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_3);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_4);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.ic_network_quality_level_5);
        }
    }

    private class RemoteParticipantListener implements RemoteParticipant.Listener {

        private ImageView networkQualityImage;

        RemoteParticipantListener(ParticipantView primaryView, String sid) {
            networkQualityImage = primaryView.getNetworkQualityImage();
            setNetworkQualityLevelImage(networkQualityImage, networkQualityLevels.get(sid), sid);
        }

        @Override
        public void onNetworkQualityLevelChanged(@NonNull RemoteParticipant remoteParticipant,
                                                 @NonNull NetworkQualityLevel networkQualityLevel) {
            setNetworkQualityLevelImage(networkQualityImage,
                    networkQualityLevel,
                    remoteParticipant.getSid());
        }

        @Override
        public void onAudioTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                          @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i("onAudioTrackPublished: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onAudioTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i("onAudioTrackUnpublished: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onVideoTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                          @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i("onVideoTrackPublished: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onVideoTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i("onVideoTrackUnpublished: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onAudioTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                           @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                           @NonNull RemoteAudioTrack remoteAudioTrack) {
            Timber.i("onAudioTrackSubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
            boolean newAudioState = !remoteAudioTrackPublication.isTrackEnabled();

            if (participantController.getPrimaryItem().getSid().equals(remoteParticipant.getSid())) {
                // update audio state for primary view
                participantController.getPrimaryItem().setMuted(newAudioState);
                participantController.getPrimaryView().setMuted(newAudioState);
            } else {
                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), newAudioState);
            }
        }

        @Override
        public void onAudioTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                   @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                   @NonNull TwilioException twilioException) {
            Timber.w("onAudioTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            Snackbar.make(binding.layoutContentRoom.primaryVideo, "onAudioTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onAudioTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                             @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                             @NonNull RemoteAudioTrack remoteAudioTrack) {
            Timber.i("onAudioTrackUnsubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());

            if (participantController.getPrimaryItem().getSid().equals(remoteParticipant.getSid())) {
                // update audio state for primary view
                participantController.getPrimaryItem().setMuted(true);
                participantController.getPrimaryView().setMuted(true);
            } else {
                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), true);
            }
        }

        @Override
        public void onVideoTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.i("onVideoTrackSubscribed: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null
                    && primary.getSid().equals(remoteParticipant.getSid())
                    && primary.getVideoTrack() == null) {
                // no thumb needed - render as primary
                primary.setVideoTrack(remoteVideoTrack);
                participantController.renderAsPrimary(primary);
            } else {
                // not a primary remoteParticipant requires thumb
                participantController.addOrUpdateThumb(
                        remoteParticipant.getSid(),
                        remoteParticipant.getIdentity(),
                        null,
                        remoteVideoTrack);
            }
        }

        @Override
        public void onVideoTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                   @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                   @NonNull TwilioException twilioException) {
            Timber.w("onVideoTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            Snackbar.make(binding.layoutContentRoom.primaryVideo, "onVideoTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onVideoTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                             @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                             @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.i("onVideoTrackUnsubscribed: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null
                    && primary.getSid().equals(remoteParticipant.getSid())
                    && primary.getVideoTrack() == remoteVideoTrack) {

                // Remove primary video track
                primary.setVideoTrack(null);

                // Try to find another video track to render as primary
                List<RemoteVideoTrackPublication> remoteVideoTracks =
                        remoteParticipant.getRemoteVideoTracks();
                for (RemoteVideoTrackPublication newRemoteVideoTrackPublication :
                        remoteVideoTracks) {
                    RemoteVideoTrack newRemoteVideoTrack =
                            newRemoteVideoTrackPublication.getRemoteVideoTrack();
                    if (newRemoteVideoTrack != remoteVideoTrack) {
                        participantController.removeThumb(
                                remoteParticipant.getSid(), newRemoteVideoTrack);
                        primary.setVideoTrack(newRemoteVideoTrack);
                        break;
                    }
                }
                participantController.renderAsPrimary(primary);
            } else {
                // remove thumb or leave empty video thumb
                participantController.removeOrEmptyThumb(
                        remoteParticipant.getSid(),
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack);
            }
        }

        @Override
        public void onDataTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                         @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            Timber.i("onDataTrackPublished: remoteParticipant: %s, data: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled());
        }

        @Override
        public void onDataTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                           @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            Timber.i("onDataTrackUnpublished: remoteParticipant: %s, data: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled());
        }

        @Override
        public void onDataTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                          @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                          @NonNull RemoteDataTrack remoteDataTrack) {
            Timber.i("onDataTrackSubscribed: remoteParticipant: %s, data: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled(),
                    remoteDataTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onDataTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                  @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                                  @NonNull TwilioException twilioException) {
            Timber.w("onDataTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            Snackbar.make(binding.layoutContentRoom.primaryVideo, "onDataTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onDataTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                            @NonNull RemoteDataTrack remoteDataTrack) {
            Timber.i("onDataTrackUnsubscribed: remoteParticipant: %s, data: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled(),
                    remoteDataTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onAudioTrackEnabled(@NonNull RemoteParticipant remoteParticipant,
                                        @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i("onAudioTrackEnabled: remoteParticipant: %s, audio: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled());
        }

        @Override
        public void onAudioTrackDisabled(@NonNull RemoteParticipant remoteParticipant,
                                         @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i("onAudioTrackDisabled: remoteParticipant: %s, audio: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled());
        }

        @Override
        public void onVideoTrackEnabled(@NonNull RemoteParticipant remoteParticipant,
                                        @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i("onVideoTrackEnabled: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());
        }

        @Override
        public void onVideoTrackDisabled(@NonNull RemoteParticipant remoteParticipant,
                                         @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i("onVideoTrackDisabled: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_PHONE_CALL_STATE);
        registerReceiver(broadcastReceiver, intentFilter);

        restoreCameraTrack();
        publishLocalTracks();
        addParticipantViews();
    }

    /**
     * Try to restore camera video track after going to the settings screen or background
     */
    private void restoreCameraTrack() {
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints();
            setupLocalVideoTrack();
            renderLocalParticipantStub();
            restoreLocalVideoCameraTrack = false;
        }
    }

    private void publishLocalTracks() {
        if (localParticipant != null) {
            if (cameraVideoTrack != null) {
                Timber.d("Camera track: %s", cameraVideoTrack);
                localParticipant.publishTrack(cameraVideoTrack);
            }

            if (localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
        }
    }

    private void addParticipantViews() {
        if (room != null && localParticipant != null) {
            localParticipantSid = localParticipant.getSid();
            // remove primary view
            participantController.removePrimary();

            // add local thumb and "click" on it to make primary
            participantController.addThumb(
                    localParticipantSid,
                    getString(R.string.you),
                    cameraVideoTrack,
                    localAudioTrack == null,
                    cameraCapturer == null || cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

            ParticipantView thumb = participantController.getThumb(localParticipantSid, cameraVideoTrack);
            if (thumb != null) {
                localParticipant.setListener(new LocalParticipantListener(thumb));
                thumb.callOnClick();
            }

            setRemoteThumbVisibility();

            // add existing room participants thumbs
            boolean isFirstParticipant = true;
            for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                addParticipant(remoteParticipant, isFirstParticipant);
                isFirstParticipant = false;
                if (room.getDominantSpeaker() != null) {
                    if (room.getDominantSpeaker().getSid().equals(remoteParticipant.getSid())) {
                        VideoTrack videoTrack = (remoteParticipant.getRemoteVideoTracks().size() > 0) ?
                                remoteParticipant.getRemoteVideoTracks()
                                        .get(0)
                                        .getRemoteVideoTrack()
                                : null;
                        if (videoTrack != null) {
                            ParticipantView participantView = participantController.getThumb(
                                    remoteParticipant.getSid(), videoTrack);
                            participantController.setDominantSpeaker(participantView);
                        }
                    }
                }
            }
        }
    }

    private void setRemoteThumbVisibility() {
        if (room.getRemoteParticipants().size() == 0) {
            binding.layoutContentRoom.remoteVideoThumbnails.setVisibility(View.GONE);
        } else {
            binding.layoutContentRoom.remoteVideoThumbnails.setVisibility(View.VISIBLE);
        }
    }

    private class LocalParticipantListener implements LocalParticipant.Listener {

        private ImageView networkQualityImage;

        LocalParticipantListener(ParticipantView primaryView) {
            networkQualityImage = primaryView.getNetworkQualityImage();
        }

        @Override
        public void onAudioTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrackPublication localAudioTrackPublication) {
        }

        @Override
        public void onAudioTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrack localAudioTrack,
                @NonNull TwilioException twilioException) {
        }

        @Override
        public void onVideoTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrackPublication localVideoTrackPublication) {
        }

        @Override
        public void onVideoTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrack localVideoTrack,
                @NonNull TwilioException twilioException) {
        }

        @Override
        public void onDataTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrackPublication localDataTrackPublication) {
        }

        @Override
        public void onDataTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrack localDataTrack,
                @NonNull TwilioException twilioException) {
        }

        @Override
        public void onNetworkQualityLevelChanged(
                @NonNull LocalParticipant localParticipant,
                @NonNull NetworkQualityLevel networkQualityLevel) {
            setNetworkQualityLevelImage(networkQualityImage, networkQualityLevel, localParticipant.getSid());
        }
    }

    /**
     * Provides remoteParticipant a listener for media events and add thumb.
     *
     * @param remoteParticipant newly joined room remoteParticipant
     */
    private void addParticipant(RemoteParticipant remoteParticipant, boolean renderAsPrimary) {
        boolean muted = remoteParticipant.getRemoteAudioTracks().size() <= 0
                || !remoteParticipant.getRemoteAudioTracks().get(0).isTrackEnabled();
        List<RemoteVideoTrackPublication> remoteVideoTrackPublications =
                remoteParticipant.getRemoteVideoTracks();

        if (remoteVideoTrackPublications.isEmpty()) {
            /*
             * Add placeholder UI by passing null video track for a participant that is not
             * sharing any video tracks.
             */
            addParticipantVideoTrack(remoteParticipant, muted, null, renderAsPrimary);
        } else {
            for (RemoteVideoTrackPublication remoteVideoTrackPublication :
                    remoteVideoTrackPublications) {
                addParticipantVideoTrack(
                        remoteParticipant,
                        muted,
                        remoteVideoTrackPublication.getRemoteVideoTrack(),
                        renderAsPrimary);
                renderAsPrimary = false;
            }
        }
    }

    private void addParticipantVideoTrack(
            RemoteParticipant remoteParticipant,
            boolean muted,
            RemoteVideoTrack remoteVideoTrack,
            boolean renderAsPrimary) {
        if (renderAsPrimary) {
            ParticipantPrimaryView primaryView = participantController.getPrimaryView();

            renderItemAsPrimary(
                    new ParticipantController.Item(
                            remoteParticipant.getSid(),
                            remoteParticipant.getIdentity(),
                            remoteVideoTrack,
                            muted,
                            false));
            RemoteParticipantListener listener =
                    new RemoteParticipantListener(primaryView, remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        } else {
            participantController.addThumb(
                    remoteParticipant.getSid(),
                    remoteParticipant.getIdentity(),
                    remoteVideoTrack,
                    muted,
                    false);

            RemoteParticipantListener listener =
                    new RemoteParticipantListener(participantController.getThumb(
                            remoteParticipant.getSid(), remoteVideoTrack),
                            remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        }
    }

    private void requestScreenCapturePermission() {
        Timber.d("Requesting permission to capture screen");
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mediaProjectionManager != null) {
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE);
        }
    }

    private void startScreenCapture() {
        screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer, SCREEN_TRACK_NAME);

        if (screenVideoTrack != null) {
            binding.ivMediaControlScreenShare.setImageResource(R.drawable.ic_stop_screen_share_white);
            binding.ivMediaControlScreenShare.setTag(getString(R.string.stop_screen_share));
            localVideoTrackNames.put(screenVideoTrack.getName(), getString(R.string.screen_video_track));

            if (localParticipant != null) {
                localParticipant.publishTrack(screenVideoTrack);
            }
        } else {
            Snackbar.make(
                    binding.layoutContentRoom.primaryVideo,
                    R.string.failed_to_add_screen_video_track,
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }
    }

    private void switchCamera() {
        if (cameraCapturer != null) {
            boolean mirror = cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.BACK_CAMERA;
            cameraCapturer.switchCamera();

            if (participantController.getPrimaryItem().getSid().equals(localParticipantSid)) {
                participantController.updatePrimaryThumb(mirror);
            } else {
                participantController.updateThumb(localParticipantSid, cameraVideoTrack, mirror);
            }
        }
    }

    @Override
    protected void onStop() {
        removeCameraTrack();
        removeAllParticipants();
        super.onStop();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     * Removes all participant thumbs and push local camera as primary with empty sid.
     */
    private void removeAllParticipants() {
        if (room != null) {
            participantController.removeAllThumbs();
            participantController.removePrimary();

            renderLocalParticipantStub();
        }
    }

    /**
     * Remove the video track and mark the track to be restored when going to the settings screen or
     * going to the background
     */
    private void removeCameraTrack() {
        if (cameraVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            restoreLocalVideoCameraTrack = true;
            cameraVideoTrack = null;
        }
    }

    @Override
    protected void onDestroy() {
        // Reset the speakerphone
        audioManager.setSpeakerphoneOn(false);

        // Teardown tracks
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (cameraVideoTrack != null) {
            cameraVideoTrack.release();
            cameraVideoTrack = null;
        }
        if (screenVideoTrack != null) {
            screenVideoTrack.release();
            screenVideoTrack = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && permissions.length == requiredPermissionsForVideoCall.length) {
            boolean recordAudioPermissionGranted =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean cameraPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean writeExternalStoragePermissionGranted =
                    grantResults[2] == PackageManager.PERMISSION_GRANTED;
            boolean permissionsGranted =
                    recordAudioPermissionGranted
                            && cameraPermissionGranted
                            && writeExternalStoragePermissionGranted;

            if (permissionsGranted) {
                initiateVideoCall();
            } else {
                Snackbar.make(binding.layoutContentRoom.primaryVideo, R.string.permissions_required, Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onClickEvent(View view) {
        super.onClickEvent(view);

        try {
            int id = view.getId();
            if (id == R.id.iv_mediaControl_audio) {
                if (permissionsGranted()) {
                    setMediaControlOptionState((ImageView) view, !view.isSelected());
                    toggleLocalAudio();
                } else {
                    requestPermissions();
                }
            } else if (id == R.id.iv_mediaControl_video) {
                if (permissionsGranted()) {
                    setMediaControlOptionState((ImageView) view, !view.isSelected());
                    toggleLocalVideo();
                } else {
                    requestPermissions();
                }
            } else if (id == R.id.iv_mediaControl_moreOptions) {
                setMediaControlOptionState((ImageView) view, !view.isSelected());
                binding.groupMediaControlsMoreOptions.setVisibility(binding.ivMediaControlMoreOptions.isSelected() ? View.VISIBLE : View.GONE);
            } else if (id == R.id.iv_mediaControl_cameraSwitch) {
                if (permissionsGranted()) {
                    setMediaControlOptionState((ImageView) view, !view.isSelected());
                    switchCamera();
                } else {
                    requestPermissions();
                }
            } else if (id == R.id.iv_mediaControl_audioOutput) {
                if (permissionsGranted()) {
                    setMediaControlOptionState((ImageView) view, !view.isSelected());

                    if (audioManager.isSpeakerphoneOn()) {
                        audioManager.setSpeakerphoneOn(false);
                        binding.ivMediaControlAudioOutput.setImageResource(R.drawable.ic_phonelink_ring_white);
                    } else {
                        audioManager.setSpeakerphoneOn(true);
                        binding.ivMediaControlAudioOutput.setImageResource(R.drawable.ic_volume_up_white);
                    }
                } else {
                    requestPermissions();
                }
            } else if (id == R.id.iv_mediaControl_screenShare) {
                String shareScreen = getString(R.string.share_screen);

                if (view.getTag() == null || view.getTag().equals(shareScreen)) {
                    if (screenCapturer == null) {
                        requestScreenCapturePermission();
                    } else {
                        startScreenCapture();
                        setMediaControlOptionState((ImageView) view, !view.isSelected());
                    }
                } else {
                    stopScreenCapture();
                    setMediaControlOptionState((ImageView) view, !view.isSelected());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleLocalAudio() {
//        int icon;
        if (localAudioTrack == null) {
            isAudioMuted = false;
            localAudioTrack = LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME);

            if (localAudioTrack != null) {
                // enable audio settings
                binding.ivMediaControlAudioOutput.setVisibility(localAudioTrack.isEnabled() ? View.VISIBLE : View.GONE);

                if (localParticipant != null) {
                    localParticipant.publishTrack(localAudioTrack);
                }
            }
//            icon = R.drawable.ic_mic_white;
        } else {
            isAudioMuted = true;
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localAudioTrack);
            }
            localAudioTrack.release();
            localAudioTrack = null;

            // disable audio settings
            binding.ivMediaControlAudioOutput.setVisibility(View.GONE);
//            icon = R.drawable.ic_mic_off_gray;
        }
        setMediaControlOptionState(binding.ivMediaControlAudio, localAudioTrack != null);
    }

    private void toggleLocalVideo() {
        // remember old video reference for updating thumb in room
        VideoTrack oldVideo = cameraVideoTrack;

        if (cameraVideoTrack == null) {
            isVideoMuted = false;

            // add local camera track
            cameraVideoTrack = LocalVideoTrack.create(this,
                    true,
                    cameraCapturer.getVideoCapturer(),
                    videoConstraints,
                    CAMERA_TRACK_NAME);

            if (cameraVideoTrack != null) {
                // enable video settings
                binding.ivMediaControlCameraSwitch.setVisibility(cameraVideoTrack.isEnabled() ? View.VISIBLE : View.GONE);

                if (localParticipant != null) {
                    localParticipant.publishTrack(cameraVideoTrack);
                }
            }
        } else {
            isVideoMuted = true;
            // remove local camera track
            cameraVideoTrack.removeRenderer(binding.layoutContentRoom.primaryVideo);

            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            cameraVideoTrack = null;

            // disable video settings
            binding.ivMediaControlCameraSwitch.setVisibility(View.GONE);
        }

        if (room != null && room.getState() == CONNECTED) {
            // update local participant thumb
            participantController.updateThumb(localParticipantSid, oldVideo, cameraVideoTrack);
            if (participantController.getPrimaryItem().getSid().equals(localParticipantSid)) {
                // local video was rendered as primary view - refreshing
                participantController.renderAsPrimary(localParticipantSid,
                        getString(R.string.you),
                        cameraVideoTrack,
                        localAudioTrack == null,
                        cameraCapturer.getCameraSource()
                                == CameraCapturer.CameraSource.FRONT_CAMERA);

                participantController.getPrimaryView().showIdentityBadge(false);

                // update thumb state
                participantController.updateThumb(
                        localParticipantSid, cameraVideoTrack, SELECTED);
            }
        } else {
            renderLocalParticipantStub();
        }

        setMediaControlOptionState(binding.ivMediaControlVideo, cameraVideoTrack != null);

        // update toggle button icon
//        binding.ivMediaControlVideo.setImageResource(
//                cameraVideoTrack != null
//                        ? R.drawable.ic_videocam_white
//                        : R.drawable.ic_videocam_off_gray);
    }

    private void toggleLocalAudioTrackState() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
        }
    }

    private void toggleLocalVideoTrackState() {
        if (cameraVideoTrack != null) {
            boolean enable = !cameraVideoTrack.isEnabled();
            cameraVideoTrack.enable(enable);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("onReceive: broadcastReceiver");
            String intentAction = intent.getAction();

            if (intentAction != null && intentAction.equalsIgnoreCase(BROADCAST_PHONE_CALL_STATE) && intent.hasExtra(PHONE_CALL_STATE)) {
                switch (intent.getStringExtra(PHONE_CALL_STATE)) {
                    case PhoneCallStateConstants.INCOMING_CALL_ANSWERED:
                    case PhoneCallStateConstants.OUTGOING_CALL_STARTED:
                    case PhoneCallStateConstants.INCOMING_CALL_ENDED:
                    case PhoneCallStateConstants.OUTGOING_CALL_ENDED:
                        // Handling video call pause and resume on phone call state change
                        toggleLocalAudioTrackState();
                        toggleLocalVideoTrackState();
                        break;
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.dialog_title_meeting_end))
                .setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
                    if (room.getState() == CONNECTED) {
                        viewModel.disconnect();
                    } else {
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private void setMediaControlOptionState(ImageView imageView, boolean isSelected) {
        int viewId = imageView.getId();

        /*if (isSelected) {
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.skypeLobbyMediaControlBlue));
        } else {
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.skypeLobbyMediaControlGrey));
        }*/

        int resourceId = 0;
        if (viewId == R.id.iv_mediaControl_audio) {
            resourceId = isSelected ? R.drawable.ic_media_control_mute : R.drawable.ic_media_control_unmute;
        } else if (viewId == R.id.iv_mediaControl_video) {
            resourceId = isSelected ? R.drawable.ic_media_control_video_enabled : R.drawable.ic_media_control_video_disabled;
        } /*else if (viewId == R.id.iv_mediaControl_moreOptions) {
            resourceId = isSelected ? R.drawable.ic_media_control_more_options_enabled : R.drawable.ic_media_control_more_options_disabled;
        } else if (viewId == R.id.iv_mediaControl_cameraSwitch) {
            resourceId = isSelected ? R.drawable.ic_media_control_camera_switch_enabled : R.drawable.ic_media_control_camera_switch_disabled;
        } else if (viewId == R.id.iv_mediaControl_screenShare) {
            resourceId = isSelected ? R.drawable.ic_media_control_screen_share_enabled : R.drawable.ic_media_control_screen_share_disabled;
        } else if (viewId == R.id.iv_mediaControl_audioOutput) {
            resourceId = isSelected ? R.drawable.ic_media_control_audio_output_enabled : R.drawable.ic_media_control_audio_output_disabled;
        }*/

        imageView.setSelected(isSelected);
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        }
    }
}