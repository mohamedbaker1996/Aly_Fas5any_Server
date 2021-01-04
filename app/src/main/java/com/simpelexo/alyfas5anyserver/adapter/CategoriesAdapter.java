package com.simpelexo.alyfas5anyserver.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simpelexo.alyfas5anyserver.EventBus.CategoryClick;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.callback.ItemClickListener;
import com.simpelexo.alyfas5anyserver.model.Category;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class

CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
    private Context context;
    private Activity activity;
    private List <Category> categoryList;

//    private List<RestaurantClientData> restaurantDataList = new ArrayList<>();


    public CategoriesAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item,
                parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.ViewHolder holder, int position) {
        setData(holder, position);
        setAction(holder, position);
    }

    private void setData(ViewHolder holder, int position) {
        Glide.with(context).load(categoryList.get(position).getImage())
                .into(holder.menu_image);
        holder.menu_name.setText(new StringBuilder(categoryList.get(position).getName()));
    }

    private void setAction(ViewHolder holder, int position) {
     holder.setListener((view,position1, isLongClick) ->{

         Common.categorySelected = categoryList.get(position1);
         EventBus.getDefault().postSticky(new CategoryClick(true,categoryList.get(position1)));
     } );
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;

        @BindView(R.id.menu_image)
        ImageView menu_image;
        @BindView(R.id.menu_name)
        TextView menu_name;

        ItemClickListener listener;

        public void setListener(ItemClickListener listener) {
            this.listener = listener;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ButterKnife.bind(this, view);
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            listener.onClick(v,getAdapterPosition(),false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (categoryList.size() == 1) {
            return Common.DEFAULT_COLUMN_COUNT;
        }else {
            if (categoryList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == categoryList.size()-1) ? Common.FULL_WIDTH_COLUMN :Common.DEFAULT_COLUMN_COUNT ;
        }
    }
}
