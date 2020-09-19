package com.example.chatall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>
{
    private List<Contacts> users;

    public UsersAdapter(List<Contacts> users)
    {
        this.users=users;
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder
    {
        public TextView UserName , UserStatus;
        public CircleImageView UserPicture;

        public UsersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            UserName = itemView.findViewById(R.id.user_profile_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            UserPicture = itemView.findViewById(R.id.users_profile_image);
        }
    }



    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.activity_find_friends,viewGroup,false);
        return new UsersViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

    }



    @Override
    public int getItemCount()
    {
        return users.size();

    }


}
