package com.example.chatall;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private String messageReceiverID , messageReceiverName , messageReceiverImage , messageSenderID;

    private TextView userName , userLastSeen;
    private CircleImageView userImage;
    private ImageButton SendMessageButton , SendFilesButton;
    private EditText MessageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef , reference;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private RecyclerView userMessagesList;
    RelativeLayout rl;

    FirebaseUser fuser;

    private String McurrentTime , McurrentDate;
    private String checker = "" , myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;

    boolean notify = false;



    private Toolbar ChatToolbar;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rl = findViewById(R.id.rl_chat_activity);
        String color1 = ThemeColor.theme;

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_Image").toString();

        //if(color1.equals("Black"))
        //{
            //rl.setBackgroundColor(Color.parseColor("#000000"));
        //}



        //Toast.makeText(ChatActivity.this, "ID" + messageReceiverID, Toast.LENGTH_SHORT).show();
        //Toast.makeText(ChatActivity.this, "Name" +messageReceiverName, Toast.LENGTH_SHORT).show();

        InitializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        notify = true;

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();

            }
        });


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Document and Other Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                {
                    builder.setTitle("Select File Format");
                    builder.setItems(options, new DialogInterface.OnClickListener()
                    {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if(i == 0)
                            {
                                checker = "image";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                            }
                            if(i == 1)
                            {
                                checker = "pdf";

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/pdf");
                                startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                            }
                            if(i == 2)
                            {
                                checker = "docx";

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/*");
                                startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                            }
                        }
                    });

                    builder.show();
                }
            }
        });


        DisplayLastSeen();



    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode== RESULT_OK && data!=null && data.getData()!=null)
        {

            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            fileUri = data.getData();

            if(!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" +messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filepath = storageReference.child(messagePushID + "." + checker);

                filepath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                String downloadUrl = uri.toString();

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message",downloadUrl);
                                messageTextBody.put("name",fileUri.getLastPathSegment());
                                messageTextBody.put("type",checker);
                                messageTextBody.put("from",messageSenderID);
                                messageTextBody.put("to",messageReceiverID);
                                messageTextBody.put("messageID",messagePushID);
                                messageTextBody.put("time",McurrentTime);
                                messageTextBody.put("date",McurrentDate);


                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double p = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setTitle("Sending File");
                        loadingBar.setMessage((int)p + "% Uploading...");
                    }
                });


            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" +messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filepath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filepath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation()
                {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();


                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageReceiverID);
                            messageTextBody.put("messageID",messagePushID);
                            messageTextBody.put("time",McurrentTime);
                            messageTextBody.put("date",McurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
                            {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });

                        }
                    }
                });
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void InitializeControllers()
    {


        ChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        SendFilesButton = findViewById(R.id.send_files_btn);

        SendMessageButton = findViewById(R.id.send_message_btn);
        MessageInputText = findViewById(R.id.input_message);

        loadingBar = new ProgressDialog(ChatActivity.this);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

    }


    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("online"))
                            {
                               userLastSeen.setText("Online");
                            }

                            if(state.equals("offline"))
                            {
                                userLastSeen.setText(date+" "+time);
                            }

                        }
                        else
                        {
                            userLastSeen.setText("User does not have an updated version");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart()
    {
        super.onStart();





        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messagesAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void SendMessage()
    {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
        McurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        McurrentTime = currentTime.format(calendar.getTime());

        String messageText = MessageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_LONG).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" +messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",McurrentTime);
            messageTextBody.put("date",McurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }



    }



    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(messageSenderID).child("userState")
                .updateChildren(onlineStateMap);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.theme_option,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.chat_dark_theme)
        {
            rl.setBackground(getResources().getDrawable(R.drawable.background1));
        }
        if (item.getItemId() == R.id.chat_light_theme)
        {
            rl.setBackground(getResources().getDrawable(R.drawable.background4));
        }

        return true;
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        updateUserStatus("online");

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        updateUserStatus("offline");
    }

}