package adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import andbook.example.locationservice.AddressListActivity;
import dto.AddressDTO;
import andbook.example.locationservice.R;

public class AddressAdapter extends BaseAdapter {
    private Context context = null;
    private LayoutInflater layoutInflater = null;
    private ArrayList<AddressDTO> arrayList;

    public AddressAdapter(Context context, ArrayList<AddressDTO> data){
        this.context=context;
        this.arrayList=data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        return arrayList.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public AddressDTO getItem(int position){
        return arrayList.get(position);
    }

    @Override
    public View getView(final int position, View converView, ViewGroup parent){
        View view = layoutInflater.inflate(R.layout.activity_addressrow,null);

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView phone = (TextView) view.findViewById(R.id.phone);

        name.setText(arrayList.get(position).getName());
        phone.setText(arrayList.get(position).getPhone());

        return view;
    }
}
