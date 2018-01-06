package news.zomia.zomianews.Lists.storyadapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 06.01.2018.
 */

public abstract class SelectableAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private static final String TAG = SelectableAdapter.class.getSimpleName();

    private SparseBooleanArray selectedItems;

    public SelectableAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    //Check if item is selected
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    //Change selection status
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    //Clear selected items
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    //Get count of selected items
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    //Get selected items numbers
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}