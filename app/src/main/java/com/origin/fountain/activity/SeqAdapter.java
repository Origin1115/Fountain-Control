package com.origin.fountain.activity;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import com.origin.fountain.R;

public class SeqAdapter extends ArrayAdapter<Sequence> {
    private LayoutInflater mInflater;
    private List<Sequence> seqList;
    Sequence mySeq;

    public String getSeqNamee() {
        return seqNamee;
    }

    public String getSeqSpeedd() {
        return seqSpeedd;
    }
    public int getSeqId() {
        return seqIdd;
    }
    private String seqNamee;
    private String seqSpeedd;
    private int seqIdd;
    private int index = -1;
    public SeqAdapter(Activity activity,  List<Sequence> listSeq) {
        super(activity,0,listSeq);
        mInflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        //gösterilecek listeyi de alalım
        seqList = listSeq;
    }


    @Override
    public int getCount() {
        return seqList.size();
    }

    @Override
    public Sequence getItem(int position) {
        //şöyle de olabilir: public Object getItem(int position)
        return seqList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return seqList.get(position).getSeq_id();
    }
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View satirView=convertView;

        if(satirView==null) {
            satirView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_seq, null);
        }
//        TextView seqId =
//                (TextView) satirView.findViewById(R.id.seq_id);
        TextView seqName = (TextView) satirView.findViewById(R.id.seq_name);
        EditText seqSpeed = (EditText)satirView.findViewById(R.id.seq_speed);

        Sequence asset = getItem(position);

//        seqId.setText(Integer.toString(asset.getSeq_id()));
        seqName.setText(asset.getSeq_name());
        seqSpeed.setText(asset.getSeq_speed());
        seqSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                seqList.get(position).setSeq_speed(seqSpeed.getText().toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        satirView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(index>-1){
//                    parent.getChildAt(index).setBackgroundColor(Color.TRANSPARENT);
                    parent.getChildAt(index).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_border));
                }
//                parent.getChildAt(position).setBackgroundColor(Color.parseColor("#FF00FF"));
                parent.getChildAt(position).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.green_border));

//                Toast.makeText(getContext(), "Item"+ asset.getSeq_speed()+ "Clicked",Toast.LENGTH_LONG).show();
                seqNamee  = asset.getSeq_name();
                seqSpeedd = asset.getSeq_speed();
                seqIdd = asset.getSeq_id();
                index = position;
            }
        });

        return satirView;
    }


}
