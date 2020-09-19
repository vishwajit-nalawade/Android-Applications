package com.example.chatall;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText , receiverMessageText , text_seen;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture , messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            //text_seen = itemView.findViewById(R.id.seen_message);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        mAuth =FirebaseAuth.getInstance();

       View view = LayoutInflater.from(viewGroup.getContext())
               .inflate(R.layout.custom_messages_layout,viewGroup,false);
       return new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String  messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();



        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if(fromMessageType.equals("text"))
        {

            if(fromUserID.equals(messageSenderID))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setText(messages.getMessage());//+ "\n" + messages.getTime()+":"+messages.getDate());
            }
            else
            {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());// + "\n" + messages.getTime()+":"+messages.getDate());

            }
        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserID.equals(messageSenderID))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            }
            else
            {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        }
        else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
        {
            if(fromUserID.equals(messageSenderID))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                //messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatall-ebf22.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=3ba87267-e624-4029-9923-db2d5d3140f4").into(messageViewHolder.messageSenderPicture);


            }
            else
            {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                //messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatall-ebf22.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=3ba87267-e624-4029-9923-db2d5d3140f4").into(messageViewHolder.messageReceiverPicture);

            }
        }



        if(fromUserID.equals(messageSenderID))
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    deleteSentMessage(position,messageViewHolder);
                                }
                                else if(i == 2)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                                else if(i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    deleteSentMessage(position,messageViewHolder);
                                }
                                else if(i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View full scale image",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    deleteSentMessage(position,messageViewHolder);
                                }
                                else if(i == 2)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        else
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);
                                }
                                else if(i == 2)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                                else if(i == 3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);
                                }
                                else if(i == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View full scale image",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select your option.");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if(i == 1)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);
                                }
                                else if(i == 2)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }



    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() 
        {
            @Override
            public void onComplete(@NonNull Task<Void> task) 
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "An Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "An Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position , final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "An Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
