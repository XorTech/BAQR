package com.bluetooth;

import java.util.ArrayList;

import com.baqr.baqr.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Base adapter for the action list, uses custom rows to show a title and a
 * description. When an item is clicked the class attached to the element is
 * started.
 */
public class ActionListBaseAdapter extends BaseAdapter
{
	private static ArrayList<Action> actionArrayList;

	private LayoutInflater mInflater;

	public ActionListBaseAdapter(Context context, ArrayList<Action> results)
	{
		actionArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount()
	{
		return actionArrayList.size();
	}

	public Object getItem(int position)
	{
		return actionArrayList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;

		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.baqrblue_action_row_view, null);
			holder = new ViewHolder();
			holder.tvAction = (TextView) convertView.findViewById(R.id.tvAction);
			holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvAction.setText(actionArrayList.get(position).getAction());
		holder.tvDescription.setText(actionArrayList.get(position).getDescripiton());

		return convertView;
	}

	static class ViewHolder
	{
		TextView tvAction;
		TextView tvDescription;
	}
}
