package com.stikhonenko.remoteplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.*;

/**
 * User: Tikhonenko.S
 * Date: 06.08.14
 * Time: 20:03
 */
public abstract class ViewArrayAdapter<Element, ViewHolder> extends BaseAdapter {
    protected static final int NORMAL_VIEW_TYPE = 0;
    protected static final int VIEW_TYPES_COUNT = 1;

    private List<Element> elements;
    private LayoutInflater inflater;

    @Override
    public int getCount() {
        if(elements == null){
            return 0;
        }

        return elements.size();
    }

    @Override
    public Object getItem(int position) {
        return getElement(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return NORMAL_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES_COUNT;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup viewGroup) {
        Element element = getElement(position);

        if(element == null){
            throw new NullPointerException("Element should not be null");
        }

        ViewHolder viewHolder = null;
        if(convertView != null){
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(viewHolder == null){
            convertView = inflater.inflate(getRootLayoutId(getItemViewType(position)),null);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        reuseView(element, viewHolder, position, convertView);

        return convertView;
    }

    protected abstract int getRootLayoutId(int viewType);
    protected abstract ViewHolder createViewHolder(View view);
    protected abstract void reuseView(Element element, ViewHolder viewHolder, int position,
                                      View view);

    public final Element getElement(int index){
        return elements.get(index);
    }

    public final void setElements(List<Element> elements){
        this.elements = elements;
        notifyDataSetChanged();
    }

    public ViewArrayAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }
}
