package com.example.swiftdatahop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class TaskInfo {

	/**
	 * An array of tasks.
	 */
	public static List<TaskItem> ITEMS = new ArrayList<TaskItem>();

	/**
	 * A map tasks, by ID.
	 */
	public static Map<String, TaskItem> ITEM_MAP = new HashMap<String, TaskItem>();

	static {
		// Add 3 sample items.
		addItem(new TaskItem(Constants.FRAG_SHOWPEERS_ID, "Show Peers"));
		addItem(new TaskItem(Constants.FRAG_PEERDETAILS_ID, "Peer Details"));
		addItem(new TaskItem(Constants.FRAG_MOREINFO_ID, "More info"));
	}

	private static void addItem(TaskItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class TaskItem {
		public String id;
		public String content;

		public TaskItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
