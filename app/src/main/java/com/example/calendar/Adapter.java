package com.example.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private String TAG = "Adapter";
    public static Context mContext;
    private ArrayList<Data> mArrayList;

    public Adapter(Context context, ArrayList<Data> mArrayList) {
        this.mArrayList = mArrayList;
        this.mContext = context;
    }

    //리스트의 각 항목을 이루는 디자인 xml 적용
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate (R.layout.item, parent, false);
        ViewHolder vh = new ViewHolder (view);
        return vh;
    }

    //리스트의 각 항목에 들어갈 데이터 지정
    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        Data data = mArrayList.get(position);
        holder.todo.setText(data.getTodo());
        int pos = position;

        //삭제버튼
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArrayList.remove(pos);
                notifyDataSetChanged();
                ((MainActivity)mContext).dotCheck();
            }
        });

        //수정버튼
        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.todo.setVisibility(View.INVISIBLE);
                holder.editBtn.setVisibility(View.INVISIBLE);
                holder.removeBtn.setVisibility(View.INVISIBLE);
                holder.editInput.setVisibility(View.VISIBLE);
                holder.saveBtn.setVisibility(View.VISIBLE);
                holder.editInput.setText(data.getTodo());
            }
        });

        //저장버튼
        holder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String todo = holder.editInput.getText().toString();
                if(todo.trim().equals("")){
                    Toast.makeText (mContext,"내용을 입력해주세요.", Toast.LENGTH_SHORT).show ();
                } else {
                    holder.todo.setVisibility(View.VISIBLE);
                    holder.editBtn.setVisibility(View.VISIBLE);
                    holder.removeBtn.setVisibility(View.VISIBLE);
                    holder.editInput.setVisibility(View.INVISIBLE);
                    holder.saveBtn.setVisibility(View.INVISIBLE);

                    holder.editInput.setText("");
                    mArrayList.remove(pos);
                    Data data = new Data(todo);
                    mArrayList.add(pos, data);
                    notifyDataSetChanged();
                }
            }
        });
    }

    //화면에 보여줄 데이터 개수 리턴
    @Override
    public int getItemCount() {
        return (null != mArrayList ? mArrayList.size() : 0);
    }

    //뷰홀더 객체에 저장되어 화면에 표시
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView todo;
        Button removeBtn;
        Button editBtn;
        Button saveBtn;
        EditText editInput;

        public ViewHolder(@NonNull View itemView) {
            super (itemView);
            this.todo = itemView.findViewById(R.id.todo);
            this.removeBtn = itemView.findViewById(R.id.removeBtn);
            this.editBtn = itemView.findViewById(R.id.editBtn);
            this.saveBtn = itemView.findViewById(R.id.saveBtn);
            this.editInput = itemView.findViewById(R.id.editInput);
        }
    }
}