package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.CurrencyItem;
import com.example.myapplication.R;

import java.util.ArrayList;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<CurrencyItem> mList;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean check = true;

    public CurrencyAdapter(Context context, ArrayList<CurrencyItem> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public CurrencyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.currency_item, parent, false);

        return new CurrencyAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CurrencyAdapter.MyViewHolder holder, int position) {
        holder.title.setText(mList.get(position).getDescription());
        holder.amount.setText("" + mList.get(position).getAmount());
        holder.description.setText(mList.get(position).getCurrencyName());


        Glide.with(context).load("https://www.countryflags.io/" + mList.get(position).getCountryCode() + "/shiny/64.png").into(holder.img_country_flag);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setList(ArrayList<CurrencyItem> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void setConvertingList(ArrayList<CurrencyItem> mList, int position) {
        this.mList = mList;
        notifyItemChanged(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, description;
        private EditText amount;
        private ImageView img_country_flag;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            amount = (EditText) itemView.findViewById(R.id.amount);
            img_country_flag = (ImageView) itemView.findViewById(R.id.img_country_flag);

            itemView.setOnClickListener(this);
            amount.setOnClickListener(this);
            amount.addTextChangedListener(new MyCustomEditTextListener(amount));

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent("swapItems");
            intent.putExtra("isSwaping", true);
            intent.putExtra("swapPosition", getAdapterPosition());
            context.sendBroadcast(intent);
        }
    }

    public class MyCustomEditTextListener implements TextWatcher {
        private EditText amount;

        public MyCustomEditTextListener(EditText amount) {
            this.amount = amount;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int lengthBefore, int lengthAfter) {

        }

        @Override
        public void onTextChanged(final CharSequence charSequence, int start, int lengthBefore, int lengthAfter) {
        }

        @Override
        public void afterTextChanged(final Editable editable) {
            if (amount.hasFocus()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (check) {
                            Intent intent = new Intent("value");
                            intent.putExtra("newValue", editable.toString());
                            context.sendBroadcast(intent);
                            if (editable.length() == 0) {
                                amount.setHint("0");
                            }
                        }
                        check = false;
                        handler.removeCallbacks(runnable);
                    }
                };
                handler.postDelayed(runnable, 10);
            }
            check = true;
        }
    }
}
