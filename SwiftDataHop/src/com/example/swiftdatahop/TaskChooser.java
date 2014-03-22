package com.example.swiftdatahop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/* Needed a way to pick the fragment that was started based on menu item selected.
 * I'm not totally sure this is the best way ever, but it at least puts the decision in one spot! */
public class TaskChooser {
	public static void configTaskFragment(String id, FragmentTransaction transaction) {
		// In two-pane mode, show the detail view in this activity by
		// adding or replacing the detail fragment using a
		// fragment transaction.
		Bundle arguments = new Bundle();
		if (id.equals("1")) {
			arguments.putString(TaskDetailFragment_ShowPeers.ARG_ITEM_ID, id);
			TaskDetailFragment_ShowPeers fragment = new TaskDetailFragment_ShowPeers();
			fragment.setArguments(arguments);
			transaction.replace(R.id.task_detail_container, fragment, "CONFIG");
		} else if (id.equals("2")) {
			arguments.putString(TaskDetailFragment_PeerDetails.ARG_ITEM_ID, id);
			TaskDetailFragment_PeerDetails fragment = new TaskDetailFragment_PeerDetails();
			fragment.setArguments(arguments);
			transaction.replace(R.id.task_detail_container, fragment, "PEER");		
		} else if (id.equals("3")){
			arguments.putString(TaskDetailFragment_MoreInfo.ARG_ITEM_ID, id);
			TaskDetailFragment_MoreInfo fragment = new TaskDetailFragment_MoreInfo();
			fragment.setArguments(arguments);
			transaction.replace(R.id.task_detail_container, fragment, "MORE"); //todo-kh - use 'id' string here if this works
		} else {
			assert(false);
		}
		//transaction.addToBackStack(null);
	}

	public static void putExtraOnIntent(String id, Intent detailIntent)  {

		if (id.equals("1"))
			detailIntent.putExtra(TaskDetailFragment_ShowPeers.ARG_ITEM_ID, id);
		else if (id.equals("2"))
			detailIntent.putExtra(TaskDetailFragment_PeerDetails.ARG_ITEM_ID, id);
		else if (id.equals("3"))
			detailIntent.putExtra(TaskDetailFragment_MoreInfo.ARG_ITEM_ID, id);
		else
			assert(false);
	}
}
