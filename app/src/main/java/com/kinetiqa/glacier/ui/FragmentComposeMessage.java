package com.kinetiqa.glacier.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.core.SidebarManager;
import com.kinetiqa.glacier.dialogs.DialogAudioPreview;
import com.kinetiqa.glacier.dialogs.DialogAudioRecorder;
import com.kinetiqa.glacier.dialogs.DialogVideoPreview;
import com.kinetiqa.glacier.menu.MenuManager;
import com.kinetiqa.glacier.messaging.Message;
import com.kinetiqa.glacier.messaging.MessagingManager;

import java.io.File;
import java.io.IOException;


public class FragmentComposeMessage extends Fragment {
    public static final int REQUEST_VIDEO_CAPTURED = 1000;

    private String audioAttachmentLocation = null;
    private String videoAttachmentLocation = null;

    private View fragmentView;
    private Button attachAudio;
    private Button attachVideo;
    private Button removeAttachedButton;
    private Button attachedButton;
    private Button sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.compose_new_message_fragment, container, false);
        this.fragmentView = view;
        init();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURED) {
            attachVideoCallback(data.getData());
        }
    }

    private void init() {
        attachAudio = (Button) fragmentView.findViewById(R.id.cm_add_audio);
        attachAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachAudio();
            }
        });

        attachVideo = (Button) fragmentView.findViewById(R.id.cm_add_video);
        attachVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachVideo();
            }
        });

        removeAttachedButton = (Button) fragmentView.findViewById(R.id.cm_remove_attached);
        removeAttachedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the attached audio/video
                hideAttachedButtons();
            }
        });

        attachedButton = (Button) fragmentView.findViewById(R.id.cm_attachment);
        attachedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play the attached audio/video
                if (audioAttachmentLocation != null) {
                    DialogAudioPreview audioPreview = new DialogAudioPreview(getActivity(), audioAttachmentLocation);
                    audioPreview.show();
                } else if (videoAttachmentLocation != null) {
                    DialogVideoPreview videoPreview = new DialogVideoPreview(getActivity(), videoAttachmentLocation);
                    videoPreview.show();
                }
            }
        });

        sendButton = (Button) fragmentView.findViewById(R.id.cm_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void showAttachedButtons() {
        attachVideo.setVisibility(View.GONE);
        attachAudio.setVisibility(View.GONE);
        removeAttachedButton.setVisibility(View.VISIBLE);
        attachedButton.setVisibility(View.VISIBLE);

        if (audioAttachmentLocation != null) {
            attachedButton.setText("Attached Audio");
        } else if (videoAttachmentLocation != null) {
            attachedButton.setText("Attached Video");
        } else {
            attachedButton.setVisibility(View.GONE);
            removeAttachedButton.setVisibility(View.GONE);
        }
    }

    private void hideAttachedButtons() {
        attachVideo.setVisibility(View.VISIBLE);
        attachAudio.setVisibility(View.VISIBLE);
        removeAttachedButton.setVisibility(View.GONE);
        attachedButton.setVisibility(View.GONE);

        audioAttachmentLocation = null;
        videoAttachmentLocation = null;
    }

    private void attachAudio() {
        DialogAudioRecorder dialogAudioRecorder = new DialogAudioRecorder(getActivity(), this);
        dialogAudioRecorder.show();
        dialogAudioRecorder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
    }

    public void attachAudioCallback(String audioFileLocation) {
        audioAttachmentLocation = audioFileLocation;
        showAttachedButtons();
    }

    private void attachVideo() {
        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

        File dir = new File(Config.MESSAGE_MEDIA_PATH_PREFIX);
        dir.mkdirs();

        File nomedia = new File(dir, ".nomedia");
        try {
            nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File videoFile;
        try {
            videoFile = File.createTempFile("video", ".mp4", dir);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile((videoFile)));
        } catch (IOException e) {
            System.err.println(e);
        }
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURED);
    }

    public void attachVideoCallback(Uri videoUri) {
        videoAttachmentLocation = videoUri.toString().replace("file://", "");
        showAttachedButtons();
    }

    private void sendMessage() {
        EditText composeSubjects = (EditText) fragmentView.findViewById(R.id.cm_title);
        EditText composeBody = (EditText) fragmentView.findViewById(R.id.cm_body);

        String attachedFileLocation = "";
        int messageType = Message.TYPE_TEXT;

        if (audioAttachmentLocation != null) {
            messageType = Message.TYPE_AUDIO;
            attachedFileLocation = audioAttachmentLocation.substring(audioAttachmentLocation.lastIndexOf('/') + 1);
        } else if (videoAttachmentLocation != null) {
            messageType = Message.TYPE_VIDEO;
            attachedFileLocation = videoAttachmentLocation.substring(videoAttachmentLocation.lastIndexOf('/') + 1);
        }

        Message message = new Message(composeSubjects.getText().toString(), composeBody.getText().toString(), attachedFileLocation, messageType);
        MessagingManager.getInstance(getActivity()).sendMessage(message);

        if (getActivity() instanceof Home) {
            Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_LONG).show();
            ((Home) getActivity()).showView(SidebarManager.OUTBOX);
        }
    }
}