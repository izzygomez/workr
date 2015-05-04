package com.izzygomez.workr;

/**
 * Created by Jordan on 5/2/2015.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardArrayAdapter  extends ArrayAdapter<ListedItem> {
    private static final String TAG = "CardArrayAdapter";
    private List<ListedItem> cardList = new ArrayList<ListedItem>();

    static class CardViewHolder {
        TextView line1;
        TextView line2;
        TextView line3;
        TextView line4;

    }

    public CardArrayAdapter(Context context, int textViewResourceId, List<ListedItem> items) {
        super(context, textViewResourceId, items);

    }

    public void updateList(List<ListedItem> l){
        cardList.clear();
        cardList.addAll(l);
        this.notifyDataSetChanged();
    }

    public void removeFirstElt(){
        cardList.remove(0);
    }

    @Override
    public void add(ListedItem object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public ListedItem getItem(int index) {
        return this.cardList.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CardViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_view, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
            viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
            viewHolder.line3 = (TextView) row.findViewById(R.id.line3);
            viewHolder.line4 = (TextView) row.findViewById(R.id.line4);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }

        ListedItem card = getItem(position);
        viewHolder.line1.setText(card.returnArrayList().get(0));
        viewHolder.line2.setText(card.returnArrayList().get(1));
        viewHolder.line3.setText(card.returnArrayList().get(2));
        viewHolder.line4.setText(card.returnArrayList().get(3));
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
