package com.example.cristinaleon.foodtrucks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for list of vendor items
 *
 * <p>Bugs: none
 *
 * @author cristinaleon
 */

public class VendorAdapter extends ArrayAdapter<Vendor>{
    /** reference to activity context */
    private Context context;

    /** reference to list of vendors for adapter */
    private ArrayList<Vendor> vendorList;

    /**
     * Constructor to call superclass
     *
     * @param vendorList List of vendor objects
     * @param context Activity context
     */
    public VendorAdapter(ArrayList<Vendor> vendorList, Context context) {
        super(context,0,vendorList);

    }

    /**
     * Return view of adapter element
     *
     * @param position Position of element within view
     * @param view View of vendorList element
     * @param viewGroup Group within view
     * @return View
     */
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        return view;
    }
}
