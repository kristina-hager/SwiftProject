package com.example.swiftdatahop;

import com.example.swiftdatahop.Fragment_ShowPeers.DeviceActionListener;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Task detail screen. This fragment is either
 * contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 */
public class Fragment_MoreInfo extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private TaskInfo.TaskItem mItem;
    AppDataManager mAppData = AppDataManager.getInstance();
    private View mContentView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Fragment_MoreInfo() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = TaskInfo.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.tdf_moreinfo,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) mContentView.findViewById(R.id.task_detail)).setText(mItem.content);
			showDeviceSelections();
		}
		
		(mContentView.findViewById(R.id.btn_clear_settings)).setOnClickListener(
	                new View.OnClickListener() {
	                    @Override
	                    public void onClick(View v) {
	                    	mAppData.setDownStreamDevice(null);
	                    	mAppData.setUpStreamDevice(null);
	                    	showDeviceSelections();
	                    }
	                });


		return mContentView;
	}

	private void showDeviceSelections() {
		WifiP2pDevice upStream = mAppData.getUpStreamDevice();
		WifiP2pDevice downStream = mAppData.getDownStreamDevice();
		if(upStream != null) {
		    ((TextView) mContentView.findViewById(R.id.upstream_device)).setText(upStream.deviceName + "\n");
		} else {
			((TextView) mContentView.findViewById(R.id.upstream_device)).setText("\n");
		}
		if(downStream != null) {
		    ((TextView) mContentView.findViewById(R.id.downstream_device)).setText(downStream.deviceName);	
		} else {
			((TextView) mContentView.findViewById(R.id.downstream_device)).setText("\n");
		}
	}
}
