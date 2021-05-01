package com.example.instagramdenemesolo.Adapter;

import android.content.Context;
import android.nfc.Tag;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramdenemesolo.R;

import java.util.List;

import static android.os.Build.VERSION_CODES;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mTags;
    private List<String> mTagsCount;

    public TagAdapter(Context mContext, List<String> mTags, List<String> mTagsCount) {
        this.mContext = mContext;
        this.mTags = mTags;
        this.mTagsCount = mTagsCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tag_item,parent,false);
        return new TagAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tag.setText("#" + mTags.get(position));
        holder.numberPosts.setText(mTagsCount.get(position)+" gönderi");
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tag;
        private TextView numberPosts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tag = itemView.findViewById(R.id.hash_tag);
            numberPosts = itemView.findViewById(R.id.number_of_posts);
        }
    }

    public void filter (List<String> filterTags, List<String> filterTagsCount){
        this.mTags = filterTags;
        this.mTagsCount =filterTagsCount;
        notifyDataSetChanged();
        
    }

}
