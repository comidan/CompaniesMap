package com.dev.companiesmap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SubListAdapter extends BaseExpandableListAdapter
{
    private final Context context;
    private final ArrayList<String[]> mainCategories;
    private final String[] mainCategoriesName;
    private final Handler callbackSearchHandler;
    private int selectedItem = - 1,
                selectedGroup = - 1;

    public SubListAdapter(Context context, Handler callbackSearchHandler) {
        this.context = context;
        mainCategories = new ArrayList<>();
        mainCategories.add(context.getResources().getStringArray(R.array.shops));
        mainCategories.add(context.getResources().getStringArray(R.array.services));
        mainCategories.add(context.getResources().getStringArray(R.array.fun));
        mainCategories.add(context.getResources().getStringArray(R.array.clubs));
        mainCategories.add(context.getResources().getStringArray(R.array.transports));
        mainCategories.add(context.getResources().getStringArray(R.array.health));
        mainCategoriesName = new String[]{context.getString(R.string.shops),
                                          context.getString(R.string.services),
                                          context.getString(R.string.fun),
                                          context.getString(R.string.clubs),
                                          context.getString(R.string.transports),
                                          context.getString(R.string.health)};
        this.callbackSearchHandler = callbackSearchHandler;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
        textView.setText("\t\t\t" + mainCategories.get(groupPosition)[childPosition]);
        if(childPosition == selectedItem && groupPosition == selectedGroup)
            textView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedItem = childPosition;
                selectedGroup = groupPosition;
                int poi_count = 0;
                for(int index = 0; index < groupPosition; index ++)
                    poi_count += mainCategories.get(groupPosition).length;
                poi_count += childPosition;
                SearchQueryObject searchQueryObject = new SearchQueryObject(mainCategoriesName[groupPosition],
                                                                            mainCategories.get(groupPosition)[childPosition],
                                                                            groupPosition,
                                                                            poi_count);
                Bundle content = new Bundle();
                content.putSerializable("SEARCH_QUERY", searchQueryObject);
                Message searchQueryMessage = new Message();
                searchQueryMessage.setData(content);
                callbackSearchHandler.sendMessage(searchQueryMessage);
                notifyDataSetChanged();
            }
        });
        return rowView;
    }

    @Override
    public int getGroupCount() {
        return mainCategoriesName.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mainCategories.get(groupPosition).length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mainCategoriesName[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mainCategories.get(groupPosition)[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    private static final class ViewHolder {
        TextView textLabel;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;

        if (resultView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(android.R.id.text1);
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }
        holder.textLabel.setText("\t\t" + mainCategoriesName[groupPosition]);
        holder.textLabel.setBackgroundColor(getColorByGroupIndex(groupPosition));
        /*if(groupPosition == selectedGroup)
            holder.textLabel.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));*/
        return resultView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private int getColorByGroupIndex(int groupPosition)
    {
        switch (groupPosition)
        {
            default:
            case 0 : return context.getResources().getColor(R.color.red);
            case 1 : return context.getResources().getColor(R.color.orange);
            case 2 : return context.getResources().getColor(R.color.yellow);
            case 3 : return context.getResources().getColor(R.color.blue);
            case 4 : return context.getResources().getColor(R.color.violet);
            case 5 : return context.getResources().getColor(R.color.green);
        }
    }
}
